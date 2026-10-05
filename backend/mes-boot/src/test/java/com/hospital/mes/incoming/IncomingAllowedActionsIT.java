package com.hospital.mes.incoming;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class IncomingAllowedActionsIT extends IncomingQualityFixture {
 @Test void serverActionsFollowRequestStateAndSampleType() {
  var request=request();
  assertThat(request.has("allowedActions")).isTrue();
  assertThat(request.path("allowedActions")).isEmpty();
  var sample=sample(request);
  assertThat(sample.path("allowedActions")).extracting(n->n.asText()).contains("label").doesNotContain("retain","receive");
  var task=task(request,sample);
  assertThat(task.path("allowedActions")).extracting(n->n.asText()).doesNotContain("submit-review");
  assertThat(task.path("items").get(0).path("allowedActions")).extracting(n->n.asText()).contains("executions").doesNotContain("approved-retests");
  var execution=execution(task);
  assertThat(execution.path("allowedActions")).extracting(n->n.asText()).containsExactly("results");
  permissions.remove("ebr:sign");as(analyst);
  assertThat(get("qms_inspection_task",id(task)).path("items").get(0).path("executions").get(0).path("allowedActions")).isEmpty();
  permissions.add("ebr:sign");as(analyst);
  var result=result(task,execution,"2","PASS");
  assertThat(result.path("allowedActions")).extracting(n->n.asText()).containsExactly("revisions");
  var latest=get("qms_inspection_task",id(task));
  assertThat(latest.path("allowedActions")).extracting(n->n.asText()).contains("submit-review");
  assertThat(latest.path("items").get(0).path("executions").get(0).path("allowedActions")).extracting(n->n.asText()).containsExactly("revisions");
  as(reviewer);
  assertThat(get("qms_inspection_task",id(task)).path("items").get(0).path("allowedActions")).isEmpty();
  review(task,result);
  var report=report(request);
  assertThat(report.path("allowedActions")).isEmpty();
  as(qa);
  assertThat(service.releaseReview(lot).path("allowedActions")).extracting(n->n.asText()).containsExactly("release-decisions");
  assertThat(verifier.verify(1,result.path("signatureId").asLong())).isTrue();
 }
}
