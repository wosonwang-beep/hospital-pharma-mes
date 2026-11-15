package com.hospital.mes.release.application;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class EbrPdfBusinessLayoutTest {
 @Test void readableChaptersPreserveFullOriginalSourceAsEmbeddedJson()throws Exception{
  var json=new ObjectMapper();var model=json.createObjectNode();
  model.put("mainBatchId","655").put("recordDigest","b".repeat(64)).put("definitionHash","a".repeat(64));
  var batch=model.putObject("batch");batch.put("batchNo","HYTEA-655").put("status","QA_RELEASED").put("finishedQualityStatus","RELEASED").put("finishedInventoryStatus","AVAILABLE");
  batch.putObject("finishedProductSnapshot").put("productName","喉咽茶合剂").put("specification","100ml/瓶");
  var process=model.putObject("processSnapshot").putObject("snapshot").putObject("ebr").putObject("definition");
  var def=process.putArray("forms").addObject().put("formCode","OP01_RECORD");
  def.putArray("fields").addObject().put("fieldCode","OP01_OBSERVATION").put("label","实际操作与观察记录");
  var form=model.putArray("forms").addObject();
  form.putObject("form").put("id","822").put("formCode","OP01_RECORD").put("formName","水提投料记录").put("status","VERIFIED").put("formRevision",2);
  var original=form.putArray("values").addObject().put("id","300").put("fieldCode","OP01_OBSERVATION").put("revisionNo",2).put("recordedBy","11572");
  original.put("normalizedValueJson",json.writeValueAsString("投料完整、核对复核，通过观察。"));
  form.putArray("reviews").addObject().put("decision","PASS").put("reviewerId","19289").put("signatureId","5282");
  var source=json.createObjectNode();source.put("trace","批次完整来源未截断").put("quantity","398000");
  source.putArray("signatureRecords").addObject().put("id","5281").put("objectType","EBR_FORM_INSTANCE").put("objectId","822").put("status","VALID");
  var qc=model.putArray("qcTests").addObject().put("id","514").put("testCode","APPEARANCE");
  qc.putObject("specificationSnapshot").put("itemName","性状").put("resultType","TEXT").put("textAcceptanceCriteria","棕褐色液体");
  var rev=qc.putArray("results").addObject().put("id","839").put("resultText","棕褐色液体").put("recordedBy","11567").put("signatureId","5331");
  rev.putObject("review").put("disposition","CONFIRMED").put("reviewedBy","11568").put("signatureId","5332");
  model.putArray("decisions").addObject().put("id","690").put("decision","RELEASED").put("signatureId","5348");
  byte[] bytes=new EbrPdfRenderer(json).render(model,source,"REVIEW_COPY");
  try(var pdf=Loader.loadPDF(bytes)){
   assertThat(pdf.getNumberOfPages()).isBetween(5,30);
   assertThat(pdf.getDocumentCatalog().getDocumentOutline()).isNotNull();
   var names=pdf.getDocumentCatalog().getNames().getEmbeddedFiles().getNames();
   assertThat(names).containsKey("source-evidence.json");
   byte[] embedded=names.get("source-evidence.json").getEmbeddedFile().toByteArray();
   assertThat(json.readTree(embedded)).isEqualTo(json.readTree(source.toString()));
   String text=new PDFTextStripper().getText(pdf);
   assertThat(text).contains("喉咽茶合剂","水提投料记录","实际操作与观察记录","投料完整、核对复核","棕褐色液体","5348","审核副本");
  }
 }
}
