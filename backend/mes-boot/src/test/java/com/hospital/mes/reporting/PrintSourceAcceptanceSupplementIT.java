package com.hospital.mes.reporting;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.common.exception.PermissionException;
import com.hospital.mes.reporting.application.*;
import com.hospital.mes.wms.application.WmsService;
import com.hospital.mes.wms.domain.WmsCommands.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.annotation.Transactional;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Same persistent DEV; rollback-only business fixture, no users/roles/credentials created. */
@SpringBootTest @ActiveProfiles("ci") @Transactional
class PrintSourceAcceptanceSupplementIT {
 @Autowired JdbcTemplate jdbc; @Autowired ObjectMapper json; @Autowired DataSource database;
 @Autowired PrintDataSourceService sources; @Autowired ConfiguredPrintResolver resolver; @Autowired WmsService wms;
 @MockitoBean CurrentPlatformContextResolver contexts;
 long actor; String sourceId,baseline,receiptNo; long users,roles,grants; Set<String> rights;
 String key(){return UUID.randomUUID().toString();}
 void context(Set<String> permissions){when(contexts.current()).thenReturn(new CurrentPlatformContext(1,actor,Set.of(),permissions,"acceptance-isolated","acceptance-isolated"));}
 String fingerprint(){return jdbc.queryForObject("SELECT CONCAT_WS('|',version_no,COALESCE(draft_revision_id,0),COALESCE(published_revision_id,0),status) FROM mes_print_data_source WHERE id=?",String.class,sourceId);}
 @BeforeEach void setup(){
  actor=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized='admin'",Long.class);
  sourceId=jdbc.queryForObject("SELECT id FROM mes_print_data_source WHERE org_id=1 AND source_code='QA20261010MultiType'",String.class);
  baseline=fingerprint(); users=jdbc.queryForObject("SELECT COUNT(*) FROM sys_user",Long.class);roles=jdbc.queryForObject("SELECT COUNT(*) FROM sys_role",Long.class);grants=jdbc.queryForObject("SELECT COUNT(*) FROM sys_role_permission",Long.class);
  rights=new HashSet<>(Set.of("print:source:view","print:source:manage","print:source:publish","wms:receipt:view","wms:receipt:create","wms:receipt:update","wms:inventory:view","master:material:view","master:supplier:view","master:uom:view"));context(rights);
 }
 ObjectNode field(ArrayNode fields,String key,String node,String column,String dataset,String type){
  var f=fields.addObject().put("fieldId","F"+key).put("fieldKey",key).put("label",key.equals("receiptNo")?"收货单号":key.equals("lotNo")?"批号":"实收数量").put("dataType",type).put("displayOrder",fields.size()-1).put("dataGroup",dataset==null?"MAIN":dataset).put("missing","ERROR").put("available",true);
  if(dataset==null)f.putNull("datasetKey");else f.put("datasetKey",dataset);f.putObject("sourcePath").put("nodeId",node).put("catalogFieldId",column);return f;
 }
 @Test void actualThreeLineSqlMappingKeepsDescendingOrderAndIndependentCollections(){
  var references=jdbc.queryForMap("SELECT r.supplier_id,r.warehouse_id,i.material_id,i.unit_id,i.location_id FROM wms_material_receipt r JOIN wms_material_receipt_item i ON i.org_id=r.org_id AND i.receipt_id=r.id WHERE r.org_id=1 AND r.id=6 ORDER BY i.id LIMIT 1");
  receiptNo="REC-20261010-"+UUID.randomUUID().toString().replace("-","").substring(0,8).toUpperCase(Locale.ROOT);
  var lines=new ArrayList<ReceiptItemInput>();
  for(int i=1;i<=3;i++)lines.add(new ReceiptItemInput(references.get("material_id").toString(),"20261010"+String.format("%02d",i),"20260929"+String.format("%02d",i),null,null,null,null, switch(i){case 1->"12.500";case 2->"6.250";default->"3.750";},references.get("unit_id").toString(),"25kg/袋",1L,references.get("location_id").toString(),null,true,true,true,true,true));
  var receipt=wms.createReceipt(new ReceiptCreate(receiptNo,references.get("supplier_id").toString(),"PO-20261010-001",null,references.get("warehouse_id").toString(),true,lines),key(),false);
  var config=json.createObjectNode().put("schemaVersion",1).put("sourceName","收货单").put("mainCatalogId","wms.receipt");config.putArray("parameterDeclarations").addObject().put("name","recordId").put("source","DOCUMENT_ID").putNull("contextKey");
  var joins=config.putArray("joins");for(String dataset:List.of("items","quantities")){var j=joins.addObject().put("nodeId",dataset).put("label",dataset.equals("items")?"收货明细":"数量明细").put("parentNodeId","main").put("catalogId","wms.receiptItem").put("knownRelationId","receipt.items").put("cardinality","1:N").put("datasetKey",dataset).put("displayOrder",joins.size()-1);j.putArray("keys").addObject().put("parentFieldId","id").put("targetFieldId","receiptId");j.putArray("sort").addObject().put("catalogFieldId","lotNo").put("direction",dataset.equals("items")?"DESC":"ASC");}
  var fields=config.putArray("fields");field(fields,"receiptNo","main","receiptNo",null,"TEXT");field(fields,"lotNo","items","lotNo","items","TEXT");field(fields,"receivedQty","items","receivedQty","items","NUMBER");field(fields,"quantityLotNo","quantities","lotNo","quantities","TEXT");field(fields,"quantity","quantities","receivedQty","quantities","NUMBER");
  var meta=json.createObjectNode().put("authorizationPolicyRef","provider.WMS_RECEIPT");meta.putArray("fixedTypeFilters");
  var result=resolver.resolve(1,config,meta,receipt.path("id").asText(),false);
  var raw=json.valueToTree(result.rawData());var displayed=json.valueToTree(result.snapshot().data());
  assertThat(raw.path("receiptNo").asText()).isEqualTo(receiptNo);
  assertThat(raw.path("items").size()).isEqualTo(3);assertThat(raw.path("quantities").size()).isEqualTo(3);
  assertThat(raw.path("items").findValuesAsText("lotNo")).containsExactly("2026101003","2026101002","2026101001");
  assertThat(raw.path("quantities").findValuesAsText("quantityLotNo")).containsExactly("2026101001","2026101002","2026101003");
  assertThat(raw.path("items").get(0).path("receivedQty").isTextual()).isTrue();
  assertThat(new java.math.BigDecimal(raw.path("items").get(0).path("receivedQty").asText())).isEqualByComparingTo("3.750");
  assertThat(displayed.path("items").size()+displayed.path("quantities").size()).isEqualTo(6);
  assertThat(raw.path("items").get(0).has("quantityLotNo")).isFalse();assertThat(raw.path("quantities").get(0).has("lotNo")).isFalse();
 }
 @ParameterizedTest @ValueSource(strings={"view","manage","publish"})
 void sourcePermissionSlicesEnforcePositiveAndNegativeOperations(String slice){
  var source=sources.detail(sourceId);var request=json.createObjectNode().put("expectedVersion",source.path("versionNo").asLong()).put("sourceName",source.path("sourceName").asText()).put("reason","验收补充；事务回滚");
  var types=request.putArray("businessTypes");for(var t:source.path("businessTypes"))types.add(t.path("businessType").asText());request.set("configuration",source.path("published").path("configuration"));
  var draft=sources.save(sourceId,request,source.path("versionNo").asText(),key());
  var check=json.createObjectNode().put("expectedVersion",draft.path("versionNo").asLong()).put("reason","验收补充；事务回滚");for(var t:source.path("businessTypes"))check.withArray("samples").addObject().put("businessType",t.path("businessType").asText()).put("recordId","6");
  var proof=sources.validate(sourceId,check,draft.path("versionNo").asText(),key());assertThat(proof.path("result").asText()).isEqualTo("PASS");
  var publish=json.createObjectNode().put("expectedVersion",draft.path("versionNo").asLong()).put("revisionId",draft.path("draft").path("id").asText()).put("configHash",draft.path("draft").path("configHash").asText()).put("validationId",proof.path("id").asText()).put("reason","验收补充；事务回滚");
  context(Set.of("print:source:"+slice,"wms:receipt:view"));
  if(slice.equals("view")){assertThat(sources.detail(sourceId).path("id").asText()).isEqualTo(sourceId);assertThat(sources.catalogs().path("entities").isArray()).isTrue();}else assertThatThrownBy(()->sources.detail(sourceId)).isInstanceOf(PermissionException.class);
  request.put("expectedVersion",draft.path("versionNo").asLong());
  if(slice.equals("manage")){assertThat(sources.save(sourceId,request,draft.path("versionNo").asText(),key()).path("draft").isObject()).isTrue();}else assertThatThrownBy(()->sources.save(sourceId,request,draft.path("versionNo").asText(),key())).isInstanceOf(PermissionException.class);
  if(slice.equals("publish"))assertThat(sources.publish(sourceId,publish,draft.path("versionNo").asText(),key()).path("draft").isNull()).isTrue();else assertThatThrownBy(()->sources.publish(sourceId,publish,draft.path("versionNo").asText(),key())).isInstanceOf(PermissionException.class);
 }
 @Test void concurrentTransactionsCannotInsertTwoAdditionalOwnersForAnOccupiedType()throws Exception{
  var original=jdbc.queryForMap("SELECT source_id,id FROM mes_print_data_source_type_binding WHERE org_id=1 AND business_type='QA_RECEIPT_ACCEPTANCE_A'");
  long other=jdbc.queryForObject("SELECT id FROM mes_print_data_source WHERE org_id=1 AND source_code='QA20261010ReceiptSource'",Long.class);
  var barrier=new CyclicBarrier(2);var executor=Executors.newFixedThreadPool(2);
  Callable<String> attempt=()->{try(var connection=database.getConnection()){connection.setAutoCommit(false);try(var statement=connection.prepareStatement("INSERT INTO mes_print_data_source_type_binding(org_id,created_by,created_at,updated_by,updated_at,business_type,source_id) VALUES(1,?,UTC_TIMESTAMP(3),?,UTC_TIMESTAMP(3),'QA_RECEIPT_ACCEPTANCE_A',?)")){statement.setLong(1,actor);statement.setLong(2,actor);statement.setLong(3,other);barrier.await(5,TimeUnit.SECONDS);try{statement.executeUpdate();return "UNEXPECTED_INSERT";}catch(SQLException e){return e.getErrorCode()+":"+e.getSQLState()+":"+e.getMessage();}}finally{connection.rollback();}}};
  try{var first=executor.submit(attempt);var second=executor.submit(attempt);assertThat(first.get(10,TimeUnit.SECONDS)).startsWith("1062:23000:").contains("uk_source_type_binding");assertThat(second.get(10,TimeUnit.SECONDS)).startsWith("1062:23000:").contains("uk_source_type_binding");}finally{executor.shutdownNow();assertThat(executor.awaitTermination(5,TimeUnit.SECONDS)).isTrue();}
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mes_print_data_source_type_binding WHERE org_id=1 AND business_type='QA_RECEIPT_ACCEPTANCE_A'",Long.class)).isEqualTo(1);
  assertThat(jdbc.queryForMap("SELECT source_id,id FROM mes_print_data_source_type_binding WHERE org_id=1 AND business_type='QA_RECEIPT_ACCEPTANCE_A'")).isEqualTo(original);
 }
 @AfterTransaction void verifyRollback(){
  assertThat(fingerprint()).isEqualTo(baseline);if(receiptNo!=null)assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_material_receipt WHERE org_id=1 AND receipt_no=?",Long.class,receiptNo)).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_user",Long.class)).isEqualTo(users);assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_role",Long.class)).isEqualTo(roles);assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_role_permission",Long.class)).isEqualTo(grants);
 }
}
