package com.hospital.mes.qms.domain;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
class IncomingActionPolicyTest {
 @Test void completedAndUnknownStatesHaveNoMutationCandidates(){
  assertThat(IncomingActionPolicy.candidates("qms_inspection_task","QC_PASSED",null)).isEmpty();
  assertThat(IncomingActionPolicy.candidates("qms_inspection_request","UNKNOWN",null)).isEmpty();
  assertThatThrownBy(()->IncomingActionPolicy.require("qms_inspection_request","ACCEPTED",null,"submit")).hasMessageContaining("Command not allowed");
 }
 @Test void earlyRetentionIsRestrictedToRetentionSamples(){
  assertThat(IncomingActionPolicy.candidates("qms_sample","RECEIVED","TEST_SAMPLE")).doesNotContain("retain");
  assertThat(IncomingActionPolicy.candidates("qms_sample","RECEIVED","RETENTION_SAMPLE")).contains("retain");
  assertThat(IncomingActionPolicy.candidates("qms_sample","TEST_COMPLETED","TEST_SAMPLE")).contains("retain","dispose");
 }
 @Test void crossroleSamplingRequiresWarehouseHandover(){
  assertThat(IncomingActionPolicy.candidates("qms_sampling_task","IN_PROGRESS","CROSSROLE"))
    .contains("details","submit-to-warehouse").doesNotContain("complete");
  assertThat(IncomingActionPolicy.candidates("qms_sampling_task","PENDING_WAREHOUSE","CROSSROLE"))
    .containsExactly("confirm-sample-outbound");
 }
 @Test void legacyUnplannedSamplingRemainsUnchanged(){
  assertThat(IncomingActionPolicy.candidates("qms_sampling_task","IN_PROGRESS",null)).contains("details","complete").doesNotContain("submit-to-warehouse");
  assertThat(IncomingActionPolicy.candidates("qms_sampling_task","PENDING_WAREHOUSE",null)).isEmpty();
 }
 @Test void waitingInspectionTaskOnlyAllowsSampleBinding(){
  assertThat(IncomingActionPolicy.candidates("qms_inspection_task","CREATED","WAITING_SAMPLE"))
    .containsExactly("bind-sample");
 }
}
