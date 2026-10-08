package com.hospital.mes.reporting.infrastructure;
import com.hospital.mes.reporting.application.PdfConverter;
import com.hospital.mes.common.exception.ComplianceException;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Semaphore;
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="mes.print",name="converter",havingValue="libreoffice")
public class LibreOfficePdfConverter implements PdfConverter {
 private final String officeHome;private final long executionTimeout;private final Semaphore gate=new Semaphore(1);
 @org.springframework.beans.factory.annotation.Autowired
 public LibreOfficePdfConverter(@Value("${mes.print.office-home:}") String officeHome){this(officeHome,30000L);}
 public LibreOfficePdfConverter(String officeHome,long executionTimeout){if(executionTimeout<1||executionTimeout>30000)throw new IllegalArgumentException("Invalid conversion deadline");this.officeHome=officeHome;this.executionTimeout=executionTimeout;}
 public byte[] convert(byte[] docx){
  if(officeHome.isBlank()||!Files.isDirectory(Path.of(officeHome,"program")))throw new ComplianceException("PRINT_CONVERTER_NOT_READY","需配置已安装的官方 LibreOffice 路径");
  if(!gate.tryAcquire())throw new ComplianceException("PRINT_CONVERTER_BUSY","转换忙，请稍后重试");
  Path temp=null;LocalOfficeManager manager=null;
  try{
   temp=Files.createTempDirectory("mes-print-");
   // The Windows UNO pipe transport requires the optional native jpipe library.
   // Use the loopback-only socket transport instead; reserve an ephemeral port for each isolated conversion.
   int port;
   try(var probe=new java.net.ServerSocket(0,50,java.net.InetAddress.getLoopbackAddress())){port=probe.getLocalPort();}
   manager=LocalOfficeManager.builder().officeHome(officeHome).workingDir(temp.toFile()).portNumbers(port).taskExecutionTimeout(executionTimeout).taskQueueTimeout(5000L).processTimeout(10000L).maxTasksPerProcess(1).startFailFast(true).build();
   manager.start();Path input=temp.resolve("input.docx"),output=temp.resolve("output.pdf");Files.write(input,docx);
   LocalConverter.builder().officeManager(manager).build().convert(input.toFile()).to(output.toFile()).execute();
   long size=Files.size(output);if(size<5||size>25*1024*1024)throw new IllegalStateException("无效 PDF 大小");
   byte[] bytes=Files.readAllBytes(output);if(!new String(bytes,0,5,java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-"))throw new IllegalStateException("转换未产出 PDF");return bytes;
  }catch(Exception e){throw new ComplianceException("PRINT_CONVERSION_FAILED","转换失败或超时；未生成归档产物");}
  finally{
   try{if(manager!=null)manager.stop();}catch(Exception ignored){org.slf4j.LoggerFactory.getLogger(getClass()).warn("Print converter stop failed");}
   if(temp!=null)cleanupTemporaryDirectory(temp);
   gate.release();
  }
 }
 private void cleanupTemporaryDirectory(Path root){
  Exception failure=null;
  for(int attempt=0;attempt<8;attempt++){
   if(!Files.exists(root))return;
   try(var paths=Files.walk(root)){
    for(Path part:paths.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(part);
    return;
   }catch(Exception e){failure=e;}
   if(attempt<7)try{Thread.sleep(200L*(attempt+1));}catch(InterruptedException interrupted){Thread.currentThread().interrupt();break;}
  }
  org.slf4j.LoggerFactory.getLogger(getClass()).warn("Print temporary directory cleanup failed",failure);
 }
}

