package com.hospital.mes.reporting;
import com.hospital.mes.reporting.application.*;
import com.hospital.mes.reporting.infrastructure.Docx4jFopPdfConverter;
import com.hospital.mes.common.exception.ComplianceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class Docx4jIsolationTest {
 @TempDir Path root;
 @Test void hardDeadlineStopsWorkerAndCleansJobWithoutArchivableOutput()throws Exception {
  System.setProperty("mes.print.docx4j.timeout-ms","1");
  System.setProperty("mes.print.docx4j.temp-root",root.toString());
  try {
   byte[] input=new RecordTableDocx().create("合成超时安全验收","无业务数据",List.of("字段","实际"),List.of(List.of("记录","中文长文本")));
   long start=System.nanoTime();
   assertEquals("PRINT_CONVERSION_TIMEOUT",assertThrows(ComplianceException.class,()->new Docx4jFopPdfConverter().convert(input)).code());
   assertTrue(java.time.Duration.ofNanos(System.nanoTime()-start).toMillis()<8000,"Caller must not wait for unrestricted conversion");
   try(var files=Files.list(root)){assertEquals(0,files.count(),"Failed job must leave no DOCX/PDF/logs");}
   assertTrue(ProcessHandle.current().descendants().noneMatch(p->p.info().commandLine().orElse("").contains("Docx4jPdfWorker")),"No converter descendant may remain alive");
  } finally {System.clearProperty("mes.print.docx4j.timeout-ms");System.clearProperty("mes.print.docx4j.temp-root");}
 }
 @Test void interruptionAndConcurrentRequestCannotLeaveUnboundedWorker()throws Exception {
  byte[] input=new RecordTableDocx().create("合成中断安全验收","仅测试",List.of("字段","实际"),List.of(List.of("记录","中文")));
  var converter=new Docx4jFopPdfConverter(45000,root.toString());
  var result=new java.util.concurrent.atomic.AtomicReference<Throwable>();
  Thread request=new Thread(()->{try{converter.convert(input);}catch(Throwable error){result.set(error);}},"synthetic-interrupted-conversion");
  request.start();try{
   long stop=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
   boolean created=false;while(System.nanoTime()<stop){try(var files=Files.list(root)){created=files.findAny().isPresent();}if(created)break;Thread.sleep(5);}
   assertTrue(created,"Real job must start before it is interrupted");
   assertEquals("PRINT_CONVERTER_BUSY",assertThrows(ComplianceException.class,()->converter.convert(input)).code());
   request.interrupt();request.join(8000);assertFalse(request.isAlive());assertInstanceOf(ComplianceException.class,result.get());
   assertEquals("PRINT_CONVERSION_INTERRUPTED",((ComplianceException)result.get()).code());
   try(var files=Files.list(root)){assertEquals(0,files.count());}
   assertTrue(ProcessHandle.current().descendants().noneMatch(p->p.info().commandLine().orElse("").contains("Docx4jPdfWorker")));
  }finally{if(request.isAlive()){request.interrupt();request.join(8000);}}
 }
 @Test void unsafeFilledDocxIsRejectedBeforeWorkerStarts()throws Exception {
  var data=new java.io.ByteArrayOutputStream();try(var zip=new java.util.zip.ZipOutputStream(data)){
   zip.putNextEntry(new java.util.zip.ZipEntry("word/document.xml"));zip.write("<doc/>".getBytes());zip.closeEntry();
   zip.putNextEntry(new java.util.zip.ZipEntry("[Content_Types].xml"));zip.write("<types/>".getBytes());zip.closeEntry();
   zip.putNextEntry(new java.util.zip.ZipEntry("word/_rels/document.xml.rels"));zip.write("<Relationships><Relationship TargetMode='External' Target='file:///private'/></Relationships>".getBytes());zip.closeEntry();
  }
  assertThrows(ComplianceException.class,()->new Docx4jFopPdfConverter(45000,root.toString()).convert(data.toByteArray()));
  try(var files=Files.list(root)){assertEquals(0,files.count());}
 }
}
