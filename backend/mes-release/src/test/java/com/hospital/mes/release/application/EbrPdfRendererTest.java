package com.hospital.mes.release.application;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import static org.assertj.core.api.Assertions.*;
class EbrPdfRendererTest {
 @Test void identicalSourceKeysInDifferentInsertionOrderGenerateIdenticalUnicodePdfBytes()throws Exception{var json=new ObjectMapper();var model=json.createObjectNode().put("mainBatchId","100").put("definitionHash","a".repeat(64)).put("recordDigest","b".repeat(64));var first=json.createObjectNode().put("quantity","10.000").put("materialName","医院制剂");var second=json.createObjectNode().put("materialName","医院制剂").put("quantity","10.000");var pdf=new EbrPdfRenderer(json);byte[] original=pdf.render(model,first,"REVIEW_COPY"),repeat=pdf.render(model,second,"REVIEW_COPY");assertThat(repeat).isEqualTo(original);try(var doc=Loader.loadPDF(original)){assertThat(new PDFTextStripper().getText(doc)).contains("医院制剂","审核副本","10.000");}}
}
