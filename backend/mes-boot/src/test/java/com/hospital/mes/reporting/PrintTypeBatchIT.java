package com.hospital.mes.reporting;

import com.hospital.mes.reporting.application.*;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.wms.application.*;
import com.hospital.mes.wms.domain.WmsCommands.*;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import java.util.*;
import java.time.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** New test-owned WMS records and real database/PDF, all writes rolled back. */
@SpringBootTest @ActiveProfiles("ci") @Transactional
class PrintTypeBatchIT {
 @Autowired PrintService printing;@Autowired PrintBatchService batches;@Autowired NativePrintDesigner designer;
 @Autowired JdbcTemplate jdbc;@Autowired ObjectMapper json;@Autowired WmsService wms;@Autowired MaterialService materials;
 @Autowired UnitService units;@Autowired SupplierService suppliers;@Autowired MaterialSupplierService links;
 @MockitoBean CurrentPlatformContextResolver contexts;
 String suffix,login,first,second;long actor;Set<String> rights;
 @BeforeEach void setup(){
  suffix=UUID.randomUUID().toString().replace("-","").substring(0,12);login="batch_it_"+suffix;
  jdbc.update("INSERT INTO sys_user(login_name,login_name_normalized,display_name,password_hash,enabled,must_change_password) VALUES(?,?,?,?,TRUE,FALSE)",login,login,"Batch rollback operator","test-only");
  actor=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?",Long.class,login);
  rights=new HashSet<>(Set.of("print:template:view","print:template:manage","print:template:publish","print:document:generate"));
  for(String prefix:List.of("master:material:","master:supplier:","master:uom:","wms:inventory:","wms:receipt:"))for(String action:List.of("view","create","update"))rights.add(prefix+action);as(1);
  String unit=units.create(new UnitCommands.Create("BU"+suffix,"kg","MASS",6),key()).path("id").asText();
  String material=materials.create(json.convertValue(Map.of("materialCode","BM"+suffix,"materialName","Unique salt "+suffix,"materialType","RAW","baseUnitId",unit,"lotControlled",true),MaterialCommands.Create.class),key()).path("id").asText();
  String supplier=suppliers.create(new SupplierCommands.Create("BS"+suffix,"Unique supplier "+suffix,LocalDate.now(ZoneOffset.UTC).plusYears(1)),key()).path("id").asText();
  links.assign(material,new SupplierCommands.Assign(0L,"Test-owned source",List.of(new SupplierCommands.Relationship(supplier,true,true,null))),null,key());
  String warehouse=wms.maintenance("Warehouse",null,new WarehouseCreate("BW"+suffix,"Batch warehouse","RAW"),null,key()).path("id").asText();
  String location=wms.maintenance("Location",null,new LocationCreate(warehouse,"BL"+suffix,"Batch location"),null,key()).path("id").asText();
  for(int i=1;i<=2;i++){
   var item=new ReceiptItemInput(material,"LOT"+suffix+i,"SUPLOT"+suffix+i,null,null,null,null,""+(i*5),unit,"TESTSPEC-"+i,1L,location,null,true,true,true,true,true);
   var receipt=wms.createReceipt(new ReceiptCreate("BATCH"+suffix+i,supplier,"PO-"+i,null,warehouse,true,List.of(item)),key(),false);
   if(i==1)first=receipt.path("id").asText();else second=receipt.path("id").asText();
  }
 }
 String key(){return UUID.randomUUID().toString();}
 void as(long org){when(contexts.current()).thenReturn(new CurrentPlatformContext(org,actor,Set.of("TEST"),rights,"batch-session","batch-request"));}
 JsonNode template(String kind)throws Exception{
  var schema=PrintSchema.of(printing.provider("WMS_RECEIPT"),kind);
  String columns=kind.equals("LIST")?"\"recordReceiptNo\",\"recordSupplierName\",\"recordMaterialNames\"":"\"materialName\",\"receivedQty\",\"unit\",\"packageSpec\"";
  var design=json.readTree("{\"blocks\":[{\"id\":\"mode\",\"type\":\"FIELD\",\"fieldKey\":\"modeLabel\"},{\"id\":\"no\",\"type\":\"FIELD\",\"fieldKey\":\"reportNo\"},{\"id\":\"rows\",\"type\":\"TABLE\",\"columns\":["+columns+"]}]}");
  var row=printing.upload("BT"+suffix+kind,"Batch test "+kind,"WMS_RECEIPT",kind,designer.generate(design,schema.definitions()),"Rollback test");
  row=printing.validate(row.path("id").asText(),row.path("versionNo").asLong(),"Real PDF test");row=printing.publish(row.path("id").asText(),row.path("versionNo").asLong(),"Test publish");printing.bind(row.path("id").asText(),"WMS_RECEIPT",true,"Test binding");return row;
 }
 @Test void sameSelectionPrintsCompleteDocumentsOrOneListAndFreezesActualFacts()throws Exception{
  var document=template("DOCUMENT");var list=template("LIST");var ids=List.of(second,first);
  assertThat(printing.nativeDesign(list.path("id").asText()).path("template").path("printType").asText()).isEqualTo("LIST");
  assertThat(printing.fields("WMS_RECEIPT","LIST").get("itemFields").toString()).contains("recordReceiptNo").doesNotContain("receivedQty");
  var documentBatch=batches.generate("WMS_RECEIPT",ids,document.path("id").asText(),false);var listBatch=batches.generate("WMS_RECEIPT",ids,list.path("id").asText(),false);
  byte[] frozen=batches.pdf(listBatch.path("id").asText());
  try(var pdf=Loader.loadPDF(batches.pdf(documentBatch.path("id").asText()))){assertThat(pdf.getNumberOfPages()).isEqualTo(2);String text=new PDFTextStripper().getText(pdf);assertThat(text).contains("TESTSPEC-1","TESTSPEC-2","BATCH"+suffix+1,"BATCH"+suffix+2);assertThat(text.indexOf("BATCH"+suffix+2)).isLessThan(text.indexOf("BATCH"+suffix+1));}
  try(var pdf=Loader.loadPDF(frozen)){assertThat(pdf.getNumberOfPages()).isEqualTo(1);String text=new PDFTextStripper().getText(pdf);assertThat(text).contains("BATCH"+suffix+1,"BATCH"+suffix+2).containsPattern("Unique supplier\\s+"+suffix).doesNotContain("TESTSPEC-1","TESTSPEC-2");}
  jdbc.update("UPDATE wms_material_receipt SET purchase_order_no='CHANGED TEST-OWNED SOURCE' WHERE id=?",first);
  assertThat(batches.pdf(listBatch.path("id").asText())).isEqualTo(frozen);
  assertThatThrownBy(()->jdbc.update("UPDATE mes_print_template_version SET print_type='DOCUMENT' WHERE id=?",list.path("id").asText())).hasMessageContaining("Immutable print template type");
  assertThatThrownBy(()->jdbc.update("UPDATE mes_print_batch SET snapshot_json='{}' WHERE id=?",listBatch.path("id").asText())).hasMessageContaining("Immutable print batch");
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mes_print_artifact WHERE created_by=?",Long.class,actor)).isEqualTo(2);
 }
 @Test void rejectsWrongModeDuplicatesForeignScopeAndMissingReadPermission()throws Exception{
  var list=template("LIST");String id=list.path("id").asText();
  assertThatThrownBy(()->batches.generate("WMS_RECEIPT",List.of(first),id,true)).hasMessageContaining("列表汇总");
  assertThatThrownBy(()->batches.generate("WMS_RECEIPT",List.of(first,first),id,false)).hasMessageContaining("重复");
  assertThatThrownBy(()->batches.generate("WMS_RECEIPT",List.of(first,"9223372036854775807"),id,false)).isInstanceOf(NoSuchElementException.class);
  rights.remove("wms:receipt:view");as(1);assertThatThrownBy(()->batches.generate("WMS_RECEIPT",List.of(first),id,false)).hasMessageContaining("Permission required");rights.add("wms:receipt:view");as(2);
  assertThatThrownBy(()->batches.generate("WMS_RECEIPT",List.of(first),id,false)).isInstanceOf(NoSuchElementException.class);
  as(1);assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mes_print_batch WHERE created_by=?",Long.class,actor)).isZero();
 }
 @AfterTransaction void rollback(){assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_user WHERE login_name_normalized=?",Long.class,login)).isZero();assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_material_receipt WHERE receipt_no LIKE ?",Long.class,"BATCH"+suffix+"%")).isZero();}
}
