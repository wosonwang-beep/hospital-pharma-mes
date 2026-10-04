package com.hospital.mes.qms.application;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class IncomingQualityQueryService {
 private final IncomingQualityService service;private final IncomingMaterialPort lots;
 public IncomingQualityQueryService(IncomingQualityService service,IncomingMaterialPort lots){this.service=service;this.lots=lots;}
 @Transactional(readOnly=true) public JsonNode getLotChain(long orgId,long lotId){return service.lotChain(orgId,lotId);}
 @Transactional public JsonNode evaluate(long orgId,long lotId,String useContext,Instant at){lots.lock(orgId,lotId);return service.eligibility(orgId,lotId,useContext,at);}
 @Transactional public void requireEligible(long orgId,long lotId,String useContext,Instant at){JsonNode result=evaluate(orgId,lotId,useContext,at);if(!result.path("eligible").asBoolean())throw new com.hospital.mes.common.exception.ComplianceException("MATERIAL_NOT_ELIGIBLE",result.path("reasons").toString());}
}
