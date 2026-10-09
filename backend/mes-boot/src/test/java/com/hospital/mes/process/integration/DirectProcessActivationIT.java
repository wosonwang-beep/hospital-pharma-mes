package com.hospital.mes.process.integration;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import com.fasterxml.jackson.databind.*;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.audit.signature.*;
import com.hospital.mes.process.application.*;
import com.hospital.mes.process.domain.ProcessCommands.*;
import com.hospital.mes.masterdata.application.*;
import java.time.*;import java.util.*;
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
/** Native persistent DEV: every unique fixture and command is rolled back. Never modifies shared seeds. */
@SpringBootTest @ActiveProfiles("ci") @AutoConfigureMockMvc @Transactional
class DirectProcessActivationIT {
 @Autowired ProcessService service;@Autowired ProcessQueryService query;@Autowired UnitService units;@Autowired UnitConversionService conversions;@Autowired MaterialService materials;
 @Autowired JdbcTemplate jdbc;@Autowired ObjectMapper json;@Autowired MockMvc mvc;@Autowired PlatformTransactionManager transactions;@Autowired SignatureTransactionService signatures;
 @MockitoBean CurrentPlatformContextResolver contexts;@MockitoBean ReauthenticationPort reauth;@MockitoSpyBean AuditApplicationService audit;@MockitoSpyBean com.hospital.mes.process.infrastructure.ProcessStore store;
 String suffix,unit,material;long creator,approver;Set<String> permissions;CurrentPlatformContext context;
 @BeforeEach void setup(){suffix=UUID.randomUUID().toString().replace("-","").substring(0,12);permissions=new HashSet<>(Set.of("ebr:sign"));for(String p:List.of("master:product:","master:uom:","master:material:","process:package:"))for(String a:List.of("view","create","update","edit","submit","approve","publish"))permissions.add(p+a);creator=actor("proc_c"+suffix);approver=actor("proc_a"+suffix);as(1,creator);when(reauth.consume(anyString(),any())).thenReturn(new ConsumedReauthentication(Instant.now(),"PASSWORD"));unit=units.create(new UnitCommands.Create("PU"+suffix,"Process unit","MASS",3),key()).path("id").asText();var body=Map.of("materialCode","PM"+suffix,"materialName","Process material","materialType","RAW","baseUnitId",unit,"lotControlled",true);material=materials.create(json.convertValue(body,MaterialCommands.Create.class),key()).path("id").asText();}
 long actor(String name){jdbc.update("INSERT INTO sys_user(login_name,login_name_normalized,display_name,password_hash,enabled,must_change_password) VALUES(?,?,?,?,TRUE,FALSE)",name,name,"Process rollback actor","test-only");return jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?",Long.class,name);}
 void as(long org,long actor){context=new CurrentPlatformContext(org,actor,Set.of("PROCESS_TEST"),permissions,"session","proc-"+suffix);when(contexts.current()).thenReturn(context);}
 String key(){return UUID.randomUUID().toString();}
 JsonNode pkg(){var product=service.createProduct(new ProductCreate("PP"+suffix,"Product",null,null,unit),key());return service.createPackage(new PackageCreate(product.path("id").asText(),"PKG"+suffix),key());}
 FormulaSave formula(long version,String u){return new FormulaSave(version,"BOM evidence","F"+suffix,"10",unit,List.of(new FormulaLine(1,material,"2",u,"0",true)));}
 RouteSave route(long version,boolean cycle){var param=new Parameter("TEMP","Temperature","HYBRID",unit,"1","3");return new RouteSave(version,"Route evidence","R"+suffix,List.of(new Operation("A","Mix",1,null,new CompletionRule("PARAMETERS_WITHIN_LIMITS",null),cycle?List.of("B"):List.of(),null,true,List.of(param)),new Operation("B","Fill",2,null,null,List.of("A"),null,false,List.of())));}
 JsonNode save(JsonNode p,String qty,boolean cycle,String key){var f=new FormulaSave(null,null,"F"+suffix,"10",unit,List.of(new FormulaLine(1,material,qty,unit,"0",true)));return service.saveCurrentDefinition(p.path("id").asText(),new CurrentDefinitionSave(null,null,null,f,route(0,cycle)),null,key);}
 JsonNode current(JsonNode p){return query.requireCurrent(1,p.path("id").asLong());}
 void nested(Runnable work){var tx=new TransactionTemplate(transactions);tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_NESTED);tx.execute(t->{work.run();return null;});}
 @Test void creatorMaintainsCurrentDefinitionWithoutApprovalSignature(){var p=pkg();var saved=save(p,"2",false,key());assertThat(saved.path("currentDefinition").path("status").asText()).isEqualTo("EFFECTIVE");assertThat(current(p).has("businessVersion")).isFalse();verifyNoInteractions(reauth);verify(audit,never()).append(any());}
 @Test void cyclesAndMissingDefinitionsCannotBecomeUsable(){var p=pkg();assertThatThrownBy(()->current(p)).hasMessageContaining("incomplete");assertThatThrownBy(()->nested(()->save(p,"2",true,key()))).hasMessageContaining("cycle");assertThat(service.getPackage(p.path("id").asText(),null).path("currentDefinition").path("status").asText()).isEqualTo("DRAFT");}
 @Test void retiredCommandsCannotReintroduceProcessRevisionControl(){var p=pkg();save(p,"2",false,key());for(String action:List.of("SUBMIT","APPROVE","PUBLISH"))assertThatThrownBy(()->service.transition(p.path("id").asText(),action,new Transition(null,null,null),null,key())).isInstanceOf(UnsupportedOperationException.class);assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM proc_package_version WHERE package_id=?",Long.class,p.path("id").asLong())).isZero();}
 @Test void maintainCurrentIsOneAtomicSaveWithoutOperatorVersionWorkflow(){var p=pkg();save(p,"2",false,key());var original=current(p).deepCopy();String k=key();var updated=save(p,"3",false,k);assertThat(save(p,"3",false,k)).isEqualTo(updated);assertThat(updated.path("currentDefinition").path("id")).isEqualTo(service.getPackage(p.path("id").asText(),null).path("currentDefinition").path("id"));assertThat(original.path("formula").path("items").get(0).path("requiredQty").asText()).isEqualTo("2");verifyNoInteractions(reauth);}
 @Test void failedCurrentMaintenanceLeavesNoHalfSavedConfiguration(){var p=pkg();save(p,"2",false,key());var old=current(p).deepCopy();String k=key();assertThatThrownBy(()->nested(()->save(p,"3",true,k))).hasMessageContaining("cycle");assertThat(current(p)).isEqualTo(old);assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,k)).isZero();}
}
