package com.hospital.mes.ebr.integration;

import com.fasterxml.jackson.databind.*;
import com.hospital.mes.ebr.application.*;
import com.hospital.mes.ebr.domain.EbrCommands.*;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.audit.signature.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.process.application.*;
import com.hospital.mes.process.domain.ProcessCommands;
import com.hospital.mes.common.exception.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/** Native DEV: unique test-owned fixtures; outer transaction rolls back every row. */
@SpringBootTest @ActiveProfiles("ci") @AutoConfigureMockMvc @Transactional
class EbrIT {
    @Autowired EbrService service;@Autowired EbrQueryService query;@Autowired ProcessService process;@Autowired ProcessQueryService processQuery;@Autowired UnitService units;@Autowired MaterialService materials;@Autowired JdbcTemplate jdbc;@Autowired ObjectMapper json;@Autowired MockMvc mvc;@Autowired PlatformTransactionManager transactions;
    @MockitoBean CurrentPlatformContextResolver contexts;@MockitoBean ReauthenticationPort reauthentication;@MockitoSpyBean AuditApplicationService audit;
    @MockitoSpyBean com.hospital.mes.ebr.infrastructure.EbrStore store;
    String suffix,unit,processId,operationId,role;long creator,approver;Set<String> permissions;
    @BeforeEach void setup(TestInfo info){
        suffix=UUID.randomUUID().toString().replace("-","").substring(0,12);role="EBR"+suffix;
        permissions=new HashSet<>(Set.of("ebr:sign","ebr:designer:edit"));for(String prefix:List.of("master:product:","master:uom:","master:material:","process:package:","ebr:template:"))for(String action:List.of("view","create","update","edit","submit","approve","publish"))permissions.add(prefix+action);
        if(info.getTestMethod().orElseThrow().getName().equals("familyLockRejectsStaleWriterAfterRealConcurrentTransaction"))return;
        creator=actor("ebrc"+suffix);approver=actor("ebra"+suffix);jdbc.update("INSERT INTO sys_role(role_code,display_name,enabled,version) VALUES(?,?,TRUE,0)",role,"eBR rollback role");
        as(1,creator);when(reauthentication.consume(anyString(),any())).thenReturn(new ConsumedReauthentication(Instant.now(),"PASSWORD"));
        unit=units.create(new UnitCommands.Create("EU"+suffix,"eBR unit","MASS",3),key()).path("id").asText();
        String material=materials.create(json.convertValue(Map.of("materialCode","EM"+suffix,"materialName","eBR material","materialType","RAW","baseUnitId",unit,"lotControlled",true),MaterialCommands.Create.class),key()).path("id").asText();
        var product=process.createProduct(new ProcessCommands.ProductCreate("EP"+suffix,"eBR product",null,null,unit),key());
        var pkg=process.createPackage(new ProcessCommands.PackageCreate(product.path("id").asText(),"EK"+suffix),key());processId=pkg.path("selectedVersion").path("id").asText();
        process.saveFormula(processId,new ProcessCommands.FormulaSave(0L,"Fixture BOM","EF"+suffix,"10",unit,List.of(new ProcessCommands.FormulaLine(1,material,"1",unit,"0",true))),null,key());
        process.saveRoute(processId,new ProcessCommands.RouteSave(1L,"Fixture route","ER"+suffix,List.of(new ProcessCommands.Operation("MIX","Mix",1,null,null,List.of(),null,false,List.of()))),null,key());
        process.transition(processId,"SUBMIT",new ProcessCommands.Transition(2L,"Fixture submit",null),null,key());as(1,approver);process.transition(processId,"APPROVE",new ProcessCommands.Transition(3L,"Fixture approve","test-token"),null,key());process.transition(processId,"PUBLISH",new ProcessCommands.Transition(4L,"Fixture publish",null),null,key());as(1,creator);
        operationId=processQuery.operations(1,Long.parseLong(processId)).getFirst().id();
    }
    long actor(String name){jdbc.update("INSERT INTO sys_user(login_name,login_name_normalized,display_name,password_hash,enabled,must_change_password) VALUES(?,?,?,?,TRUE,FALSE)",name,name,"eBR rollback actor","test-only");return jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?",Long.class,name);}
    void as(long org,long actor){when(contexts.current()).thenReturn(new CurrentPlatformContext(org,actor,Set.of(role),permissions,"ebr-test-session","ebr-"+suffix));}
    String key(){return UUID.randomUUID().toString();}
    Definition definition(){
        var field=new Field("WEIGHT","G","Weight","NUMBER","MANUAL","DECIMAL",unit,3,true,false,null,null,null,1,null,List.of());
        var rule=new Rule("LIMIT","F","WEIGHT","VALIDATION","ON_SUBMIT","value('WEIGHT') < 10","BLOCK","OUT_OF_RANGE","Outside range",true,true);
        return new Definition(List.of(new Section("S","Section",1,"NONE",null,false,List.of(new Group("G","Group",1,2,"LIST",1,5)))),List.of(new Form("F","Form",operationId,"1.0",1,List.of(field))),List.of(rule),List.of(new SignatureRule("FORM","F","APPROVE",role,true,1,true)),List.of(new ReviewRule("FORM","F","VERIFY",role,true,1)));
    }
    JsonNode draft(){var row=service.create(new Create(processId,"ET"+suffix),key());return service.save(row.path("id").asText(),new Save(0L,"Define template",definition()),null,key());}
    JsonNode command(JsonNode row,String action){return service.command(row.path("id").asText(),action,new Command(row.path("versionNo").asLong(),"Controlled "+action),null,key());}
    @Test void normalizedLifecyclePublishedSnapshotAndVersionCopyStayImmutable(){
        var row=draft();String id=row.path("id").asText();assertThat(row.path("operationChoices").get(0).path("id").asText()).isEqualTo(operationId);
        assertThat(command(row,"LINT").path("valid").asBoolean()).isTrue();var submitted=command(row,"SUBMIT");as(1,approver);var approved=command(submitted,"APPROVE");assertThat(approved.path("approvedBy").asText()).isEqualTo(Long.toString(approver));var effective=command(approved,"PUBLISH");
        var snapshot=query.requirePublished(1,Long.parseLong(id));assertThat(snapshot.path("definitionHash").asText()).hasSize(64);assertThat(snapshot.path("definitionHash")).isEqualTo(effective.path("contentHash"));
        assertThat(jdbc.queryForObject("SELECT schema_json FROM ebr_form_def WHERE template_version_id=?",String.class,id)).contains("\"schemaVersion\":\"1.0\"","\"rules\"");
        assertThatThrownBy(()->service.save(id,new Save(effective.path("versionNo").asLong(),"Forbidden edit",definition()),null,key())).hasMessageContaining("draft");
        var copied=service.version(id,new VersionCreate(effective.path("versionNo").asLong(),"New version",true),null,key());assertThat(copied.path("version").asInt()).isEqualTo(2);assertThat(copied.path("definition")).isEqualTo(row.path("definition"));assertThat(query.requirePublished(1,Long.parseLong(id))).isEqualTo(snapshot);assertThat(service.compare(id,copied.path("id").asText()).differences()).isEmpty();
        assertThatThrownBy(()->service.version(id,new VersionCreate(effective.path("versionNo").asLong(),"Duplicate draft",false),null,key())).isInstanceOf(ResourceConflictException.class);
    }
    @Test void nativeApiRejectsDuplicateFieldWithLocationAndPreservesDraft() throws Exception {
        var row=service.create(new Create(processId,"DUP"+suffix),key());
        var d=definition();var form=d.forms().getFirst();
        var duplicate=new Form(form.formCode(),form.formName(),form.operationDefId(),form.schemaVersion(),form.sequenceNo(),List.of(form.fields().getFirst(),form.fields().getFirst()));
        var request=new Save(0L,"Duplicate field validation",new Definition(d.sections(),List.of(duplicate),d.rules(),d.signatureRules(),d.reviewRules()));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/ebr/templates/"+row.path("id").asText())
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("designer").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ebr:template:update")))
            .header("If-Match","\"0\"").header("Idempotency-Key",key()).contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(request)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnprocessableEntity())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code").value("LINT_FAILED"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.message").value(org.hamcrest.Matchers.containsString("fields")));
        assertThat(service.get(row.path("id").asText()).path("definition").path("forms")).isEmpty();
        assertThat(jdbc.queryForObject("SELECT version_no FROM ebr_template_version WHERE id=?",Long.class,row.path("id").asText())).isZero();
    }
    @Test void replayStaleVersionsAndOmittedControlledRowsAreRejected(){
        var row=draft();String id=row.path("id").asText(),k=key();var request=new Save(1L,"Relabel",definition());var saved=service.save(id,request,null,k);assertThat(service.save(id,request,null,k)).isEqualTo(saved);
        assertThatThrownBy(()->service.save(id,new Save(1L,"Stale",definition()),null,key())).isInstanceOf(ResourceConflictException.class);
        assertThatThrownBy(()->service.save(id,new Save(2L,"Reused",definition()),null,k)).isInstanceOf(ResourceConflictException.class);
        var empty=new Definition(List.of(),List.of(),List.of(),List.of(),List.of());assertThatThrownBy(()->service.save(id,new Save(2L,"Omit",empty),null,key())).hasMessageContaining("retained");
        var d=definition();var field=d.forms().getFirst().fields().getFirst();
        var moved=new Field(field.fieldCode(),null,field.label(),field.fieldType(),field.sourceType(),field.dataType(),field.unitId(),field.precisionScale(),field.requiredFlag(),field.readonlyFlag(),field.defaultExpr(),field.placeholder(),field.helpText(),field.sequenceNo(),field.validationJson(),field.options());
        var movedDefinition=new Definition(d.sections(),List.of(new Form("F","Form",operationId,"1.0",1,List.of(moved))),d.rules(),d.signatureRules(),d.reviewRules());
        assertThatThrownBy(()->service.save(id,new Save(2L,"Move group",movedDefinition),null,key())).hasMessageContaining("ownership");
        var rule=d.rules().getFirst();var movedRule=new Rule(rule.ruleCode(),null,null,rule.ruleType(),rule.triggerPoint(),rule.expression(),rule.severity(),rule.errorCode(),rule.messageTemplate(),rule.deviationTrigger(),rule.activeFlag());
        assertThatThrownBy(()->service.save(id,new Save(2L,"Move rule",new Definition(d.sections(),d.forms(),List.of(movedRule),d.signatureRules(),d.reviewRules())),null,key())).hasMessageContaining("ownership");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ebr_field_def f JOIN ebr_form_def d ON d.id=f.form_def_id WHERE d.template_version_id=?",Long.class,id)).isEqualTo(1);
    }
    @Test void disabledRoleAndForeignProcessOperationFailLintAndPreserveDraft(){
        var row=draft();String id=row.path("id").asText();jdbc.update("UPDATE sys_role SET enabled=FALSE WHERE role_code=?",role);var lint=command(row,"LINT");assertThat(lint.path("valid").asBoolean()).isFalse();assertThat(lint.path("issues").toString()).contains("requiredRole");
        assertThatThrownBy(()->command(row,"SUBMIT")).isInstanceOf(ComplianceException.class);jdbc.update("UPDATE sys_role SET enabled=TRUE WHERE role_code=?",role);
        assertThatThrownBy(()->processQuery.requireOperation(1,Long.parseLong(processId),Long.MAX_VALUE)).isInstanceOf(NoSuchElementException.class);assertThatThrownBy(()->processQuery.operations(2,Long.parseLong(processId))).isInstanceOf(NoSuchElementException.class);
        var d=definition();var bad=new Definition(d.sections(),List.of(new Form("F","Form",Long.toString(Long.MAX_VALUE),"1.0",1,d.forms().getFirst().fields())),d.rules(),d.signatureRules(),d.reviewRules());assertThatThrownBy(()->service.save(id,new Save(1L,"Wrong operation",bad),null,key())).hasMessageContaining("operationDefId");
        assertThat(service.get(id).path("status").asText()).isEqualTo("DRAFT");
    }
    @Test void independentApprovalHashAndUnsafeExpressionsFailClosed(){
        var row=draft();var submitted=command(row,"SUBMIT");assertThatThrownBy(()->command(submitted,"APPROVE")).hasMessageContaining("Independent");as(1,approver);var approved=command(submitted,"APPROVE");
        jdbc.update("UPDATE ebr_field_def f JOIN ebr_form_def d ON d.id=f.form_def_id SET f.label='Tampered fixture' WHERE d.template_version_id=?",row.path("id").asText());assertThatThrownBy(()->command(approved,"PUBLISH")).hasMessageContaining("hash");
        as(1,creator);var newDraft=service.version(row.path("id").asText(),new VersionCreate(approved.path("versionNo").asLong(),"New empty",false),null,key());var d=definition();var malicious=new Rule("R",null,null,"VALIDATION","ON_SUBMIT","T(java.lang.Runtime)","BLOCK",null,null,false,true);var bad=new Definition(d.sections(),d.forms(),List.of(malicious),d.signatureRules(),d.reviewRules());assertThatThrownBy(()->service.save(newDraft.path("id").asText(),new Save(0L,"Unsafe",bad),null,key())).hasMessageContaining("DSL");
    }
    @Test void simulationProducesBlockEvidenceWithoutRuntimeRows(){
        var row=draft();String id=row.path("id").asText();var command=new Simulation(1L,"Boundary test","ON_SUBMIT",List.of(new Input("WEIGHT",List.of(json.valueToTree(20)))));String k=key();var result=service.simulate(id,command,null,k);assertThat(result.path("allowed").asBoolean()).isFalse();assertThat(result.path("results").get(0).path("inputSnapshot")).hasSize(1);assertThat(service.simulate(id,command,null,k)).isEqualTo(result);assertThat(service.get(id).path("versionNo").asLong()).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_audit_event WHERE object_type='EbrTemplateVersion' AND object_id=? AND action='EbrTemplate:SIMULATE'",Long.class,id)).isEqualTo(1);
    }
    @Test void apiPermissionsClosedDtoAndOrganizationIsolation() throws Exception {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_role_permission rp JOIN sys_role r ON r.id=rp.role_id JOIN sys_permission p ON p.id=rp.permission_id WHERE r.role_code='SYSTEM_ADMIN' AND p.permission_code IN ('ebr:template:view','ebr:template:create','ebr:template:update','ebr:template:submit','ebr:template:approve','ebr:template:publish','ebr:designer:edit') AND p.enabled=TRUE",Long.class)).isEqualTo(7);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_role_menu rm JOIN sys_role r ON r.id=rm.role_id JOIN sys_menu m ON m.id=rm.menu_id WHERE r.role_code='SYSTEM_ADMIN' AND m.route_path='/ebr/templates' AND m.status='ACTIVE'",Long.class)).isPositive();
        var row=draft();String id=row.path("id").asText();mvc.perform(get("/api/v1/ebr/templates/"+id).with(user("test").authorities(new SimpleGrantedAuthority("ebr:template:view")))).andExpect(status().isOk()).andExpect(jsonPath("$.data.templateCode").value("ET"+suffix));
        mvc.perform(get("/api/v1/ebr/templates/"+id).with(user("test").authorities(new SimpleGrantedAuthority("ebr:designer:edit")))).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/ebr/templates").with(user("test").authorities(new SimpleGrantedAuthority("ebr:template:create"))).header("Idempotency-Key",key()).contentType(MediaType.APPLICATION_JSON).content("{\"packageVersionId\":\""+processId+"\",\"templateCode\":\"unknown\",\"name\":\"Unfrozen\"}")).andExpect(status().isBadRequest());
        as(2,creator);assertThatThrownBy(()->service.get(id)).isInstanceOf(NoSuchElementException.class);assertThatThrownBy(()->query.requirePublished(2,Long.parseLong(id))).isInstanceOf(NoSuchElementException.class);
        as(1,creator);permissions.remove("ebr:template:update");as(1,creator);assertThatThrownBy(()->service.save(id,new Save(1L,"Forbidden",definition()),null,key())).isInstanceOf(PermissionException.class);
    }
    @Test void auditFailureRollsBackDefinitionVersionAndIdempotency(){
        var row=draft();String id=row.path("id").asText(),k=key();var tx=new TransactionTemplate(transactions);tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_NESTED);
        doAnswer(invocation->{var command=(com.hospital.mes.audit.domain.AuditCommand)invocation.getArgument(0);if(command.action().equals("EbrTemplate:SAVE"))throw new IllegalStateException("Injected eBR audit failure");return invocation.callRealMethod();}).when(audit).append(any());
        assertThatThrownBy(()->tx.execute(t->service.save(id,new Save(1L,"Rollback test",definition()),null,k))).hasMessageContaining("Injected eBR audit failure");assertThat(service.get(id).path("versionNo").asLong()).isEqualTo(1);assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,k)).isZero();
    }
    @Test void familyLockRejectsStaleWriterAfterRealConcurrentTransaction() throws Exception {
        var tx=new TransactionTemplate(transactions);tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        long[] ids=tx.execute(t->{
            long actor=actor("erace"+suffix);
            jdbc.update("INSERT INTO md_unit(org_id,created_by,updated_by,unit_code,unit_name,dimension,scale) VALUES(1,?,?,?,'eBR race','MASS',3)",actor,actor,"ERU"+suffix);
            long u=jdbc.queryForObject("SELECT id FROM md_unit WHERE unit_code=?",Long.class,"ERU"+suffix);
            jdbc.update("INSERT INTO md_product(org_id,created_by,updated_by,product_code,product_name,base_unit_id,status) VALUES(1,?,?,?,'eBR race',?,'ACTIVE')",actor,actor,"ERP"+suffix,u);
            long product=jdbc.queryForObject("SELECT id FROM md_product WHERE product_code=?",Long.class,"ERP"+suffix);
            jdbc.update("INSERT INTO proc_package(org_id,created_by,updated_by,product_id,package_code,status) VALUES(1,?,?,?,?,'ACTIVE')",actor,actor,product,"ERK"+suffix);
            long pkg=jdbc.queryForObject("SELECT id FROM proc_package WHERE package_code=?",Long.class,"ERK"+suffix);
            jdbc.update("INSERT INTO proc_package_version(org_id,created_by,updated_by,package_id,version,status) VALUES(1,?,?,?,1,'DRAFT')",actor,actor,pkg);
            long version=jdbc.queryForObject("SELECT id FROM proc_package_version WHERE package_id=?",Long.class,pkg);
            jdbc.update("INSERT INTO ebr_template_version(org_id,created_by,updated_by,package_version_id,template_code,version,status) VALUES(1,?,?,?,?,1,'DRAFT')",actor,actor,version,"ERT"+suffix);
            long template=jdbc.queryForObject("SELECT id FROM ebr_template_version WHERE template_code=?",Long.class,"ERT"+suffix);
            return new long[]{actor,u,product,pkg,version,template};
        });
        as(1,ids[0]);String k=key();var held=new java.util.concurrent.CountDownLatch(1);var waiting=new java.util.concurrent.CountDownLatch(1);var pool=java.util.concurrent.Executors.newFixedThreadPool(2);
        doAnswer(call->{waiting.countDown();return call.callRealMethod();}).when(store).lock(1,ids[5]);
        try{
            var writer=pool.submit(()->tx.execute(t->{jdbc.queryForList("SELECT id FROM ebr_template_version WHERE id=? FOR UPDATE",ids[5]);held.countDown();try{if(!waiting.await(5,java.util.concurrent.TimeUnit.SECONDS))throw new AssertionError("Second writer did not reach template lock");}catch(InterruptedException ex){throw new RuntimeException(ex);}jdbc.update("UPDATE ebr_template_version SET version_no=1 WHERE id=?",ids[5]);return null;}));
            assertThat(held.await(5,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            var stale=pool.submit(()->{try{tx.execute(t->{service.save(Long.toString(ids[5]),new Save(0L,"Concurrent draft edit",new Definition(List.of(),List.of(),List.of(),List.of(),List.of())),null,k);t.setRollbackOnly();return null;});return (RuntimeException)null;}catch(RuntimeException ex){return ex;}});
            writer.get(10,java.util.concurrent.TimeUnit.SECONDS);assertThat(stale.get(10,java.util.concurrent.TimeUnit.SECONDS)).isInstanceOf(ResourceConflictException.class);
            Long idempotencyCount=tx.execute(t->jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,k));assertThat(idempotencyCount).isZero();
            Long auditCount=tx.execute(t->jdbc.queryForObject("SELECT COUNT(*) FROM gxp_audit_event WHERE object_type='EbrTemplateVersion' AND object_id=?",Long.class,Long.toString(ids[5])));assertThat(auditCount).isZero();
        }finally{
            pool.shutdownNow();pool.awaitTermination(10,java.util.concurrent.TimeUnit.SECONDS);reset(store);
            tx.execute(t->{jdbc.update("DELETE FROM ebr_template_version WHERE id=? AND template_code=?",ids[5],"ERT"+suffix);jdbc.update("DELETE FROM proc_package_version WHERE id=? AND package_id=?",ids[4],ids[3]);jdbc.update("DELETE FROM proc_package WHERE id=? AND package_code=?",ids[3],"ERK"+suffix);jdbc.update("DELETE FROM md_product WHERE id=? AND product_code=?",ids[2],"ERP"+suffix);jdbc.update("DELETE FROM md_unit WHERE id=? AND unit_code=?",ids[1],"ERU"+suffix);jdbc.update("DELETE FROM sys_user WHERE id=? AND login_name_normalized=?",ids[0],"erace"+suffix);return null;});
        }
    }
}
