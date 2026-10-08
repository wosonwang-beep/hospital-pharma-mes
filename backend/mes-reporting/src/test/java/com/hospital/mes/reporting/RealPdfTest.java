package com.hospital.mes.reporting;
import com.hospital.mes.reporting.application.*;import com.hospital.mes.reporting.infrastructure.LibreOfficePdfConverter;
import org.junit.jupiter.api.*;import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.apache.pdfbox.Loader;import org.apache.pdfbox.text.PDFTextStripper;import org.apache.pdfbox.rendering.PDFRenderer;
import java.nio.file.*;import java.util.*;import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;
@EnabledIfSystemProperty(named="mes.print.real-pdf",matches="true")
class RealPdfTest {
 private final Path evidence=Path.of(System.getProperty("mes.print.evidence","C:/Users/Administrator/Documents/Codex/2026-10-08/task/print-evidence"));
 @Test void realChineseMultipageLongTextHeadersPageNumbersAndSignatureBlock()throws Exception {
  Files.createDirectories(evidence);byte[] source=InspectionSampleTemplate.create();byte[] docx=new DocxRenderer(new DocxGuard()).render(source,InspectionSampleTemplate.example(75),InspectionSampleTemplate.fields(),InspectionSampleTemplate.itemFields());byte[] pdf=new LibreOfficePdfConverter("C:/Program Files/LibreOffice").convert(docx);
  Files.write(evidence.resolve("inspection-report-template.docx"),source);Files.write(evidence.resolve("inspection-report-75-items.docx"),docx);Files.write(evidence.resolve("inspection-report-75-items.pdf"),pdf);
  try(var d=Loader.loadPDF(pdf)){assertTrue(d.getNumberOfPages()>1);String text=new PDFTextStripper().getText(d);Files.writeString(evidence.resolve("pdf-text.txt"),text);assertTrue(text.contains("药品检验报告"));assertTrue(text.replaceAll("\\s+", "").contains("检验项目75"), "Complete item 75 missing");assertFalse(text.contains("{{"));assertTrue(text.contains("示例不含真实签名"));assertFalse(text.contains("检验方法"));assertFalse(text.contains("仪器"));
   var strip=new PDFTextStripper();int headerPages=0;for(int i=1;i<=d.getNumberOfPages();i++){strip.setStartPage(i);strip.setEndPage(i);String page=strip.getText(d);if(page.contains("检验项目")){assertTrue(page.contains("标准/要求"),"Missing repeated table header on page "+i);headerPages++;}assertTrue(page.contains("页 / 共"),"Missing page number on page "+i);if(page.contains("审批人"))assertTrue(page.contains("电子签名证据编号"),"Split signature block");}assertTrue(headerPages>1);
   var render=new PDFRenderer(d);for(int n=0;n<d.getNumberOfPages();n++)ImageIO.write(render.renderImageWithDPI(n,80),"png",evidence.resolve(String.format("pdf-page-%02d.png",n+1)).toFile());ImageIO.write(render.renderImageWithDPI(0,110),"png",evidence.resolve("pdf-first-page.png").toFile());ImageIO.write(render.renderImageWithDPI(d.getNumberOfPages()-1,110),"png",evidence.resolve("pdf-last-page.png").toFile());Files.writeString(evidence.resolve("real-pdf-result.txt"),"LibreOffice real DOCX-to-PDF: pages="+d.getNumberOfPages()+", repeated-header-pages="+headerPages+", SHA256="+PrintService.hash(pdf));
  }
 }
 @Test void realEmptyItemsPdf()throws Exception {Files.createDirectories(evidence);byte[] docx=new DocxRenderer(new DocxGuard()).render(InspectionSampleTemplate.create(),InspectionSampleTemplate.example(0),InspectionSampleTemplate.fields(),InspectionSampleTemplate.itemFields());byte[] pdf=new LibreOfficePdfConverter("C:/Program Files/LibreOffice").convert(docx);Files.write(evidence.resolve("inspection-report-empty.pdf"),pdf);try(var d=Loader.loadPDF(pdf)){String text=new PDFTextStripper().getText(d);assertTrue(text.contains("药品检验报告"));assertFalse(text.contains("[itemName]"));}}
 @Test void contentControlsUseChineseLabelsButRenderStableKeys()throws Exception {byte[] source=EditorLoopDocx.create(List.of("itemName","criteria","result"),InspectionSampleTemplate.dictionary());byte[] rendered=new DocxRenderer(new DocxGuard()).render(source,InspectionSampleTemplate.example(4),InspectionSampleTemplate.fields(),InspectionSampleTemplate.itemFields());try(var d=new org.apache.poi.xwpf.usermodel.XWPFDocument(new java.io.ByteArrayInputStream(rendered))){assertEquals(5,d.getTables().getFirst().getNumberOfRows());assertTrue(d.getTables().getFirst().getText().contains("检验项目4"));}}
 @Test void realConversionTimeoutDoesNotReturnAnArtifact()throws Exception {
  byte[] docx=new DocxRenderer(new DocxGuard()).render(InspectionSampleTemplate.create(),InspectionSampleTemplate.example(75),InspectionSampleTemplate.fields(),InspectionSampleTemplate.itemFields());
  var temp=Path.of(System.getProperty("java.io.tmpdir"));Set<Path> before;try(var files=Files.list(temp)){before=files.filter(p->p.getFileName().toString().startsWith("mes-print-")).collect(java.util.stream.Collectors.toSet());}
  var start=java.time.Instant.now();var e=assertThrows(com.hospital.mes.common.exception.ComplianceException.class,()->new LibreOfficePdfConverter("C:/Program Files/LibreOffice",1).convert(docx));assertEquals("PRINT_CONVERSION_FAILED",e.code());assertTrue(java.time.Duration.between(start,java.time.Instant.now()).toSeconds()<60);
  try(var files=Files.list(temp)){assertTrue(files.filter(p->p.getFileName().toString().startsWith("mes-print-")).allMatch(before::contains),"Timeout left a temporary conversion directory");}
 }
}
