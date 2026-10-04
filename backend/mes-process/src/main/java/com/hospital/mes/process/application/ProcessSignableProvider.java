package com.hospital.mes.process.application;
import com.hospital.mes.audit.signature.*;
import com.hospital.mes.masterdata.application.MasterMutation;
import com.hospital.mes.process.infrastructure.ProcessStore;
import java.util.Set;
@org.springframework.stereotype.Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ProcessSignableProvider implements SignableObjectProvider {
 private final ProcessStore db;private final ProcessDefinitions definitions;
 public ProcessSignableProvider(ProcessStore db,ProcessDefinitions definitions){this.db=db;this.definitions=definitions;}
 public String objectType(){return "ProcessVersion";}public Set<SignatureMeaning> allowedMeanings(){return Set.of(SignatureMeaning.APPROVE);}
 public SignableObject loadForSignature(long org,String id){return definitions.signable(org,db.versions().get(org,MasterMutation.id(id)));}
 public void validateSignable(SignatureValidationContext c,SignableObject object){var p=c.platform();if(!p.hasPermission("process:package:approve")||!p.hasPermission("ebr:sign"))throw new com.hospital.mes.common.exception.PermissionException("PERMISSION_DENIED","Approval and signing permission required");var v=db.versions().get(p.organizationId(),MasterMutation.id(object.objectId()));com.hospital.mes.process.domain.ProcessRules.transition(v.getStatus(),"APPROVE");definitions.lint(p.organizationId(),v);definitions.independentApprover(p.organizationId(),v,p.actorId());}
}
