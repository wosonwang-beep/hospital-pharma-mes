package com.hospital.mes.reporting;

import com.hospital.mes.reporting.application.*;
import com.hospital.mes.reporting.infrastructure.LibreOfficePdfConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

/** Explicit opt-in: real LO conversion, no database, no user artifact and no persistent files. */
class LibreOfficeIntegrationSmokeTest {
 @Test
 @EnabledIfSystemProperty(named="mes.print.smoke",matches="true")
 void chineseInspectionDocxConvertsToRealPdfWithoutLeavingTemporaryDirectories() throws Exception {
  String home=System.getProperty("mes.print.office-home");
  assertNotNull(home,"Provide -Dmes.print.office-home=<installed LibreOffice directory>");
  assertTrue(Files.isRegularFile(Path.of(home,"program","soffice.exe")),"Office executable must exist");
  long before;
  try(var paths=Files.list(Path.of(System.getProperty("java.io.tmpdir")))){
   before=paths.filter(p->p.getFileName().toString().startsWith("mes-print-")).count();
  }
  byte[] docx=new DocxRenderer(new DocxGuard()).render(
    InspectionSampleTemplate.create(),InspectionSampleTemplate.example(4),
    InspectionSampleTemplate.fields(),InspectionSampleTemplate.itemFields());
  byte[] pdf=new LibreOfficePdfConverter(home).convert(docx);
  assertTrue(pdf.length>3500,"Actual converted PDF expected");
  assertEquals("%PDF-",new String(pdf,0,5,java.nio.charset.StandardCharsets.US_ASCII));
  long after;
  try(var paths=Files.list(Path.of(System.getProperty("java.io.tmpdir")))){
   after=paths.filter(p->p.getFileName().toString().startsWith("mes-print-")).count();
  }
  assertEquals(before,after,"Converter should cleanup per-call temporary files");
  System.out.println("R12_REAL_PDF_CONVERTED bytes="+pdf.length+" tempDelta="+(after-before));
 }
}
