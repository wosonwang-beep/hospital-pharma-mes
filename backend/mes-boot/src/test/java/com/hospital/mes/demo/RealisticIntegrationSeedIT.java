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
import com.hospital.mes.masterdata.application.MaterialSupplierService;
import com.hospital.mes.masterdata.application.SupplierCommands;
import com.hospital.mes.masterdata.application.SupplierService;
import com.hospital.mes.qc.application.QcSpecificationService;
import com.hospital.mes.qc.domain.QcCommands;
import com.hospital.mes.qms.application.IncomingQualityService;
import com.hospital.mes.security.password.PasswordService;
import com.hospital.mes.wms.application.WmsService;
import com.hospital.mes.wms.domain.WmsCommands.ReceiptCreate;
import com.hospital.mes.wms.domain.WmsCommands.ReceiptItemInput;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
    "mes.qualification.required-codes.sample-execute=MES_DEMO_GMP",
    "mes.qualification.required-codes.test-execute=MES_DEMO_GMP",
    "mes.qualification.required-codes.test-review=MES_DEMO_GMP",
    "mes.qualification.required-codes.qa-release=MES_DEMO_GMP"
})
@ActiveProfiles("ci")
@Transactional
@Commit
class RealisticIntegrationSeedIT {
    @Autowired SupplierService suppliers;
    @Autowired MaterialSupplierService materialSuppliers;
    @Autowired WmsService wms;
    @Autowired QcSpecificationService qc;
    @Autowired IncomingQualityService incoming;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    @Autowired PasswordService passwords;

    @MockitoBean CurrentPlatformContextResolver contexts;
    @MockitoBean ReauthenticationPort reauth;

    Set<String> permissions;
    final Map<String,String> approvedSpecVersions = new HashMap<>();
    long masterActor, warehouseActor, samplerActor, analystActor, reviewerActor, qaActor;
    String runTag;

    record MaterialCase(
        String materialId, String materialCode, String materialName,
        String supplierId, String unitId, String receivedQty,
        String packageSpec, long packageCount,
        String sampleQty, String testSampleQty, String retentionQty,
        String lower, String upper, String passValue
    ) {}
    record LotChain(MaterialCase material, String lotId, String specVersionId, JsonNode receipt) {}

    @Test
    void seedRealisticIntegratedData() {
        runTag = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        permissions = new HashSet<>(jdbc.queryForList(
            "SELECT permission_code FROM sys_permission WHERE enabled=TRUE", String.class));
        when(reauth.consume(anyString(), any())).thenReturn(
            new ConsumedReauthentication(Instant.now(), "PASSWORD"));

        masterActor = actor("demo.master", "林静（主数据）");
        warehouseActor = actor("demo.warehouse", "李倩（仓储）");
        samplerActor = actor("demo.sampler", "陈晨（取样）");
        analystActor = actor("demo.qc.analyst", "周敏（QC检验）");
        reviewerActor = actor("demo.qc.reviewer", "王睿（QC复核）");
        qaActor = actor("demo.qa", "赵宁（QA）");

        var kcl = new MaterialCase("1","KCL","氯化钾","1","1","50000","25 kg/桶",2,"50","40","10","99.0","101.0","99.6");
        var glycerin = new MaterialCase("4","GLYCERIN","甘油","4","3","50000","25 L/桶",2,"100","80","20","99.0","101.0","99.4");
        var ethanol = new MaterialCase("3","ETHANOL_95","95%酒精","3","3","20000","20 L/桶",1,"100","80","20","94.0","96.0","95.2");
        var sucrose = new MaterialCase("10","SUCROSE","蔗糖","5","2","50","25 kg/袋",2,"0.20","0.15","0.05","99.0","100.5","99.8");
        var benzoate = new MaterialCase("11","SODIUM_BENZOATE","苯甲酸钠","6","2","25","25 kg/袋",1,"0.10","0.08","0.02","99.0","100.5","99.4");

        for (var c : List.of(kcl,glycerin,ethanol,sucrose,benzoate)) prepareApprovedSource(c);

        // A: 完整放行
        var releasedKcl = receive(kcl, "RM-KCL-"+runTag+"-01", "QF-KCL-260923", "REC-"+runTag+"-001", "PO-20260928-017");
        var rqA = acceptedRequest(releasedKcl, "氯化钾原料入厂全检");
        var smA = collectedAndReceivedSample(releasedKcl, rqA);
        var tkA = startedTask(rqA, smA);
        var rsA = measuredResult(tkA, releasedKcl, kcl.passValue(), "PASS");
        review(tkA, rsA);
        var rpA = approvedReport(rqA, "氯化钾入厂检验报告");
        releaseLot(releasedKcl, rpA);

        // B: 请验已受理，待取样
        var glycerinLot = receive(glycerin, "RM-GLY-"+runTag+"-01", "BY-GLY-260930", "REC-"+runTag+"-002", "PO-20261001-006");
        var rqB = acceptedRequest(glycerinLot, "甘油原料入厂全检");

        // C: 已取样并接收，待检
        var ethanolLot = receive(ethanol, "RM-ETH-"+runTag+"-01", "AT-ETH-261002", "REC-"+runTag+"-003", "PO-20261002-011");
        var rqC = acceptedRequest(ethanolLot, "95%酒精入厂全检");
        var smC = collectedAndReceivedSample(ethanolLot, rqC);

        // D: 检验中
        var sucroseLot = receive(sucrose, "RM-SUC-"+runTag+"-01", "GZ-SUC-260925", "REC-"+runTag+"-004", "PO-20260930-025");
        var rqD = acceptedRequest(sucroseLot, "蔗糖辅料入厂全检");
        var smD = collectedAndReceivedSample(sucroseLot, rqD);
        var tkD = startedTask(rqD, smD);

        // E: 检验报告已批准，待 QA 最终物料放行
        var benzoateLot = receive(benzoate, "RM-SB-"+runTag+"-01", "HY-SB-261001", "REC-"+runTag+"-005", "PO-20261001-019");
        var rqE = acceptedRequest(benzoateLot, "苯甲酸钠辅料入厂全检");
        var smE = collectedAndReceivedSample(benzoateLot, rqE);
        var tkE = startedTask(rqE, smE);
        var rsE = measuredResult(tkE, benzoateLot, benzoate.passValue(), "PASS");
        review(tkE, rsE);
        var rpE = approvedReport(rqE, "苯甲酸钠入厂检验报告");

        // F: 真实 OOS，保留开放调查
        var oosKcl = receive(kcl, "RM-KCL-"+runTag+"-02", "QF-KCL-261003", "REC-"+runTag+"-006", "PO-20261003-023");
        var rqF = acceptedRequest(oosKcl, "氯化钾原料异常批全检");
        var smF = collectedAndReceivedSample(oosKcl, rqF);
        var tkF = startedTask(rqF, smF);
        var rsF = measuredResult(tkF, oosKcl, "97.8", "FAIL");
        review(tkF, rsF);

        Map<String,Object> summary = new LinkedHashMap<>();
        summary.put("runTag", runTag);
        summary.put("releasedKclLot", releasedKcl.lotId());
        summary.put("pendingSamplingRequest", id(rqB));
        summary.put("sampleReceived", id(smC));
        summary.put("inspectionInProgress", id(tkD));
        summary.put("pendingQaReport", id(rpE));
        summary.put("oosLot", oosKcl.lotId());
        summary.put("oosResult", id(rsF));
        summary.put("receipts", count("wms_material_receipt"));
        summary.put("requests", count("qms_inspection_request"));
        summary.put("samples", count("qms_sample"));
        summary.put("reports", count("qms_inspection_report"));
        summary.put("releaseDecisions", count("qms_release_decision"));
        summary.put("deviations", count("qms_deviation"));
        try { System.out.println("REALISTIC_SEED_SUMMARY=" + json.writeValueAsString(summary)); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }

    private long count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM "+table, Long.class);
    }

    private long actor(String login, String displayName) {
        List<Long> existing = jdbc.queryForList(
            "SELECT id FROM sys_user WHERE login_name_normalized=?", Long.class, login);
        long id;
        if (existing.isEmpty()) {
            jdbc.update("INSERT INTO sys_user(login_name,login_name_normalized,display_name,password_hash,enabled,must_change_password) VALUES(?,?,?,?,TRUE,FALSE)",
                login, login, displayName, passwords.hash("DemoUser@2026"));
            id = jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?", Long.class, login);
        } else {
            id = existing.getFirst();
        }
        Integer q = jdbc.queryForObject(
            "SELECT COUNT(*) FROM md_qualification WHERE org_id=1 AND user_id=? AND qualification_code='MES_DEMO_GMP'",
            Integer.class, id);
        if (q == null || q == 0) {
            jdbc.update("INSERT INTO md_qualification(org_id,created_by,updated_by,user_id,qualification_code,valid_from,valid_to,status) VALUES(1,?,?,?,'MES_DEMO_GMP',?,?,'ACTIVE')",
                id,id,id,LocalDate.now(ZoneOffset.UTC).minusDays(30),LocalDate.now(ZoneOffset.UTC).plusYears(1));
        }
        return id;
    }

    private void as(long actor, String role) {
        when(contexts.current()).thenReturn(new CurrentPlatformContext(
            1, actor, Set.of(role), permissions, "demo-seed-session", "demo-seed-"+runTag));
    }

    private void prepareApprovedSource(MaterialCase c) {
        as(masterActor, "MASTER_DATA");
        JsonNode supplier = suppliers.get(c.supplierId());
        if ("UNAPPROVED".equals(supplier.path("qualificationStatus").asText())) {
            suppliers.command(c.supplierId(),
                new SupplierCommands.Update("QUALIFY", supplier.path("versionNo").asLong(),
                    "年度供应商资质复核通过，用于医院制剂原辅料采购", null, null),
                null, key());
        }
        JsonNode material = materialSuppliers.get(c.materialId());
        boolean ready = false;
        for (JsonNode relation : material.path("suppliers")) {
            if (c.supplierId().equals(relation.path("supplierId").asText())
                && relation.path("approved").asBoolean()
                && relation.path("preferred").asBoolean()) ready = true;
        }
        if (!ready) {
            materialSuppliers.assign(c.materialId(),
                new SupplierCommands.Assign(material.path("versionNo").asLong(),
                    "确认合格供应商并设为该物料首选来源",
                    List.of(new SupplierCommands.Relationship(
                        c.supplierId(), true, true,
                        LocalDate.now(ZoneOffset.UTC).plusYears(1), null))),
                null, key());
        }
    }

    private LotChain receive(MaterialCase c, String lotNo, String supplierLotNo,
                             String receiptNo, String poNo) {
        String specVersion = approvedSpec(c);
        as(warehouseActor, "WAREHOUSE");
        var item = new ReceiptItemInput(
            c.materialId(), lotNo, supplierLotNo, null,
            LocalDate.now(ZoneOffset.UTC).minusDays(14).toString(),
            LocalDate.now(ZoneOffset.UTC).plusYears(2).toString(),
            LocalDate.now(ZoneOffset.UTC).plusYears(1).toString(),
            c.receivedQty(), c.unitId(), c.packageSpec(), c.packageCount(),
            "1", null, true,true,true,true,true);
        JsonNode receipt = wms.createReceipt(new ReceiptCreate(
            receiptNo, c.supplierId(), poNo, "DN-"+receiptNo.substring(4),
            "1", true, List.of(item)), key(), true);
        String lotId = receipt.path("items").get(0).path("materialLotId").asText();
        return new LotChain(c, lotId, specVersion, receipt);
    }

    private String approvedSpec(MaterialCase c) {
        String existing = approvedSpecVersions.get(c.materialId());
        if (existing != null) return existing;
        as(analystActor, "QC_ANALYST");
        var root = qc.create(new QcCommands.CreateSpecification(
            c.materialId(), "RM-"+c.materialCode()+"-"+runTag,
            c.materialName()+"入厂质量标准", "建立本批集成测试冻结质量标准"), key());
        var draft = qc.createVersion(root.id(), new QcCommands.CreateVersion(
            1, List.of(new QcCommands.Item(
                "ASSAY", "含量", true, "NUMERIC",
                c.lower(), c.upper(), "7", null, "PHARMACOPOEIA", "2025")),
            "依据现行质量标准建立入厂检验项目"), key());
        as(reviewerActor, "QC_REVIEWER");
        String versionId = qc.approveVersion(draft.id(),
            new QcCommands.SignVersion(draft.versionNo(), "独立复核并批准质量标准", "demo-reauth"),
            quote(draft.versionNo()), key()).id();
        approvedSpecVersions.put(c.materialId(), versionId);
        return versionId;
    }

    private JsonNode acceptedRequest(LotChain chain, String reason) {
        as(analystActor, "QC_ANALYST");
        JsonNode r = incoming.createRequest(body(
            "materialLotId", chain.lotId(),
            "qcSpecificationVersionId", chain.specVersionId(),
            "requestType", "INITIAL",
            "reason", reason,
            "requestedQuantity", chain.material().sampleQty(),
            "requestedUnitId", chain.material().unitId(),
            "requestedPackageCount", 1,
            "requestedDate", LocalDate.now(ZoneOffset.UTC).toString(),
            "priority", "NORMAL"), key());
        r = incoming.requestAction(id(r), "submit", body("reason", "仓储提交来料请验"), token(r), key());
        return incoming.requestAction(id(r), "accept", body("reason", "QC 接收并安排取样"), token(r), key());
    }

    private JsonNode collectedAndReceivedSample(LotChain chain, JsonNode request) {
        as(analystActor, "QC_ANALYST");
        JsonNode task = incoming.createSampling(body(
            "inspectionRequestId", id(request),
            "inspectionRequestItemId", request.path("items").get(0).path("id").asText(),
            "samplingPlan", "按包装件随机抽取代表性样品，留样与检验样分开封装",
            "requiredPackageCount", 1,
            "reason", "制定来料取样计划"), key());
        task = incoming.samplingAction(id(task), "assign",
            body("assignedTo", Long.toString(samplerActor), "reason", "指派取样人员"), token(task), key());

        as(samplerActor, "SAMPLER");
        task = incoming.samplingAction(id(task), "start",
            body("reason", "在待验区开始受控取样"), token(task), key());
        task = incoming.samplingAction(id(task), "details", body(
            "containerNo", "PKG-01",
            "samplingPoint", "原包装中上部",
            "sampleQuantity", chain.material().sampleQty(),
            "unitId", chain.material().unitId(),
            "sampledAt", Instant.now().minusSeconds(30).toString(),
            "packageResealed", true,
            "samples", List.of(
                Map.of("sampleType","TEST_SAMPLE","quantity",chain.material().testSampleQty(),"storageLocation","QC待检样品柜"),
                Map.of("sampleType","RETENTION_SAMPLE","quantity",chain.material().retentionQty(),"storageLocation","留样室")
            ),
            "reason", "完成代表性取样并重新密封"), token(task), key());
        task = incoming.samplingAction(id(task), "complete",
            body("reason", "取样记录核对无误", "reauthToken", "demo-reauth"), token(task), key());

        JsonNode sample = task.path("details").get(0).path("samples").get(0);
        return incoming.sampleAction(id(sample), "receive", body(
            "receivedAt", Instant.now().toString(),
            "storageLocation", "QC待检样品柜 A-01",
            "reason", "QC 接收检验样品"), token(sample), key());
    }

    private JsonNode startedTask(JsonNode request, JsonNode sample) {
        as(analystActor, "QC_ANALYST");
        JsonNode task = incoming.createTask(body(
            "inspectionRequestId", id(request),
            "sampleId", id(sample),
            "reason", "按冻结质量标准执行含量检验"), key());
        task = incoming.taskAction(id(task), "assign",
            body("assignedTo", Long.toString(analystActor), "reason", "指派检验人员"), token(task), key());
        return incoming.taskAction(id(task), "start",
            body("reason", "样品与标准核对后开始检验"), token(task), key());
    }

    private JsonNode measuredResult(JsonNode task, LotChain chain, String value, String conclusion) {
        as(analystActor, "QC_ANALYST");
        JsonNode execution = incoming.execution(
            task.path("items").get(0).path("id").asText(), false,
            body("rawData", Map.of("instrument","HPLC-01","reading",value),
                 "calculationInput", Map.of("dilutionFactor","1.0000"),
                 "startedAt", Instant.now().minusSeconds(20).toString(),
                 "completedAt", Instant.now().minusSeconds(5).toString(),
                 "performedBy", Long.toString(analystActor),
                 "reason", "完成原始数据采集与计算"),
            token(task), key());
        JsonNode current = incoming.get("qms_inspection_task", "qms:test:view", id(task));
        return incoming.result(task.path("items").get(0).path("id").asText(), false,
            body("testExecutionId", id(execution),
                 "resultNumeric", value,
                 "resultUnitId", "7",
                 "resultConclusion", conclusion,
                 "reauthToken", "demo-reauth"),
            token(current), key());
    }

    private JsonNode review(JsonNode task, JsonNode result) {
        as(analystActor, "QC_ANALYST");
        JsonNode current = incoming.get("qms_inspection_task", "qms:test:view", id(task));
        current = incoming.taskAction(id(current), "submit-review",
            body("reason", "检验结果提交独立复核"), token(current), key());
        as(reviewerActor, "QC_REVIEWER");
        return incoming.taskAction(id(current), "review",
            body("resultRevisionIds", List.of(id(result)),
                 "reauthToken", "demo-reauth",
                 "reason", "复核原始记录、计算和结果结论"), token(current), key());
    }

    private JsonNode approvedReport(JsonNode request, String title) {
        as(analystActor, "QC_ANALYST");
        JsonNode report = incoming.createReport(body(
            "inspectionRequestId", id(request), "reason", title+"汇总"), key());
        as(reviewerActor, "QC_REVIEWER");
        report = incoming.reportAction(id(report), "review",
            body("reason", "QC 独立复核检验报告"), token(report), key());
        as(qaActor, "QA");
        return incoming.reportAction(id(report), "approve",
            body("reason", "QA 审核检验报告证据完整", "reauthToken", "demo-reauth"), token(report), key());
    }

    private JsonNode releaseLot(LotChain chain, JsonNode report) {
        as(qaActor, "QA");
        JsonNode gate = incoming.releaseReview(chain.lotId());
        return incoming.decideRelease(chain.lotId(), body(
            "decision", "RELEASED",
            "releaseBasis", "FULL_INSPECTION",
            "inspectionReportId", id(report),
            "reason", "检验合格且放行门禁全部满足，批准投入生产使用",
            "reauthToken", "demo-reauth"), token(gate), key());
    }

    private ObjectNode body(Object... pairs) {
        ObjectNode n = json.createObjectNode();
        for (int i=0;i<pairs.length;i+=2) n.set((String)pairs[i], json.valueToTree(pairs[i+1]));
        return n;
    }
    private String key() { return UUID.randomUUID().toString(); }
    private String id(JsonNode node) { return node.path("id").asText(); }
    private String token(JsonNode node) { return quote(node.path("versionNo").asLong()); }
    private String quote(long v) { return "\"" + v + "\""; }
}
