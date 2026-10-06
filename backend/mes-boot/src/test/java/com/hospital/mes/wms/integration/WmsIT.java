package com.hospital.mes.wms.integration;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.wms.application.*;
import com.hospital.mes.wms.domain.WmsCommands.*;
import java.util.*;
import java.time.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Native persistent DEV: unique test-owned fixtures; transaction rollback preserves the ledger. */
@SpringBootTest @ActiveProfiles("ci") @AutoConfigureMockMvc @Transactional
class WmsIT {
 @Autowired MasterMutation sourceAuditDigest;
 @Autowired WmsService service;@Autowired WmsQueryService queries;@Autowired MaterialService materials;@Autowired UnitService units;@Autowired UnitConversionService conversions;@Autowired SupplierService suppliers;@Autowired MaterialSupplierService links;
 @Autowired ObjectMapper json;@Autowired JdbcTemplate jdbc;@Autowired MockMvc mvc;@Autowired PlatformTransactionManager transactions;
 @MockitoSpyBean com.hospital.mes.wms.infrastructure.WmsStore store;
 @MockitoBean CurrentPlatformContextResolver contexts;@MockitoSpyBean AuditApplicationService audit;
 String suffix,unit,material,supplier,warehouse,location,destination;long actor;Set<String> permissions;
 @BeforeEach void setup(TestInfo info){
  suffix=UUID.randomUUID().toString().replace("-","").substring(0,12);permissions=new HashSet<>();for(String p:List.of("master:material:","master:supplier:","master:uom:","wms:inventory:","wms:receipt:","wms:issue:","wms:reservation:"))for(String a:List.of("view","create","update","disable","confirm","return","move","adjust"))permissions.add(p+a);
  if(info.getTestMethod().orElseThrow().getName().equals("concurrentLotWriterMakesWaitingAdjustmentRejectStaleVersion"))return;
  String name="wms_"+suffix;jdbc.update("INSERT INTO sys_user(login_name,login_name_normalized,display_name,password_hash,enabled,must_change_password) VALUES(?,?,?,?,TRUE,FALSE)",name,name,"WMS rollback actor","test-only");actor=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?",Long.class,name);as(1);
  unit=units.create(new UnitCommands.Create("WU"+suffix,"kg","MASS",6),key()).path("id").asText();material=materials.create(json.convertValue(Map.of("materialCode","WM"+suffix,"materialName","Raw salt","materialType","RAW","baseUnitId",unit,"lotControlled",true),MaterialCommands.Create.class),key()).path("id").asText();
  var s=suppliers.create(new SupplierCommands.Create("WS"+suffix,"Approved source",LocalDate.now(ZoneOffset.UTC).plusYears(1)),key());supplier=s.path("id").asText();suppliers.command(supplier,new SupplierCommands.Update("QUALIFY",0L,"Qualification evidence",null,null),null,key());links.assign(material,new SupplierCommands.Assign(0L,"Approved source",List.of(new SupplierCommands.Relationship(supplier,true,true,null))),null,key());
  warehouse=service.maintenance("Warehouse",null,new WarehouseCreate("WH"+suffix,"Warehouse","RAW"),null,key()).path("id").asText();location=service.maintenance("Location",null,new LocationCreate(warehouse,"A"+suffix,"Area A"),null,key()).path("id").asText();destination=service.maintenance("Location",null,new LocationCreate(warehouse,"B"+suffix,"Area B"),null,key()).path("id").asText();
 }
 void as(long org){when(contexts.current()).thenReturn(new CurrentPlatformContext(org,actor,Set.of("WMS_TEST"),permissions,"test","wms-"+suffix));}
 String key(){return UUID.randomUUID().toString();}
 ReceiptItemInput item(String lot,String qty,String u,boolean checks){return new ReceiptItemInput(material,lot,"SLOT"+suffix,null,LocalDate.now(ZoneOffset.UTC).minusDays(1).toString(),LocalDate.now(ZoneOffset.UTC).plusYears(1).toString(),null,qty,u,null,1L,location,null,checks,true,true,true,true);}
 ReceiptCreate receipt(boolean checks){return new ReceiptCreate("WR"+suffix,supplier,null,null,warehouse,true,List.of(item("WL"+suffix,"5",unit,checks)));}
 JsonNode received(){return service.createReceipt(receipt(true),key(),true);}
 String lot(JsonNode r){return r.path("items").get(0).path("materialLotId").asText();}
 long count(String table){return jdbc.queryForObject("SELECT COUNT(*) FROM "+table+" WHERE created_by=?",Long.class,actor);}

 @Test void draftSaveConfirmIdempotencyAndSnapshotAreStable(){
  var r=service.createReceipt(receipt(true),key(),false);String id=r.path("id").asText();assertThat(r.path("recordStatus").asText()).isEqualTo("DRAFT");assertThat(count("md_material_lot")).isZero();
  var body=receipt(true);var saved=service.updateReceipt(id,new ReceiptUpdate(body.receiptNo(),body.supplierId(),"PO-1",null,body.warehouseId(),true,body.items(),0L,"Saved evidence"),null,key());assertThat(saved.path("versionNo").asInt()).isEqualTo(1);
  String k=key();var command=new Command(1L,"Receipt checked");var confirmed=service.confirmReceipt(id,command,null,k);assertThat(service.confirmReceipt(id,command,null,k)).isEqualTo(confirmed);assertThat(count("md_material_lot")).isEqualTo(1);assertThat(count("wms_inventory_ledger")).isEqualTo(1);var l=queries.materialLot(1,Long.parseLong(lot(confirmed)));assertThat(l.path("qualityStatus").asText()).isEqualTo("QUARANTINE");assertThat(l.path("inventoryStatus").asText()).isEqualTo("BLOCKED");
  jdbc.update("UPDATE md_material SET material_name='Changed basic data',requires_incoming_inspection=0 WHERE id=?",material);assertThat(queries.materialLot(1,Long.parseLong(lot(confirmed))).path("materialSnapshot")).isEqualTo(l.path("materialSnapshot"));assertThat(l.path("materialSnapshot").has("materialVersionId")).isFalse();
  assertThatThrownBy(()->service.confirmReceipt(id,new Command(2L,"again"),null,key())).hasMessageContaining("draft");assertThatThrownBy(()->service.updateReceipt(id,new ReceiptUpdate(body.receiptNo(),supplier,null,null,warehouse,true,body.items(),2L,"edit confirmed"),null,key())).hasMessageContaining("draft");
 }
 @Test void directReceivePreservesOriginalPermissionAndRecordsControlledExemption() throws Exception {
  jdbc.update("UPDATE md_material SET requires_incoming_inspection=0 WHERE id=?",material);permissions.remove("wms:receipt:confirm");as(1);String k=key();String content=json.writeValueAsString(receipt(true));
  for(int i=0;i<2;i++)mvc.perform(post("/api/v1/inventory/receive").with(user("wms").authorities(new SimpleGrantedAuthority("wms:receipt:create"))).header("Idempotency-Key",k).contentType(MediaType.APPLICATION_JSON).content(content)).andExpect(status().isOk()).andExpect(jsonPath("$.data.recordStatus").value("APPROVED"));
  assertThat(count("wms_inventory_ledger")).isEqualTo(1);assertThat(jdbc.queryForObject("SELECT inventory_status FROM md_material_lot WHERE created_by=?",String.class,actor)).isEqualTo("AVAILABLE");assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_release_decision WHERE material_lot_id IN (SELECT id FROM md_material_lot WHERE created_by=?) AND decision_source='SYSTEM_RULE'",Long.class,actor)).isEqualTo(1);assertThat(jdbc.queryForObject("SELECT requires_incoming_inspection_snapshot FROM md_material_lot WHERE created_by=?",Boolean.class,actor)).isFalse();
 }
 @Test void approvedNonpreferredSupplierAllowedButExpiredSourceAndFailedChecksBlocked(){
  var other=suppliers.create(new SupplierCommands.Create("WP"+suffix,"Preferred source",LocalDate.now(ZoneOffset.UTC).plusYears(1)),key());String preferred=other.path("id").asText();suppliers.command(preferred,new SupplierCommands.Update("QUALIFY",0L,"Approved",null,null),null,key());links.assign(material,new SupplierCommands.Assign(1L,"Two approved sources",List.of(new SupplierCommands.Relationship(supplier,true,false,null),new SupplierCommands.Relationship(preferred,true,true,null))),null,key());
  var r=service.createReceipt(receipt(false),key(),false);String id=r.path("id").asText();assertThatThrownBy(()->service.confirmReceipt(id,new Command(0L,"checks"),null,key())).hasMessageContaining("Every receipt");assertThat(count("wms_inventory_ledger")).isZero();
  var input=receipt(true);service.updateReceipt(id,new ReceiptUpdate(input.receiptNo(),supplier,null,null,warehouse,true,input.items(),0L,"Correct package checks"),null,key());jdbc.update("UPDATE md_supplier SET valid_to=? WHERE id=?",LocalDate.now(ZoneOffset.UTC).minusDays(1),supplier);assertThatThrownBy(()->service.confirmReceipt(id,new Command(1L,"expiry"),null,key())).hasMessageContaining("SUPPLIER_NOT_APPROVED");assertThat(count("md_material_lot")).isZero();jdbc.update("UPDATE md_supplier SET valid_to=? WHERE id=?",LocalDate.now(ZoneOffset.UTC).plusYears(1),supplier);assertThat(service.confirmReceipt(id,new Command(1L,"valid nonpreferred source"),null,key()).path("recordStatus").asText()).isEqualTo("APPROVED");
 }
 @Test void exemptReceiptRequiresEveryCheckAndApprovedSupplierBeforeAnyReleaseFacts(){
  jdbc.update("UPDATE md_material SET requires_incoming_inspection=0 WHERE id=?",material);
  var tx=new TransactionTemplate(transactions);tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_NESTED);
  for(int failed=0;failed<5;failed++){
   boolean[] checks={true,true,true,true,true};checks[failed]=false;
   var input=new ReceiptItemInput(material,"EXLOT"+suffix+failed,"EXSUP"+suffix,null,LocalDate.now(ZoneOffset.UTC).minusDays(1).toString(),LocalDate.now(ZoneOffset.UTC).plusYears(1).toString(),null,"5",unit,null,1L,location,null,checks[0],checks[1],checks[2],checks[3],checks[4]);
   String id=service.createReceipt(new ReceiptCreate("EXR"+suffix+failed,supplier,null,null,warehouse,true,List.of(input)),key(),false).path("id").asText();
   assertThatThrownBy(()->tx.execute(s->service.confirmReceipt(id,new Command(0L,"Failed receipt check"),null,key()))).hasMessageContaining("Every receipt");
   assertThat(jdbc.queryForObject("SELECT record_status FROM wms_material_receipt WHERE id=?",String.class,id)).isEqualTo("DRAFT");
  }
  String id=service.createReceipt(new ReceiptCreate("EXSOURCE"+suffix,supplier,null,null,warehouse,true,List.of(item("EXSLOT"+suffix,"5",unit,true))),key(),false).path("id").asText();
  jdbc.update("UPDATE md_supplier SET qualification_status='UNAPPROVED' WHERE id=?",supplier);
  assertThatThrownBy(()->tx.execute(s->service.confirmReceipt(id,new Command(0L,"Unapproved source"),null,key()))).hasMessageContaining("SUPPLIER_NOT_APPROVED");
  jdbc.update("UPDATE md_supplier SET qualification_status='APPROVED',valid_to=? WHERE id=?",LocalDate.now(ZoneOffset.UTC).minusDays(1),supplier);
  assertThatThrownBy(()->tx.execute(s->service.confirmReceipt(id,new Command(0L,"Expired source"),null,key()))).hasMessageContaining("SUPPLIER_NOT_APPROVED");
  jdbc.update("UPDATE md_supplier SET valid_to=? WHERE id=?",LocalDate.now(ZoneOffset.UTC).plusYears(1),supplier);
  jdbc.update("UPDATE md_material_supplier SET approved=0,preferred=0 WHERE material_id=? AND supplier_id=?",material,supplier);
  assertThatThrownBy(()->tx.execute(s->service.confirmReceipt(id,new Command(0L,"Unapproved material source"),null,key()))).hasMessageContaining("SUPPLIER_NOT_APPROVED");
  jdbc.update("UPDATE md_material_supplier SET approved=1,valid_to=? WHERE material_id=? AND supplier_id=?",LocalDate.now(ZoneOffset.UTC).minusDays(1),material,supplier);
  assertThatThrownBy(()->tx.execute(s->service.confirmReceipt(id,new Command(0L,"Expired material source"),null,key()))).hasMessageContaining("SUPPLIER_NOT_APPROVED");
  assertThat(count("md_material_lot")).isZero();assertThat(count("wms_inventory_ledger")).isZero();assertThat(count("qms_release_decision")).isZero();
 }
 @Test void controlledExemptionHasRuleEvidenceNullHumanSignerAndNoFabricatedQcRecords(){
  jdbc.update("UPDATE md_material SET requires_incoming_inspection=0 WHERE id=?",material);
  String lot=lot(received());
  var decision=jdbc.queryForMap("SELECT * FROM qms_release_decision WHERE material_lot_id=?",lot);
  assertThat(decision.get("decision_source")).isEqualTo("SYSTEM_RULE");assertThat(decision.get("release_basis")).isEqualTo("INSPECTION_EXEMPT");assertThat(decision.get("decision_by")).isNull();assertThat(decision.get("signature_id")).isNull();assertThat(decision.get("signature_evidence_json")).isNull();assertThat(decision.get("inspection_report_id")).isNull();
  JsonNode evidence;try{evidence=json.readTree(decision.get("rule_evidence_json").toString());}catch(Exception e){throw new AssertionError(e);}
  assertThat(evidence.path("ruleCode").asText()).isEqualTo("INCOMING_INSPECTION_EXEMPTION");assertThat(evidence.path("materialLotId").asText()).isEqualTo(lot);assertThat(evidence.path("receiptChecksPassed").asBoolean()).isTrue();assertThat(evidence.path("requiresIncomingInspectionSnapshot").asBoolean()).isFalse();assertThat(evidence.path("evidenceDigest").asText()).isEqualTo(decision.get("evidence_digest"));
  for(String table:List.of("qms_inspection_request","qms_sampling_task","qms_sample","qms_inspection_task","qms_test_execution","qms_test_result_revision","qms_inspection_report"))assertThat(count(table)).as("No fabricated %s",table).isZero();
  assertThat(jdbc.queryForObject("SELECT quality_status FROM md_material_lot WHERE id=?",String.class,lot)).isEqualTo("RELEASED");
 }
 @Test void ledgerRebuildMoveAdjustmentPrecisionAndPhysicalAppendOnly(){
  var receipt=received();String lot=lot(receipt);String k=key();var move=new InventoryMove(lot,location,null,destination,null,"2",unit,0L,"Physical quarantine transfer");var moved=service.move(move,null,k);assertThat(service.move(move,null,k)).isEqualTo(moved);assertThat(count("wms_inventory_ledger")).isEqualTo(3);
  service.adjust(new InventoryAdjust(lot,destination,null,"-1",unit,1L,"Count correction"),null,key());var stock=service.inventory(0,20,null,Map.of("materialLotId",lot));assertThat(stock.items()).hasSize(2);assertThat(stock.items().stream().map(x->new java.math.BigDecimal(x.path("quantity").asText())).reduce(java.math.BigDecimal.ZERO,java.math.BigDecimal::add)).isEqualByComparingTo("4");
  assertThatThrownBy(()->service.adjust(new InventoryAdjust(lot,destination,null,"-2",unit,2L,"Would go negative"),null,key())).hasMessageContaining("negative");assertThatThrownBy(()->service.move(move,null,key())).hasMessageContaining("Reload");
  String small=units.create(new UnitCommands.Create("SM"+suffix,"small","MASS",6),key()).path("id").asText();conversions.create(new UnitConversionCommands.Create(small,unit,"0.0000001",material),key());assertThatThrownBy(()->service.adjust(new InventoryAdjust(lot,destination,null,"1",small,2L,"Precision evidence"),null,key())).hasMessageContaining("lose");
  Long ledger=jdbc.queryForObject("SELECT MIN(id) FROM wms_inventory_ledger WHERE created_by=?",Long.class,actor);assertThatThrownBy(()->jdbc.update("UPDATE wms_inventory_ledger SET delta_qty=99 WHERE id=?",ledger)).hasMessageContaining("append-only");assertThatThrownBy(()->jdbc.update("DELETE FROM wms_inventory_ledger WHERE id=?",ledger)).hasMessageContaining("append-only");
 }
 @Test void organizationPermissionClosedDtoAndInvalidProductionWritesFailClosed() throws Exception {
  var r=received();String lot=lot(r);mvc.perform(get("/api/v1/wms/receipts/"+r.path("id").asText()).with(user("wms").authorities(new SimpleGrantedAuthority("wms:receipt:view")))).andExpect(status().isOk());
  mvc.perform(post("/api/v1/material-issues").with(user("wms").authorities(new SimpleGrantedAuthority("wms:issue:create"))).header("Idempotency-Key",key()).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
  mvc.perform(post("/api/v1/main-batches/123/reservations").with(user("wms").authorities(new SimpleGrantedAuthority("wms:reservation:create"))).header("Idempotency-Key",key()).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
  mvc.perform(post("/api/v1/material-lots").with(user("wms").authorities(new SimpleGrantedAuthority("wms:inventory:create"))).header("Idempotency-Key",key()).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.code").value("RECEIPT_REQUIRED"));
  mvc.perform(post("/api/v1/warehouses").with(user("wms").authorities(new SimpleGrantedAuthority("wms:inventory:create"))).header("Idempotency-Key",key()).contentType(MediaType.APPLICATION_JSON).content("{\"warehouseCode\":\"BAD\",\"status\":\"INACTIVE\"}")).andExpect(status().isBadRequest());
  ObjectNode invalidDate=json.valueToTree(receipt(true));invalidDate.put("receiptNo","INVALID-DATE-"+suffix);((ObjectNode)invalidDate.path("items").get(0)).put("manufactureDate","2026-99-01");
  mvc.perform(post("/api/v1/wms/receipts").with(user("wms").authorities(new SimpleGrantedAuthority("wms:receipt:create"))).header("Idempotency-Key",key()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(invalidDate))).andExpect(status().isBadRequest());
  mvc.perform(get("/api/v1/inventory").with(user("wms").authorities(new SimpleGrantedAuthority("wms:receipt:view")))).andExpect(status().isForbidden());as(2);assertThatThrownBy(()->queries.materialLot(2,Long.parseLong(lot))).isInstanceOf(NoSuchElementException.class);assertThat(service.inventory(0,20,null,Map.of()).total()).isZero();
 }
 @Test void frozenAndExpiredLotsCannotMoveOrAdjust(){String lot=lot(received());jdbc.update("UPDATE md_material_lot SET inventory_status='FROZEN' WHERE id=?",lot);assertThatThrownBy(()->service.adjust(new InventoryAdjust(lot,location,null,"1",unit,0L,"frozen"),null,key())).hasMessageContaining("Frozen");jdbc.update("UPDATE md_material_lot SET inventory_status='BLOCKED',expiry_date=? WHERE id=?",LocalDate.now(ZoneOffset.UTC),lot);assertThatThrownBy(()->service.move(new InventoryMove(lot,location,null,destination,null,"1",unit,0L,"expired"),null,key())).hasMessageContaining("Expiry");assertThat(count("wms_inventory_ledger")).isEqualTo(1);}
 @Test void confirmAuditFailureRollsBackLotLedgerSnapshotAndIdempotency(){var r=service.createReceipt(receipt(true),key(),false);String id=r.path("id").asText(),k=key();var tx=new TransactionTemplate(transactions);tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_NESTED);doAnswer(inv->{var a=(com.hospital.mes.audit.domain.AuditCommand)inv.getArgument(0);if(a.action().equals("Receipt:CONFIRM"))throw new IllegalStateException("injected WMS audit failure");return inv.callRealMethod();}).when(audit).append(any());assertThatThrownBy(()->tx.execute(t->service.confirmReceipt(id,new Command(0L,"atomic confirmation"),null,k))).hasMessageContaining("injected WMS audit failure");assertThat(count("md_material_lot")).isZero();assertThat(count("wms_inventory_ledger")).isZero();assertThat(jdbc.queryForObject("SELECT source_snapshot_json FROM wms_material_receipt_item WHERE receipt_id=?",String.class,id)).isNull();assertThat(jdbc.queryForObject("SELECT record_status FROM wms_material_receipt WHERE id=?",String.class,id)).isEqualTo("DRAFT");assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,k)).isZero();}
 @Test void draftCannotDropStoredItemsAndStorageCannotDisableStock(){
  var draft=service.createReceipt(new ReceiptCreate("DRAFT"+suffix,supplier,null,null,warehouse,true,List.of(item("D1"+suffix,"1",unit,true),item("D2"+suffix,"1",unit,true))),key(),false);
  assertThatThrownBy(()->service.updateReceipt(draft.path("id").asText(),new ReceiptUpdate("DRAFT"+suffix,supplier,null,null,warehouse,true,List.of(item("D1"+suffix,"1",unit,true)),0L,"Remove saved row"),null,key())).hasMessageContaining("retained");
  received();assertThatThrownBy(()->service.maintenance("Location",location,new LocationUpdate(warehouse,"A"+suffix,"A","Disable with stock",0L,"DISABLE"),null,key())).hasMessageContaining("nonzero inventory");assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success=0",Long.class)).isZero();assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.referential_constraints WHERE constraint_schema=DATABASE() AND table_name IN ('wms_reservation','wms_material_issue') AND referenced_table_name='prd_main_batch'",Long.class)).isEqualTo(2);
 }

 @Test void concurrentLotWriterMakesWaitingAdjustmentRejectStaleVersion() throws Exception {
  var tx=new TransactionTemplate(transactions);tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  // Committed, uniquely owned lock fixture has no ledger. Only these fixture rows are removed.
  long[] ids=tx.execute(t->{
   String login="wrace_"+suffix;jdbc.update("INSERT INTO sys_user(login_name,login_name_normalized,display_name,password_hash,enabled,must_change_password) VALUES(?,?,?,'test-only',TRUE,FALSE)",login,login,"WMS lock fixture");
   long a=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?",Long.class,login);
   jdbc.update("INSERT INTO md_unit(org_id,created_by,updated_by,unit_code,unit_name,dimension,scale) VALUES(1,?,?,?,'Lock unit','MASS',6)",a,a,"WRU"+suffix);long u=jdbc.queryForObject("SELECT id FROM md_unit WHERE unit_code=?",Long.class,"WRU"+suffix);
   jdbc.update("INSERT INTO md_material(org_id,created_by,updated_by,material_code,material_name,material_type,base_unit_id,lot_controlled,status) VALUES(1,?,?,?,'Lock fixture','RAW',?,TRUE,'ACTIVE')",a,a,"WRM"+suffix,u);long m=jdbc.queryForObject("SELECT id FROM md_material WHERE material_code=?",Long.class,"WRM"+suffix);
   jdbc.update("INSERT INTO md_material_lot(org_id,created_by,updated_by,material_id,lot_no,material_snapshot_json,requires_incoming_inspection_snapshot,quality_status,inventory_status) VALUES(1,?,?,?,?,?,TRUE,'QUARANTINE','BLOCKED')",a,a,m,"WRL"+suffix,"{\"baseUnitId\":\""+u+"\"}");long l=jdbc.queryForObject("SELECT id FROM md_material_lot WHERE lot_no=?",Long.class,"WRL"+suffix);
   return new long[]{a,u,m,l};
  });
  actor=ids[0];as(1);var held=new java.util.concurrent.CountDownLatch(1);var waiting=new java.util.concurrent.CountDownLatch(1);var pool=java.util.concurrent.Executors.newFixedThreadPool(2);
  doAnswer(call->{waiting.countDown();return call.callRealMethod();}).when(store).materialLot();
  try {
   var writer=pool.submit(()->tx.execute(t->{jdbc.queryForList("SELECT id FROM md_material_lot WHERE id=? FOR UPDATE",ids[3]);held.countDown();try{if(!waiting.await(5,java.util.concurrent.TimeUnit.SECONDS))throw new AssertionError("Adjustment did not reach lot lock");}catch(InterruptedException e){Thread.currentThread().interrupt();throw new RuntimeException(e);}jdbc.update("UPDATE md_material_lot SET version_no=1 WHERE id=?",ids[3]);return null;}));
   assertThat(held.await(5,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
   var adjustment=pool.submit(()->{try{tx.execute(t->{t.setRollbackOnly();return service.adjust(new InventoryAdjust(Long.toString(ids[3]),"1",null,"1",Long.toString(ids[1]),0L,"Contending write"),null,key());});return (RuntimeException)null;}catch(RuntimeException e){return e;}});
   writer.get(10,java.util.concurrent.TimeUnit.SECONDS);assertThat(adjustment.get(10,java.util.concurrent.TimeUnit.SECONDS)).isInstanceOf(com.hospital.mes.common.exception.ResourceConflictException.class);
   Long ledgerCount=tx.execute(t->jdbc.queryForObject("SELECT COUNT(*) FROM wms_inventory_ledger WHERE material_lot_id=?",Long.class,ids[3]));assertThat(ledgerCount).isZero();
  } finally {
   pool.shutdownNow();pool.awaitTermination(10,java.util.concurrent.TimeUnit.SECONDS);reset(store);
   tx.execute(t->{jdbc.update("DELETE FROM md_material_lot WHERE id=? AND lot_no=?",ids[3],"WRL"+suffix);jdbc.update("DELETE FROM md_material WHERE id=? AND material_code=?",ids[2],"WRM"+suffix);jdbc.update("DELETE FROM md_unit WHERE id=? AND unit_code=?",ids[1],"WRU"+suffix);jdbc.update("DELETE FROM sys_user WHERE id=? AND login_name_normalized=?",ids[0],"wrace_"+suffix);return null;});
  }
 }

 @Test void receiptFreezesRelationshipManufacturerAndLotReadsSameSource(){
  var assigned=links.get(material);links.assign(material,new SupplierCommands.Assign(assigned.path("versionNo").asLong(),"Source at draft",List.of(new SupplierCommands.Relationship(supplier,true,true,null,"厂家甲"))),null,key());
  var draft=service.createReceipt(receipt(true),key(),false);assertThat(draft.path("items").get(0).path("sourceSnapshot").isNull()).isTrue();
  assigned=links.get(material);links.assign(material,new SupplierCommands.Assign(assigned.path("versionNo").asLong(),"Source at confirmation",List.of(new SupplierCommands.Relationship(supplier,true,true,null,"厂家乙"))),null,key());
  String id=draft.path("id").asText(),k=key();var confirmed=service.confirmReceipt(id,new Command(0L,"Freeze source"),null,k);var source=confirmed.path("items").get(0).path("sourceSnapshot").deepCopy();assertThat(jdbc.queryForObject("SELECT new_value_digest FROM gxp_audit_event WHERE object_id=? AND action='Receipt:CONFIRM'",String.class,id)).isEqualTo(sourceAuditDigest.digest(confirmed));assertThat(source.path("manufacturerName").asText()).isEqualTo("厂家乙");assertThat(source.path("supplierId").asText()).isEqualTo(supplier);
  String lotId=lot(confirmed);var lotRead=queries.materialLot(1,Long.parseLong(lotId));assertThat(lotRead.path("sourceSnapshot")).isEqualTo(source);assertThat(lotRead.path("qualityStatus").asText()).isEqualTo("QUARANTINE");assertThat(lotRead.path("inventoryStatus").asText()).isEqualTo("BLOCKED");
  assigned=links.get(material);links.assign(material,new SupplierCommands.Assign(assigned.path("versionNo").asLong(),"Later source",List.of(new SupplierCommands.Relationship(supplier,true,true,null,"厂家丙"))),null,key());
  assertThat(service.get("Receipt",id).path("items").get(0).path("sourceSnapshot")).isEqualTo(source);assertThat(queries.materialLot(1,Long.parseLong(lotId)).path("sourceSnapshot")).isEqualTo(source);assertThat(service.confirmReceipt(id,new Command(0L,"Freeze source"),null,k)).isEqualTo(confirmed);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_audit_event WHERE object_id=? AND action='Receipt:CONFIRM'",Long.class,id)).isEqualTo(1);
  jdbc.update("UPDATE wms_material_receipt_item SET source_snapshot_json=NULL WHERE id=?",confirmed.path("items").get(0).path("id").asLong());assertThat(queries.materialLot(1,Long.parseLong(lotId)).path("sourceSnapshot").isNull()).isTrue();
  var direct=service.createReceipt(new ReceiptCreate("DIRECTSRC"+suffix,supplier,null,null,warehouse,true,List.of(item("DIRSRCLOT"+suffix,"1",unit,true))),key(),true);
  assertThat(direct.path("items").get(0).path("sourceSnapshot").path("manufacturerName").asText()).isEqualTo("厂家丙");
  as(2);assertThatThrownBy(()->queries.materialLot(2,Long.parseLong(lotId))).isInstanceOf(NoSuchElementException.class);
 }
}
