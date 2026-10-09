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
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
    "mes.qualification.required-codes.sample-execute=MES_DEMO_GMP",
    "mes.qualification.required-codes.test-execute=MES_DEMO_GMP",
    "mes.qualification.required-codes.test-review=MES_DEMO_GMP",
    "mes.qualification.required-codes.qa-release=MES_DEMO_GMP",
    "mes.qualification.required-codes.operation-start=MES_DEMO_GMP",
    "mes.qualification.required-codes.operation-resume=MES_DEMO_GMP",
    "mes.qualification.required-codes.weigh-create=MES_DEMO_GMP",
    "mes.qualification.required-codes.weigh-verify=MES_DEMO_GMP",
    "mes.qualification.required-codes.charge-create=MES_DEMO_GMP",
    "mes.qualification.required-codes.charge-reverse=MES_DEMO_GMP",
    "mes.weighing.scale-equipment-types=DEMO_SCALE"
})
@ActiveProfiles("ci")
@Transactional
@Commit
class RealisticProductionFinishedSeedIT {
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

    @MockitoBean CurrentPlatformContextResolver contexts;
    @MockitoBean ReauthenticationPort reauth;

    Set<String> permissions;
    long productionActor, warehouseActor, samplerActor, analystActor, reviewerActor, qaActor;
    String tag, packageVersionId, templateVersionId, finishedSpecVersionId;
    String kclLot, waterLot;
    final String KCL="1", WATER="5", FINISHED="17", G="1", ML="3", PERCENT="7", PH="10";

    @Test
    void seedProductionAndFinishedChain() {
        tag = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        initActors();
        normalizeFinishedMaterial();
        installWeighingPolicies();

        kclLot = jdbc.queryForObject("""
            SELECT id FROM md_material_lot
            WHERE material_id=1 AND quality_status='RELEASED' AND inventory_status='AVAILABLE'
            ORDER BY id DESC LIMIT 1
            """, String.class);
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

        JsonNode inbound = confirmFinishedInbound(complete);
        JsonNode finishedReport = completeFinishedInspection(inbound);
        JsonNode pendingQa = submitFinishedQa(complete);
        JsonNode release = releaseFinished(pendingQa);
        JsonNode shipment = shipFinished(pendingQa, inbound);

        Map<String,Object> summary = new LinkedHashMap<>();
        summary.put("draftBatchId", id(draft));
        summary.put("draftBatchNo", draft.path("batchNo").asText());
        summary.put("runningBatchId", id(running));
        summary.put("runningBatchNo", running.path("batchNo").asText());
        summary.put("releasedBatchId", id(pendingQa));
        summary.put("releasedBatchNo", pendingQa.path("batchNo").asText());
        summary.put("finishedLotId", pendingQa.path("finishedLotId").asText());
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
        List<Long> ids=jdbc.queryForList("SELECT id FROM sys_user WHERE login_name_normalized=?",Long.class,login);
        long id;
        if(ids.isEmpty()){
            jdbc.update("INSERT INTO sys_user(login_name,login_name_normalized,display_name,password_hash,enabled,must_change_password) VALUES(?,?,?,?,TRUE,FALSE)",
                login,login,display,passwords.hash("DemoUser@2026"));
            id=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?",Long.class,login);
        } else id=ids.getFirst();
        Integer q=jdbc.queryForObject("SELECT COUNT(*) FROM md_qualification WHERE org_id=1 AND user_id=? AND qualification_code='MES_DEMO_GMP'",Integer.class,id);
        if(q==null||q==0) jdbc.update("INSERT INTO md_qualification(org_id,created_by,updated_by,user_id,qualification_code,valid_from,valid_to,status) VALUES(1,?,?,?,'MES_DEMO_GMP',?,?,'ACTIVE')",
            id,id,id,LocalDate.now(ZoneOffset.UTC).minusDays(30),LocalDate.now(ZoneOffset.UTC).plusYears(1));
        return id;
    }

    private void as(long actor,String role) {
        when(contexts.current()).thenReturn(new CurrentPlatformContext(
            1,actor,Set.of(role),permissions,"demo-production-session","demo-production-"+tag));
    }

    private void normalizeFinishedMaterial() {
        as(productionActor,"MASTER_DATA");
        JsonNode m=materials.get(FINISHED);
        if("FINISHED".equals(m.path("materialType").asText())) return;
        materials.update(FINISHED,new MaterialCommands.Update(
            m.path("versionNo").asLong(),
            "规范成品物料类型编码，使其符合生产质量计划业务契约",
            m.path("materialName").asText(),
            "FINISHED",
            nullable(m,"specification"),nullable(m,"gradePurity"),nullable(m,"appearance"),
            m.path("baseUnitId").asText(),nullable(m,"packSpec"),nullable(m,"packUnitId"),
            nullable(m,"manufacturerName"),m.path("lotControlled").asBoolean(true),
            nullable(m,"effectiveFrom"),nullable(m,"effectiveTo"),nullable(m,"remark"),
            m.path("requiresIncomingInspection").asBoolean(true),nullable(m,"storageCondition")),
            token(m),key());
    }

    private String nullable(JsonNode n,String field){return n.hasNonNull(field)?n.path(field).asText():null;}

    private void installWeighingPolicies() {
        Map<String,Object> p=new HashMap<>();
        policy(p,0,KCL,"0.001","0.5");
        policy(p,1,WATER,"0.001","0.5");
        environment.getPropertySources().addFirst(new MapPropertySource("realistic-production-"+tag,p));
    }
    private void policy(Map<String,Object> p,int i,String material,String precision,String tolerance) {
        String k="mes.production.material-weighing-policies["+i+"].";
        p.put(k+"organization-id","1");p.put(k+"material-id",material);p.put(k+"required",true);
        p.put(k+"precision",precision);p.put(k+"tolerance-pct",tolerance);p.put(k+"policy-version","DEMO-"+tag);
    }

    private String ensureReleasedPurifiedWater() {
        List<String> existing=jdbc.queryForList("""
            SELECT id FROM md_material_lot
            WHERE material_id=5 AND quality_status='RELEASED' AND inventory_status='AVAILABLE'
            ORDER BY id DESC
            """,String.class);
        if(!existing.isEmpty()) return existing.getFirst();

        as(productionActor,"MASTER_DATA");
        JsonNode supplier=suppliers.get("7");
        if("UNAPPROVED".equals(supplier.path("qualificationStatus").asText()))
            suppliers.command("7",new SupplierCommands.Update("QUALIFY",supplier.path("versionNo").asLong(),
                "院内制剂室纯化水供应资格确认",null,null),null,key());
        JsonNode relations=materialSuppliers.get(WATER);
        boolean ready=false;
        for(JsonNode row:relations.path("suppliers"))
            if("7".equals(row.path("supplierId").asText())&&row.path("approved").asBoolean()&&row.path("preferred").asBoolean())ready=true;
        if(!ready) materialSuppliers.assign(WATER,new SupplierCommands.Assign(relations.path("versionNo").asLong(),
            "院内纯化水作为首选合格来源",List.of(new SupplierCommands.Relationship("7",true,true,LocalDate.now().plusYears(1),"中山大学附属第三医院制剂室"))),null,key());

        as(analystActor,"QC_ANALYST");
        var root=qc.create(new QcCommands.CreateSpecification(WATER,"PW-"+tag,"纯化水入厂质量标准","建立纯化水检验标准"),key());
        var draft=qc.createVersion(root.id(),new QcCommands.CreateVersion(1,List.of(
            new QcCommands.Item("PH","pH",true,"NUMERIC","5.0","7.0",PH,null,"PH-METHOD","2025")
        ),"依据纯化水质量标准"),key());
        as(reviewerActor,"QC_REVIEWER");
        String spec=qc.approveVersion(draft.id(),new QcCommands.SignVersion(draft.versionNo(),"独立批准纯化水标准","demo-reauth"),quote(draft.versionNo()),key()).id();

        as(warehouseActor,"WAREHOUSE");
        var item=new ReceiptItemInput(WATER,"PW-"+tag+"-01","PW-LOOP-"+tag,null,
            LocalDate.now().toString(),LocalDate.now().plusDays(7).toString(),null,
            "200000",ML,"100 L不锈钢储罐",2L,"1",null,true,true,true,true,true);
        JsonNode receipt=wms.createReceipt(new ReceiptCreate("REC-PW-"+tag,"7","PW-INTERNAL-"+tag,"PW-DN-"+tag,"1",true,List.of(item)),key(),true);
        String lot=receipt.path("items").get(0).path("materialLotId").asText();

        as(analystActor,"QC_ANALYST");
        JsonNode request=incoming.createRequest(body("materialLotId",lot,"qcSpecificationVersionId",spec,"requestType","INITIAL",
            "reason","纯化水使用前质量确认","requestedQuantity","100","requestedUnitId",ML,"requestedPackageCount",1,
            "requestedDate",LocalDate.now().toString(),"priority","NORMAL"),key());
        request=incoming.requestAction(id(request),"submit",body("reason","提交纯化水请验"),token(request),key());
        request=incoming.requestAction(id(request),"accept",body("reason","QC受理纯化水请验"),token(request),key());
        JsonNode st=incoming.createSampling(body("inspectionRequestId",id(request),"inspectionRequestItemId",request.path("items").get(0).path("id").asText(),
            "samplingPlan","循环水取样口冲洗后取代表性样","requiredPackageCount",1,"reason","纯化水取样计划"),key());
        st=incoming.samplingAction(id(st),"assign",body("assignedTo",Long.toString(samplerActor),"reason","指派取样"),token(st),key());
        as(samplerActor,"SAMPLER");
        st=incoming.samplingAction(id(st),"start",body("reason","开始纯化水取样"),token(st),key());
        st=incoming.samplingAction(id(st),"details",body("containerNo","PW-PORT-01","samplingPoint","纯化水循环回水取样点",
            "sampleQuantity","100","unitId",ML,"sampledAt",Instant.now().minusSeconds(30).toString(),"packageResealed",true,
            "samples",List.of(Map.of("sampleType","TEST_SAMPLE","quantity","80","storageLocation","QC待检"),
                              Map.of("sampleType","RETENTION_SAMPLE","quantity","20","storageLocation","留样室")),
            "reason","完成纯化水取样"),token(st),key());
        st=incoming.samplingAction(id(st),"complete",body("reason","取样完成","reauthToken","demo-reauth"),token(st),key());
        JsonNode sample=st.path("details").get(0).path("samples").get(0);
        sample=incoming.sampleAction(id(sample),"receive",body("receivedAt",Instant.now().toString(),"storageLocation","QC水样区","reason","接收水样"),token(sample),key());

        as(analystActor,"QC_ANALYST");
        JsonNode task=incoming.createTask(body("inspectionRequestId",id(request),"sampleId",id(sample),"reason","执行pH检验"),key());
        task=incoming.taskAction(id(task),"assign",body("assignedTo",Long.toString(analystActor),"reason","指派检验"),token(task),key());
        task=incoming.taskAction(id(task),"start",body("reason","开始pH检验"),token(task),key());
        JsonNode exec=incoming.execution(task.path("items").get(0).path("id").asText(),false,
            body("rawData",Map.of("meter","PH-01","reading","6.2"),"calculationInput",Map.of(),
                "startedAt",Instant.now().minusSeconds(20).toString(),"completedAt",Instant.now().minusSeconds(5).toString(),
                "performedBy",Long.toString(analystActor),"reason","记录pH原始数据"),token(task),key());
        JsonNode current=incoming.get("qms_inspection_task","qms:test:view",id(task));
        JsonNode result=incoming.result(task.path("items").get(0).path("id").asText(),false,
            body("testExecutionId",id(exec),"resultNumeric","6.2","resultUnitId",PH,"resultConclusion","PASS","reauthToken","demo-reauth"),token(current),key());
        current=incoming.get("qms_inspection_task","qms:test:view",id(task));
        current=incoming.taskAction(id(current),"submit-review",body("reason","提交pH结果复核"),token(current),key());
        as(reviewerActor,"QC_REVIEWER");
        incoming.taskAction(id(current),"review",body("resultRevisionIds",List.of(id(result)),"reauthToken","demo-reauth","reason","复核pH结果"),token(current),key());

        as(analystActor,"QC_ANALYST");
        JsonNode report=incoming.createReport(body("inspectionRequestId",id(request),"reason","汇总纯化水检验"),key());
        as(reviewerActor,"QC_REVIEWER");
        report=incoming.reportAction(id(report),"review",body("reason","QC复核纯化水报告"),token(report),key());
        as(qaActor,"QA");
        report=incoming.reportAction(id(report),"approve",body("reason","QA批准纯化水报告","reauthToken","demo-reauth"),token(report),key());
        JsonNode gate=incoming.releaseReview(lot);
        incoming.decideRelease(lot,body("decision","RELEASED","releaseBasis","FULL_INSPECTION","inspectionReportId",id(report),
            "reason","纯化水检验合格，批准用于生产","reauthToken","demo-reauth"),token(gate),key());
        return lot;
    }

    private void prepareProcessAndEbr() {
        as(productionActor,"PRODUCTION");
        JsonNode pkg=process.createPackage(new ProcessCommands.PackageCreate("1","KCL30-INT-"+tag),key());
        packageVersionId=pkg.path("id").asText();
        process.saveCurrentDefinition(packageVersionId,new com.hospital.mes.process.domain.ProcessCommands.CurrentDefinitionSave(null,null,null,new ProcessCommands.FormulaSave(0L,"建立氯化钾溶液1000mL标准处方","KCL30-F-"+tag,"1000",ML,
            List.of(new ProcessCommands.FormulaLine(1,KCL,"20",G,"0",true),
                    new ProcessCommands.FormulaLine(2,WATER,"980",ML,"0",true))),new ProcessCommands.RouteSave(1L,"建立配制与灌装工艺","KCL30-R-"+tag,List.of(
            new ProcessCommands.Operation("COMPOUND","配制",1,null,null,List.of(),null,false,List.of()),
            new ProcessCommands.Operation("FILL","过滤灌装",2,null,null,List.of("COMPOUND"),null,false,List.of())
        ))),null,key());
        
        as(qaActor,"QA");
        
        

        as(productionActor,"PRODUCTION");
        String opDef=processQuery.currentOperations(1,Long.parseLong(packageVersionId)).getFirst().id();
        var field=new EbrCommands.Field("OBS",null,"配制过程观察记录","TEXT","MANUAL","STRING",null,null,true,false,null,null,null,1,null,List.of());
        var def=new EbrCommands.Definition(List.of(),List.of(
            new EbrCommands.Form("COMPOUND_FORM","配制记录",opDef,"1.0",1,List.of(field))
        ),List.of(),List.of(),List.of());
        JsonNode template=ebr.create(new EbrCommands.Create(packageVersionId,"KCL30-EBR-"+tag,"Test eBR template"),key());
        template=ebr.save(id(template),new EbrCommands.Save(0L,"建立配制电子批记录",def),null,key());
        template=ebr.command(id(template),"SUBMIT",new EbrCommands.Command(template.path("versionNo").asLong(),"提交eBR审核"),null,key());
        as(qaActor,"QA");
        template=ebr.command(id(template),"APPROVE",new EbrCommands.Command(template.path("versionNo").asLong(),"独立批准eBR"),null,key());
        template=ebr.command(id(template),"PUBLISH",new EbrCommands.Command(template.path("versionNo").asLong(),"发布eBR"),null,key());
        templateVersionId=id(template);
    }

    private String createFinishedSpec() {
        as(analystActor,"QC_ANALYST");
        var root=qc.create(new QcCommands.CreateSpecification(FINISHED,"FG-KCL-"+tag,"氯化钾溶液成品质量标准","建立成品冻结标准"),key());
        var draft=qc.createVersion(root.id(),new QcCommands.CreateVersion(1,List.of(
            new QcCommands.Item("KCL_ASSAY","氯化钾含量",true,"NUMERIC","1.8","2.2",PERCENT,null,"ASSAY-METHOD","2025")
        ),"依据氯化钾溶液质量标准"),key());
        as(reviewerActor,"QC_REVIEWER");
        return qc.approveVersion(draft.id(),new QcCommands.SignVersion(draft.versionNo(),"独立批准成品标准","demo-reauth"),quote(draft.versionNo()),key()).id();
    }

    private JsonNode createBatch(String batchNo,String orderNo,String qty) {
        as(productionActor,"PRODUCTION");
        JsonNode order=production.createOrder(body("orderNo",orderNo,"productId","1","plannedQty",qty,"unitId",ML,"reason","医院制剂生产计划"),key());
        return production.createBatch(body("batchNo",batchNo,"productionOrderId",id(order),"productId","1","plannedQty",qty,"unitId",ML,"reason","创建氯化钾溶液生产批"),key());
    }

    private void createApprovedPlan(JsonNode batch,boolean approve) {
        as(productionActor,"PRODUCTION");
        JsonNode plan=plans.create(body("mainBatchId",id(batch),"finishedMaterialId",FINISHED,"qcSpecificationVersionId",finishedSpecVersionId,
            "balanceRules",List.of(Map.of(
                "balanceCode","TOTAL","basis","BATCH","unitId",ML,
                "formulaExpr",Map.of("dslVersion",1,"expected",Map.of("sum",List.of("OUTPUT")),"actual",Map.of("sum",List.of("OUTPUT","LOSS")),"metric","DIFFERENCE_PCT"),
                "toleranceLow","-1","toleranceHigh","1","checkPoint","BATCH_COMPLETE")),
            "reason","建立生产质量计划与物料平衡规则"),key());
        if(approve){
            as(qaActor,"QA");
            plans.approve(id(plan),body("versionNo",plan.path("versionNo").asLong(),"reason","QA独立批准生产质量计划","signature",Map.of("reauthToken","demo-reauth")),token(plan),key());
        }
    }

    private JsonNode releaseAndStart(JsonNode batch,String issueNo) {
        as(productionActor,"PRODUCTION");
        JsonNode released=production.releaseBatch(id(batch),body("processPackageId",packageVersionId,"ebrTemplateVersionId",templateVersionId,"reason","冻结已批准工艺、eBR和质量计划"),token(batch),key());
        JsonNode snap=productionQuery.batch(1,Long.parseLong(id(released))).snapshot();
        JsonNode f1=snap.path("process").path("formula").path("items").get(0);
        JsonNode f2=snap.path("process").path("formula").path("items").get(1);
        stock.reserve(id(released),body("items",List.of(
            Map.of("formulaItemId",f1.path("formulaItemId").asText(),"materialLotId",kclLot,"reservedQty","20","unitId",G),
            Map.of("formulaItemId",f2.path("formulaItemId").asText(),"materialLotId",waterLot,"reservedQty","980","unitId",ML)
        ),"reason","按冻结处方锁定合格原辅料"),token(released),key());
        JsonNode issue=stock.createIssue(body("mainBatchId",id(released),"issueNo",issueNo,"items",List.of(
            Map.of("formulaItemId",f1.path("formulaItemId").asText(),"materialLotId",kclLot,"issuedQty","20","unitId",G),
            Map.of("formulaItemId",f2.path("formulaItemId").asText(),"materialLotId",waterLot,"issuedQty","980","unitId",ML)
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
        JsonNode scale=equipment.create(new EquipmentCommands.Create("SC-KCL-"+tag,"电子称量设备","DEMO_SCALE",LocalDate.now().plusMonths(6),"制剂室配制间"),key());
        JsonNode exec=production.execution(eid);

        JsonNode w1=weighing.createWeighing(body("executionUnitId",eid,"materialLotId",kclLot,"formulaItemId",f1.path("formulaItemId").asText(),
            "targetQty","20","actualQty","20","unitId",G,"scaleEquipmentId",id(scale),"versionNo",exec.path("versionNo").asLong(),"reason","氯化钾称量"),key());
        as(reviewerActor,"QC_REVIEWER");
        w1=weighing.verifyWeighing(id(w1),body("reason","独立复核氯化钾称量","reauthToken","demo-reauth"),token(w1),key());
        as(productionActor,"PRODUCTION");
        weighing.createCharge(body("executionUnitId",eid,"operationExecutionId",id(compound),"materialLotId",kclLot,"weighingRecordId",id(w1),
            "chargedQty","20","unitId",G,"versionNo",compound.path("versionNo").asLong(),"reason","投入氯化钾"),key());

        compound=execution.operation(1,Long.parseLong(id(compound)));
        exec=production.execution(eid);
        JsonNode w2=weighing.createWeighing(body("executionUnitId",eid,"materialLotId",waterLot,"formulaItemId",f2.path("formulaItemId").asText(),
            "targetQty","980","actualQty","980","unitId",ML,"scaleEquipmentId",id(scale),"versionNo",exec.path("versionNo").asLong(),"reason","纯化水计量"),key());
        as(reviewerActor,"QC_REVIEWER");
        w2=weighing.verifyWeighing(id(w2),body("reason","独立复核纯化水计量","reauthToken","demo-reauth"),token(w2),key());
        as(productionActor,"PRODUCTION");
        compound=execution.operation(1,Long.parseLong(id(compound)));
        weighing.createCharge(body("executionUnitId",eid,"operationExecutionId",id(compound),"materialLotId",waterLot,"weighingRecordId",id(w2),
            "chargedQty","980","unitId",ML,"versionNo",compound.path("versionNo").asLong(),"reason","加入纯化水配液"),key());

        recordAndSubmitForm(eid);
        compound=execution.operation(1,Long.parseLong(id(compound)));
        execution.complete(id(compound),body("reason","配制完成，eBR已提交"),token(compound),key());

        JsonNode fill=execution.operations(eid).get(1);
        fill=execution.start(id(fill),body("reason","开始过滤灌装"),token(fill),key());
        fill=execution.complete(id(fill),body("reason","过滤灌装完成"),token(fill),key());

        JsonNode current=production.batch(bid);
        quantities.record(bid,body("versionNo",current.path("versionNo").asLong(),"eventType","OUTPUT","amount","995","unitId",ML,
            "sourceRef","FG-OUTPUT-"+tag,"lotNo","FG-KCL-"+tag,"locationId","7","productionDate",LocalDate.now().toString(),
            "expiryDate",LocalDate.now().plusMonths(6).toString(),"reason","记录成品实际产出","signature",Map.of("reauthToken","demo-reauth")),token(current),key());
        current=production.batch(bid);
        quantities.record(bid,body("versionNo",current.path("versionNo").asLong(),"eventType","LOSS","amount","5","unitId",ML,
            "sourceRef","FG-LOSS-"+tag,"reason","记录过滤灌装正常损耗","signature",Map.of("reauthToken","demo-reauth")),token(current),key());
        current=production.batch(bid);
        balances.recalculate(bid,body("versionNo",current.path("versionNo").asLong(),"reason","计算批次物料平衡"),token(current),key());

        current=production.batch(bid);
        String finishedLot=current.path("finishedLotId").asText();
        JsonNode sample=productionQuality.createSample(body("investigationScope","PRODUCTION","mainBatchId",bid,"sampleNo","IPC-FG-"+tag,
            "sampleType","TEST_SAMPLE","materialLotId",finishedLot,"quantity","30","unitId",ML,"sourceRef","FG-QC-"+tag,"reason","生产完成前成品质量检验样"),key());
        sample=productionQuality.receiveSample(id(sample),body("versionNo",sample.path("versionNo").asLong(),"reason","QC接收成品检验样"),token(sample),key());
        String specItem=productionQuery.batch(1,Long.parseLong(bid)).snapshot().path("qualityPlan").path("specification").path("items").get(0).path("specificationItemId").asText();
        JsonNode test=productionQuality.createTest(body("sampleId",id(sample),"specificationItemId",specItem,"reason","执行氯化钾含量检验"),key());
        test=productionQuality.result(id(test),false,body("versionNo",test.path("versionNo").asLong(),"resultNumeric","2.0","reason","氯化钾含量符合标准","signature",Map.of("reauthToken","demo-reauth")),token(test),key());
        as(reviewerActor,"QC_REVIEWER");
        productionQuality.review(id(test),body("versionNo",test.path("versionNo").asLong(),"resultRevisionId",test.path("currentResultRevisionId").asText(),
            "disposition","CONFIRMED","reason","独立复核生产质量结果","signature",Map.of("reauthToken","demo-reauth")),token(test),key());
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
        return finishedGoods.inboundAction(id(r),"confirm",body("versionNo",r.path("versionNo").asLong(),"locationId","7",
            "reason","仓储复核实际产量并接收入成品待检区","signature",Map.of("reauthToken","demo-reauth")),token(r),key());
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
            "samplingMethod","按成品包装随机抽样","reason","成品放行检验取样","signature",Map.of("reauthToken","demo-reauth")),token(request),key());
        as(analystActor,"QC_ANALYST");
        JsonNode sample=productionQuality.get(ProductionQualityService.SAMPLE,sampling.path("sampleId").asText());
        sample=productionQuality.receiveSample(id(sample),body("versionNo",sample.path("versionNo").asLong(),"reason","QC接收成品样品"),token(sample),key());
        String item=request.path("specificationSnapshot").path("specification").path("items").get(0).path("specificationItemId").asText();
        JsonNode test=productionQuality.createTest(body("sampleId",id(sample),"specificationItemId",item,"reason","执行成品氯化钾含量检验"),key());
        test=productionQuality.result(id(test),false,body("versionNo",test.path("versionNo").asLong(),"resultNumeric","2.0","reason","成品含量符合标准","signature",Map.of("reauthToken","demo-reauth")),token(test),key());
        as(reviewerActor,"QC_REVIEWER");
        productionQuality.review(id(test),body("versionNo",test.path("versionNo").asLong(),"resultRevisionId",test.path("currentResultRevisionId").asText(),
            "disposition","CONFIRMED","reason","独立复核成品检验结果","signature",Map.of("reauthToken","demo-reauth")),token(test),key());

        as(analystActor,"QC_ANALYST");
        JsonNode fresh=finishedInspection.getRequest(id(request));
        JsonNode report=finishedInspection.generateReport(id(request),body("versionNo",fresh.path("versionNo").asLong(),"reportNo","FG-RP-"+tag,
            "reason","汇总成品放行检验结果"),token(fresh),key());
        as(reviewerActor,"QC_REVIEWER");
        return finishedInspection.approveReport(id(report),body("versionNo",report.path("versionNo").asLong(),"reason","独立批准成品检验报告",
            "signature",Map.of("reauthToken","demo-reauth")),token(report),key());
    }

    private JsonNode submitFinishedQa(JsonNode completed) {
        as(qaActor,"QA");
        JsonNode fresh=production.batch(id(completed));
        return production.submitQa(id(fresh),body("reason","生产、成品入库与成品检验全部完成，提交QA批审核"),token(fresh),key());
    }

    private JsonNode releaseFinished(JsonNode pending) {
        as(qaActor,"QA");
        JsonNode review=finishedQa.review(id(pending));
        ObjectNode command=body("mainBatchId",id(pending),"finishedLotId",pending.path("finishedLotId").asText(),
            "versionNo",review.path("versionNo").asLong(),"decision","RELEASED","releaseBasis","FULL_INSPECTION",
            "reviewDigest",review.path("reviewDigest").asText(),"reason","QA最终审核通过，批准成品放行",
            "signature",Map.of("reauthToken","demo-reauth"));
        return finishedQa.decide(command,token(pending),key());
    }

    private JsonNode shipFinished(JsonNode batch,JsonNode inbound) {
        as(warehouseActor,"WAREHOUSE");
        JsonNode shipment=finishedGoods.createShipment(body("shipmentNo","FG-SHIP-"+tag,"mainBatchId",id(batch),
            "locationId",inbound.path("locationId").asText(),"quantity","300","unitId",ML,
            "receivingParty","门诊药房","reason","按已放行成品批次发货至门诊药房"),key());
        return finishedGoods.shipmentAction(id(shipment),"confirm",body("versionNo",shipment.path("versionNo").asLong(),
            "reason","复核成品放行状态、批号与发货数量","signature",Map.of("reauthToken","demo-reauth")),token(shipment),key());
    }

    private long count(String table){return jdbc.queryForObject("SELECT COUNT(*) FROM "+table,Long.class);}
    private ObjectNode body(Object...p){ObjectNode n=json.createObjectNode();for(int i=0;i<p.length;i+=2)n.set((String)p[i],json.valueToTree(p[i+1]));return n;}
    private String key(){return UUID.randomUUID().toString();}
    private String id(JsonNode n){return n.path("id").asText();}
    private String token(JsonNode n){return quote(n.path("versionNo").asLong());}
    private String quote(long v){return "\""+v+"\"";}
}
