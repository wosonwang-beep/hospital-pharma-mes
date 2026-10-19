package com.hospital.mes.demo;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.audit.signature.ConsumedReauthentication;
import com.hospital.mes.audit.signature.ReauthenticationPort;
import com.hospital.mes.ebr.application.EbrRuntimeService;
import com.hospital.mes.ebr.application.EbrService;
import com.hospital.mes.ebr.domain.EbrCommands;
import com.hospital.mes.ebr.domain.EbrRuntimeCommands;
import com.hospital.mes.equipment.application.EquipmentCommands;
import com.hospital.mes.equipment.application.EquipmentService;
import com.hospital.mes.execution.application.ExecutionService;
import com.hospital.mes.execution.application.ProductionQuantityService;
import com.hospital.mes.execution.application.WeighChargeService;
import com.hospital.mes.masterdata.application.MaterialCommands;
import com.hospital.mes.masterdata.application.MaterialService;
import com.hospital.mes.masterdata.application.MaterialSupplierService;
import com.hospital.mes.masterdata.application.SupplierCommands;
import com.hospital.mes.masterdata.application.SupplierService;
import com.hospital.mes.process.application.ProcessQueryService;
import com.hospital.mes.process.application.ProcessService;
import com.hospital.mes.process.domain.ProcessCommands;
import com.hospital.mes.production.application.ProductionQueryService;
import com.hospital.mes.production.application.ProductionService;
import com.hospital.mes.qc.application.QcSpecificationService;
import com.hospital.mes.qc.domain.QcCommands;
import com.hospital.mes.qms.application.FinishedInspectionService;
import com.hospital.mes.qms.application.IncomingQualityService;
import com.hospital.mes.qms.application.MaterialBalanceService;
import com.hospital.mes.qms.application.ProductionQualityPlanService;
import com.hospital.mes.qms.application.ProductionQualityService;
import com.hospital.mes.release.application.FinishedReleaseService;
import com.hospital.mes.security.password.PasswordService;
import com.hospital.mes.wms.application.FinishedGoodsService;
import com.hospital.mes.wms.application.WmsProductionService;
import com.hospital.mes.wms.application.WmsQueryService;
import com.hospital.mes.wms.application.WmsService;
import com.hospital.mes.wms.domain.WmsCommands.ReceiptCreate;
import com.hospital.mes.wms.domain.WmsCommands.ReceiptItemInput;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.assertj.core.api.Assertions.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
    "mes.qualification.required-codes.sample-execute=GMP_OPERATION_AUTHORIZATION",
    "mes.qualification.required-codes.test-execute=GMP_OPERATION_AUTHORIZATION",
    "mes.qualification.required-codes.test-review=GMP_OPERATION_AUTHORIZATION",
    "mes.qualification.required-codes.qa-release=GMP_OPERATION_AUTHORIZATION",
    "mes.qualification.required-codes.operation-start=GMP_OPERATION_AUTHORIZATION",
    "mes.qualification.required-codes.operation-resume=GMP_OPERATION_AUTHORIZATION",
    "mes.qualification.required-codes.weigh-create=GMP_OPERATION_AUTHORIZATION",
    "mes.qualification.required-codes.weigh-verify=GMP_OPERATION_AUTHORIZATION",
    "mes.qualification.required-codes.charge-create=GMP_OPERATION_AUTHORIZATION",
    "mes.qualification.required-codes.charge-reverse=GMP_OPERATION_AUTHORIZATION",
    "mes.weighing.scale-equipment-types=ELECTRONIC_SCALE"
})
@ActiveProfiles("local")
@Transactional
public class RealBusinessScenarioSeedIT {
    @DynamicPropertySource
    static void isolatedOnly(DynamicPropertyRegistry registry) { /* use local DEV datasource from explicit SPRING_CONFIG_IMPORT */ }
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    @Autowired PasswordService passwords;
    @Autowired ConfigurableEnvironment environment;

    @Autowired SupplierService suppliers;
    @Autowired MaterialService materials;
    @Autowired MaterialSupplierService materialSuppliers;
    @Autowired WmsService wms;
    @Autowired IncomingQualityService incoming;
    @Autowired QcSpecificationService qc;

    @Autowired ProcessService process;
    @Autowired ProcessQueryService processQuery;
    @Autowired EbrService ebr;
    @Autowired EbrRuntimeService runtime;
    @Autowired ProductionService production;
    @Autowired ProductionQueryService productionQuery;
    @Autowired ProductionQualityPlanService plans;
    @Autowired WmsProductionService stock;
    @Autowired WmsQueryService stockQuery;
    @Autowired ExecutionService execution;
    @Autowired EquipmentService equipment;
    @Autowired WeighChargeService weighing;
    @Autowired ProductionQuantityService quantities;
    @Autowired MaterialBalanceService balances;
    @Autowired ProductionQualityService productionQuality;

    @Autowired FinishedGoodsService finishedGoods;
    @Autowired FinishedInspectionService finishedInspection;
    @Autowired FinishedReleaseService finishedQa;
    @Autowired com.hospital.mes.release.application.EbrArchiveService archive;
    @Autowired com.hospital.mes.configuration.EbrFormPrintProvider formPrint;
    @Autowired com.hospital.mes.qc.application.QcSpecificationQueryService specificationHistory;

    @MockitoBean CurrentPlatformContextResolver contexts;
    @MockitoBean ReauthenticationPort reauth;

    Set<String> permissions;
    long productionActor, warehouseActor, samplerActor, analystActor, reviewerActor, qaActor;
    String tag, packageVersionId, templateVersionId, finishedSpecVersionId;
    String kclLot, waterLot;
    String KCL,WATER,G,ML,PERCENT,PH,PRODUCT,FG_LOCATION;
    private String material(String code){return jdbc.queryForObject("SELECT id FROM md_material WHERE org_id=1 AND material_code=?",String.class,code);}
    private String unit(String code){return jdbc.queryForObject("SELECT id FROM md_unit WHERE org_id=1 AND unit_code=?",String.class,code);}
    private void resolveReferences(){
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM md_material WHERE material_type='FINISHED'",Long.class)).isZero();
        KCL=material("YL-0001");WATER=material("FL-0004");G=unit("g");ML=unit("ml");PERCENT=unit("PERCENT");PH=unit("PH");
        PRODUCT=jdbc.queryForObject("SELECT id FROM md_product WHERE org_id=1 AND product_code='KCL30'",String.class);
        FG_LOCATION=jdbc.queryForObject("SELECT id FROM wms_location WHERE org_id=1 AND location_code='FG-QC'",String.class);
    }

    @Autowired org.springframework.transaction.PlatformTransactionManager transactionManager;

    @Test
    @Transactional(propagation=org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    protected void competingInstructionCommandsWaitForRollbackAndLeaveNoData() throws Exception {
        // No fixture commit: each worker owns one REQUIRED transaction and always rolls it back.
        resolveReferences();tag="CONCUR_"+UUID.randomUUID().toString().replace("-","");
        permissions=new HashSet<>(jdbc.queryForList("SELECT permission_code FROM sys_permission WHERE enabled=TRUE",String.class));
        permissions.addAll(Set.of("production:batch:create","production:batch:view"));
        productionActor=jdbc.queryForObject("SELECT created_by FROM md_product WHERE id=?",Long.class,PRODUCT);as(productionActor,"PRODUCTION");
        var tx=new org.springframework.transaction.support.TransactionTemplate(transactionManager);tx.setTimeout(15);
        var command=body("batchNo",tag,"productId",PRODUCT,"plannedQty","1000","unitId",ML,"reason","Competing isolated rollback-only instructions");String sharedKey=key();
        var firstReady=new java.util.concurrent.CountDownLatch(1);var secondStarted=new java.util.concurrent.CountDownLatch(1);var secondDone=new java.util.concurrent.CountDownLatch(1);var release=new java.util.concurrent.CountDownLatch(1);
        try(var workers=java.util.concurrent.Executors.newFixedThreadPool(2)){
            var first=workers.submit(()->tx.execute(status->{try{var result=production.createInstruction(command.deepCopy(),sharedKey);firstReady.countDown();if(!release.await(10,java.util.concurrent.TimeUnit.SECONDS))throw new IllegalStateException("Release timeout");return result;}catch(InterruptedException ex){Thread.currentThread().interrupt();throw new IllegalStateException(ex);}finally{status.setRollbackOnly();}}));
            assertThat(firstReady.await(8,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            var second=workers.submit(()->tx.execute(status->{try{secondStarted.countDown();return production.createInstruction(command.deepCopy(),sharedKey);}finally{status.setRollbackOnly();secondDone.countDown();}}));
            try{assertThat(secondStarted.await(5,java.util.concurrent.TimeUnit.SECONDS)).isTrue();assertThat(secondDone.await(400,java.util.concurrent.TimeUnit.MILLISECONDS)).isFalse();}
            finally{release.countDown();}
            assertThat(first.get(10,java.util.concurrent.TimeUnit.SECONDS).path("productionSchemaVersion").asInt()).isEqualTo(2);
            assertThat(second.get(10,java.util.concurrent.TimeUnit.SECONDS).path("productionSchemaVersion").asInt()).isEqualTo(2);
        }finally{release.countDown();}
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM prd_main_batch WHERE batch_no=?",Long.class,tag)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM md_material WHERE material_type='FINISHED'",Long.class)).isZero();
    }

    @Test
    protected void productStandardCannotEnterMaterialHistoricalReader() {
        // An independent Spring test transaction owns its own fixture and rolls back at method end.
        tag="NEG_"+UUID.randomUUID().toString().replace("-","");
        resolveReferences();initActors();finishedSpecVersionId=createFinishedSpec();
        assertThatThrownBy(()->specificationHistory.getHistorical(1,Long.parseLong(finishedSpecVersionId)))
            .isInstanceOf(com.hospital.mes.common.exception.ResourceConflictException.class)
            .hasMessageContaining("material reader");
        // Do not run positive QA/business commands after the expected rollback-only boundary.
    }

    @Test
    @Commit
    protected void seedProductionAndFinishedChain() {
        BusinessSeedPayloadGuard.check(java.util.Map.of("qualificationCode","GMP_OPERATION_AUTHORIZATION","equipmentType","ELECTRONIC_SCALE","templateName","氯化钾溶液配制电子批记录","batchPrefix","KCL"));
        tag = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        resolveReferences();
        initActors();
        installWeighingPolicies();

        kclLot = jdbc.queryForObject("""
            SELECT id FROM md_material_lot
            WHERE material_id=? AND quality_status='RELEASED' AND inventory_status='AVAILABLE'
            ORDER BY id DESC LIMIT 1
            """, String.class,KCL);
        waterLot = ensureReleasedPurifiedWater();

        prepareProcessAndEbr();
        finishedSpecVersionId = createFinishedSpec();

        JsonNode draft = createBatch("KCL-"+tag+"-D", "PO-KCL-"+tag+"-D", "1000");
        createApprovedPlan(draft, false);

        JsonNode running = createBatch("KCL-"+tag+"-R", "PO-KCL-"+tag+"-R", "1000");
        createApprovedPlan(running, true);
        running = releaseAndStart(running, "ISS-KCL-"+tag+"-R");

        JsonNode complete = createBatch("KCL-"+tag+"-F", "PO-KCL-"+tag+"-F", "1000");
        createApprovedPlan(complete, true);
        complete = releaseAndStart(complete, "ISS-KCL-"+tag+"-F");
        complete = executeAndCompleteProduction(complete);

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_finished_inventory_ledger WHERE main_batch_id=?",Long.class,id(complete))).isZero();
        assertThat(specificationHistory.getProductHistorical(1,Long.parseLong(PRODUCT),Long.parseLong(finishedSpecVersionId)).productId()).isEqualTo(Long.parseLong(PRODUCT));
        JsonNode inbound = confirmFinishedInbound(complete);
        JsonNode finishedReport = completeFinishedInspection(inbound);
        JsonNode pendingQa = submitFinishedQa(complete);
        JsonNode release = releaseFinished(pendingQa);
        JsonNode shipment = shipFinished(pendingQa, inbound);

        assertThat(shipment.path("status").asText()).isEqualTo("CONFIRMED");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM md_material WHERE material_type='FINISHED'",Long.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_finished_inventory_ledger WHERE main_batch_id=?",Long.class,id(pendingQa))).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT SUM(delta_qty) FROM wms_finished_inventory_ledger WHERE main_batch_id=?",java.math.BigDecimal.class,id(pendingQa))).isEqualByComparingTo("695");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_finished_inbound_request WHERE main_batch_id=? AND material_lot_id IS NOT NULL",Long.class,id(pendingQa))).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM prd_main_batch WHERE id=? AND production_schema_version=2 AND finished_lot_id IS NULL AND finished_quality_status='RELEASED'",Long.class,id(pendingQa))).isEqualTo(1);
        JsonNode archiveFacts=archive.sourceEvidence(1,Long.parseLong(id(pendingQa)));
        assertThat(archiveFacts.path("finishedInventory").path("mainBatchId").asText()).isEqualTo(id(pendingQa));
        assertThat(archiveFacts.path("finishedInventory").path("productId").asText()).isEqualTo(PRODUCT);
        assertThat(archiveFacts.path("finishedInventory").path("ledger").size()).isEqualTo(2);
        as(productionActor,"PRODUCTION");
        String executionId=production.batch(id(pendingQa)).path("executionUnits").get(0).path("id").asText();
        JsonNode form=runtime.forms(executionId).get(0);
        var printed=formPrint.load(id(form),true);
        assertThat(printed.businessVersion()).isNotBlank();
        assertThat(printed.data().get("definitionHash")).isEqualTo(form.path("definitionHash").asText());
        assertThat(archive.sourceEvidence(1,Long.parseLong(id(pendingQa))).path("finishedInventory")).isEqualTo(archiveFacts.path("finishedInventory"));
        Map<String,Object> summary = new LinkedHashMap<>();
        summary.put("draftBatchId", id(draft));
        summary.put("draftBatchNo", draft.path("batchNo").asText());
        summary.put("runningBatchId", id(running));
        summary.put("runningBatchNo", running.path("batchNo").asText());
        summary.put("releasedBatchId", id(pendingQa));
        summary.put("releasedBatchNo", pendingQa.path("batchNo").asText());
        summary.put("finishedMainBatchId", id(pendingQa));
        summary.put("finishedInboundId", id(inbound));
        summary.put("finishedReportId", id(finishedReport));
        summary.put("releaseDecisionId", id(release));
        summary.put("shipmentId", id(shipment));
        summary.put("mainBatches", count("prd_main_batch"));
        summary.put("finishedInbounds", count("wms_finished_inbound_request"));
        summary.put("finishedRequests", count("qms_finished_inspection_request"));
        summary.put("finishedReports", count("qms_finished_inspection_report"));
        summary.put("finishedShipments", count("wms_finished_shipment"));
        try { System.out.println("PRODUCTION_FINISHED_SEED_SUMMARY="+json.writeValueAsString(summary)); }
        catch(Exception e){ throw new IllegalStateException(e); }
    }

    private void initActors() {
        permissions = new HashSet<>(jdbc.queryForList(
            "SELECT permission_code FROM sys_permission WHERE enabled=TRUE", String.class));
        permissions.addAll(Set.of(
            "ebr:sign","ebr:form:view","ebr:form:edit","ebr:form:submit","ebr:designer:edit",
            "production:batch:view","production:batch:create","production:batch:update","production:batch:release","production:batch:start","production:batch:complete",
            "production:order:view","production:order:create","production:order:update",
            "process:package:view","process:package:create","process:package:edit","process:package:submit","process:package:approve","process:package:publish",
            "mes:execution:view","mes:operation:view","mes:operation:start","mes:operation:complete",
            "mes:weigh:create","mes:weigh:verify","mes:charge:create","mes:equipment:bind",
            "wms:reservation:view","wms:reservation:create","wms:issue:view","wms:issue:create","wms:issue:confirm",
            "qms:plan:view","qms:plan:create","qms:plan:update","qms:plan:approve",
            "qms:sample:view","qms:sample:create","qms:sample:receive",
            "qms:test:view","qms:test:record","qms:test:review",
            "balance:view","balance:investigate","balance:approve","mes:quantity:record",
            "qa:batch-review","qa:release",
            "wms:finished-inbound:view","wms:finished-inbound:create","wms:finished-inbound:submit","wms:finished-inbound:confirm",
            "qms:finished-request:view","qms:finished-request:create","qms:finished-request:submit","qms:finished-request:accept",
            "qms:finished-sampling:view","qms:finished-sampling:create",
            "qms:finished-report:view","qms:finished-report:generate","qms:finished-report:approve",
            "wms:finished-shipment:view","wms:finished-shipment:create","wms:finished-shipment:confirm"
        ));
        when(reauth.consume(anyString(), any())).thenReturn(new ConsumedReauthentication(Instant.now(),"PASSWORD"));
        productionActor=actor("demo.production","刘峰（生产）");
        warehouseActor=actor("demo.warehouse","李倩（仓储）");
        samplerActor=actor("demo.sampler","陈晨（取样）");
        analystActor=actor("demo.qc.analyst","周敏（QC检验）");
        reviewerActor=actor("demo.qc.reviewer","王睿（QC复核）");
        qaActor=actor("demo.qa","赵宁（QA）");
    }

    private long actor(String login,String display) {
        List<Long> ids=jdbc.queryForList("SELECT id FROM sys_user WHERE login_name_normalized=? AND enabled=TRUE",Long.class,login);
        if(ids.isEmpty()) throw new IllegalStateException("Required existing operational identity is unavailable: "+display);
        long id=ids.getFirst();
        Integer q=jdbc.queryForObject("SELECT COUNT(*) FROM md_qualification WHERE org_id=1 AND user_id=? AND qualification_code='GMP_OPERATION_AUTHORIZATION'",Integer.class,id);
        if(q==null||q==0) jdbc.update("INSERT INTO md_qualification(org_id,created_by,updated_by,user_id,qualification_code,valid_from,valid_to,status) VALUES(1,?,?,?,'GMP_OPERATION_AUTHORIZATION',?,?,'ACTIVE')",
            id,id,id,LocalDate.now(ZoneOffset.UTC).minusDays(30),LocalDate.now(ZoneOffset.UTC).plusYears(1));
        return id;
    }

    private void as(long actor,String role) {
        when(contexts.current()).thenReturn(new CurrentPlatformContext(
            1,actor,Set.of(role),permissions,"dev-business-session","dev-business-"+tag));
    }

    private void installWeighingPolicies() {
        Map<String,Object> p=new HashMap<>();
        policy(p,0,KCL,"0.001","0.5");
        policy(p,1,WATER,"0.001","0.5");
        environment.getPropertySources().addFirst(new MapPropertySource("realistic-production-"+tag,p));
    }
    private void policy(Map<String,Object> p,int i,String material,String precision,String tolerance) {
        String k="mes.production.material-weighing-policies["+i+"].";
        p.put(k+"organization-id","1");p.put(k+"material-id",material);p.put(k+"required",true);
        p.put(k+"precision",precision);p.put(k+"tolerance-pct",tolerance);p.put(k+"policy-version","GMP-"+tag);
    }

    private String ensureReleasedPurifiedWater() {
        return jdbc.queryForObject("SELECT id FROM md_material_lot WHERE org_id=1 AND material_id=? AND quality_status='RELEASED' AND inventory_status='AVAILABLE' ORDER BY id DESC LIMIT 1",String.class,WATER);
    }

    private void prepareProcessAndEbr() {
        as(productionActor,"PRODUCTION");
        JsonNode pkg=process.createPackage(new ProcessCommands.PackageCreate(PRODUCT,"KCL30-INT-"+tag),key());
        packageVersionId=pkg.path("id").asText();
        process.saveCurrentDefinition(packageVersionId,new com.hospital.mes.process.domain.ProcessCommands.CurrentDefinitionSave(null,null,null,new ProcessCommands.FormulaSave(0L,"建立氯化钾溶液1000mL标准处方","KCL30-F-"+tag,"1000",ML,
            List.of(new ProcessCommands.FormulaLine(1,KCL,"100",G,"0",true),
                    new ProcessCommands.FormulaLine(2,WATER,"900",ML,"0",true))),new ProcessCommands.RouteSave(1L,"建立配制与灌装工艺","KCL30-R-"+tag,List.of(
            new ProcessCommands.Operation("COMPOUND","配制",1,null,null,List.of(),null,false,List.of()),
            new ProcessCommands.Operation("FILL","过滤灌装",2,null,null,List.of("COMPOUND"),null,false,List.of())
        ))),null,key());
        
        as(qaActor,"QA");
        
        

        as(productionActor,"PRODUCTION");
        String opDef=processQuery.currentOperations(1,Long.parseLong(packageVersionId)).getFirst().id();
        var field=new EbrCommands.Field("OBS",null,"配制过程观察记录","TEXT","MANUAL","STRING",null,null,true,false,null,null,null,1,null,List.of());
        var def=new EbrCommands.Definition(List.of(),List.of(
            new EbrCommands.Form("COMPOUND_FORM","配制记录",opDef,"1.0",1,List.of(field))
        ),List.of(),List.of(),List.of(),"2.0");
        JsonNode template=ebr.create(new EbrCommands.Create(packageVersionId,"KCL30-EBR-"+tag,"氯化钾溶液配制电子批记录"),key());
        template=ebr.save(id(template),new EbrCommands.Save(0L,"建立配制电子批记录",def),null,key());
        template=ebr.command(id(template),"SUBMIT",new EbrCommands.Command(template.path("versionNo").asLong(),"提交eBR审核"),null,key());
        as(qaActor,"QA");
        template=ebr.command(id(template),"APPROVE",new EbrCommands.Command(template.path("versionNo").asLong(),"独立批准eBR"),null,key());
        template=ebr.command(id(template),"PUBLISH",new EbrCommands.Command(template.path("versionNo").asLong(),"发布eBR"),null,key());
        templateVersionId=id(template);
    }

    private String createFinishedSpec() {
        as(analystActor,"QC_ANALYST");
        var root=qc.create(new QcCommands.CreateSpecification("PRODUCT",null,PRODUCT,"FG-KCL-"+tag,"氯化钾溶液成品质量标准","建立成品冻结标准"),key());
        var draft=qc.createVersion(root.id(),new QcCommands.CreateVersion(1,List.of(
            new QcCommands.Item("KCL_ASSAY","氯化钾含量",true,"NUMERIC","9.5","10.5",PERCENT,null,"ASSAY-METHOD","2025")
        ),"依据氯化钾溶液质量标准"),key());
        as(reviewerActor,"QC_REVIEWER");
        return qc.approveVersion(draft.id(),new QcCommands.SignVersion(draft.versionNo(),"独立批准成品标准","reauth-evidence"),quote(draft.versionNo()),key()).id();
    }

    private JsonNode createBatch(String batchNo,String orderNo,String qty) {
        as(productionActor,"PRODUCTION");
        return production.createInstruction(body("batchNo",batchNo,"productId",PRODUCT,"plannedQty",qty,"unitId",ML,"reason","按生产计划创建医院制剂生产批"),key());
    }

    private void createApprovedPlan(JsonNode batch,boolean approve) {
        as(productionActor,"PRODUCTION");
        JsonNode plan=plans.create(body("mainBatchId",id(batch),"qcSpecificationVersionId",finishedSpecVersionId,
            "balanceRules",List.of(Map.of(
                "balanceCode","TOTAL","basis","BATCH","unitId",ML,
                "formulaExpr",Map.of("dslVersion",1,"expected",Map.of("sum",List.of("OUTPUT")),"actual",Map.of("sum",List.of("OUTPUT","LOSS")),"metric","DIFFERENCE_PCT"),
                "toleranceLow","-1","toleranceHigh","1","checkPoint","BATCH_COMPLETE")),
            "reason","建立生产质量计划与物料平衡规则"),key());
        if(approve){
            as(qaActor,"QA");
            plans.approve(id(plan),body("versionNo",plan.path("versionNo").asLong(),"reason","QA独立批准生产质量计划","signature",Map.of("reauthToken","reauth-evidence")),token(plan),key());
        }
    }

    private JsonNode releaseAndStart(JsonNode batch,String issueNo) {
        as(productionActor,"PRODUCTION");
        JsonNode released=production.releaseBatch(id(batch),body("processPackageId",packageVersionId,"ebrTemplateVersionId",templateVersionId,"reason","冻结已批准工艺、eBR和质量计划"),token(batch),key());
        JsonNode snap=productionQuery.batch(1,Long.parseLong(id(released))).snapshot();
        JsonNode f1=snap.path("process").path("formula").path("items").get(0);
        JsonNode f2=snap.path("process").path("formula").path("items").get(1);
        stock.reserve(id(released),body("items",List.of(
            Map.of("formulaItemId",f1.path("formulaItemId").asText(),"materialLotId",kclLot,"reservedQty","100","unitId",G),
            Map.of("formulaItemId",f2.path("formulaItemId").asText(),"materialLotId",waterLot,"reservedQty","900","unitId",ML)
        ),"reason","按冻结处方锁定合格原辅料"),token(released),key());
        JsonNode issue=stock.createIssue(body("mainBatchId",id(released),"issueNo",issueNo,"items",List.of(
            Map.of("formulaItemId",f1.path("formulaItemId").asText(),"materialLotId",kclLot,"issuedQty","100","unitId",G),
            Map.of("formulaItemId",f2.path("formulaItemId").asText(),"materialLotId",waterLot,"issuedQty","900","unitId",ML)
        ),"reason","按生产批发放原辅料"),key());
        stock.confirmIssue(id(issue),body("reason","仓储复核并确认出库"),token(issue),key());
        return production.startBatch(id(released),body("reason","原辅料齐套，开始生产"),token(released),key());
    }

    private JsonNode executeAndCompleteProduction(JsonNode batch) {
        as(productionActor,"PRODUCTION");
        String bid=id(batch), eid=batch.path("executionUnits").get(0).path("id").asText();
        List<JsonNode> ops=execution.operations(eid);
        JsonNode compound=execution.start(id(ops.get(0)),body("reason","开始配制"),token(ops.get(0)),key());

        JsonNode snap=productionQuery.batch(1,Long.parseLong(bid)).snapshot();
        JsonNode f1=snap.path("process").path("formula").path("items").get(0);
        JsonNode f2=snap.path("process").path("formula").path("items").get(1);
        JsonNode scale=equipment.create(new EquipmentCommands.Create("SC-KCL-"+tag,"电子称量设备","ELECTRONIC_SCALE",LocalDate.now().plusMonths(6),"制剂室配制间"),key());
        JsonNode exec=production.execution(eid);

        JsonNode w1=weighing.createWeighing(body("executionUnitId",eid,"materialLotId",kclLot,"formulaItemId",f1.path("formulaItemId").asText(),
            "targetQty","100","actualQty","100","unitId",G,"scaleEquipmentId",id(scale),"versionNo",exec.path("versionNo").asLong(),"reason","氯化钾称量"),key());
        as(reviewerActor,"QC_REVIEWER");
        w1=weighing.verifyWeighing(id(w1),body("reason","独立复核氯化钾称量","reauthToken","reauth-evidence"),token(w1),key());
        as(productionActor,"PRODUCTION");
        weighing.createCharge(body("executionUnitId",eid,"operationExecutionId",id(compound),"materialLotId",kclLot,"weighingRecordId",id(w1),
            "chargedQty","100","unitId",G,"versionNo",compound.path("versionNo").asLong(),"reason","投入氯化钾"),key());

        compound=execution.operation(1,Long.parseLong(id(compound)));
        exec=production.execution(eid);
        JsonNode w2=weighing.createWeighing(body("executionUnitId",eid,"materialLotId",waterLot,"formulaItemId",f2.path("formulaItemId").asText(),
            "targetQty","900","actualQty","900","unitId",ML,"scaleEquipmentId",id(scale),"versionNo",exec.path("versionNo").asLong(),"reason","纯化水计量"),key());
        as(reviewerActor,"QC_REVIEWER");
        w2=weighing.verifyWeighing(id(w2),body("reason","独立复核纯化水计量","reauthToken","reauth-evidence"),token(w2),key());
        as(productionActor,"PRODUCTION");
        compound=execution.operation(1,Long.parseLong(id(compound)));
        weighing.createCharge(body("executionUnitId",eid,"operationExecutionId",id(compound),"materialLotId",waterLot,"weighingRecordId",id(w2),
            "chargedQty","900","unitId",ML,"versionNo",compound.path("versionNo").asLong(),"reason","加入纯化水配液"),key());

        recordAndSubmitForm(eid);
        compound=execution.operation(1,Long.parseLong(id(compound)));
        execution.complete(id(compound),body("reason","配制完成，eBR已提交"),token(compound),key());

        JsonNode fill=execution.operations(eid).get(1);
        fill=execution.start(id(fill),body("reason","开始过滤灌装"),token(fill),key());
        fill=execution.complete(id(fill),body("reason","过滤灌装完成"),token(fill),key());

        JsonNode current=production.batch(bid);
        quantities.record(bid,body("versionNo",current.path("versionNo").asLong(),"eventType","OUTPUT","amount","995","unitId",ML,
            "sourceRef","FG-OUTPUT-"+tag,"locationId",FG_LOCATION,"productionDate",LocalDate.now().toString(),
            "expiryDate",LocalDate.now().plusMonths(6).toString(),"reason","记录成品实际产出","signature",Map.of("reauthToken","reauth-evidence")),token(current),key());
        current=production.batch(bid);
        quantities.record(bid,body("versionNo",current.path("versionNo").asLong(),"eventType","LOSS","amount","5","unitId",ML,
            "sourceRef","FG-LOSS-"+tag,"reason","记录过滤灌装正常损耗","signature",Map.of("reauthToken","reauth-evidence")),token(current),key());
        current=production.batch(bid);
        balances.recalculate(bid,body("versionNo",current.path("versionNo").asLong(),"reason","计算批次物料平衡"),token(current),key());

        current=production.batch(bid);
        JsonNode sample=productionQuality.createSample(body("investigationScope","PRODUCTION","mainBatchId",bid,"sampleNo","IPC-FG-"+tag,
            "sampleType","TEST_SAMPLE","quantity","30","unitId",ML,"sourceRef","FG-QC-"+tag,"reason","生产完成前成品质量检验样"),key());
        sample=productionQuality.receiveSample(id(sample),body("versionNo",sample.path("versionNo").asLong(),"reason","QC接收成品检验样"),token(sample),key());
        String specItem=productionQuery.batch(1,Long.parseLong(bid)).snapshot().path("qualityPlan").path("specification").path("items").get(0).path("specificationItemId").asText();
        JsonNode test=productionQuality.createTest(body("sampleId",id(sample),"specificationItemId",specItem,"reason","执行氯化钾含量检验"),key());
        test=productionQuality.result(id(test),false,body("versionNo",test.path("versionNo").asLong(),"resultNumeric","10.0","reason","氯化钾含量符合标准","signature",Map.of("reauthToken","reauth-evidence")),token(test),key());
        as(reviewerActor,"QC_REVIEWER");
        productionQuality.review(id(test),body("versionNo",test.path("versionNo").asLong(),"resultRevisionId",test.path("currentResultRevisionId").asText(),
            "disposition","CONFIRMED","reason","独立复核生产质量结果","signature",Map.of("reauthToken","reauth-evidence")),token(test),key());
        as(productionActor,"PRODUCTION");
        current=production.batch(bid);
        return production.completeProduction(bid,body("reason","生产、eBR、检验与物料平衡均完成"),token(current),key());
    }

    private void recordAndSubmitForm(String executionUnitId) {
        JsonNode form=runtime.forms(executionUnitId).get(0);
        String fid=id(form);
        JsonNode saved=runtime.command("SAVE",fid,new EbrRuntimeCommands.Save(form.path("versionNo").asLong(),"记录配制过程",
            List.of(new EbrRuntimeCommands.Value("OBS","",json.getNodeFactory().textNode("溶液澄清，无可见异物；配制过程符合工艺要求"),null,null))),
            token(form),key());
        runtime.command("SUBMIT",fid,new EbrRuntimeCommands.Command(saved.path("versionNo").asLong(),"配制记录完成并提交"),
            token(saved),key());
    }

    private JsonNode confirmFinishedInbound(JsonNode completed) {
        as(productionActor,"PRODUCTION");
        JsonNode r=finishedGoods.createInbound(body("requestNo","FG-IN-"+tag,"mainBatchId",id(completed),"reason","生产完成成品入库申请"),key());
        r=finishedGoods.inboundAction(id(r),"submit",body("versionNo",r.path("versionNo").asLong(),"reason","提交成品待检入库"),token(r),key());
        as(warehouseActor,"WAREHOUSE");
        var command=body("versionNo",r.path("versionNo").asLong(),"locationId",FG_LOCATION,
            "reason","仓储复核实际产量并接收入成品待检区","signature",Map.of("reauthToken","reauth-evidence"));String confirmationKey=key();
        var confirmed=finishedGoods.inboundAction(id(r),"confirm",command,token(r),confirmationKey);
        assertThat(finishedGoods.inboundAction(id(r),"confirm",command,token(r),confirmationKey)).isEqualTo(confirmed);
        return confirmed;
    }

    private JsonNode completeFinishedInspection(JsonNode inbound) {
        as(analystActor,"QC_ANALYST");
        JsonNode request=finishedInspection.createRequest(body("inspectionRequestNo","FG-IQ-"+tag,"inboundRequestId",id(inbound),"reason","成品放行前正式请验"),key());
        request=finishedInspection.requestAction(id(request),"submit",body("versionNo",request.path("versionNo").asLong(),"reason","提交成品请验"),token(request),key());
        as(qaActor,"QA");
        request=finishedInspection.requestAction(id(request),"accept",body("versionNo",request.path("versionNo").asLong(),"reason","QA/QC受理成品请验"),token(request),key());

        as(samplerActor,"SAMPLER");
        JsonNode sampling=finishedInspection.sample(id(request),body("versionNo",request.path("versionNo").asLong(),"samplingNo","FG-SR-"+tag,
            "sampleNo","FG-SM-"+tag,"sampleType","TEST_SAMPLE","samplingLocation","成品待检区 FG-QC","quantity","30","unitId",ML,
            "samplingMethod","按成品包装随机抽样","reason","成品放行检验取样","signature",Map.of("reauthToken","reauth-evidence")),token(request),key());
        as(analystActor,"QC_ANALYST");
        JsonNode sample=productionQuality.get(ProductionQualityService.SAMPLE,sampling.path("sampleId").asText());
        sample=productionQuality.receiveSample(id(sample),body("versionNo",sample.path("versionNo").asLong(),"reason","QC接收成品样品"),token(sample),key());
        String item=request.path("specificationSnapshot").path("specification").path("items").get(0).path("specificationItemId").asText();
        JsonNode test=productionQuality.createTest(body("sampleId",id(sample),"specificationItemId",item,"reason","执行成品氯化钾含量检验"),key());
        test=productionQuality.result(id(test),false,body("versionNo",test.path("versionNo").asLong(),"resultNumeric","10.0","reason","成品含量符合标准","signature",Map.of("reauthToken","reauth-evidence")),token(test),key());
        as(reviewerActor,"QC_REVIEWER");
        productionQuality.review(id(test),body("versionNo",test.path("versionNo").asLong(),"resultRevisionId",test.path("currentResultRevisionId").asText(),
            "disposition","CONFIRMED","reason","独立复核成品检验结果","signature",Map.of("reauthToken","reauth-evidence")),token(test),key());

        as(analystActor,"QC_ANALYST");
        JsonNode fresh=finishedInspection.getRequest(id(request));
        JsonNode report=finishedInspection.generateReport(id(request),body("versionNo",fresh.path("versionNo").asLong(),"reportNo","FG-RP-"+tag,
            "reason","汇总成品放行检验结果"),token(fresh),key());
        as(reviewerActor,"QC_REVIEWER");
        return finishedInspection.approveReport(id(report),body("versionNo",report.path("versionNo").asLong(),"reason","独立批准成品检验报告",
            "signature",Map.of("reauthToken","reauth-evidence")),token(report),key());
    }

    private JsonNode submitFinishedQa(JsonNode completed) {
        as(qaActor,"QA");
        JsonNode fresh=production.batch(id(completed));
        return production.submitQa(id(fresh),body("reason","生产、成品入库与成品检验全部完成，提交QA批审核"),token(fresh),key());
    }

    private JsonNode releaseFinished(JsonNode pending) {
        as(qaActor,"QA");
        JsonNode review=finishedQa.review(id(pending));
        ObjectNode command=body("mainBatchId",id(pending),
            "versionNo",review.path("versionNo").asLong(),"decision","RELEASED","releaseBasis","FULL_INSPECTION",
            "reviewDigest",review.path("reviewDigest").asText(),"reason","QA最终审核通过，批准成品放行",
            "signature",Map.of("reauthToken","reauth-evidence"));
        return finishedQa.decide(command,token(pending),key());
    }

    private JsonNode shipFinished(JsonNode batch,JsonNode inbound) {
        as(warehouseActor,"WAREHOUSE");
        JsonNode shipment=finishedGoods.createShipment(body("shipmentNo","FG-SHIP-"+tag,"mainBatchId",id(batch),
            "locationId",inbound.path("locationId").asText(),"quantity","300","unitId",ML,
            "receivingParty","门诊药房","reason","按已放行成品批次发货至门诊药房"),key());
        var command=body("versionNo",shipment.path("versionNo").asLong(),
            "reason","复核成品放行状态、批号与发货数量","signature",Map.of("reauthToken","reauth-evidence"));String confirmationKey=key();
        var confirmed=finishedGoods.shipmentAction(id(shipment),"confirm",command,token(shipment),confirmationKey);
        assertThat(finishedGoods.shipmentAction(id(shipment),"confirm",command,token(shipment),confirmationKey)).isEqualTo(confirmed);
        return confirmed;
    }

    private long count(String table){return jdbc.queryForObject("SELECT COUNT(*) FROM "+table,Long.class);}
    private ObjectNode body(Object...p){ObjectNode n=json.createObjectNode();for(int i=0;i<p.length;i+=2)n.set((String)p[i],json.valueToTree(p[i+1]));return n;}
    private String key(){return UUID.randomUUID().toString();}
    private String id(JsonNode n){return n.path("id").asText();}
    private String token(JsonNode n){return quote(n.path("versionNo").asLong());}
    private String quote(long v){return "\""+v+"\"";}
}
