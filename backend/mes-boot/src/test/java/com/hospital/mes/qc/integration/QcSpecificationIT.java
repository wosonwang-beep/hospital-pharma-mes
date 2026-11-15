package com.hospital.mes.qc.integration;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.qc.application.*;
import com.hospital.mes.qc.domain.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
/** Native persistent database: all unique actors/materials/QC rows roll back. No shared seed changes. */
@SpringBootTest @ActiveProfiles("ci") @Transactional
class QcSpecificationIT {
 @Autowired QcSpecificationService service; @Autowired QcSpecificationQueryService query;
 @Autowired UnitService units; @Autowired MaterialService materials; @Autowired JdbcTemplate jdbc; @Autowired ObjectMapper json;
 @Autowired PlatformTransactionManager transactions;
 @MockitoBean CurrentPlatformContextResolver contexts;

 long creator,editor; String suffix,unit,material,otherMaterial; Set<String> permissions;
 @BeforeEach void setup(){suffix=UUID.randomUUID().toString().replace("-","").substring(0,12);creator=actor("qc_c"+suffix);editor=actor("qc_e"+suffix);permissions=new HashSet<>(Set.of("qms:specification:view","qms:specification:create","qms:specification:edit","qms:specification:delete","master:uom:create","master:material:create"));as(1,creator);
  unit=units.create(new UnitCommands.Create("QU"+suffix,"QC unit","MASS",3),key()).path("id").asText();
  material=material("QM"+suffix,"QC material");otherMaterial=material("QN"+suffix,"QC second material");}
 String material(String code,String name){return materials.create(json.convertValue(Map.of("materialCode",code,"materialName",name,"materialType","RAW","baseUnitId",unit,"lotControlled",true),MaterialCommands.Create.class),key()).path("id").asText();}
 long actor(String n){jdbc.update("INSERT INTO sys_user(login_name,login_name_normalized,display_name,password_hash,enabled,must_change_password) VALUES(?,?,?,?,TRUE,FALSE)",n,n,"QC rollback actor","test-only");return jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?",Long.class,n);}
 void as(long org,long user){when(contexts.current()).thenReturn(new CurrentPlatformContext(org,user,Set.of("QC_TEST"),permissions,"qc-session","qc-"+suffix));}
 String key(){return UUID.randomUUID().toString();}
 List<QcCommands.Item> items(){return List.of(new QcCommands.Item("ASSAY","Assay",true,"NUMERIC","1","3",unit,null,"M1","1"),new QcCommands.Item("COLOR","Color",true,"TEXT",null,null,null,"白色粉末","M2","1"));}
 QcCommands.Create create(String code,String basisType,String reference,List<String> materials_,List<String> products,List<QcCommands.Item> items_){return new QcCommands.Create(code,"氯化钾入厂检验标准",basisType,reference,null,"依据药典方法制定",materials_,products,items_,"建立标准");}
 JsonNode standard(){return service.create(create("QS"+suffix,"CHINESE_PHARMACOPOEIA","《中国药典》2025年版四部 0301",List.of(material),List.of(),items()),key());}
 String id(JsonNode n){return n.path("id").asText();}
 String match(JsonNode n){return match(n.path("versionNo").asLong());}
 String match(long version){return "\""+version+"\"";}
 @Test void standardIsEditableInPlaceWithoutAnyConclusionEvidence(){
  var created=standard();
  assertThat(created.path("status").asText()).isEqualTo("ACTIVE");
  assertThat(created.path("basisType").asText()).isEqualTo("CHINESE_PHARMACOPOEIA");
  assertThat(created.path("basisReference").asText()).isEqualTo("《中国药典》2025年版四部 0301");
  assertThat(created.path("items")).hasSize(2);assertThat(created.path("scopes")).hasSize(1);
  assertThat(created.path("items").get(0).path("unitId").asText()).isEqualTo(unit);
  assertThat(created.path("revisions")).hasSize(1);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_signature WHERE object_type LIKE 'QcSpecification%' AND object_id=?",Long.class,id(created))).isZero();
  as(1,editor);var edited=service.edit(id(created),new QcCommands.Edit(created.path("versionNo").asLong(),"QS"+suffix,"氯化钾入厂检验标准","ENTERPRISE_INTERNAL","企业内控标准 Q/QC 001-2026",null,"收严含量下限","ACTIVE","修订依据"),match(created),key());
  assertThat(edited.path("basisType").asText()).isEqualTo("ENTERPRISE_INTERNAL");
  assertThat(edited.path("revisions")).hasSize(2);
  as(1,creator);var reread=service.get(id(created));
  assertThat(reread.path("basisReference").asText()).isEqualTo("企业内控标准 Q/QC 001-2026");
  assertThat(reread.path("items")).hasSize(2);
 }
 @Test void oneStandardCoversManyMaterialsAndManyProducts(){
  var created=service.create(create("QM"+suffix,"DRUG_REGISTRATION_STANDARD","药品注册标准 YBH00012025",List.of(material,otherMaterial),List.of(),items()),key());
  var scopes=created.path("scopes");
  assertThat(scopes).hasSize(2);
  assertThat(scopes.get(0).path("subjectType").asText()).isEqualTo("MATERIAL");
  assertThat(scopes.get(0).path("subjectCode").asText()).isEqualTo("QM"+suffix);
  assertThat(query.requireSelectable(1,Long.parseLong(material),Long.parseLong(id(created))).materials()).containsExactlyInAnyOrder(material,otherMaterial);
  var snapshot=query.requireSelectable(1,Long.parseLong(otherMaterial),Long.parseLong(id(created)));
  assertThat(snapshot.schemaVersion()).isEqualTo(QcSpecificationQueryService.SNAPSHOT_SCHEMA_VERSION);
  assertThat(snapshot.materials()).containsExactlyInAnyOrder(material,otherMaterial);
  assertThat(snapshot.products()).isEmpty();
  assertThat(snapshot.items()).hasSize(2);
  as(1,editor);var narrowed=service.replaceScope(id(created),new QcCommands.ReplaceScope(created.path("versionNo").asLong(),List.of(material),List.of(),"只保留一个物料"),match(created),key());
  assertThat(narrowed.path("scopes")).hasSize(1);
  assertThatThrownBy(()->query.requireSelectable(1,Long.parseLong(otherMaterial),Long.parseLong(id(created)))).hasMessageContaining("does not apply");
 }
 @Test void staleLockScopesRejectedAndUnknownFieldsFailClosed(){
  var created=standard();
  long stale=created.path("versionNo").asLong()+7;
  assertThatThrownBy(()->service.edit(id(created),new QcCommands.Edit(stale,"QS"+suffix,"氯化钾入厂检验标准","CHINESE_PHARMACOPOEIA","《中国药典》2025年版四部 0301",null,null,"ACTIVE","并发"),match(stale),key())).hasMessageContaining("changed");
  assertThatThrownBy(()->service.replaceScope(id(created),new QcCommands.ReplaceScope(created.path("versionNo").asLong(),List.of("999999999"),List.of(),"不存在物料"),match(created),key())).hasMessageContaining("not found");
  assertThatThrownBy(()->service.replaceItems(id(created),new QcCommands.ReplaceItems(created.path("versionNo").asLong(),List.of(new QcCommands.Item("ASSAY","含量",true,"NUMERIC","3","1",unit,null,"M1","1")),"倒置限值"),match(created),key())).hasMessageContaining("exceeds upper limit");
  as(2,creator);assertThatThrownBy(()->service.get(id(created))).hasMessageContaining("not found");
 }
 @Test void replacingItemsReusesCodesCaseInsensitivelyAndDropsRemovedOnes(){
  var created=standard();
  as(1,editor);var replaced=service.replaceItems(id(created),new QcCommands.ReplaceItems(created.path("versionNo").asLong(),List.of(new QcCommands.Item("assay","含量",true,"NUMERIC","0.99","1.01",unit,null,"M1","2")),"修订项目"),match(created),key());
  assertThat(replaced.path("items")).hasSize(1);
  assertThat(replaced.path("items").get(0).path("methodVersion").asText()).isEqualTo("2");
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qc_specification_item WHERE specification_id=?",Long.class,id(created))).isEqualTo(1);
 }
 @Test void deletionRemovesLibraryEntryAndKeepsRevisionTrail(){
  var created=standard();
  var deleted=service.delete(id(created),new QcCommands.Remove(created.path("versionNo").asLong(),"重复建立"),match(created),key());
  assertThat(deleted.path("deleted").asBoolean()).isTrue();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qc_specification WHERE id=?",Long.class,id(created))).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qc_specification_item WHERE specification_id=?",Long.class,id(created))).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qc_specification_revision WHERE specification_id=?",Long.class,id(created))).isEqualTo(2);
  as(1,editor);assertThatThrownBy(()->service.get(id(created))).hasMessageContaining("not found");
 }
 @Test void inactiveStandardStaysReadableButIsNoLongerSelectable(){
  var created=standard();
  as(1,editor);var inactive=service.edit(id(created),new QcCommands.Edit(created.path("versionNo").asLong(),"QS"+suffix,"氯化钾入厂检验标准","CHINESE_PHARMACOPOEIA","《中国药典》2025年版四部 0301",null,"标准暂停执行","INACTIVE","停用"),match(created),key());
  assertThat(inactive.path("status").asText()).isEqualTo("INACTIVE");
  assertThatThrownBy(()->query.requireSelectable(1,Long.parseLong(material),Long.parseLong(id(created)))).hasMessageContaining("inactive");
  assertThat(query.read(1,Long.parseLong(id(created))).specificationId()).isEqualTo(Long.parseLong(id(created)));
 }
}
