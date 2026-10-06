package com.hospital.mes.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.qms.application.FinishedInspectionService;
import com.hospital.mes.wms.domain.FinishedInboundInspectionDraftRequested;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Synchronous composition boundary: exceptions roll back the original warehouse command. */
@org.springframework.stereotype.Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class FinishedInboundInspectionDraftListener {
 private final FinishedInspectionService quality;
 private final CurrentPlatformContextResolver contexts;
 private final ObjectMapper json;
 public FinishedInboundInspectionDraftListener(FinishedInspectionService quality,CurrentPlatformContextResolver contexts,ObjectMapper json){this.quality=quality;this.contexts=contexts;this.json=json;}
 @EventListener @Transactional(propagation=Propagation.MANDATORY)
 public void createDraft(FinishedInboundInspectionDraftRequested event){
  var context=contexts.current();
  if(context.organizationId()!=event.organizationId()||context.actorId()!=event.actorId())throw new IllegalStateException("Original finished draft actor/scope required");
  var command=json.createObjectNode().put("inspectionRequestNo",event.inspectionRequestNo()).put("inboundRequestId",Long.toString(event.inboundRequestId())).put("reason",event.reason());
  quality.createInboundDraft(command,event.idempotencyKey());
 }
}
