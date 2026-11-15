package com.hospital.mes.incoming;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/** Regression for the existing InspectionTask read contract, not new approval semantics.
 * Fixture writes are rolled back; signature dependencies use the existing test doubles.
 */
class InspectionTaskReadProjectionIT extends IncomingQualityFixture {
 @Test void pendingTaskExposesEmptyReviewedResultArrayWithoutClaimingReview() {
  var request=request();var sample=sample(request);var task=task(request,sample);
  assertThat(task.has("reviewedResultIds")).as("Required public read projection").isTrue();
  assertThat(task.path("reviewedResultIds").isArray()).isTrue();
  assertThat(task.path("reviewedResultIds")).isEmpty();
  assertThat(task.path("reviewSignatureId").isNull()).isTrue();
  assertThat(task.path("status").asText()).isEqualTo("IN_PROGRESS");
 }
 @Test void reviewedTaskExposesExactlyPersistedResultReferencesAndRetainsSignature() throws Exception {
  var request=request();var sample=sample(request);var task=task(request,sample);
  var limits=task.path("items").get(0);
  var numeric=new java.math.BigDecimal(limits.path("lowerLimit").asText()).add(new java.math.BigDecimal(limits.path("upperLimit").asText())).divide(new java.math.BigDecimal("2")).toPlainString();
  var execution=execution(task);var result=result(task,execution,numeric,"PASS");
  var reviewed=review(task,result);
  var stored=jdbc.queryForObject("SELECT reviewed_result_ids_json FROM qms_inspection_task WHERE id=?",String.class,id(task));
  assertThat(reviewed.path("reviewedResultIds")).isEqualTo(json.readTree(stored));
  assertThat(reviewed.path("reviewedResultIds")).hasSize(1);
  assertThat(reviewed.path("reviewedResultIds").get(0).asText()).isEqualTo(id(result));
  assertThat(reviewed.path("reviewSignatureId").isNull()).isFalse();
  assertThat(reviewed.path("status").asText()).isEqualTo("QC_PASSED");
 }
}
