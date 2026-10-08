package com.hospital.mes.reporting;
import com.hospital.mes.reporting.application.*;
import org.apache.poi.xwpf.usermodel.*;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import static org.junit.jupiter.api.Assertions.*;
class ControlledDocxTest {
 byte[] doc(String text) throws Exception {try(var d=new XWPFDocument();var b=new ByteArrayOutputStream()){d.createParagraph().createRun().setText(text);d.write(b);return b.toByteArray();}}
 @Test void fillsOnlyWhitelistedText() throws Exception {var guard=new DocxGuard();var engine=new DocxRenderer(guard);byte[] out=engine.render(doc("报告 {{reportNo}} {{modeLabel}}"),Map.of("reportNo","R-001","modeLabel","草稿"),Set.of("reportNo","modeLabel"),Set.of());try(var d=new XWPFDocument(new ByteArrayInputStream(out))){assertEquals("报告 R-001 草稿",d.getParagraphs().getFirst().getText());}}
 @Test void rejectsExpressionsAndUnknownFields() throws Exception {var guard=new DocxGuard();for(String t:List.of("{{T(java.lang.Runtime).getRuntime()}}","{{sql}}","{{#items}}","{{reportNo + 1}}","{{reportNo}")){byte[] d=doc(t);assertThrows(IllegalArgumentException.class,()->guard.validate(d,Set.of("reportNo"),Set.of()));}}
 @Test void rejectsExternalRelationshipsAndMacros() throws Exception {for(String entry:List.of("word/_rels/document.xml.rels","word/vbaProject.bin","word/embeddings/a.bin")){var b=new ByteArrayOutputStream();try(var z=new ZipOutputStream(b)){z.putNextEntry(new ZipEntry(entry));z.write("<Relationships><Relationship TargetMode=\"External\" Target=\"https://example.org\"/></Relationships>".getBytes());z.closeEntry();}assertThrows(IllegalArgumentException.class,()->new DocxGuard().validate(b.toByteArray(),Set.of(),Set.of()));}}
 @Test void rejectsZipBombAndOversizeUpload() throws Exception {assertThrows(IllegalArgumentException.class,()->new DocxGuard().validate(new byte[5*1024*1024+1],Set.of(),Set.of()));var b=new ByteArrayOutputStream();try(var z=new ZipOutputStream(b)){z.putNextEntry(new ZipEntry("word/document.xml"));z.write(new byte[41*1024*1024]);z.closeEntry();}assertThrows(IllegalArgumentException.class,()->new DocxGuard().validate(b.toByteArray(),Set.of(),Set.of()));}
 @Test void loopsAllItemsAndKeepsRepeatedHeader() throws Exception {var bytes=InspectionSampleTemplate.create();var engine=new DocxRenderer(new DocxGuard());var fields=InspectionSampleTemplate.fields();var data=InspectionSampleTemplate.example(75);byte[] out=engine.render(bytes,data,fields,InspectionSampleTemplate.itemFields());try(var d=new XWPFDocument(new ByteArrayInputStream(out))){assertEquals(76,d.getTables().getFirst().getNumberOfRows());assertTrue(d.getTables().getFirst().getRow(0).isRepeatHeader());assertTrue(d.getTables().getFirst().getText().contains("检验项目75"));assertFalse(d.getTables().getFirst().getText().contains("{{"));}}
 @Test void emptyItemsRenderWithoutPlaceholder() throws Exception {byte[] out=new DocxRenderer(new DocxGuard()).render(InspectionSampleTemplate.create(),InspectionSampleTemplate.example(0),InspectionSampleTemplate.fields(),InspectionSampleTemplate.itemFields());try(var d=new XWPFDocument(new ByteArrayInputStream(out))){assertFalse(d.getTables().getFirst().getText().contains("[itemName]"));}}
 @Test void rejectsAbsoluteRelationshipEvenIfExternalModeWasOmitted()throws Exception {
  for(String target:List.of("file:///private/data","https://outside.invalid/file","//outside.invalid/file")){
   var out=new ByteArrayOutputStream();try(var zip=new ZipOutputStream(out)){
    for(var part:Map.of("[Content_Types].xml","<Types/>","word/document.xml","<document/>","word/_rels/document.xml.rels","<Relationships><Relationship Target='"+target+"'/></Relationships>").entrySet()){zip.putNextEntry(new ZipEntry(part.getKey()));zip.write(part.getValue().getBytes());zip.closeEntry();}
   }
   assertThrows(IllegalArgumentException.class,()->new DocxGuard().validateStructure(out.toByteArray()));
  }
 }
}
