package com.hospital.mes.production;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.signature.ConsumedReauthentication;
import com.hospital.mes.ebr.application.EbrRuntimeService;
import com.hospital.mes.ebr.application.EbrService;
import com.hospital.mes.ebr.domain.EbrCommands;
import com.hospital.mes.equipment.application.EquipmentCommands;
import com.hospital.mes.equipment.application.EquipmentService;
import com.hospital.mes.execution.application.ExecutionService;
import com.hospital.mes.execution.application.ProductionQuantityService;
import com.hospital.mes.execution.application.WeighChargeService;
import com.hospital.mes.incoming.IncomingQualityFixture;
import com.hospital.mes.process.application.ProcessQueryService;
import com.hospital.mes.process.application.ProcessService;
import com.hospital.mes.production.application.ProductionQueryService;
import com.hospital.mes.production.application.ProductionService;
import com.hospital.mes.process.domain.ProcessCommands;
import com.hospital.mes.qc.domain.QcCommands;
import com.hospital.mes.qms.application.FinishedInspectionService;
import com.hospital.mes.qms.application.MaterialBalanceService;
import com.hospital.mes.qms.application.ProductionQualityPlanService;
import com.hospital.mes.qms.application.ProductionQualityService;
import com.hospital.mes.release.application.FinishedReleaseService;
import com.hospital.mes.wms.application.FinishedGoodsService;
import com.hospital.mes.wms.application.WmsProductionService;
import com.hospital.mes.wms.application.WmsQueryService;
import com.hospital.mes.wms.domain.WmsCommands.InventoryMove;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
    "mes.qualification.required-codes.sample-execute=MES_DEMO_GMP",
    "mes.qualification.required-codes.test-execute=MES_DEMO_GMP",
    "mes.qualification.required-codes.operation-start=MES_DEMO_GMP",
    "mes.qualification.required-codes.operation-resume=MES_DEMO_GMP",
    "mes.qualification.required-codes.weigh-create=MES_DEMO_GMP",
    "mes.qualification.required-codes.weigh-verify=MES_DEMO_GMP",
    "mes.qualification.required-codes.charge-create=MES_DEMO_GMP",
    "mes.qualification.required-codes.charge-reverse=MES_DEMO_GMP",
    "mes.qualification.required-codes.test-review=MES_DEMO_GMP",
    "mes.qualification.required-codes.qa-release=MES_DEMO_GMP",
    "mes.weighing.scale-equipment-types=PRODUCTION_SCALE",
    "mes.production.material-weighing-policies[0].organization-id=1",
    "mes.production.material-weighing-policies[0].material-id=1",
    "mes.production.material-weighing-policies[0].required=true",
    "mes.production.material-weighing-policies[0].precision=0.001",
    "mes.production.material-weighing-policies[0].tolerance-pct=0.5",
    "mes.production.material-weighing-policies[0].policy-version=KCL-DEMO-2026"
})
@ActiveProfiles("ci")
@Transactional
@Commit
class RealisticProductionFinishedSeedIT extends IncomingQualityFixture {
    @Autowired ProductionService production;
    @Autowired ProductionQueryService productionQuery;
    @Autowired ProcessService process;
    @Autowired ProcessQueryService processQuery;
    @Autowired EbrService ebr;
    @Autowired EbrRuntimeService runtime;
    @Autowired ExecutionService execution;
    @Autowired WeighChargeService weighing;
    @Autowired WmsProductionService stock;
    @Autowired WmsQueryService stockQuery;
    @Autowired EquipmentService equipment;
    @Autowired ProductionQualityPlanService plans;
    @Autowired ProductionQuantityService quantities;
    @Autowired MaterialBalanceService balances;
    @Autowired ProductionQualityService productionQuality;
    @Autowired FinishedGoodsService finishedGoods;
    @Autowired FinishedInspectionService finishedInspection;
    @Autowired FinishedReleaseService finishedQa;
    @Autowired ConfigurableEnvironment environment;

    String runTag;
    long warehouseActor, samplerActor;
    String productId = "1";          // KCL30 氯化钾溶液
    String finishedMaterialId = "17";// FG_KCL_30
    String productUnit = "3";        // ml
    String rawUnit = "1";            // g
    String packageVersionId;
    String templateVersionId;
    String finishedSpecVersionId;
    String formulaItemId;
    String scaleId;

    @Override
    @BeforeEach
    protected void setup() {
        runTag = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        suffix = runTag.substring(4);
        permissions = new HashSet<>(jdbc.queryForList(
            "SELECT permission_code FROM sys_permission WHERE enabled=TRUE", String.class));
        for (String prefix : List.of("master:product:","master:equipment:","process:package:","ebr:template:","production:order:","production:batch:","mes:execution:","mes:operation:","mes:weigh:","mes:charge:","wms:reservation:","wms:issue:"))
            for (String action : List.of("view","create","update","edit","submit","approve","publish","release","start","pause","complete","verify","reverse","confirm","return")) permissions.add(prefix+action);
        permissions.addAll(Set.of(
            "production:subbatch:create","ebr:designer:edit","ebr:form:view","ebr:form:edit","ebr:form:submit","trace:view",
            "qms:plan:view","qms:plan:create","qms:plan:update","qms:plan:approve",
            "balance:view","balance:investigate","balance:approve","mes:quantity:record","mes:quantity:reverse",
            "qms:specification:view","qms:specification:create","qms:specification:edit","qms:specification:approve",
            "qms:sample:view","qms:sample:create","qms:sample:receive","qms:test:record","qms:test:view","qms:test:review","qms:test:retest",
            "qms:deviation:view","qms:deviation:create","qms:deviation:update","qms:deviation:investigate","qms:deviation:decide","qms:deviation:close",
            "qa:batch-review","qa:release","wms:inventory:view","wms:inventory:move","wms:inventory:adjust",
            "wms:finished-inbound:view","wms:finished-inbound:create","wms:finished-inbound:submit","wms:finished-inbound:confirm","wms:finished-inbound:cancel",
            "qms:finished-request:view","qms:finished-request:create","qms:finished-request:submit","qms:finished-request:accept",
            "qms:finished-sampling:view","qms:finished-sampling:create","qms:finished-report:view","qms:finished-report:generate","qms:finished-report:approve",
            "wms:finished-shipment:view","wms:finished-shipment:create","wms:finished-shipment:confirm","wms:finished-shipment:cancel"));
        author = user("demo.production");
        warehouseActor = user("demo.warehouse");
        samplerActor = user("demo.sampler");
        approver = user("demo.qc.reviewer");
        analyst = user("demo.qc.analyst");
        reviewer = user("demo.qc.reviewer");
        qa = user("demo.qa");
        ensureQualification(author);
        ensureQualification(warehouseActor);
        ensureQualification(samplerActor);
        ensureQualification(approver);
        ensureQualification(analyst);
        ensureQualification(qa);
        unit = productUnit;
        material = "1";
        lot = jdbc.queryForObject("""
            SELECT l.id FROM md_material_lot l
            WHERE l.material_id=1 AND l.quality_status='RELEASED'
              AND l.inventory_status='AVAILABLE'
            ORDER BY l.id DESC LIMIT 1
            """, String.class);
        as(author);
        when(reauth.consume(anyString(), any())).thenReturn(
            new ConsumedReauthentication(Instant.now(), "PASSWORD"));
    }

    @Test
    void seedProductionAndFinishedGoodsChain() {
        createPublishedProcessAndEbr();
        finishedSpecVersionId = createFinishedQualitySpecification();
        scaleId = createScale();

        JsonNode first = createApprovedBatch("20261007-KCL-001", "PO-KCL-20261007-001");
        first = reserveIssueAndStart(first);
        JsonNode completed = completeProduction(first, "FG-KCL-20261007-001");
        JsonNode released = completeFinishedChain(completed);

        JsonNode second = createApprovedBatch("20261007-KCL-002", "PO-KCL-20261007-002");
        second = reserveIssueAndStart(second);
        JsonNode currentOp = execution.operations(second.path("executionUnits").get(0).path("id").asText()).getFirst();
        currentOp = execution.start(id(currentOp), body("reason","第二批进入配制工序"), token(currentOp), key());

        JsonNode third = createDraftBatch("20261008-KCL-003", "PO-KCL-20261008-003");

        Map<String,Object> summary = new LinkedHashMap<>();
        summary.put("releasedBatch", id(released));
        summary.put("releasedBatchNo", released.path("batchNo").asText());
        summary.put("releasedFinishedLot", released.path("finishedLotId").asText());
        summary.put("inProgressBatch", id(second));
        summary.put("inProgressBatchNo", second.path("batchNo").asText());
        summary.put("draftBatch", id(third));
        summary.put("draftBatchNo", third.path("batchNo").asText());
        summary.put("productionOrders", jdbc.queryForObject("SELECT COUNT(*) FROM prd_production_order", Long.class));
        summary.put("mainBatches", jdbc.queryForObject("SELECT COUNT(*) FROM prd_main_batch", Long.class));
        summary.put("finishedInbound", jdbc.queryForObject("SELECT COUNT(*) FROM wms_finished_inbound_request", Long.class));
        summary.put("finishedRequests", jdbc.queryForObject("SELECT COUNT(*) FROM qms_finished_inspection_request", Long.class));
        summary.put("finishedReports", jdbc.queryForObject("SELECT COUNT(*) FROM qms_finished_inspection_report", Long.class));
        summary.put("finishedShipments", jdbc.queryForObject("SELECT COUNT(*) FROM wms_finished_shipment", Long.class));
        try { System.out.println("REALISTIC_PRODUCTION_SEED_SUMMARY=" + json.writeValueAsString(summary)); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }

    private long user(String login) {
        return jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?", Long.class, login);
    }

    private void ensureQualification(long actor) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM md_qualification WHERE org_id=1 AND user_id=? AND qualification_code='MES_DEMO_GMP' AND status='ACTIVE'",
            Integer.class, actor);
        if (count == null || count == 0) {
            jdbc.update("INSERT INTO md_qualification(org_id,created_by,updated_by,user_id,qualification_code,valid_from,valid_to,status) VALUES(1,?,?,?,'MES_DEMO_GMP',?,?,'ACTIVE')",
                actor, actor, actor, LocalDate.now(ZoneOffset.UTC).minusDays(30),
                LocalDate.now(ZoneOffset.UTC).plusYears(1));
        }
    }

    private void createPublishedProcessAndEbr() {
        as(author);
        JsonNode pkg = process.createPackage(new ProcessCommands.PackageCreate(
            productId, "KCL30-PKG-"+runTag), key());
        packageVersionId = pkg.path("id").asText();
        process.saveCurrentDefinition(packageVersionId,new com.hospital.mes.process.domain.ProcessCommands.CurrentDefinitionSave(null,null,null,new ProcessCommands.FormulaSave(
            0L, "建立氯化钾溶液 1000 mL 标准生产处方",
            "KCL30-FML-"+runTag, "1000", productUnit,
            List.of(new ProcessCommands.FormulaLine(
                1, material, "100", rawUnit, "0", true))),new ProcessCommands.RouteSave(
            1L, "建立配制与终检工艺路线", "KCL30-ROUTE-"+runTag,
            List.of(new ProcessCommands.Operation(
                "MIX", "配制混合", 1, null, null, List.of(),
                null, false, List.of(), List.of())))),null,key());
        
        as(approver);
        
        

        as(author);
        String operationDef = processQuery.currentOperations(1, Long.parseLong(packageVersionId)).getFirst().id();
        var field = new EbrCommands.Field(
            "OBS", null, "配制观察记录", "TEXT", "MANUAL", "STRING",
            null, null, true, false, null, null, null, 1, null, List.of());
        var definition = new EbrCommands.Definition(
            List.of(),
            List.of(new EbrCommands.Form(
                "MIX_RECORD", "配制工序电子批记录", operationDef, "1.0", 1, List.of(field))),
            List.of(), List.of(), List.of());
        JsonNode template = ebr.create(new EbrCommands.Create(
            packageVersionId, "KCL30-EBR-"+runTag,"Test eBR template"), key());
        template = ebr.save(id(template),
            new EbrCommands.Save(0L, "建立氯化钾溶液配制电子批记录", definition), null, key());
        template = ebr.command(id(template), "SUBMIT",
            new EbrCommands.Command(template.path("versionNo").asLong(), "提交 eBR 模板审核"), null, key());
        as(approver);
        template = ebr.command(id(template), "APPROVE",
            new EbrCommands.Command(template.path("versionNo").asLong(), "独立审核 eBR 模板"), null, key());
        template = ebr.command(id(template), "PUBLISH",
            new EbrCommands.Command(template.path("versionNo").asLong(), "发布本批执行模板"), null, key());
        templateVersionId = id(template);
    }

    private String createFinishedQualitySpecification() {
        as(analyst);
        var root = qc.create(new QcCommands.CreateSpecification(
            finishedMaterialId, "FG-KCL-QC-"+runTag,
            "氯化钾溶液成品质量标准", "建立成品检验冻结标准"), key());
        var draft = qc.createVersion(root.id(), new QcCommands.CreateVersion(
            1, List.of(new QcCommands.Item(
                "ASSAY", "氯化钾含量", true, "NUMERIC",
                "98.0", "102.0", "7", null, "HPLC", "2025")),
            "依据医院制剂质量标准建立成品检验项目"), key());
        as(approver);
        return qc.approveVersion(draft.id(),
            new QcCommands.SignVersion(draft.versionNo(), "独立复核并批准成品质量标准", "demo-reauth"),
            tokenNode(draft.versionNo()), key()).id();
    }

    private String createScale() {
        as(author);
        return id(equipment.create(new EquipmentCommands.Create(
            "SCALE-KCL-"+runTag, "配制称量电子秤", "PRODUCTION_SCALE",
            LocalDate.now(ZoneOffset.UTC).plusMonths(6), "制剂室称量间"), key()));
    }

    private JsonNode createDraftBatch(String batchNo, String orderNo) {
        as(author);
        JsonNode order = production.createOrder(body(
            "orderNo", orderNo, "productId", productId,
            "plannedQty", "1000", "unitId", productUnit,
            "plannedDate", LocalDate.now(ZoneOffset.UTC).plusDays(1).toString(),
            "reason", "根据医院制剂生产计划创建生产订单"), key());
        return production.createBatch(body(
            "batchNo", batchNo, "productionOrderId", id(order),
            "productId", productId, "plannedQty", "1000", "unitId", productUnit,
            "plannedDate", LocalDate.now(ZoneOffset.UTC).plusDays(1).toString(),
            "reason", "创建氯化钾溶液生产批"), key());
    }

    private JsonNode createApprovedBatch(String batchNo, String orderNo) {
        JsonNode batch = createDraftBatch(batchNo, orderNo);
        as(author);
        JsonNode plan = plans.create(body(
            "mainBatchId", id(batch),
            "finishedMaterialId", finishedMaterialId,
            "qcSpecificationVersionId", finishedSpecVersionId,
            "balanceRules", List.of(Map.of(
                "balanceCode", "FINISHED_OUTPUT",
                "basis", "BATCH",
                "unitId", productUnit,
                "formulaExpr", Map.of(
                    "dslVersion", 1,
                    "expected", Map.of("sum", List.of("OUTPUT")),
                    "actual", Map.of("sum", List.of("OUTPUT","LOSS")),
                    "metric", "DIFFERENCE_PCT"),
                "toleranceLow", "-1",
                "toleranceHigh", "1",
                "checkPoint", "BATCH_COMPLETE")),
            "reason", "建立本批成品质量与物料平衡计划"), key());
        as(qa);
        plans.approve(id(plan), body(
            "versionNo", plan.path("versionNo").asLong(),
            "reason", "QA 独立审核本批质量计划",
            "signature", Map.of("reauthToken","demo-reauth")), token(plan), key());
        as(author);
        return production.releaseBatch(id(batch), body(
            "processPackageId", packageVersionId,
            "ebrTemplateVersionId", templateVersionId,
            "reason", "冻结已批准工艺、eBR 和质量计划并下达生产"),
            token(batch), key());
    }

    private JsonNode reserveIssueAndStart(JsonNode batch) {
        as(author);
        String batchId = id(batch);
        formulaItemId = productionQuery.batch(1, Long.parseLong(batchId))
            .snapshot().path("process").path("formula").path("items").get(0)
            .path("formulaItemId").asText();
        stock.reserve(batchId, body(
            "items", List.of(Map.of(
                "formulaItemId", formulaItemId,
                "materialLotId", lot,
                "reservedQty", "100",
                "unitId", rawUnit)),
            "reason", "按冻结处方预留已放行氯化钾原料"), token(batch), key());
        JsonNode issue = stock.createIssue(body(
            "mainBatchId", batchId,
            "issueNo", "ISS-KCL-"+batch.path("batchNo").asText(),
            "items", List.of(Map.of(
                "formulaItemId", formulaItemId,
                "materialLotId", lot,
                "issuedQty", "100",
                "unitId", rawUnit)),
            "reason", "按生产批冻结处方发料"), key());
        as(warehouseActor);
        stock.confirmIssue(id(issue), body(
            "reason", "仓储复核物料、批号和数量后确认出库"), token(issue), key());
        as(author);
        return production.startBatch(batchId, body(
            "reason", "物料齐套并完成发料，开始生产"), token(batch), key());
    }

    private JsonNode completeProduction(JsonNode batch, String finishedLotNo) {
        as(author);
        String batchId = id(batch);
        String executionId = batch.path("executionUnits").get(0).path("id").asText();
        JsonNode op = execution.operations(executionId).getFirst();
        op = execution.start(id(op), body("reason","开始氯化钾溶液配制工序"), token(op), key());

        JsonNode exec = production.execution(executionId);
        JsonNode weighed = weighing.createWeighing(body(
            "executionUnitId", executionId,
            "materialLotId", lot,
            "formulaItemId", formulaItemId,
            "targetQty", "100",
            "actualQty", "100",
            "unitId", rawUnit,
            "scaleEquipmentId", scaleId,
            "versionNo", exec.path("versionNo").asLong(),
            "reason", "按冻结处方称量氯化钾 100 g"), key());
        as(reviewer);
        weighed = weighing.verifyWeighing(id(weighed), body(
            "reason", "第二人复核物料、批号、称量值与电子秤状态",
            "reauthToken", "demo-reauth"), token(weighed), key());

        as(author);
        weighing.createCharge(body(
            "executionUnitId", executionId,
            "operationExecutionId", id(op),
            "materialLotId", lot,
            "weighingRecordId", id(weighed),
            "chargedQty", "100",
            "unitId", rawUnit,
            "versionNo", op.path("versionNo").asLong(),
            "reason", "复核后投入配制罐"), key());

        JsonNode current = production.batch(batchId);
        quantities.record(batchId, body(
            "versionNo", current.path("versionNo").asLong(),
            "eventType", "OUTPUT",
            "amount", "1000",
            "unitId", productUnit,
            "sourceRef", "OUTPUT-"+batch.path("batchNo").asText(),
            "lotNo", finishedLotNo,
            "locationId", "7",
            "productionDate", LocalDate.now(ZoneOffset.UTC).toString(),
            "expiryDate", LocalDate.now(ZoneOffset.UTC).plusMonths(6).toString(),
            "reason", "记录配制完成后实际成品产出",
            "signature", Map.of("reauthToken","demo-reauth")), token(current), key());

        current = production.batch(batchId);
        quantities.record(batchId, body(
            "versionNo", current.path("versionNo").asLong(),
            "eventType", "LOSS",
            "amount", "0",
            "unitId", productUnit,
            "sourceRef", "LOSS-"+batch.path("batchNo").asText(),
            "reason", "本批无额外工艺损耗",
            "signature", Map.of("reauthToken","demo-reauth")), token(current), key());
        current = production.batch(batchId);
        balances.recalculate(batchId, body(
            "versionNo", current.path("versionNo").asLong(),
            "reason", "按实际产出和损耗重新计算物料平衡"), token(current), key());

        JsonNode latestBatch = production.batch(batchId);
        as(samplerActor);
        JsonNode sample = productionQuality.createSample(body(
            "investigationScope", "PRODUCTION",
            "mainBatchId", batchId,
            "sampleNo", "IPC-"+batch.path("batchNo").asText(),
            "sampleType", "TEST_SAMPLE",
            "materialLotId", latestBatch.path("finishedLotId").asText(),
            "quantity", "10",
            "unitId", productUnit,
            "sourceRef", "FINISHED-BULK",
            "reason", "生产完成后采集成品质量检验样品"), key());
        as(analyst);
        sample = productionQuality.receiveSample(id(sample), body(
            "versionNo", sample.path("versionNo").asLong(),
            "reason", "QC 接收生产成品样品"), token(sample), key());
        String item = productionQuery.batch(1, Long.parseLong(batchId))
            .snapshot().path("qualityPlan").path("specification").path("items").get(0)
            .path("specificationItemId").asText();
        JsonNode test = productionQuality.createTest(body(
            "sampleId", id(sample),
            "specificationItemId", item,
            "reason", "执行冻结成品质量标准含量检验"), key());
        test = productionQuality.result(id(test), false, body(
            "versionNo", test.path("versionNo").asLong(),
            "resultNumeric", "100.1",
            "reason", "氯化钾含量检验结果",
            "signature", Map.of("reauthToken","demo-reauth")), token(test), key());
        as(reviewer);
        productionQuality.review(id(test), body(
            "versionNo", test.path("versionNo").asLong(),
            "resultRevisionId", test.path("currentResultRevisionId").asText(),
            "disposition", "CONFIRMED",
            "reason", "QC 独立复核原始数据和结果",
            "signature", Map.of("reauthToken","demo-reauth")), token(test), key());

        as(author);
        JsonNode form = runtime.forms(executionId).get(0);
        runtime.command("SAVE", id(form), new com.hospital.mes.ebr.domain.EbrRuntimeCommands.Save(
            form.path("versionNo").asLong(),
            "记录实际配制观察结果",
            List.of(new com.hospital.mes.ebr.domain.EbrRuntimeCommands.Value(
                "OBS", "", json.getNodeFactory().textNode("溶液澄清，无可见异物，配制过程正常"), null, null))),
            token(form), key());
        JsonNode saved = runtime.forms(executionId).get(0);
        runtime.command("SUBMIT", id(saved),
            new com.hospital.mes.ebr.domain.EbrRuntimeCommands.Command(
                saved.path("versionNo").asLong(), "提交配制电子批记录"),
            token(saved), key());

        JsonNode latestOp = execution.operation(1, Long.parseLong(id(op)));
        execution.complete(id(latestOp), body(
            "reason", "称量、投料、检验和 eBR 记录均完成"), token(latestOp), key());
        JsonNode ready = production.batch(batchId);
        return production.completeProduction(batchId, body(
            "reason", "生产执行、质量检验和物料平衡门禁均通过"), token(ready), key());
    }

    private JsonNode completeFinishedChain(JsonNode completed) {
        as(author);
        JsonNode inbound = finishedGoods.createInbound(body(
            "requestNo", "FIR-"+completed.path("batchNo").asText(),
            "mainBatchId", id(completed),
            "reason", "生产完成后申请成品待检入库"), key());
        inbound = finishedGoods.inboundAction(id(inbound), "submit", body(
            "versionNo", inbound.path("versionNo").asLong(),
            "reason", "提交成品生产入库申请"), token(inbound), key());
        as(warehouseActor);
        inbound = finishedGoods.inboundAction(id(inbound), "confirm", body(
            "versionNo", inbound.path("versionNo").asLong(),
            "locationId", "7",
            "reason", "独立复核批号、数量并确认进入成品待检区",
            "signature", Map.of("reauthToken","demo-reauth")), token(inbound), key());

        as(author);
        JsonNode request = finishedInspection.createRequest(body(
            "inspectionRequestNo", "FIQ-"+completed.path("batchNo").asText(),
            "inboundRequestId", id(inbound),
            "reason", "成品入库后发起正式请验"), key());
        request = finishedInspection.requestAction(id(request), "submit", body(
            "versionNo", request.path("versionNo").asLong(),
            "reason", "提交成品请验"), token(request), key());
        as(analyst);
        request = finishedInspection.requestAction(id(request), "accept", body(
            "versionNo", request.path("versionNo").asLong(),
            "reason", "QC 接收成品请验"), token(request), key());

        as(samplerActor);
        JsonNode sampling = finishedInspection.sample(id(request), body(
            "versionNo", request.path("versionNo").asLong(),
            "samplingNo", "FSR-"+completed.path("batchNo").asText(),
            "sampleNo", "FSM-"+completed.path("batchNo").asText(),
            "sampleType", "TEST_SAMPLE",
            "samplingLocation", "成品待检区 FG-QC",
            "quantity", "10",
            "unitId", productUnit,
            "samplingMethod", "按成品取样规程随机抽取代表性样品",
            "reason", "完成成品正式取样",
            "signature", Map.of("reauthToken","demo-reauth")), token(request), key());

        JsonNode sample = productionQuality.get(
            ProductionQualityService.SAMPLE, sampling.path("sampleId").asText());
        as(analyst);
        sample = productionQuality.receiveSample(id(sample), body(
            "versionNo", sample.path("versionNo").asLong(),
            "reason", "QC 接收成品检验样品"), token(sample), key());
        String item = request.path("specificationSnapshot").path("specification")
            .path("items").get(0).path("specificationItemId").asText();
        JsonNode test = productionQuality.createTest(body(
            "sampleId", id(sample),
            "specificationItemId", item,
            "reason", "按冻结成品标准执行含量检验"), key());
        test = productionQuality.result(id(test), false, body(
            "versionNo", test.path("versionNo").asLong(),
            "resultNumeric", "100.0",
            "reason", "成品含量检验原始结果",
            "signature", Map.of("reauthToken","demo-reauth")), token(test), key());
        as(reviewer);
        productionQuality.review(id(test), body(
            "versionNo", test.path("versionNo").asLong(),
            "resultRevisionId", test.path("currentResultRevisionId").asText(),
            "disposition", "CONFIRMED",
            "reason", "独立复核成品检验结果",
            "signature", Map.of("reauthToken","demo-reauth")), token(test), key());

        as(analyst);
        request = finishedInspection.getRequest(id(request));
        JsonNode report = finishedInspection.generateReport(id(request), body(
            "versionNo", request.path("versionNo").asLong(),
            "reportNo", "FREP-"+completed.path("batchNo").asText(),
            "reason", "汇总成品正式检验结果生成报告"), token(request), key());
        as(reviewer);
        report = finishedInspection.approveReport(id(report), body(
            "versionNo", report.path("versionNo").asLong(),
            "reason", "QC 独立审核成品检验报告",
            "signature", Map.of("reauthToken","demo-reauth")), token(report), key());

        as(qa);
        JsonNode pending = production.submitQa(id(completed), body(
            "reason", "成品检验报告完成，提交 QA 批审核"), token(completed), key());
        JsonNode review = finishedQa.review(id(pending));
        JsonNode decision = finishedQa.decide(body(
            "mainBatchId", id(pending),
            "finishedLotId", pending.path("finishedLotId").asText(),
            "versionNo", review.path("versionNo").asLong(),
            "decision", "RELEASED",
            "releaseBasis", "FULL_INSPECTION",
            "reviewDigest", review.path("reviewDigest").asText(),
            "reason", "批记录、检验报告和放行门禁均满足，批准成品放行",
            "signature", Map.of("reauthToken","demo-reauth")), token(pending), key());

        JsonNode released = production.batch(id(pending));
        JsonNode lotView = stockQuery.materialLot(1, released.path("finishedLotId").asLong());
        as(warehouseActor);
        wms.move(new InventoryMove(
            released.path("finishedLotId").asText(), "7", null, "8", null,
            "1000", productUnit, lotView.path("versionNo").asLong(),
            "QA 放行后由待检区转入成品放行区"), token(lotView), key());

        as(author);
        JsonNode shipment = finishedGoods.createShipment(body(
            "shipmentNo", "SHIP-"+released.path("batchNo").asText(),
            "mainBatchId", id(released),
            "locationId", "8",
            "quantity", "200",
            "unitId", productUnit,
            "receivingParty", "院内住院药房",
            "reason", "按院内调拨计划发出已放行制剂"), key());
        as(warehouseActor);
        finishedGoods.shipmentAction(id(shipment), "confirm", body(
            "versionNo", shipment.path("versionNo").asLong(),
            "reason", "复核成品放行状态、批号和库存后确认发货",
            "signature", Map.of("reauthToken","demo-reauth")), token(shipment), key());
        return production.batch(id(released));
    }

    private String tokenNode(long version) { return "\"" + version + "\""; }
}
