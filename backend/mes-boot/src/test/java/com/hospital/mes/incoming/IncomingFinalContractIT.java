package com.hospital.mes.incoming;
import static org.assertj.core.api.Assertions.*;
import com.hospital.mes.audit.application.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
class IncomingFinalContractIT extends IncomingQualityFixture {
 @Autowired AuditEventRepository events;
 @Test void qcHandoffAuditRecordsActorTimeReasonAndBothSnapshotDigests(){
  var request=request();var audit=events.query(1,new AuditEventQuery(analyst,"INSPECTION_REQUEST_accept","qms_inspection_request",id(request),null,null,null,null,null,0,20));
  assertThat(audit.items()).hasSize(1);var event=audit.items().getFirst();assertThat(event.organizationId()).isEqualTo(1);assertThat(event.actorId()).isEqualTo(analyst);assertThat(event.reason()).isEqualTo("QC accepted");assertThat(event.occurredAt()).isNotNull();assertThat(event.oldValueDigest()).matches("[a-f0-9]{64}");assertThat(event.newValueDigest()).matches("[a-f0-9]{64}").isNotEqualTo(event.oldValueDigest());assertThat(event.transactionId()).isNotBlank();assertThat(event.requestId()).isNotBlank();
  assertThat(request.path("acceptedBy").asText()).isEqualTo(Long.toString(analyst));assertThat(request.path("acceptedAt").asText()).isNotBlank();assertThat(request.path("qcSpecificationVersionId").asText()).isEqualTo(version);
 }
}
