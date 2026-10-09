package com.hospital.mes.production;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.masterdata.application.MaterialCommands;
import com.hospital.mes.qc.domain.QcCommands;
import com.hospital.mes.qms.application.ProductionQualityPlanService;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.*;

/** Actual controlled plan/dispatch/signature commands; unique fixtures roll back. */
class ProductionQualityPlanIT extends IncomingProductionIT {
    @Autowired ProductionQualityPlanService plans;
    @BeforeEach void qualityPermissions() {
        permissions.addAll(Set.of("qms:plan:view","qms:plan:create","qms:plan:update","qms:plan:approve")); as(author);
    }
    JsonNode planBody(JsonNode batch) {
        as(author);
        var material = materials.create(json.convertValue(Map.of("materialCode","F"+suffix,"materialName","Finished preparation","materialType","FINISHED","baseUnitId",unit,"lotControlled",true), MaterialCommands.Create.class),key());
        var spec=qc.create(new QcCommands.CreateSpecification(id(material),"FQC"+suffix,"Finished quality","Frozen test standard"),key());
        var draft=qc.createVersion(spec.id(),new QcCommands.CreateVersion(1,List.of(new QcCommands.Item("ASSAY","Assay",true,"NUMERIC","1","3",unit,null,"METHOD","1")),"Define finished standard"),key());
        as(approver);var approved=qc.approveVersion(draft.id(),new QcCommands.SignVersion(0L,"Approve standard","token"),"\"0\"",key());as(author);
        return body("mainBatchId",id(batch),"finishedMaterialId",id(material),"qcSpecificationVersionId",approved.id(),
                "balanceRules",List.of(Map.of("balanceCode","TOTAL","basis","BATCH","unitId",unit,"formulaExpr",Map.of("dslVersion",1,"expected",Map.of("sum",List.of("CHARGE")),"actual",Map.of("sum",List.of("OUTPUT","LOSS")),"metric","DIFFERENCE_PCT"),"toleranceLow","-1","toleranceHigh","1","checkPoint","BATCH_COMPLETE")),"reason","Define controlled plan");
    }
    @Test void signedIndependentApprovalFreezesActualPlanInDispatchSnapshot() {
        var batch=preparedBatch(false);var command=planBody(batch);String createKey=key();var plan=plans.create(command,createKey);
        assertThat(plans.create(command,createKey)).isEqualTo(plan);
        var approval=body("versionNo",0,"reason","Independent QA approval","signature",Map.of("reauthToken","token"));
        blockedCode("INDEPENDENT_REVIEW_REQUIRED",()->plans.approve(id(plan),approval,token(plan),key()));
        as(qa);var approved=plans.approve(id(plan),approval,token(plan),key());
        assertThat(verifier.verify(1,approved.path("signatureId").asLong())).isTrue();
        as(author);var dispatched=production.releaseBatch(id(batch),body("processPackageId",packageVersionId,"ebrTemplateVersionId",templateVersionId,"reason","Freeze actual plan"),token(batch),key());
        var snapshot=productionQuery.batch(1,Long.parseLong(id(dispatched))).snapshot();
        assertThat(snapshot.path("qualityPlan").path("contentHash").asText()).isEqualTo(approved.path("contentHash").asText());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mes_balance_rule WHERE main_batch_id=?",Integer.class,id(batch))).isEqualTo(1);
        var edit=command.deepCopy();((com.fasterxml.jackson.databind.node.ObjectNode)edit).put("versionNo",approved.path("versionNo").asLong());
        blockedCode("QUALITY_PLAN_FROZEN",()->plans.update(id(plan),edit,token(approved),key()));
    }
    @Test void draftPlanDoesNotPermitDispatchOrClientPassEvidence() {
        var batch=preparedBatch(false);var command=planBody(batch);plans.create(command,key());
        blockedCode("QUALITY_PLAN_NOT_APPROVED",()->production.releaseBatch(id(batch),body("processPackageId",packageVersionId,"ebrTemplateVersionId",templateVersionId,"reason","Draft cannot freeze"),token(batch),key()));
        assertThat(production.batch(id(batch)).path("status").asText()).isEqualTo("DRAFT");
        ((com.fasterxml.jackson.databind.node.ObjectNode)command).put("status","APPROVED");
        rejected(IllegalArgumentException.class,()->plans.create(command,key()));
    }
}
