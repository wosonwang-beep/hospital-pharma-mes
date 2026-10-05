package com.hospital.mes.qms.infrastructure;
import java.util.*;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@Component @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class IncomingMappers {
 private final Map<String,BaseMapper<? extends IncomingRow>> mappers;
 public IncomingMappers(InspectionRequestRowMapper mapper0, InspectionRequestItemRowMapper mapper1, SamplingTaskRowMapper mapper2, SamplingDetailRowMapper mapper3, SampleRowMapper mapper4, InspectionTaskRowMapper mapper5, InspectionItemRowMapper mapper6, TestExecutionRowMapper mapper7, TestResultRevisionRowMapper mapper8, InspectionReportRowMapper mapper9, InspectionReportItemRowMapper mapper10, DeviationRowMapper mapper11, ReleaseDecisionRowMapper mapper12, ProductionPlanRowMapper mapper13, BalanceRuleRowMapper mapper14, BalanceResultRowMapper mapper15, BalanceInvestigationRowMapper mapper16, ProductionTestInstanceRowMapper mapper17, ProductionTestReviewRowMapper mapper18, CapaRowMapper mapper19, CapaReviewRowMapper mapper20) {
  mappers=Map.ofEntries(Map.entry("qms_inspection_request",mapper0), Map.entry("qms_inspection_request_item",mapper1), Map.entry("qms_sampling_task",mapper2), Map.entry("qms_sampling_detail",mapper3), Map.entry("qms_sample",mapper4), Map.entry("qms_inspection_task",mapper5), Map.entry("qms_inspection_item",mapper6), Map.entry("qms_test_execution",mapper7), Map.entry("qms_test_result_revision",mapper8), Map.entry("qms_inspection_report",mapper9), Map.entry("qms_inspection_report_item",mapper10), Map.entry("qms_deviation",mapper11), Map.entry("qms_release_decision",mapper12), Map.entry("qms_production_plan",mapper13), Map.entry("mes_balance_rule",mapper14), Map.entry("mes_balance_result",mapper15), Map.entry("mes_balance_investigation",mapper16), Map.entry("qms_production_test_instance",mapper17), Map.entry("qms_production_test_review",mapper18), Map.entry("qms_capa",mapper19), Map.entry("qms_capa_review",mapper20));
 }
 @SuppressWarnings("unchecked") public BaseMapper<IncomingRow> mapper(String table) {
  var mapper=mappers.get(table);
  if(mapper==null)throw new IllegalArgumentException("Unknown quality table");
  return (BaseMapper<IncomingRow>)(BaseMapper<?>)mapper;
 }
 public IncomingRow create(String table) { return switch(table) {
  case "qms_inspection_request" -> new InspectionRequestRow();
  case "qms_inspection_request_item" -> new InspectionRequestItemRow();
  case "qms_sampling_task" -> new SamplingTaskRow();
  case "qms_sampling_detail" -> new SamplingDetailRow();
  case "qms_sample" -> new SampleRow();
  case "qms_inspection_task" -> new InspectionTaskRow();
  case "qms_inspection_item" -> new InspectionItemRow();
  case "qms_test_execution" -> new TestExecutionRow();
  case "qms_test_result_revision" -> new TestResultRevisionRow();
  case "qms_inspection_report" -> new InspectionReportRow();
  case "qms_inspection_report_item" -> new InspectionReportItemRow();
  case "qms_deviation" -> new DeviationRow();
  case "qms_release_decision" -> new ReleaseDecisionRow();
  case "qms_production_plan" -> new ProductionPlanRow();
  case "mes_balance_rule" -> new BalanceRuleRow();
  case "mes_balance_result" -> new BalanceResultRow();
  case "mes_balance_investigation" -> new BalanceInvestigationRow();
  case "qms_production_test_instance" -> new ProductionTestInstanceRow();
  case "qms_production_test_review" -> new ProductionTestReviewRow();
  case "qms_capa" -> new CapaRow();
  case "qms_capa_review" -> new CapaReviewRow();
  default -> throw new IllegalArgumentException("Unknown quality table");
 }; }
}
