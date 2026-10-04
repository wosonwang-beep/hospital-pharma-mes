package com.hospital.mes.execution.application;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.common.exception.ComplianceException;
import org.springframework.boot.context.properties.bind.*;
import org.springframework.core.env.Environment;
import java.util.*;
import java.time.Instant;
@org.springframework.stereotype.Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class QualificationGate {
 private final MasterQueryService master;private final MasterMutation mutations;private final ObjectMapper json;private final Environment environment;
 public QualificationGate(MasterQueryService master,MasterMutation mutations,ObjectMapper json,Environment environment){this.master=master;this.mutations=mutations;this.json=json;this.environment=environment;}
 public ObjectNode evidence(CurrentPlatformContext c,String action,Instant at){var codes=Binder.get(environment).bind("mes.qualification.required-codes."+action,Bindable.listOf(String.class)).orElse(List.of());if(codes.isEmpty()||codes.stream().anyMatch(x->x==null||x.isBlank()))throw new ComplianceException("QUALIFICATION_MAPPING_REQUIRED","Explicit qualification deployment binding required for "+action);var out=json.createObjectNode();out.put("action",action);out.put("checkedAt",at.toString());out.put("actorId",Long.toString(c.actorId()));out.put("configurationHash",mutations.digest(new TreeSet<>(codes)));var matched=out.putArray("qualifications");for(String code:new TreeSet<>(codes)){var e=master.qualificationEvidence(c.organizationId(),c.actorId(),code,at);var n=matched.addObject();n.put("id",e.id());n.put("versionNo",e.versionNo());n.put("code",e.code());n.put("userId",e.userId());n.put("status",e.status());if(e.validFrom()==null)n.putNull("validFrom");else n.put("validFrom",e.validFrom().toString());if(e.validTo()==null)n.putNull("validTo");else n.put("validTo",e.validTo().toString());}return out;}
}
