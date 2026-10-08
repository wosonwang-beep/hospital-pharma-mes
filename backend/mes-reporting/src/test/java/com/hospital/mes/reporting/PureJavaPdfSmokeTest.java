package com.hospital.mes.reporting;

import com.hospital.mes.reporting.application.*;
import com.hospital.mes.reporting.infrastructure.Docx4jFopPdfConverter;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PureJavaPdfSmokeTest {
 private final Docx4jFopPdfConverter converter=new Docx4jFopPdfConverter();
 private byte[] docx(int items) {
  var data=InspectionSampleTemplate.example(items);
  return new DocxRenderer(new DocxGuard()).render(
   InspectionSampleTemplate.create(),data,InspectionSampleTemplate.fields(),InspectionSampleTemplate.itemFields());
 }
 @Test void convertsRealChineseDocxToPdfWithNoOfficeService() throws Exception {
  byte[] pdf=converter.convert(docx(4));
  assertTrue(pdf.length>2500);
  assertEquals("%PDF-",new String(pdf,0,5,StandardCharsets.US_ASCII));
  try(var document=Loader.loadPDF(pdf)){
   assertTrue(document.getNumberOfPages()>=1);
   String text=new PDFTextStripper().getText(document).replaceAll("\\s+","");
   assertTrue(text.contains("药品检验报告"),"Chinese title missing from extracted PDF: "+text.substring(0,Math.min(text.length(),250)));
   assertTrue(text.contains("检验项目4"),"Last test row must remain in PDF: "+text.substring(0,Math.min(text.length(),250)));
   System.out.println("MES_DOCX4J_FOP_REAL_PDF bytes="+pdf.length+" pages="+document.getNumberOfPages()+" chinese=PASS");
  }
 }
 @Test void paginatesSeventyFiveChineseInspectionItemsWithoutLosingLastRow() throws Exception {
  byte[] pdf=converter.convert(docx(75));
  try(var document=Loader.loadPDF(pdf)){
   assertTrue(document.getNumberOfPages()>1,"Long multi-row report should paginate");
   String content=new PDFTextStripper().getText(document).replaceAll("\\\\s+","");
   assertTrue(content.contains("检验项目75"),"Last item is missing in the printed document");
   assertTrue(content.contains("药品检验报告"),"Title is missing");
   System.out.println("MES_DOCX4J_FOP_LONG_PDF bytes="+pdf.length+" pages="+document.getNumberOfPages()+" lastRow=PASS");
  }
 }
 @Test void nativeVisualDesignerWithChineseFieldsAndSeventyFiveRowsConvertsToPdf() throws Exception {
  var nativeDesigner=new NativePrintDesigner(new com.fasterxml.jackson.databind.ObjectMapper());
  byte[] nativeDocx=nativeDesigner.generate(nativeDesigner.defaults(),InspectionSampleTemplate.dictionary());
  byte[] filled=new DocxRenderer(new DocxGuard()).render(nativeDocx,
    InspectionSampleTemplate.example(75),InspectionSampleTemplate.fields(),InspectionSampleTemplate.itemFields());
  byte[] pdf=converter.convert(filled);
  try(var document=Loader.loadPDF(pdf)){
   assertTrue(document.getNumberOfPages()>1);
   String content=new PDFTextStripper().getText(document).replaceAll("\\s+","");
   assertTrue(content.contains("药品检验报告"));
   assertTrue(content.contains("检验项目75"),"Native designer loop must include every QC item");
   System.out.println("MES_NATIVE_DESIGNER_FOP_PDF bytes="+pdf.length+" pages="+document.getNumberOfPages()+" lastItem=PASS");
  }
 }
 @Test void deniesInvalidOfficeDocument() {
  assertThrows(RuntimeException.class,()->converter.convert(new byte[0]));
  assertThrows(RuntimeException.class,()->converter.convert("not a docx".getBytes(StandardCharsets.UTF_8)));
 }
}
