package com.hospital.mes.incoming;

import static org.assertj.core.api.Assertions.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.qc.domain.QcCommands;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;

/** Transaction-rollback-only synthetic QC contract evidence. Never writes 2026 business lots. */
class IncomingQcMultipleItemsIT extends IncomingQualityFixture {
 @Test void fiveMixedItemsRequireEvidenceIndependentReviewReportAndQaBeforeEligibility(){
  var spec=qc.create(new QcCommands.Create(
    "IQ-MULTI-"+suffix,"来料综合项目质量标准","ENTERPRISE_INTERNAL","企业内控项目规范",null,
    "验证数值型与文字型检验执行闭环",List.of(material),List.of(),
    List.of(
      new QcCommands.Item("ASSAY","含量",true,"NUMERIC","95","105",unit,null,"METHOD-A","1"),
      new QcCommands.Item("MOISTURE","水分",true,"NUMERIC",null,"12",unit,null,"METHOD-B","1"),
      new QcCommands.Item("EXTRACT","浸出物",true,"NUMERIC","5",null,unit,null,"METHOD-C","1"),
      new QcCommands.Item("APPEARANCE","性状",true,"TEXT",null,null,null,"均匀，无可见杂质","METHOD-D","1"),
      new QcCommands.Item("IDENTIFICATION","鉴别",true,"TEXT",null,null,null,"符合规定的鉴别特征","METHOD-E","1")
    ),"建立独立回滚集成验证标准"),key());
  version=id(spec);
  var request=request();var sample=sample(request);var task=task(request,sample);
  assertThat(task.path("items")).hasSize(5);
  var before=service.releaseReview(lot);
  assertThat(before.path("eligibleForRelease").asBoolean()).isFalse();
  assertThat(before.path("gateReasons").toString()).contains("APPROVED_REPORT_REQUIRED");
  assertThatThrownBy(()->service.createReport(body("inspectionRequestId",id(request),"reason","缺失原始结果不得成报告"),key()))
    .hasMessageContaining("Required specification item missing");
  var observed=Map.of("ASSAY","100","MOISTURE","8","EXTRACT","7",
    "APPEARANCE","均匀，无可见杂质","IDENTIFICATION","鉴别特征符合");
  List<String> resultIds=new ArrayList<>();
  for(JsonNode item:task.path("items")){
   var current=get("qms_inspection_task",id(task));
   String code=item.path("itemCode").asText();
   String recorded=observed.get(code);
   assertThat(recorded).isNotBlank();
   var execution=service.execution(id(item),false,body(
      "rawData",Map.of("recordedObservation",recorded,"method",item.path("methodCode").asText()),
      "calculationInput",Map.of(),
      "startedAt",Instant.now().minusSeconds(3).toString(),
      "completedAt",Instant.now().minusSeconds(1).toString(),
      "performedBy",Long.toString(analyst),
      "reason","记录独立事务回滚的检验观察"),token(current),key());
   current=get("qms_inspection_task",id(task));
   boolean numeric="NUMERIC".equals(item.path("resultType").asText());
   var command=numeric
     ? body("testExecutionId",id(execution),"resultNumeric",recorded,"resultUnitId",unit,
       "resultConclusion","PASS","reauthToken","token")
     : body("testExecutionId",id(execution),"resultText",recorded,
       "resultConclusion","PASS","reauthToken","token");
   var result=service.result(id(item),false,command,token(current),key());
   assertThat(verifier.verify(1,Long.parseLong(result.path("signatureId").asText()))).isTrue();
   resultIds.add(id(result));
  }
  task=get("qms_inspection_task",id(task));
  task=service.taskAction(id(task),"submit-review",body("reason","五项结果提交独立复核"),token(task),key());
  as(reviewer);
  task=service.taskAction(id(task),"review",body("resultRevisionIds",resultIds,
    "reauthToken","token","reason","独立审核五项已签署检验结果"),token(task),key());
  assertThat(task.path("status").asText()).isEqualTo("QC_PASSED");
  assertThat(verifier.verify(1,Long.parseLong(task.path("reviewSignatureId").asText()))).isTrue();
  as(analyst);
  var report=service.createReport(body("inspectionRequestId",id(request),"reason","汇总五项受控检验结果"),key());
  as(reviewer);
  report=service.reportAction(id(report),"review",body("reason","独立复核检验报告"),token(report),key());
  as(approver);
  report=service.reportAction(id(report),"approve",body("reason","批准回滚集成验证报告",
    "reauthToken","token"),token(report),key());
  assertThat(report.path("overallResult").asText()).isEqualTo("PASS");
  assertThat(verifier.verify(1,Long.parseLong(report.path("approvalSignatureId").asText()))).isTrue();
  as(qa);
  var gate=service.releaseReview(lot);
  assertThat(gate.path("eligibleForRelease").asBoolean()).isTrue();
  var release=service.decideRelease(lot,body("decision","RELEASED","releaseBasis","FULL_INSPECTION",
    "inspectionReportId",id(report),"reason","QA核对全部来源后作独立回滚验收",
    "reauthToken","token"),token(gate),key());
  assertThat(release.path("decision").asText()).isEqualTo("RELEASED");
  assertThat(verifier.verify(1,Long.parseLong(release.path("signatureId").asText()))).isTrue();
 }
}
