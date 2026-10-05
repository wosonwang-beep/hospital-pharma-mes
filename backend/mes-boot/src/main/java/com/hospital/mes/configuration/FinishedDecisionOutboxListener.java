package com.hospital.mes.configuration;
import com.hospital.mes.integration.application.OutboxApplicationService;
import com.hospital.mes.release.domain.FinishedDecisionRecorded;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.event.EventListener;
/** Synchronous domain notification joins the QA transaction; no notification before commit. */
@org.springframework.stereotype.Component @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class FinishedDecisionOutboxListener {
 private final OutboxApplicationService outbox;private final ObjectMapper json;
 public FinishedDecisionOutboxListener(OutboxApplicationService outbox,ObjectMapper json){this.outbox=outbox;this.json=json;}
 @EventListener public void recorded(FinishedDecisionRecorded event){try{outbox.enqueue(event.organizationId(),event.actorId(),"FINISHED_QA:"+event.decisionId(),"WMS","FINISHED_QA_DECIDED","ReleaseDecision",Long.toString(event.decisionId()),json.writeValueAsString(event));}catch(java.io.IOException ex){throw new IllegalStateException("Cannot encode finished QA notification",ex);}}
}
