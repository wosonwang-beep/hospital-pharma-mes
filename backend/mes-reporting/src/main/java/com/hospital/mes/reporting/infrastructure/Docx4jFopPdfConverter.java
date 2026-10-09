package com.hospital.mes.reporting.infrastructure;
import com.hospital.mes.reporting.application.*;
import com.hospital.mes.common.exception.ComplianceException;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
/** Bounded independent JVM per conversion. Never runs document interpretation inside the MES JVM. */
@Component
@ConditionalOnProperty(prefix="mes.print",name="converter",havingValue="docx4j",matchIfMissing=true)
public class Docx4jFopPdfConverter implements PdfConverter {
 private static final int MAX_DOCX=5*1024*1024,MAX_PDF=25*1024*1024;
 private static final Semaphore GATE=new Semaphore(1);
 private final long deadline;private final Path temporaryRoot;
 public Docx4jFopPdfConverter(){this(Long.getLong("mes.print.docx4j.timeout-ms",45000L),System.getProperty("mes.print.docx4j.temp-root",System.getProperty("java.io.tmpdir")));}
 @Autowired public Docx4jFopPdfConverter(@Value("${mes.print.docx4j.timeout-ms:45000}") long timeout,@Value("${mes.print.docx4j.temp-root:${java.io.tmpdir}}") String root){
  if(timeout<1||timeout>60000)throw new IllegalArgumentException("Conversion timeout must be 1..60000ms");deadline=timeout;temporaryRoot=Path.of(root).toAbsolutePath().normalize();
 }
 public byte[] convert(byte[] docx){
  if(docx==null||docx.length==0||docx.length>MAX_DOCX)throw new ComplianceException("PRINT_DOCX_INVALID","DOCX must be 1 byte..5MB");
  if(!GATE.tryAcquire())throw new ComplianceException("PRINT_CONVERTER_BUSY","Conversion worker is busy");
  Path job=null;Process worker=null;
  try{
   new DocxGuard().validateStructure(docx);
   job=Files.createTempDirectory(temporaryRoot,"mes-docx4j-");
   Path input=job.resolve("input.docx"),output=job.resolve("output.pdf");Files.write(input,docx);
   var command=new ArrayList<String>();command.add(Path.of(System.getProperty("java.home"),"bin",System.getProperty("os.name").startsWith("Windows")?"java.exe":"java").toString());
   command.addAll(List.of("-Xmx256m","-XX:MaxMetaspaceSize=128m","-XX:MaxDirectMemorySize=32m","-XX:ActiveProcessorCount=1","-Djava.awt.headless=true","-Djava.io.tmpdir="+job,"-Duser.home="+job,"-Djavax.xml.accessExternalDTD=","-Djavax.xml.accessExternalSchema="));
   String cp=Arrays.stream(System.getProperty("java.class.path").split(java.util.regex.Pattern.quote(File.pathSeparator))).map(x->Path.of(x).toAbsolutePath().normalize().toString()).collect(java.util.stream.Collectors.joining(File.pathSeparator));
   boolean boot=false;for(String entry:cp.split(java.util.regex.Pattern.quote(File.pathSeparator)))if(entry.endsWith(".jar"))try(var jar=new java.util.jar.JarFile(entry)){if(jar.getEntry("BOOT-INF/classes/")!=null){boot=true;break;}}catch(IOException ignored){}
   if(boot)command.add("-Dloader.main="+Docx4jPdfWorker.class.getName());command.addAll(List.of("-cp",cp,boot?"org.springframework.boot.loader.launch.PropertiesLauncher":Docx4jPdfWorker.class.getName(),input.toString(),output.toString(),job.toString()));
   var builder=new ProcessBuilder(command).directory(job.toFile());
   // No inherited DB passwords, JWT, app credentials or JVM injection options.
   var inherited=new HashMap<>(builder.environment());builder.environment().clear();builder.environment().putAll(systemEnvironment(inherited));builder.environment().put("TEMP",job.toString());builder.environment().put("TMP",job.toString());
   builder.redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD);
   worker=builder.start();
   if(!worker.waitFor(deadline,TimeUnit.MILLISECONDS))throw new ComplianceException("PRINT_CONVERSION_TIMEOUT","Word conversion exceeded its hard deadline; nothing archived");
   if(worker.exitValue()!=0||!Files.isRegularFile(output,LinkOption.NOFOLLOW_LINKS))throw new ComplianceException("PRINT_CONVERSION_FAILED","Isolated Word converter failed; nothing archived");
   long size=Files.size(output);if(size<100||size>MAX_PDF)throw new ComplianceException("PRINT_CONVERSION_FAILED","Invalid PDF output size");
   byte[] bytes=Files.readAllBytes(output);if(!"%PDF-".equals(new String(bytes,0,5,StandardCharsets.US_ASCII)))throw new ComplianceException("PRINT_CONVERSION_FAILED","Invalid PDF output");return bytes;
  }catch(ComplianceException e){throw e;}catch(InterruptedException e){Thread.currentThread().interrupt();throw new ComplianceException("PRINT_CONVERSION_INTERRUPTED","Word conversion interrupted; nothing archived");}
   catch(Exception e){throw new ComplianceException("PRINT_CONVERSION_FAILED","Isolated Word converter failed; nothing archived");}
  finally{
   // An interrupted caller still waits for actual process death before deleting its files.
   boolean interrupted=Thread.interrupted();boolean cleaned=false;
   try{
    if(worker!=null){var descendants=worker.descendants().toList();for(var child:descendants)if(child.isAlive())child.destroyForcibly();if(worker.isAlive())worker.destroyForcibly();
     boolean stopped=false;long stop=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
     while(System.nanoTime()<stop){try{stopped=worker.waitFor(100,TimeUnit.MILLISECONDS)&&descendants.stream().noneMatch(ProcessHandle::isAlive);if(stopped)break;}catch(InterruptedException again){interrupted=true;}}
     if(!stopped)throw new IllegalStateException("Converter worker could not be stopped; converter remains unavailable");
    }
    if(job!=null)cleanup(job);cleaned=true;
   }finally{if(cleaned)GATE.release();if(interrupted)Thread.currentThread().interrupt();}
  }
 }
 static Map<String,String> systemEnvironment(Map<String,String> inherited){
  var result=new HashMap<String,String>();
  // Windows environment names are case insensitive; HashMap is not. A workstation
  // commonly exports "windir", which the isolated font scanner needs to locate CJK fonts.
  for(String name:List.of("SystemRoot","WINDIR","ComSpec"))inherited.entrySet().stream().filter(e->e.getKey().equalsIgnoreCase(name)).findFirst().ifPresent(e->result.put(name,e.getValue()));
  return result;
 }
 private void cleanup(Path job){
  if(!job.toAbsolutePath().normalize().startsWith(temporaryRoot)||!job.getFileName().toString().startsWith("mes-docx4j-"))throw new IllegalStateException("Unsafe conversion cleanup target");
  IOException failure=null;for(int attempt=0;attempt<5;attempt++){try(var paths=Files.walk(job)){for(Path path:paths.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(path);return;}catch(IOException e){failure=e;}try{Thread.sleep(50L*(attempt+1));}catch(InterruptedException e){Thread.currentThread().interrupt();break;}}
  throw new IllegalStateException("Converter cleanup failed; result must not be archived",failure);
 }
}
