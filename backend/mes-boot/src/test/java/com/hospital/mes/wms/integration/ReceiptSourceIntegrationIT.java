package com.hospital.mes.wms.integration;

import com.fasterxml.jackson.databind.*;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.masterdata.domain.*;
import com.hospital.mes.wms.application.*;
import com.hospital.mes.wms.domain.WmsCommands.*;
import com.hospital.mes.wms.infrastructure.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Transaction-owned data, including the numbering approval-reference fixture, is rolled back.
 * This is not approval of a real hospital numbering SOP and does not enable a live rule. */
@SpringBootTest @ActiveProfiles("ci") @Transactional @org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
class ReceiptSourceIntegrationIT {
 @Autowired org.springframework.test.web.servlet.MockMvc mvc;
 @Autowired WmsService service; @Autowired ReceiptNumberingService numbering;
 @Autowired MaterialService materials;@Autowired UnitService units;@Autowired SupplierService suppliers;@Autowired MaterialSupplierService links;
 @Autowired ObjectMapper json;@Autowired JdbcTemplate jdbc;
 @Autowired com.hospital.mes.audit.attachment.application.AttachmentService fileService;
 @Autowired WmsAttachmentService fileLinks;
 @MockitoBean CurrentPlatformContextResolver contexts;
 long actor,testOrg;String suffix,unit,material,supplier,warehouse,location;
 final Set<String> permissions=new HashSet<>();
 String key(){return UUID.randomUUID().toString();}
 @BeforeEach void prepare(){
  testOrg=1_000_000_000L+(UUID.randomUUID().getMostSignificantBits()&0x3fffffffL);
  suffix=UUID.randomUUID().toString().replace("-","").substring(0,12);String name="WH"+suffix;
  jdbc.update("INSERT INTO sys_user(login_name,login_name_normalized,display_name,password_hash,enabled,must_change_password) VALUES(?,?,?,?,TRUE,FALSE)",name,name.toLowerCase(Locale.ROOT),"王海燕","{disabled}");
  actor=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?",Long.class,name.toLowerCase(Locale.ROOT));
  for(String p:List.of("master:material:","master:supplier:","master:uom:","wms:inventory:","wms:receipt:"))for(String a:List.of("view","create","update","confirm"))permissions.add(p+a);
  when(contexts.current()).thenReturn(new CurrentPlatformContext(testOrg,actor,Set.of("WAREHOUSE_OPERATOR"),permissions,"request",key()));
  unit=units.create(new UnitCommands.Create("KG"+suffix,"千克","MASS",6),key()).path("id").asText();
  material=materials.create(json.convertValue(Map.of("materialCode","GLY"+suffix,"materialName","药用甘油","materialType","RAW","baseUnitId",unit,"lotControlled",true),MaterialCommands.Create.class),key()).path("id").asText();
  supplier=suppliers.create(new SupplierCommands.Create("EK"+suffix,"湖南尔康制药股份有限公司",LocalDate.now(ZoneOffset.UTC).plusYears(1)),key()).path("id").asText();
  suppliers.command(supplier,new SupplierCommands.Update("QUALIFY",0L,"按供货资质核对",null,null),null,key());
  links.assign(material,new SupplierCommands.Assign(0L,"批准供货来源",List.of(new SupplierCommands.Relationship(supplier,true,true,null,"湖南尔康制药股份有限公司"))),null,key());
  warehouse=service.maintenance("Warehouse",null,new WarehouseCreate("WH"+suffix,"原辅料仓库","RAW"),null,key()).path("id").asText();
  location=service.maintenance("Location",null,new LocationCreate(warehouse,"A01"+suffix,"原辅料库位"),null,key()).path("id").asText();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_receipt_number_rule WHERE org_id=?",Long.class,testOrg)).as("Never overwrite an existing real approved rule").isZero();
  jdbc.update("INSERT INTO wms_receipt_number_rule(org_id,created_by,created_at,updated_by,updated_at,version_no,prefix,date_pattern,sequence_width,next_value,timezone_id,approval_reference,enabled) VALUES(?,?,CURRENT_TIMESTAMP(3),?,CURRENT_TIMESTAMP(3),0,?,'yyyyMMdd',8,1,'Asia/Shanghai','SOP-WH-004-R1',1)",testOrg,actor,actor,"RM"+suffix);
 }
 String reserve(String line){return numbering.reserve(new ReceiptNumberingService.Reserve(material,line),key()).path("lotNo").asText();}
 ReceiptItemInput item(String lot,Boolean checked){return new ReceiptItemInput(material,lot,null,"GL-20261008",LocalDate.now(ZoneOffset.UTC).minusDays(2).toString(),LocalDate.now(ZoneOffset.UTC).plusYears(1).toString(),null,"5.125",unit,null,1L,location,null,checked,checked,checked,checked,checked);}
 ReceiptCreate body(List<ReceiptItemInput> rows,Boolean checked){return new ReceiptCreate("SH"+suffix,supplier,"CG-20261018-0042","DN-20261018-0036",warehouse,checked,rows,"RECEIPT_SOURCE_A_V1",LocalDate.now(ZoneOffset.UTC).toString(),"核对实物标签与随货资料");}
 @Test void allocationIsStableAndDistinctAcrossIntents(){
  String intent=key(),k=key();var input=new ReceiptNumberingService.Reserve(material,intent);
  JsonNode first=numbering.reserve(input,k);assertThat(numbering.reserve(input,k)).isEqualTo(first);
  assertThat(numbering.reserve(input,key()).path("lotNo").asText()).isEqualTo(first.path("lotNo").asText());
  assertThat(reserve(key())).isNotEqualTo(first.path("lotNo").asText());
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM md_material_lot WHERE created_by=?",Long.class,actor)).isZero();
 }
 @Test void missingApprovedRuleNeverReturnsFallbackNumber(){
  jdbc.update("UPDATE wms_receipt_number_rule SET enabled=0 WHERE org_id=?",testOrg);
  assertThatThrownBy(()->numbering.reserve(new ReceiptNumberingService.Reserve(material,key()),key())).hasMessageContaining("批准");
 }
 @Test void unknownChecksAndOptionalSupplierMarkSaveWithoutInventory(){
  String a=reserve(key()),b=reserve(key());var r=service.createReceipt(body(List.of(item(a,null),item(b,null)),null),key(),false);
  assertThat(r.path("items").size()).isEqualTo(2);assertThat(r.path("transportCheckPassed").isNull()).isTrue();
  assertThat(r.path("arrivalDate").asText()).isEqualTo(LocalDate.now(ZoneOffset.UTC).toString());
  assertThat(r.path("items").get(0).path("supplierLotNo").isNull()).isTrue();
  assertThat(r.path("items").get(0).path("manufacturerLotNo").asText()).isEqualTo("GL-20261008");
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_receipt_number_reservation WHERE created_by=? AND bound_receipt_item_id IS NOT NULL",Long.class,actor)).isEqualTo(2);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_inventory_ledger WHERE created_by=?",Long.class,actor)).isZero();
  assertThatThrownBy(()->service.confirmReceipt(r.path("id").asText(),new Command(0L,"逐批核对"),null,key())).hasMessageContaining("check");
 }
 @Test void confirmedReceiptCreatesExactlyOneBlockedLotPerRow(){
  String a=reserve(key()),b=reserve(key());var r=service.createReceipt(body(List.of(item(a,true),item(b,true)),true),key(),false);
  String id=r.path("id").asText(),k=key();var cmd=new Command(0L,"逐批核对通过");var confirmed=service.confirmReceipt(id,cmd,null,k);
  assertThat(service.confirmReceipt(id,cmd,null,k)).isEqualTo(confirmed);
  assertThat(confirmed.path("items").get(0).path("qualityStatus").asText()).isEqualTo("QUARANTINE");
  assertThat(confirmed.path("items").get(0).path("inventoryStatus").asText()).isEqualTo("BLOCKED");
  assertThat(confirmed.path("items").get(0).path("sourceSnapshot").path("manufacturerName").asText()).isEqualTo("湖南尔康制药股份有限公司");
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_inventory_ledger WHERE created_by=?",Long.class,actor)).isEqualTo(2);
  var lotView=service.get("MaterialLot",confirmed.path("items").get(0).path("materialLotId").asText());
  assertThat(lotView.path("manufacturerLotNo").asText()).isEqualTo("GL-20261008");
  assertThat(lotView.path("supplierLotNo").isNull()).isTrue();
 }
 @Test void arbitraryUnreservedNumberIsRejected(){
  assertThatThrownBy(()->service.createReceipt(body(List.of(item("RM-20261018-90901",true)),true),key(),false)).hasMessageContaining("分配");
 }
 @Test void savedMaterialIdentityCannotBeReplaced(){
  String a=reserve(key());var r=service.createReceipt(body(List.of(item(a,true)),true),key(),false);String b=reserve(key());var x=body(List.of(item(b,true)),true);
  assertThatThrownBy(()->service.updateReceipt(r.path("id").asText(),new ReceiptUpdate(x.receiptNo(),x.supplierId(),x.purchaseOrderNo(),x.deliveryNoteNo(),x.warehouseId(),true,x.items(),0L,"复核明细",x.inputContract(),x.arrivalDate(),x.acceptanceText()),null,key())).hasMessageContaining("不能替换");
 }
 @Test void sourceContractCannotBeDowngraded(){
  String a=reserve(key());var r=service.createReceipt(body(List.of(item(a,true)),true),key(),false);
  var x=body(List.of(item(a,true)),true);
  assertThatThrownBy(()->service.updateReceipt(r.path("id").asText(),new ReceiptUpdate(x.receiptNo(),x.supplierId(),x.purchaseOrderNo(),x.deliveryNoteNo(),x.warehouseId(),true,x.items(),0L,"复核明细","LEGACY",x.arrivalDate(),x.acceptanceText()),null,key())).hasMessageContaining("Unsupported");
 }

 @Test void httpReservationAndSourceUseExistingScopedPermissions() throws Exception {
  String body=json.writeValueAsString(new ReceiptNumberingService.Reserve(material,key()));
  mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/wms/receipts/number-reservations")
   .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("warehouse").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("wms:receipt:create")))
   .header("Idempotency-Key",key()).contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(body))
   .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
  mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/wms/receipts/source-context").param("materialId",material).param("supplierId",supplier)
   .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("warehouse").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("wms:receipt:view"))))
   .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
   .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.source.manufacturerName").value("湖南尔康制药股份有限公司"));
 }
 @Test void httpReadOnlyCannotReserveAndUnknownPostsStayDenied() throws Exception {
  String body=json.writeValueAsString(new ReceiptNumberingService.Reserve(material,key()));
  for(String path:List.of("/api/v1/wms/receipts/number-reservations","/api/v1/wms/receipts/not-an-action")){
   mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(path)
    .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("reader").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("wms:receipt:view")))
    .header("Idempotency-Key",key()).contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(body))
    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
  }
 }

 private String month(){return com.hospital.mes.wms.domain.ReceiptNumberingPolicy.period(ZoneId.of("Asia/Shanghai"),Instant.now());}
 private void monthly(){permissions.add("iam:dict:update");when(contexts.current()).thenReturn(new CurrentPlatformContext(testOrg,actor,Set.of("WAREHOUSE_OPERATOR"),permissions,"request",key()));var rule=jdbc.queryForObject("SELECT version_no FROM wms_receipt_number_rule WHERE org_id=?",Long.class,testOrg);numbering.configureMonthly(new ReceiptNumberingService.ConfigureMonthly("DEV-RULE-MONTHLY-4",rule,"年月四位流水场景"),key());}
 @Test void monthlyNumberStartsAt0001AndUsesSharedMaterialSequence(){monthly();assertThat(reserve(key())).isEqualTo(month()+"0001");String other=materials.create(json.convertValue(Map.of("materialCode","KCL"+suffix,"materialName","氯化钾","materialType","RAW","baseUnitId",unit,"lotControlled",true),MaterialCommands.Create.class),key()).path("id").asText();assertThat(numbering.reserve(new ReceiptNumberingService.Reserve(other,key()),key()).path("lotNo").asText()).isEqualTo(month()+"0002");}
 @Test void monthChangeResetsAndPreviousIntentStaysStable(){monthly();String intent=key(),first=reserve(intent);jdbc.update("UPDATE wms_receipt_number_rule SET sequence_period='190001',next_value=50 WHERE org_id=?",testOrg);assertThat(reserve(intent)).isEqualTo(first);assertThat(reserve(key())).isEqualTo(month()+"0002");}
 @Test void firstNumberInNewMonthIgnoresPreviousMonthCounter(){monthly();jdbc.update("UPDATE wms_receipt_number_rule SET sequence_period='190001',next_value=8888 WHERE org_id=?",testOrg);assertThat(reserve(key())).isEqualTo(month()+"0001");}
 @Test void monthlyCapacityNeverWrapsOrExpands(){monthly();jdbc.update("UPDATE wms_receipt_number_rule SET next_value=9999 WHERE org_id=?",testOrg);assertThat(reserve(key())).isEqualTo(month()+"9999");assertThatThrownBy(()->reserve(key())).hasMessageContaining("9999");assertThat(jdbc.queryForObject("SELECT next_value FROM wms_receipt_number_rule WHERE org_id=?",Long.class,testOrg)).isEqualTo(10000);}
 @Test void reconfigurationDoesNotRecycleAbandonedOrBoundNumbers(){monthly();String a=reserve(key());var r=service.createReceipt(body(List.of(item(a,true)),true),key(),false);String abandoned=reserve(key());monthly();assertThat(abandoned).endsWith("0002");assertThat(reserve(key())).endsWith("0003");assertThat(service.get("Receipt",r.path("id").asText()).path("items").get(0).path("lotNo").asText()).isEqualTo(a);}
 @Test void monthlyConfigurationCannotBeChangedByReceiptOperator() throws Exception {
  mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/wms/receipts/number-rule/monthly-four-digit").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("warehouse").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("wms:receipt:create"))).header("Idempotency-Key",key()).contentType(org.springframework.http.MediaType.APPLICATION_JSON).content("{\"approvalReference\":\"DEV-RULE-MONTHLY-4\",\"versionNo\":0,\"reason\":\"核对规则\"}"))
   .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
 }
 @Test void staleConfigurationCannotResetSequence(){monthly();reserve(key());assertThatThrownBy(()->numbering.configureMonthly(new ReceiptNumberingService.ConfigureMonthly("DEV-RULE-MONTHLY-4",0L,"核对规则"),key())).hasMessageContaining("变化");assertThat(reserve(key())).endsWith("0002");}

 @Test void sourceAArrivalUsesTheSameBusinessDateAsMonthlyNumbering(){
  monthly();var rows=List.of(item(reserve(key()),null));var x=body(rows,null);String today=LocalDate.now(ZoneId.of("Asia/Shanghai")).toString();
  var good=new ReceiptCreate(x.receiptNo(),supplier,x.purchaseOrderNo(),x.deliveryNoteNo(),warehouse,null,rows,x.inputContract(),today,x.acceptanceText());
  assertThat(service.createReceipt(good,key(),false).path("arrivalDate").asText()).isEqualTo(today);
  var future=new ReceiptCreate(x.receiptNo()+"N",supplier,x.purchaseOrderNo(),x.deliveryNoteNo(),warehouse,null,rows,x.inputContract(),LocalDate.parse(today).plusDays(1).toString(),x.acceptanceText());
  assertThatThrownBy(()->service.createReceipt(future,key(),false)).hasMessageContaining("到货日期");
 }

 @Test void sourceAExemptReceiptUsesExistingReleaseEvent() {
  jdbc.update("UPDATE md_material SET requires_incoming_inspection=0 WHERE org_id=? AND id=?",testOrg,material);
  var r=service.createReceipt(body(List.of(item(reserve(key()),true)),true),key(),false);
  var result=service.confirmReceipt(r.path("id").asText(),new Command(0L,"按既有免验规则核对收货"),null,key());
  String lotId=result.path("items").get(0).path("materialLotId").asText();
  var lot=service.get("MaterialLot",lotId);
  assertThat(lot.path("requiresIncomingInspectionSnapshot").asBoolean()).isFalse();
  assertThat(lot.path("inventoryStatus").asText()).isEqualTo("AVAILABLE");
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_release_decision WHERE org_id=? AND material_lot_id=? AND decision_source='SYSTEM_RULE'",Long.class,testOrg,lotId)).isEqualTo(1);
 }
 @Test void sourceAUncheckedExemptReceiptCannotCreateRelease() {
  jdbc.update("UPDATE md_material SET requires_incoming_inspection=0 WHERE org_id=? AND id=?",testOrg,material);
  var r=service.createReceipt(body(List.of(item(reserve(key()),null)),null),key(),false);
  assertThatThrownBy(()->service.confirmReceipt(r.path("id").asText(),new Command(0L,"核对未完成不得接收入库"),null,key())).hasMessageContaining("check");
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM md_material_lot WHERE org_id=?",Long.class,testOrg)).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_inventory_ledger WHERE org_id=?",Long.class,testOrg)).isZero();
 }
 @Test void sourceATwoEvidenceFilesAreRetainedReplaySafeAndScoped() {
  permissions.addAll(Set.of("attachment:upload","attachment:view"));
  when(contexts.current()).thenReturn(new CurrentPlatformContext(testOrg,actor,Set.of("WAREHOUSE_OPERATOR"),permissions,"request",key()));
  String number=reserve(key());var r=service.createReceipt(body(List.of(item(number,true)),true),key(),false);
  String id=r.path("id").asText();var confirmed=service.confirmReceipt(id,new Command(0L,"逐批核对接收"),null,key());
  var before=service.get("Receipt",id).deepCopy();
  for(int n=1;n<=2;n++){
   byte[] content=("收货核对记录\n内部物料批号："+number+"\n生产批号：GL-20261008\n文件序号："+n).getBytes(java.nio.charset.StandardCharsets.UTF_8);
   var file=fileService.upload("收货核对记录"+n+".txt","text/plain",content,key());
   String linkKey=key();var command=new WmsAttachmentService.Link(confirmed.path("versionNo").asLong(),file.id(),"OTHER","保存收货核对记录");
   String header="\""+confirmed.path("versionNo").asLong()+"\"";
   var linked=fileLinks.link(id,command,header,linkKey);
   assertThat(fileLinks.link(id,command,header,linkKey)).isEqualTo(linked);
   assertThat(fileLinks.download(id,file.id()).content()).containsExactly(content);
  }
  assertThat(fileLinks.list(id)).hasSize(2);
  assertThat(service.get("Receipt",id)).isEqualTo(before);
  var fileId=fileLinks.list(id).getFirst().attachment().id();
  when(contexts.current()).thenReturn(new CurrentPlatformContext(testOrg+1,actor,Set.of("WAREHOUSE_OPERATOR"),permissions,"request",key()));
  assertThatThrownBy(()->fileLinks.list(id)).isInstanceOf(NoSuchElementException.class);
  assertThatThrownBy(()->fileLinks.download(id,fileId)).isInstanceOf(NoSuchElementException.class);
 }
 @Test void sourceAReceiptCannotBeReadOrConfirmedAcrossOrganizations() {
  var r=service.createReceipt(body(List.of(item(reserve(key()),true)),true),key(),false);String id=r.path("id").asText();
  when(contexts.current()).thenReturn(new CurrentPlatformContext(testOrg+1,actor,Set.of("WAREHOUSE_OPERATOR"),permissions,"request",key()));
  assertThatThrownBy(()->service.get("Receipt",id)).isInstanceOf(NoSuchElementException.class);
  assertThatThrownBy(()->service.confirmReceipt(id,new Command(0L,"跨组织记录不得确认"),null,key())).isInstanceOf(NoSuchElementException.class);
 }
}
