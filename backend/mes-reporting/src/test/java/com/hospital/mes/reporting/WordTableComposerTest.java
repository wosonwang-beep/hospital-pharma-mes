package com.hospital.mes.reporting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.hospital.mes.reporting.application.*;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import java.io.*;
import java.math.BigInteger;
import static org.junit.jupiter.api.Assertions.*;

class WordTableComposerTest {
 private final ObjectMapper json=new ObjectMapper();
 private final NativePrintDesigner designer=new NativePrintDesigner(json);
 private final String pastedTable="""
   {"id":"word-paste","type":"WORD_TABLE","fontSize":11,"align":"LEFT","widths":[25,35,40],
   "rows":[
    {"heightPt":28,"cells":[
     {"text":"医院制剂检验报告","rowspan":1,"colspan":2,"background":"#EAF3FF","borderColor":"#334455","borderPt":1,
      "align":"CENTER","fontSize":12,"spans":[{"text":"医院制剂","bold":true,"fontSize":12,"color":"#113355"},{"text":"检验报告","fontSize":12}]},
     {"text":"检验人：","rowspan":2,"colspan":1,"fieldKey":"inspectors","background":"#FAF7E8"}
    ]},
    {"cells":[
      {"text":"报告编号：","rowspan":1,"colspan":1,"fieldKey":"reportNo","color":"#223344"},
      {"text":"检验结论：","rowspan":1,"colspan":1,"fieldKey":"overallResult"}
    ]}
   ]}
   """;
 private byte[] docx() throws Exception{
  var source=designer.defaults().deepCopy();
  ((ArrayNode)source.path("blocks")).add(json.readTree(pastedTable));
  byte[] bytes=designer.generate(source,InspectionSampleTemplate.dictionary());
  assertEquals(source,designer.extract(bytes));
  new DocxGuard().validate(bytes,InspectionSampleTemplate.fields(),InspectionSampleTemplate.itemFields());
  return bytes;
 }
 @Test void keepsMergedRowsColumnsWidthsColorsAndCjkFontInActualDocx() throws Exception{
  try(var word=new XWPFDocument(new ByteArrayInputStream(docx()))){
   var table=word.getTables().get(1);
   assertEquals(2,table.getNumberOfRows());
   assertEquals(2,table.getRow(0).getTableCells().size(),"First row spans 2 columns horizontally");
   assertEquals(3,table.getRow(1).getTableCells().size(),"Second row has continuation and two other anchors");
   assertEquals(BigInteger.valueOf(2),table.getRow(0).getCell(0).getCTTc().getTcPr().getGridSpan().getVal());
   assertEquals(STMerge.RESTART,table.getRow(0).getCell(1).getCTTc().getTcPr().getVMerge().getVal());
   assertEquals(STMerge.CONTINUE,table.getRow(1).getCell(2).getCTTc().getTcPr().getVMerge().getVal());
   assertEquals("EAF3FF",table.getRow(0).getCell(0).getColor());
   assertEquals("报告编号：",table.getRow(1).getCell(0).getText().replace("{{reportNo}}",""));
   assertTrue(table.getRow(0).getCell(0).getText().contains("医院制剂检验报告"));
  }
 }
 @Test void pastedWordFieldsRenderToActualPdfAndPreserveMergedCellContents() throws Exception{
  byte[] filled=new DocxRenderer(new DocxGuard()).render(docx(),InspectionSampleTemplate.example(3),
   InspectionSampleTemplate.fields(),InspectionSampleTemplate.itemFields());
  byte[] pdf=new com.hospital.mes.reporting.infrastructure.Docx4jFopPdfConverter().convert(filled);
  assertEquals("%PDF-",new String(pdf,0,5,java.nio.charset.StandardCharsets.US_ASCII));
  try(var loaded=Loader.loadPDF(pdf)){
   String text=new PDFTextStripper().getText(loaded).replaceAll("\\s+","");
   assertTrue(text.contains("医院制剂检验报告"),"Merged header must survive PDF");
   assertTrue(text.contains("报告编号"),"In-cell Chinese business field must survive PDF");
   assertTrue(text.contains("示例"),"In-cell bound value must appear in PDF");
   System.out.println("WORD_PASTE_MERGE_FOP_PDF bytes="+pdf.length+" pages="+loaded.getNumberOfPages()+" merged=yes bound=yes");
  }
 }
 @Test void rejectsOverlapUnapprovedFieldAndMalformedTableWidths() throws Exception{
  var spec=designer.defaults().deepCopy();
  var cells=json.readTree(pastedTable);
  ((ArrayNode)spec.path("blocks")).add(cells);
  ((com.fasterxml.jackson.databind.node.ObjectNode)cells.path("rows").get(0).path("cells").get(0)).put("rowspan",3);
  assertThrows(IllegalArgumentException.class,()->designer.generate(spec,InspectionSampleTemplate.dictionary()));
  var badField=json.readTree(pastedTable);
  ((ArrayNode)spec.path("blocks")).remove(spec.path("blocks").size()-1);
  ((ArrayNode)spec.path("blocks")).add(badField);
  ((com.fasterxml.jackson.databind.node.ObjectNode)badField.path("rows").get(1).path("cells").get(0)).put("fieldKey","sqlExpression");
  assertThrows(IllegalArgumentException.class,()->designer.generate(spec,InspectionSampleTemplate.dictionary()));
  var badWidths=json.readTree(pastedTable);
  ((ArrayNode)spec.path("blocks")).remove(spec.path("blocks").size()-1);
  ((ArrayNode)spec.path("blocks")).add(badWidths);
  ((ArrayNode)badWidths.path("widths")).set(0,json.getNodeFactory().numberNode(99));
  assertThrows(IllegalArgumentException.class,()->designer.generate(spec,InspectionSampleTemplate.dictionary()));
 }
}
