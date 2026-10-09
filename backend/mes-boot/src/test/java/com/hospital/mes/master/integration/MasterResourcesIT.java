package com.hospital.mes.master.integration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.equipment.application.*;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
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

@SpringBootTest @ActiveProfiles("ci") @AutoConfigureMockMvc @Transactional
class MasterResourcesIT {
    @Autowired OrganizationService orgs;
    @Autowired UnitService units;
    @Autowired UnitConversionService conversions;
    @Autowired QualificationService qualifications;
    @Autowired EquipmentService equipment;
    @Autowired MasterQueryService masterQueries;
    @Autowired EquipmentQueryService equipmentQueries;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    @Autowired PlatformTransactionManager transactions;
    @MockitoBean CurrentPlatformContextResolver contexts;
    @MockitoSpyBean AuditApplicationService audit;
    @MockitoSpyBean com.hospital.mes.masterdata.infrastructure.UnitStore unitStore;
    final Set<String> permissions = new HashSet<>();
    CurrentPlatformContext context;
    String suffix;
    long actor;

    @BeforeEach void setup() {
        suffix=UUID.randomUUID().toString().replace("-","").substring(0,16);
        actor=newActor("m003_"+suffix);
        for(String type:List.of("org","uom","equipment","qualification"))for(String action:List.of("view","create","update"))permissions.add("master:"+type+":"+action);
        context=new CurrentPlatformContext(1,actor,Set.of("TEST_MASTER"),permissions,"test-session","m003-"+suffix);
        when(contexts.current()).thenReturn(context);
    }
    long newActor(String name) {
        jdbc.update("INSERT INTO sys_user(login_name,login_name_normalized,display_name,password_hash,enabled,must_change_password) VALUES(?,?,?,?,TRUE,FALSE)",name,name,"Unique rollback test actor","not-an-authentication-secret");
        return jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?",Long.class,name);
    }
    String key(){return UUID.randomUUID().toString();}
    JsonNode unit(String code,String dimension) { return units.create(new UnitCommands.Create(code+suffix,code,dimension,3),key()); }
    JsonNode eq(){return equipment.create(new EquipmentCommands.Create("EQ"+suffix,"Test mixer","MIXER",LocalDate.parse("2026-10-02"),"Test location"),key());}

    @Test void tcMd002HierarchyDisableAndHistoricalReferences() {
        var enterprise=orgs.create(new OrganizationCommands.Create(null,"E"+suffix,"Enterprise","ENTERPRISE"),key());
        String id=enterprise.get("id").asText();
        var factory=orgs.create(new OrganizationCommands.Create(id,"F"+suffix,"Factory","FACTORY"),key());
        assertThatThrownBy(()->orgs.create(new OrganizationCommands.Create(id,"L"+suffix,"Line","LINE"),key())).hasMessageContaining("ORG_HIERARCHY_INVALID");
        assertThatThrownBy(()->orgs.command(id,new OrganizationCommands.Update("DISABLE","test",0L,null,null),null,key())).hasMessageContaining("ORG_ACTIVE_CHILDREN");
        orgs.command(factory.get("id").asText(),new OrganizationCommands.Update("DISABLE","test",0L,id,null),null,key());
        orgs.command(id,new OrganizationCommands.Update("DISABLE","test",0L,null,null),null,key());
        assertThat(orgs.get(id).get("status").asText()).isEqualTo("INACTIVE");
        assertThat(orgs.get(factory.get("id").asText()).get("parentId").asText()).isEqualTo(id);
    }
    @Test void tcMd001ConversionRoundTripEvidenceAndDimensionFailure() {
        var kg=unit("KG","MASS");var g=unit("G","MASS");var l=unit("L","VOLUME");
        assertThatThrownBy(()->masterQueries.convert(1,kg.get("id").asLong(),l.get("id").asLong(),null,BigDecimal.ONE)).hasMessageContaining("UNIT_DIMENSION_MISMATCH");
        var request=new UnitConversionCommands.Create(kg.get("id").asText(),g.get("id").asText(),"1000",null);
        conversions.create(request,key());
        assertThatThrownBy(()->units.command(g.get("id").asText(),new UnitCommands.Update("UPDATE","test dimension change",0L,"G","VOLUME",3),null,key())).hasMessageContaining("UNIT_DIMENSION_IN_USE");
        var result=masterQueries.convert(1,kg.get("id").asLong(),g.get("id").asLong(),null,new BigDecimal("1.005"));
        assertThat(result.originalValue()).isEqualTo("1.005");assertThat(result.convertedValue()).isEqualTo("1005.000");
        assertThatThrownBy(()->conversions.create(request,key())).hasMessageContaining("already exists");
    }
    @Test void tcEqp001ExpiredGateThenControlledRecalibration() {
        var e=eq();String id=e.get("id").asText();
        assertThatThrownBy(()->equipmentQueries.requireUsable(1,e.get("id").asLong(),Instant.parse("2026-10-03T00:00:00Z"))).hasMessageContaining("EQUIPMENT_CALIBRATION_EXPIRED");
        equipment.command(id,new EquipmentCommands.Update("UPDATE","calibration",0L,"Test mixer","MIXER",LocalDate.parse("2026-10-03"),"Test location"),null,key());
        assertThat(equipmentQueries.requireUsable(1,e.get("id").asLong(),Instant.parse("2026-10-03T23:59:59Z")).id()).isEqualTo(id);
        equipment.command(id,new EquipmentCommands.Update("BEGIN_MAINTENANCE","maintenance",1L,null,null,null,null),null,key());
        assertThatThrownBy(()->equipmentQueries.requireUsable(1,e.get("id").asLong(),Instant.now())).hasMessageContaining("EQUIPMENT_UNAVAILABLE");
    }
    @Test void tcQual001MissingExpiredInclusiveAndWrongOrganization() {
        var at=Instant.parse("2026-10-03T23:59:59Z");
        assertThatThrownBy(()->masterQueries.requireQualification(1,actor,"WEIGH",at)).hasMessageContaining("QUALIFICATION_REQUIRED");
        qualifications.create(new QualificationCommands.Create(Long.toString(actor),"WEIGH",LocalDate.parse("2026-10-03"),LocalDate.parse("2026-10-03")),key());
        masterQueries.requireQualification(1,actor,"WEIGH",at);
        assertThatThrownBy(()->masterQueries.requireQualification(2,actor,"WEIGH",at)).hasMessageContaining("QUALIFICATION_REQUIRED");
        assertThatThrownBy(()->masterQueries.requireQualification(1,actor,"WEIGH",Instant.parse("2026-10-04T00:00:00Z"))).hasMessageContaining("QUALIFICATION_REQUIRED");
    }
    @Test void replayDoesNotDuplicateAuditAndChangedPayloadConflicts() {
        String key=key();var r=new UnitCommands.Create("R"+suffix,"Replay","MASS",3);
        var a=units.create(r,key);var b=units.create(r,key);
        assertThat(a).isEqualTo(b);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_audit_event WHERE actor_id=? AND action='Unit:CREATE'",Long.class,actor)).isZero();
        assertThatThrownBy(()->units.create(new UnitCommands.Create("S"+suffix,"Other","MASS",3),key)).hasMessageContaining("Key reused");
    }
    @Test void legacyVersionIsIgnoredAndMatchingReplayStillWorks() {
        var e=eq();String id=e.path("id").asText(),key=key();var cmd=new EquipmentCommands.Update("UPDATE",null,999L,"Changed","MIXER",LocalDate.parse("2027-10-03"),"Test location");
        var a=equipment.command(id,cmd,null,key);assertThat(a.has("versionNo")).isFalse();
        assertThat(equipment.command(id,cmd,null,key)).isEqualTo(a);
        assertThat(equipment.command(id,cmd,"0",key()).path("equipmentName").asText()).isEqualTo("Changed");
        assertThat(jdbc.queryForObject("SELECT version_no FROM md_equipment WHERE id=?",Long.class,id)).isZero();
    }
    @Test void crossOrganizationReadsWritesAndReferencesAreHidden() {
        var u=unit("ISO","MASS");
        when(contexts.current()).thenReturn(new CurrentPlatformContext(2,actor,Set.of("TEST_MASTER"),permissions,"s","r"));
        assertThatThrownBy(()->units.get(u.get("id").asText())).isInstanceOf(NoSuchElementException.class);
        assertThat(units.list(0,20,suffix,Map.of()).items()).isEmpty();
        assertThatThrownBy(()->conversions.create(new UnitConversionCommands.Create(u.get("id").asText(),"999999999","1",null),key())).isInstanceOf(NoSuchElementException.class);
    }
    @Test void unauthorizedCallerCannotReadOrReplay() {
        String key=key();var r=new UnitCommands.Create("AUTH"+suffix,"Auth","MASS",3);units.create(r,key);
        when(contexts.current()).thenReturn(new CurrentPlatformContext(1,actor,Set.of("READER"),Set.of(),"s","r"));
        assertThatThrownBy(()->units.create(r,key)).hasMessageContaining("Permission required");
        assertThatThrownBy(()->units.list(0,20,null,Map.of())).hasMessageContaining("Permission required");
    }
    @Test void apiRejectsUnknownStatusActorAndMissingWriteHeaders() throws Exception {
        var user=user("test").authorities(permissions.stream().map(SimpleGrantedAuthority::new).toList());
        String body="{\"equipmentCode\":\"E\",\"equipmentName\":\"Mixer\",\"equipmentType\":\"MIXER\",\"status\":\"ACTIVE\"}";
        mvc.perform(post("/api/v1/equipment").with(user).header("Idempotency-Key",key()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/equipment").with(user).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/equipment").with(user("reader").authorities(new SimpleGrantedAuthority("master:uom:view")))).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/equipment").with(user)).andExpect(status().isOk()).andExpect(jsonPath("$.data.items").isArray());
    }
    @Test void invalidForeignKeysAndDuplicateCodesNeverBecomeValidBusinessRecords() {
        unit("DUP","MASS");assertThatThrownBy(()->unit("DUP","MASS")).isInstanceOf(org.springframework.dao.DuplicateKeyException.class);
        assertThatThrownBy(()->qualifications.create(new QualificationCommands.Create("999999999","TEST",null,null),key())).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
    @Test void auditAvailabilityDoesNotControlBasicMaintenance() {
        doThrow(new IllegalStateException("injected audit failure")).when(audit).append(any());
        String k=key();var row=units.create(new UnitCommands.Create("NOAUDIT"+suffix,"Ordinary unit","MASS",3),k);
        assertThat(row.path("id").asText()).isNotBlank();assertThat(units.create(new UnitCommands.Create("NOAUDIT"+suffix,"Ordinary unit","MASS",3),k)).isEqualTo(row);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_audit_event WHERE actor_id=?",Long.class,actor)).isZero();
        verify(audit,never()).append(any());
    }

    @Test void realConcurrentTransactionsUseCurrentDimensionAndPair() throws Exception {
        // Committed raw fixture rows have no production/audit evidence. Every business call below
        // fails and rolls back; cleanup targets exactly these fixture IDs, never shared data.
        var tx=new TransactionTemplate(transactions);tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        long[] fixture=tx.execute(s->{long user=newActor("race_"+suffix);long[] ids=new long[3];ids[0]=user;
            for(int i=1;i<=2;i++){String code="RACE"+i+suffix;jdbc.update("INSERT INTO md_unit(org_id,created_by,updated_by,unit_code,unit_name,dimension,scale) VALUES(1,?,?,?,?,'MASS',3)",user,user,code,"Race fixture");ids[i]=jdbc.queryForObject("SELECT id FROM md_unit WHERE org_id=1 AND unit_code=?",Long.class,code);}return ids;});
        var raceContext=new CurrentPlatformContext(1,fixture[0],Set.of("TEST_MASTER"),permissions,"s","race-"+suffix);
        when(contexts.current()).thenReturn(raceContext);
        var writer=java.util.concurrent.Executors.newSingleThreadExecutor(r->new Thread(r,"master-writer"));
        var reader=java.util.concurrent.Executors.newSingleThreadExecutor(r->new Thread(r,"master-reader"));
        try {
            for(String scenario:List.of("DIMENSION","PAIR")) {
                tx.execute(s->{jdbc.update("DELETE FROM md_unit_conversion WHERE org_id=1 AND from_unit_id=? AND to_unit_id=?",fixture[1],fixture[2]);jdbc.update("UPDATE md_unit SET dimension='MASS',version_no=0 WHERE id IN (?,?)",fixture[1],fixture[2]);return null;});
                var held=new java.util.concurrent.CountDownLatch(1);var readerAtLock=new java.util.concurrent.CountDownLatch(1);var first=new java.util.concurrent.atomic.AtomicBoolean(true);
                doAnswer(call->{if(Thread.currentThread().getName().equals("master-reader")&&first.compareAndSet(true,false))readerAtLock.countDown();return call.callRealMethod();}).when(unitStore).lock(anyLong(),anyLong());
                var write=writer.submit(()->tx.execute(s->{jdbc.queryForList("SELECT id FROM md_unit WHERE id IN (?,?) ORDER BY id FOR UPDATE",fixture[1],fixture[2]);held.countDown();
                    try{if(!readerAtLock.await(5,java.util.concurrent.TimeUnit.SECONDS))throw new AssertionError("Reader did not reach lock");}catch(InterruptedException ex){throw new RuntimeException(ex);}
                    if(scenario.equals("DIMENSION"))jdbc.update("UPDATE md_unit SET dimension='VOLUME' WHERE id=?",fixture[2]);
                    if(scenario.equals("PAIR"))jdbc.update("INSERT INTO md_unit_conversion(org_id,created_by,updated_by,from_unit_id,to_unit_id,factor) VALUES(1,?,?,?,?,1)",fixture[0],fixture[0],fixture[1],fixture[2]);
                    if(scenario.equals("VERSION"))jdbc.update("UPDATE md_unit SET version_no=1 WHERE id=?",fixture[1]);return null;}));
                assertThat(held.await(5,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
                var read=reader.submit(()->{try {tx.execute(s->{if(scenario.equals("VERSION"))return units.command(Long.toString(fixture[1]),new UnitCommands.Update("UPDATE","race update",0L,"Changed","MASS",3),null,key());return conversions.create(new UnitConversionCommands.Create(Long.toString(fixture[1]),Long.toString(fixture[2]),"1",null),key());});return null;}catch(RuntimeException ex){return ex;}});
                write.get(10,java.util.concurrent.TimeUnit.SECONDS);var failure=read.get(10,java.util.concurrent.TimeUnit.SECONDS);
                assertThat(failure).isNotNull();
                String expected=scenario.equals("DIMENSION")?"UNIT_DIMENSION_MISMATCH":scenario.equals("PAIR")?"Conversion already exists":"Record changed; reload";
                if(failure instanceof com.hospital.mes.common.exception.ResourceConflictException conflict && conflict.code().equals("CONCURRENT_MODIFICATION")) {
                    // MariaDB may reject a locking read whose old RR snapshot predates the writer.
                    // This is a controlled 409, and a new transaction must observe the latest truth.
                    assertThatThrownBy(()->tx.execute(s->{if(scenario.equals("VERSION"))return units.command(Long.toString(fixture[1]),new UnitCommands.Update("UPDATE","race retry",0L,"Changed","MASS",3),null,key());return conversions.create(new UnitConversionCommands.Create(Long.toString(fixture[1]),Long.toString(fixture[2]),"1",null),key());})).hasMessageContaining(expected);
                } else {StringBuilder diagnostic=new StringBuilder();for(Throwable cause=failure;cause!=null;cause=cause.getCause()){diagnostic.append(cause.getClass().getName()).append(" ");if(cause instanceof java.sql.SQLException sql)diagnostic.append(sql.getErrorCode()).append("/").append(sql.getSQLState()).append(" ");}assertThat(failure.getMessage()).as(diagnostic.toString()).contains(expected);}
            }
            Long auditCount=tx.execute(s->jdbc.queryForObject("SELECT COUNT(*) FROM gxp_audit_event WHERE actor_id=?",Long.class,fixture[0]));assertThat(auditCount).isZero();
        } finally {
            writer.shutdownNow();reader.shutdownNow();reset(unitStore);
            tx.execute(s->{jdbc.update("DELETE FROM md_unit_conversion WHERE org_id=1 AND from_unit_id=? AND to_unit_id=?",fixture[1],fixture[2]);jdbc.update("DELETE FROM md_unit WHERE id IN (?,?) AND unit_name='Race fixture'",fixture[1],fixture[2]);jdbc.update("DELETE FROM sys_user WHERE id=? AND login_name_normalized=?",fixture[0],"race_"+suffix);return null;});
        }
    }
}
