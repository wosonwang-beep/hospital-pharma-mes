package com.hospital.mes.reporting;

import com.hospital.mes.reporting.application.*;
import com.hospital.mes.audit.application.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Real MariaDB draft/version/FO PDF/publish regression, automatically rolled back. */
@SpringBootTest @ActiveProfiles("ci") @Transactional
class NativePrintDesignerIT {
 @Autowired PrintDataSourceService sourceService;
 @Autowired com.fasterxml.jackson.databind.ObjectMapper json;
 @Autowired PrintService printing;
 @Autowired NativePrintDesigner designer;
 @Autowired JdbcTemplate jdbc;
 @MockitoBean CurrentPlatformContextResolver contexts;
 String code,login;
 @BeforeEach void setup(){
  String suffix=UUID.randomUUID().toString().replace("-","").substring(0,12);
  code="NATIVE"+suffix;login="native_it_"+suffix;
  jdbc.update("INSERT INTO sys_user(login_name,login_name_normalized,display_name,password_hash,enabled,must_change_password) VALUES(?,?,?,?,TRUE,FALSE)",
   login,login,"Native designer rollback operator","test-only");
  Long actor=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?",Long.class,login);
  var rights=Set.of("print:template:view","print:template:manage","print:template:publish","print:document:generate");
  when(contexts.current()).thenReturn(new CurrentPlatformContext(1,actor,Set.of("TEST"),rights,"native-session","native-request"));
  String sample=jdbc.queryForObject("SELECT id FROM qms_inspection_report WHERE org_id=1 ORDER BY id LIMIT 1",String.class);
  PrintSourceIntegrationFixture.ensure(sourceService,printing,json,jdbc,contexts,actor,rights,"INSPECTION_REPORT",sample);
 }
 @Test void createsAndPublishesVersionedNativeTemplateUsingRealPdf(){
  var base=designer.defaults();
  var first=printing.createNativeDesign(code,"可视化报告","INSPECTION_REPORT",base,"首次设计");
  assertThat(first.path("templateRevision").asInt()).isEqualTo(1);
  String firstId=first.path("id").asText();
  var nextDesign=base.deepCopy();
  ((ObjectNode)nextDesign.path("blocks").get(0)).put("text","药品检验报告 · 第二版");
  var second=printing.createNativeDesign(code,"可视化报告","INSPECTION_REPORT",nextDesign,"调整标题");
  assertThat(second.path("templateRevision").asInt()).isEqualTo(2);
  assertThat(second.path("id").asText()).isNotEqualTo(firstId);
  var validated=printing.validate(second.path("id").asText(),second.path("versionNo").asLong(),"验证 PDF");
  assertThat(validated.path("status").asText()).isEqualTo("VALIDATED");
  byte[] bytes=printing.preview(second.path("id").asText());
  assertThat(bytes.length).isGreaterThan(1000);
  assertThat(new String(bytes,0,5,java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
  var published=printing.publish(second.path("id").asText(),validated.path("versionNo").asLong(),"发布");
  assertThat(published.path("status").asText()).isEqualTo("PUBLISHED");
  printing.bind(second.path("id").asText(),"INSPECTION_REPORT",true,"发布业务绑定");
  assertThat(printing.applicable("INSPECTION_REPORT").toString()).contains(second.path("id").asText());
  assertThat(printing.nativeDesign(firstId).path("design")).isEqualTo(base);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mes_print_template_version WHERE template_code=?",Long.class,code)).isEqualTo(2L);
  System.out.println("NATIVE_DESIGNER_MARIADB_PASS versions=2 PDF=REAL");
 }

 @Test void realInspectionRecordCreatesFixedSourceEvidenceAndReadablePdf(){
  var context=contexts.current();var rights=new HashSet<>(context.permissionCodes());rights.addAll(printing.provider("INSPECTION_REPORT").businessReadPermissions());
  when(contexts.current()).thenReturn(new CurrentPlatformContext(1,context.actorId(),Set.of("TEST"),rights,"inspection-pdf","inspection-pdf"));
  String sample=jdbc.queryForObject("SELECT id FROM qms_inspection_report WHERE org_id=1 ORDER BY id LIMIT 1",String.class);
  var template=printing.createNativeDesign(code,"真实检验记录回滚PDF","INSPECTION_REPORT",designer.defaults(),"回滚验收");
  var validated=printing.validate(template.path("id").asText(),template.path("versionNo").asLong(),"验证PDF");
  printing.publish(template.path("id").asText(),validated.path("versionNo").asLong(),"回滚发布");
  printing.bind(template.path("id").asText(),"INSPECTION_REPORT",true,"回滚业务绑定");
  var artifact=printing.generate("INSPECTION_REPORT",sample,template.path("id").asText(),false);
  byte[] pdf=printing.pdf(artifact.path("id").asText());assertThat(new String(pdf,0,5,java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mes_print_artifact_source_evidence WHERE org_id=1 AND artifact_id=?",Long.class,artifact.path("id").asText())).isEqualTo(1L);
 }
 @Test void sourceVersionDefaultsToInheritanceAndRequiresExplicitHashForUpgrade(){
  var context=contexts.current();var rights=new HashSet<>(context.permissionCodes());rights.addAll(Set.of("print:source:view","print:source:manage","print:source:publish"));rights.addAll(printing.provider("INSPECTION_REPORT").businessReadPermissions());
  when(contexts.current()).thenReturn(new CurrentPlatformContext(1,context.actorId(),Set.of("TEST"),rights,"upgrade","upgrade"));
  var old=printing.createNativeDesign(code,"源版本回滚验收","INSPECTION_REPORT",designer.defaults(),"初始版本");
  var source=sourceService.detail(sourceService.fixed(1,"INSPECTION_REPORT",null).source().getId().toString());
  var config=source.path("published").path("configuration").deepCopy();((ObjectNode)config.path("fields").get(0)).put("label","升级后的中文名称");
  var draftReq=json.createObjectNode().put("expectedVersion",source.path("versionNo").asLong()).put("sourceName",source.path("sourceName").asText()).put("reason","回滚源版本升级");draftReq.putArray("businessTypes").add("INSPECTION_REPORT");draftReq.set("configuration",config);
  var draft=sourceService.save(source.path("id").asText(),draftReq,source.path("versionNo").asText(),UUID.randomUUID().toString());
  var validate=json.createObjectNode().put("expectedVersion",draft.path("versionNo").asLong()).put("reason","真实检验样本");validate.putArray("samples").addObject().put("businessType","INSPECTION_REPORT").put("recordId",jdbc.queryForObject("SELECT id FROM qms_inspection_report WHERE org_id=1 ORDER BY id LIMIT 1",String.class));
  var proof=sourceService.validate(source.path("id").asText(),validate,draft.path("versionNo").asText(),UUID.randomUUID().toString());assertThat(proof.path("result").asText()).isEqualTo("PASS");
  var publish=json.createObjectNode().put("expectedVersion",draft.path("versionNo").asLong()).put("revisionId",draft.path("draft").path("id").asText()).put("configHash",draft.path("draft").path("configHash").asText()).put("validationId",proof.path("id").asText()).put("reason","回滚发布");
  var next=sourceService.publish(source.path("id").asText(),publish,draft.path("versionNo").asText(),UUID.randomUUID().toString());
  var inherited=printing.createNativeDesign(code,"继承旧源","INSPECTION_REPORT",designer.defaults(),"默认继承");assertThat(inherited.path("sourceConfigHash")).isEqualTo(old.path("sourceConfigHash"));
  var choice=json.createObjectNode().put("mode","UPGRADE").put("targetSourceRevisionId",next.path("published").path("id").asText()).put("targetConfigHash","0".repeat(64));
  assertThatThrownBy(()->printing.createNativeDesign(code,"冲突升级","INSPECTION_REPORT",designer.defaults(),"拒绝错哈希",old.path("id").asText(),choice)).isInstanceOf(com.hospital.mes.common.exception.ResourceConflictException.class);
  choice.put("targetConfigHash",next.path("published").path("configHash").asText());
  var upgraded=printing.createNativeDesign(code,"显式升级","INSPECTION_REPORT",designer.defaults(),"确认目标",old.path("id").asText(),choice);
  assertThat(upgraded.path("sourceConfigHash")).isEqualTo(next.path("published").path("configHash"));assertThat(printing.nativeDesign(old.path("id").asText()).path("template").path("sourceConfigHash")).isEqualTo(old.path("sourceConfigHash"));
 }

 @AfterTransaction void verifyRollback(){
  if(code==null)return;
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mes_print_template_version WHERE template_code=?",Long.class,code)).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_user WHERE login_name_normalized=?",Long.class,login)).isZero();
  System.out.println("NATIVE_DESIGNER_ROLLBACK_CONFIRMED");
 }
 @Test void canvasWordImportPreservesSourceAndProducesRealChineseMultiPagePdf() throws Exception {
  byte[] source;
  try(var input=getClass().getResourceAsStream("/printing/canvas-editor-report.docx")){
   assertThat(input).isNotNull();source=input.readAllBytes();
  }
  try(var doc=new org.apache.poi.xwpf.usermodel.XWPFDocument(new java.io.ByteArrayInputStream(source))){
   var property=doc.getProperties().getCustomProperties().getProperty("mes.canvas.print.v1");
   assertThat(property).isNotNull();assertThat(property.getLpwstr()).contains("\"fieldKey\":\"reportNo\"","\"repeat\":true");
  }
  var draft=printing.upload(code,"Canvas Word 导入回归","INSPECTION_REPORT",source,"回滚测试");
  assertThat(printing.template(draft.path("id").asText())).isEqualTo(source);
  var validated=printing.validate(draft.path("id").asText(),draft.path("versionNo").asLong(),"真实 PDF 回归");
  assertThat(validated.path("status").asText()).isEqualTo("VALIDATED");
  byte[] pdf=printing.preview(draft.path("id").asText());
  assertThat(new String(pdf,0,5,java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
  assertThat(pdf.length).isGreaterThan(1000);
  try(var document=org.apache.pdfbox.Loader.loadPDF(pdf)){
   assertThat(new org.apache.pdfbox.text.PDFTextStripper().getText(document)).contains("检验");
  }
  assertThat(printing.template(draft.path("id").asText())).isEqualTo(source);
  var published=printing.publish(draft.path("id").asText(),validated.path("versionNo").asLong(),"Canvas 发布回归");
  assertThat(published.path("status").asText()).isEqualTo("PUBLISHED");
  printing.bind(draft.path("id").asText(),"INSPECTION_REPORT",true,"Canvas 业务绑定回归");
  assertThat(printing.applicable("INSPECTION_REPORT").toString()).contains(draft.path("id").asText());
  System.out.println("CANVAS_DOCX_SOURCE_HASH_PRESERVED REAL_PDF_PASS");
 }
}
