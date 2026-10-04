package com.hospital.mes.incoming;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.audit.signature.ConsumedReauthentication;
import com.hospital.mes.common.exception.MesException;
import com.hospital.mes.masterdata.application.UnitConversionService;
import com.hospital.mes.masterdata.application.UnitCommands;
import com.hospital.mes.masterdata.application.UnitConversionCommands;
import com.hospital.mes.qc.domain.QcCommands;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/** Remaining incoming boundaries; all records belong to the enclosing rollback transaction. */
class IncomingClosureIT extends IncomingQualityFixture {
    @Autowired UnitConversionService conversions;

    private void rejected(String code, Runnable action) {
        TransactionTemplate tx = new TransactionTemplate(transactions);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
        assertThatThrownBy(() -> tx.execute(s -> { action.run(); return null; }))
            .isInstanceOfSatisfying(MesException.class, e -> assertThat(e.code()).isEqualTo(code));
    }

    private JsonNode requestBody(String specification, String quantity, String requestedUnit, int packages) {
        return body("materialLotId", lot, "qcSpecificationVersionId", specification,
            "requestType", "INITIAL", "reason", "Closure request", "requestedQuantity", quantity,
            "requestedUnitId", requestedUnit, "requestedPackageCount", packages,
            "requestedDate", LocalDate.now(ZoneOffset.UTC).toString(), "priority", "NORMAL");
    }
    @Test void numericAndTextRequiredItemsRequireExactReviewAndAggregateOriginalSnapshots() {
        as(analyst);var spec=qc.create(new QcCommands.CreateSpecification(material,"MULTI"+suffix,"Two required incoming items","Closure"),key());
        var draft=qc.createVersion(spec.id(),new QcCommands.CreateVersion(1,List.of(
            new QcCommands.Item("ASSAY","Assay",true,"NUMERIC","1","3",unit,null,"ASSAY_METHOD","2"),
            new QcCommands.Item("APPEARANCE","Appearance",true,"TEXT",null,null,null,"CLEAR","VISUAL","1")),"Required numeric and text items"),key());
        as(approver);version=qc.approveVersion(draft.id(),new QcCommands.SignVersion(0L,"Approve multi-item standard","token"),"\"0\"",key()).id();as(analyst);
        var request=request();var sample=sample(request);var task=task(request,sample);assertThat(task.path("items")).hasSize(2);
        var numericItem=task.path("items").get(0);var textItem=task.path("items").get(1);
        // Locate by frozen type; aggregation order is not an authored business fact.
        for(var item:task.path("items")){if(item.path("resultType").asText().equals("NUMERIC"))numericItem=item;else textItem=item;}
        var execute=body("rawData",java.util.Map.of("readout","Instrument observation"),"calculationInput",java.util.Map.of(),"startedAt",Instant.now().minusSeconds(2).toString(),"completedAt",Instant.now().minusSeconds(1).toString(),"performedBy",Long.toString(analyst),"reason","Actual original observation");
        String numericId=id(numericItem),textId=id(textItem);
        var numericExecution=service.execution(numericId,false,execute,token(task),key());
        var current=get("qms_inspection_task",id(task));
        var numericBody=body("testExecutionId",id(numericExecution),"resultNumeric","2.000000001","resultUnitId",unit,"resultConclusion","PASS","reauthToken","token");
        var precisionTx=new TransactionTemplate(transactions);precisionTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
        assertThatThrownBy(()->precisionTx.execute(s->service.result(numericId,false,numericBody,token(current),key())))
            .isInstanceOf(IllegalArgumentException.class).hasMessage("Invalid field: resultNumeric");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_test_result_revision WHERE test_execution_id=?",Long.class,id(numericExecution))).isZero();
        numericBody.put("resultNumeric","2");var numeric=service.result(numericId,false,numericBody,token(current),key());
        var textExecution=service.execution(textId,false,execute,token(get("qms_inspection_task",id(task))),key());
        var incomplete=get("qms_inspection_task",id(task));
        rejected("REPORT_REQUIRED_RESULT_MISSING",()->service.taskAction(id(task),"submit-review",body("reason","Missing required text result"),token(incomplete),key()));
        var textBody=body("testExecutionId",id(textExecution),"resultText","CLEAR","resultNumeric","2","resultConclusion","PASS","reauthToken","token");
        rejected("RESULT_TYPE_MISMATCH",()->service.result(textId,false,textBody,token(incomplete),key()));
        textBody.remove("resultNumeric");var textResult=service.result(textId,false,textBody,token(incomplete),key());
        var submitted=service.taskAction(id(task),"submit-review",body("reason","All required results recorded"),token(get("qms_inspection_task",id(task))),key());as(reviewer);
        rejected("RESULT_REVIEW_MISMATCH",()->service.taskAction(id(task),"review",body("resultRevisionIds",List.of(id(numeric)),"reason","Incomplete review","reauthToken","token"),token(submitted),key()));
        service.taskAction(id(task),"review",body("resultRevisionIds",List.of(id(numeric),id(textResult)),"reason","Review all frozen required items","reauthToken","token"),token(submitted),key());
        var report=report(request);assertThat(report.path("items")).hasSize(2);
        assertThat(jdbc.queryForObject("SELECT CAST(r.qc_specification_version_id AS CHAR) FROM qms_inspection_report p JOIN qms_inspection_request r ON r.id=p.inspection_request_id WHERE p.id=?",String.class,id(report))).isEqualTo(version);
        assertThat(report.path("items").findValuesAsText("resultRevisionId")).containsExactlyInAnyOrder(id(numeric),id(textResult));
        assertThat(report.path("items").findValuesAsText("originalResultRevisionId")).containsExactlyInAnyOrder(id(numeric),id(textResult));
        assertThat(jdbc.queryForObject("SELECT lower_limit FROM qms_inspection_item WHERE id=?",java.math.BigDecimal.class,numericId)).isEqualByComparingTo("1");
        assertThat(jdbc.queryForObject("SELECT upper_limit FROM qms_inspection_item WHERE id=?",java.math.BigDecimal.class,numericId)).isEqualByComparingTo("3");
        assertThat(jdbc.queryForObject("SELECT method_version FROM qms_inspection_item WHERE id=?",String.class,numericId)).isEqualTo("2");
        assertThat(verifier.verify(1,numeric.path("signatureId").asLong())).isTrue();assertThat(verifier.verify(1,textResult.path("signatureId").asLong())).isTrue();
    }
    @Test void retentionSampleStorageLabelAndSignedDisposalPreserveSourceLineage() {
        var request=request();var testSample=sample(request);
        var samples=jdbc.queryForList("SELECT id FROM qms_sample WHERE sampling_task_id=(SELECT sampling_task_id FROM qms_sample WHERE id=?) AND sample_type='RETENTION_SAMPLE'",String.class,id(testSample));
        assertThat(samples).hasSize(1);var retained=get("qms_sample",samples.getFirst());String detailId=retained.path("samplingDetailId").asText();
        as(analyst);retained=service.sampleAction(id(retained),"receive",body("receivedAt",Instant.now().toString(),"storageLocation","Controlled retention store","reason","Receive retention sample"),token(retained),key());
        retained=service.sampleAction(id(retained),"retain",body("storageLocation","Controlled retention cabinet A","reason","Retain"),token(retained),key());
        var label=service.sampleLabel(id(retained),body("reason","Retention label"),token(retained),key());
        assertThat(label.path("materialLotId").asText()).isEqualTo(lot);assertThat(label.path("sampleType").asText()).isEqualTo("RETENTION_SAMPLE");
        assertThat(label.path("storageLocation").asText()).isEqualTo("Controlled retention cabinet A");assertThat(label.path("containerNo").asText()).isEqualTo("PK1");
        as(qa);var disposed=service.sampleAction(id(retained),"dispose",body("reason","Approved retention disposition","reauthToken","token"),token(retained),key());
        assertThat(disposed.path("status").asText()).isEqualTo("DISPOSED");assertThat(disposed.path("samplingDetailId").asText()).isEqualTo(detailId);
        assertThat(new java.math.BigDecimal(disposed.required("quantity").asText())).isEqualByComparingTo("0.2");
        assertThat(disposed.required("quantity")).isEqualTo(retained.required("quantity"));
        assertThat(disposed.required("unitId").asText()).isEqualTo(unit);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_sampling_detail WHERE id=?",Long.class,detailId)).isEqualTo(1);
        long disposalSignature=jdbc.queryForObject("SELECT id FROM gxp_signature WHERE object_type='SAMPLE_DISPOSAL' AND object_id=?",Long.class,id(disposed));
        assertThat(verifier.verify(1,disposalSignature)).isTrue();
    }
    @Test void missingRequiredResultCannotSubmitReviewAndPreservesTaskVersion() {
        var request=request();var sample=sample(request);var task=task(request,sample);
        String commandKey=key();long before=task.path("versionNo").asLong();
        rejected("REPORT_REQUIRED_RESULT_MISSING",()->service.taskAction(id(task),"submit-review",body("reason","Missing required original result"),token(task),commandKey));
        assertThat(get("qms_inspection_task",id(task)).path("status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(get("qms_inspection_task",id(task)).path("versionNo").asLong()).isEqualTo(before);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,commandKey)).isZero();
    }
    @Test void exhaustedRetestQuotaAndValidOriginalFailRemainBlockingAfterSignedClosure() {
        var request=request();var sample=sample(request);var originalTask=task(request,sample);
        var originalExecution=execution(originalTask);var fail=result(originalTask,originalExecution,"4","FAIL");review(originalTask,fail);
        String originalBytes=jdbc.queryForObject("SELECT CONCAT(result_numeric,':',result_conclusion,':',signature_id) FROM qms_test_result_revision WHERE id=?",String.class,id(fail));
        String investigation=jdbc.queryForObject("SELECT CAST(id AS CHAR) FROM qms_deviation WHERE original_result_revision_id=?",String.class,id(fail));
        as(reviewer);var inv=service.get("qms_deviation","qms:deviation:view",investigation);
        inv=service.deviationAction(investigation,"investigate",body("investigationScope","INCOMING_MATERIAL","investigationSummary","Investigate without invalidating the original factual failure","reason","Investigate"),token(inv),key());
        as(approver);inv=service.deviationAction(investigation,"decide",body("investigationScope","INCOMING_MATERIAL","decisionCode","AUTHORIZE_RETEST","disposition","One controlled retest only","authorizedRetestCount",1,"reauthToken","token","reason","Authorize bounded retest"),token(inv),key());
        as(analyst);var retestTask=task(request,sample);
        var command=body("originalExecutionId",id(originalExecution),"approvedInvestigationId",investigation,"rawData",java.util.Map.of("readout","Retest fact"),"calculationInput",java.util.Map.of(),"startedAt",Instant.now().minusSeconds(2).toString(),"completedAt",Instant.now().minusSeconds(1).toString(),"performedBy",Long.toString(analyst),"reason","Approved retest");
        String itemId=retestTask.path("items").get(0).path("id").asText();var execution=service.execution(itemId,true,command,token(retestTask),key());
        var current=get("qms_inspection_task",id(retestTask));String exhaustedKey=key();
        rejected("RETEST_LIMIT_EXCEEDED",()->service.execution(itemId,true,command,token(current),exhaustedKey));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_test_execution WHERE approved_investigation_id=?",Long.class,investigation)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,exhaustedKey)).isZero();
        var passing=result(retestTask,execution,"2","PASS");review(retestTask,passing);as(qa);
        var decided=service.get("qms_deviation","qms:deviation:view",investigation);
        var closed=service.deviationAction(investigation,"close",body("investigationScope","INCOMING_MATERIAL","selectedResultRevisionId",id(passing),"originalResultDisposition","VALID","reauthToken","token","reason","Retain valid original failure; retest does not waive it"),token(decided),key());
        assertThat(closed.path("status").asText()).isEqualTo("CLOSED");
        assertThat(verifier.verify(1,closed.path("closeSignatureId").asLong())).isTrue();
        assertThat(service.releaseReview(lot).path("gateReasons").toString()).contains("ORIGINAL_FAIL_UNRESOLVED");
        assertThat(queries.evaluate(1,Long.parseLong(lot),"PRODUCTION",Instant.now()).path("eligible").asBoolean()).isFalse();
        as(analyst);rejected("ORIGINAL_FAIL_UNRESOLVED",()->service.createReport(body("inspectionRequestId",id(request),"reason","Valid failure cannot be replaced by passing retest"),key()));
        assertThat(jdbc.queryForObject("SELECT CONCAT(result_numeric,':',result_conclusion,':',signature_id) FROM qms_test_result_revision WHERE id=?",String.class,id(fail))).isEqualTo(originalBytes);
        assertThat(verifier.verify(1,fail.path("signatureId").asLong())).isTrue();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_release_decision WHERE material_lot_id=?",Long.class,lot)).isZero();
    }

    @Test void requestRejectsDraftRetiredAndForeignMaterialWithoutWritingFacts() {
        String root = jdbc.queryForObject("SELECT specification_id FROM qc_specification_version WHERE id=?", String.class, version);
        var items = List.of(new QcCommands.Item("ASSAY", "Assay", true, "NUMERIC", "1", "3", unit, null, "METHOD", "1"));
        as(author);
        var draft = qc.createVersion(root, new QcCommands.CreateVersion(2, items, "Next standard"), key());
        rejected("QC_SPEC_NOT_SELECTABLE", () -> service.createRequest(requestBody(draft.id(), "10", unit, 2), key()));
        var otherMaterial = materials.create(json.convertValue(java.util.Map.of("materialCode", "FOREIGN" + suffix,
            "materialName", "Other test material", "materialType", "RAW", "baseUnitId", unit, "lotControlled", true),
            com.hospital.mes.masterdata.application.MaterialCommands.Create.class), key());
        var otherRoot = qc.create(new QcCommands.CreateSpecification(id(otherMaterial), "FOREIGNS" + suffix, "Other standard", "Scope"), key());
        var otherDraft = qc.createVersion(otherRoot.id(), new QcCommands.CreateVersion(1, items, "Definition"), key());
        as(approver);
        var foreign = qc.approveVersion(otherDraft.id(), new QcCommands.SignVersion(0L, "Independent approval", "token"), "\"0\"", key());
        as(analyst);
        rejected("QC_SPEC_MATERIAL_MISMATCH", () -> service.createRequest(requestBody(foreign.id(), "10", unit, 2), key()));
        as(approver);
        qc.retireVersion(version, new QcCommands.SignVersion(1L, "Retire old standard", "token"), "\"1\"", key());
        as(analyst);
        rejected("QC_SPEC_NOT_SELECTABLE", () -> service.createRequest(requestBody(version, "10", unit, 2), key()));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_inspection_request WHERE material_lot_id=?", Long.class, lot)).isZero();
        assertThat(jdbc.queryForObject("SELECT quality_status FROM md_material_lot WHERE id=?", String.class, lot)).isEqualTo("QUARANTINE");
    }

    @Test void convertedQuantityAndPackageBoundsPreserveFrozenRequestWhenNextStandardIsApproved() {
        as(author);
        String smaller = id(units.create(new UnitCommands.Create("SMALL" + suffix, "Smaller mass unit", "MASS", 3), key()));
        conversions.create(new UnitConversionCommands.Create(smaller, unit, "0.5", material), key());
        rejected("REQUEST_QUANTITY_EXCEEDED", () -> service.createRequest(requestBody(version, "20.002", smaller, 2), key()));
        rejected("REQUEST_QUANTITY_EXCEEDED", () -> service.createRequest(requestBody(version, "20", smaller, 3), key()));
        var request = service.createRequest(requestBody(version, "20", smaller, 2), key());
        String root = jdbc.queryForObject("SELECT specification_id FROM qc_specification_version WHERE id=?", String.class, version);
        var next = qc.createVersion(root, new QcCommands.CreateVersion(2,
            List.of(new QcCommands.Item("ASSAY", "Changed limits", true, "NUMERIC", "0", "9", unit, null, "METHOD", "2")), "Next limits"), key());
        as(approver);
        qc.approveVersion(next.id(), new QcCommands.SignVersion(0L, "Approve next", "token"), "\"0\"", key());
        assertThat(jdbc.queryForObject("SELECT qc_specification_version_id FROM qms_inspection_request WHERE id=?", String.class, id(request))).isEqualTo(version);
        assertThat(jdbc.queryForObject("SELECT requested_quantity FROM qms_inspection_request WHERE id=?", java.math.BigDecimal.class, id(request))).isEqualByComparingTo("20");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_inspection_request WHERE material_lot_id=?", Long.class, lot)).isEqualTo(1);
    }

    @Test void unreviewedResultsCannotGenerateReportAndCallerCannotSupplyIndependentResults() {
        as(analyst);
        var request = request();
        var sample = sample(request);
        var task = task(request, sample);
        var result = result(task, execution(task), "2", "PASS");
        rejected("REPORT_RESULT_UNREVIEWED", () -> service.createReport(body("inspectionRequestId", id(request), "reason", "Unreviewed"), key()));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_inspection_report WHERE inspection_request_id=?", Long.class, id(request))).isZero();
        review(task, result);
        as(analyst);
        TransactionTemplate tx = new TransactionTemplate(transactions);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
        assertThatThrownBy(() -> tx.execute(s -> service.createReport(body("inspectionRequestId", id(request),
            "reason", "Cannot supply results", "results", List.of(java.util.Map.of("value", "999"))), key())))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unknown field: results");
        var report = service.createReport(body("inspectionRequestId", id(request), "reason", "Use original reviewed facts"), key());
        assertThat(jdbc.queryForObject("SELECT result_revision_id FROM qms_inspection_report_item WHERE inspection_report_id=?", String.class, id(report))).isEqualTo(id(result));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_test_result_revision WHERE created_by=?", Long.class, analyst)).isEqualTo(1);
    }

    @Test void releaseRequiresIndependentActorPermissionAndReauthenticationFailureRollsBack() {
        as(analyst);
        var request = request();
        var sample = sample(request);
        var task = task(request, sample);
        var result = result(task, execution(task), "2", "PASS");
        review(task, result);
        var report = report(request);
        as(analyst);
        var gate = service.releaseReview(lot);
        var decision = body("decision", "RELEASED", "releaseBasis", "FULL_INSPECTION", "inspectionReportId", id(report),
            "reason", "Independent QA release", "reauthToken", "token");
        rejected("INDEPENDENT_REVIEW_REQUIRED", () -> service.decideRelease(lot, decision, token(gate), key()));
        as(qa);
        permissions.remove("qa:material-release:decide");
        as(qa);
        rejected("PERMISSION_DENIED", () -> service.decideRelease(lot, decision, token(gate), key()));
        permissions.add("qa:material-release:decide");
        as(qa);
        when(reauth.consume(anyString(), any())).thenThrow(new IllegalStateException("Injected release reauthentication failure"));
        String replayKey = key();
        TransactionTemplate tx = new TransactionTemplate(transactions);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
        assertThatThrownBy(() -> tx.execute(s -> service.decideRelease(lot, decision, token(gate), replayKey)))
            .hasMessageContaining("Injected release reauthentication failure");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_release_decision WHERE material_lot_id=?", Long.class, lot)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?", Long.class, replayKey)).isZero();
        assertThat(jdbc.queryForObject("SELECT quality_status FROM md_material_lot WHERE id=?", String.class, lot)).isNotEqualTo("RELEASED");
        doReturn(new ConsumedReauthentication(Instant.now(), "PASSWORD")).when(reauth).consume(anyString(), any());
        doAnswer(invocation -> {
            var command = (com.hospital.mes.audit.domain.AuditCommand) invocation.getArgument(0);
            if (command.action().equals("INCOMING_RELEASE_DECISION")) throw new IllegalStateException("Injected release audit failure");
            return invocation.callRealMethod();
        }).when(audit).append(any());
        assertThatThrownBy(() -> tx.execute(s -> service.decideRelease(lot, decision, token(gate), replayKey)))
            .hasMessageContaining("Injected release audit failure");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_release_decision WHERE material_lot_id=?", Long.class, lot)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?", Long.class, replayKey)).isZero();
        assertThat(jdbc.queryForObject("SELECT quality_status FROM md_material_lot WHERE id=?", String.class, lot)).isNotEqualTo("RELEASED");
        doCallRealMethod().when(audit).append(any());
        var released = service.decideRelease(lot, decision, token(gate), replayKey);
        assertThat(released.path("decision").asText()).isEqualTo("RELEASED");
        assertThat(verifier.verify(1, released.path("signatureId").asLong())).isTrue();
    }

    @Test void samplingAllocationAndUnapprovedRetestOrOtherPurposeCannotCreateSamples() {
        as(analyst);
        var request = request();
        var task = service.createSampling(body("inspectionRequestId", id(request), "inspectionRequestItemId", request.path("items").get(0).path("id").asText(),
            "samplingPlan", "Controlled closure sampling", "requiredPackageCount", 1, "reason", "Plan"), key());
        task = service.samplingAction(id(task), "assign", body("assignedTo", Long.toString(analyst), "reason", "Assign"), token(task), key());
        var active = service.samplingAction(id(task), "start", body("reason", "Start"), token(task), key());
        var detail = body("containerNo", "PK1", "sampleQuantity", "1", "unitId", unit, "sampledAt", Instant.now().minusSeconds(5).toString(),
            "packageResealed", true, "samples", List.of(java.util.Map.of("sampleType", "TEST_SAMPLE", "quantity", "0.9")), "reason", "Allocation");
        rejected("SAMPLE_QUANTITY_MISMATCH", () -> service.samplingAction(id(active), "details", detail, token(active), key()));
        detail.set("samples", json.valueToTree(List.of(java.util.Map.of("sampleType", "RETEST_SAMPLE", "quantity", "1"))));
        rejected("RETEST_NOT_APPROVED", () -> service.samplingAction(id(active), "details", detail, token(active), key()));
        detail.set("samples", json.valueToTree(List.of(java.util.Map.of("sampleType", "OTHER_APPROVED", "quantity", "1"))));
        rejected("SAMPLING_PLAN_APPROVAL_REQUIRED", () -> service.samplingAction(id(active), "details", detail, token(active), key()));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_sampling_detail WHERE sampling_task_id=?", Long.class, id(active))).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_sample WHERE sampling_task_id=?", Long.class, id(active))).isZero();
    }
}
