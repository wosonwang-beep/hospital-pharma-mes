package com.hospital.mes.ebr.application;
import com.fasterxml.jackson.databind.*;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.ebr.domain.*;
import com.hospital.mes.ebr.infrastructure.*;
import com.hospital.mes.common.exception.*;
import com.hospital.mes.masterdata.application.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.annotation.*;
import java.util.*;
/** Trusted server ingress; no public route or client-supplied provenance bypass. */
@org.springframework.stereotype.Service @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class EbrRuntimeSources {
 private final EbrRuntimeStore db;private final EbrRuntimeModel model;private final EbrRuntimeTransactions transactions;private final MasterMutation mutations;private final CurrentPlatformContextResolver contexts;private final ObjectProvider<EbrRuntimeSourcePort> sources;
 public EbrRuntimeSources(EbrRuntimeStore db,EbrRuntimeModel model,EbrRuntimeTransactions transactions,MasterMutation mutations,CurrentPlatformContextResolver contexts,ObjectProvider<EbrRuntimeSourcePort> sources){this.db=db;this.model=model;this.transactions=transactions;this.mutations=mutations;this.contexts=contexts;this.sources=sources;}
 public record SourceValue(long formId,long formRevision,String fieldCode,String occurrencePath,JsonNode value,String unitId,String sourceType,String sourceRef,String reason){}
 @Transactional public JsonNode append(long org,long actor,SourceValue value,String key){var c=contexts.current();if(c.organizationId()!=org||c.actorId()!=actor)throw new PermissionException("PERMISSION_DENIED","Verified source actor required");return mutations.execute(c,"EBR_SOURCE_VALUE",key,value,()->{var f=db.store(RuntimeFormEntity.class).get(org,value.formId());model.context(f,true);f=db.store(RuntimeFormEntity.class).lock(org,f.getId());model.active(f);if(!f.getStatus().equals("DRAFT")||f.getRevision()!=value.formRevision())throw new ResourceConflictException("REVISION_CONFLICT","Current draft revision required");var field=model.field(f,value.fieldCode());if(!Set.of("SYSTEM","INSTRUMENT").contains(value.sourceType())||!field.sourceType().equals(value.sourceType()))throw new ComplianceException("EBR_SOURCE_REQUIRED","Frozen producer source required");if(value.sourceRef()==null||value.sourceRef().isBlank())throw new IllegalArgumentException("Source record identity required");EbrRuntimeRules.reason(value.reason());EbrRuntimeRules.occurrence(model.definition(f),field,value.occurrencePath());var source=sources.orderedStream().filter(p->p.sourceType().equals(value.sourceType())).findFirst().orElseThrow(()->new ComplianceException("EBR_SOURCE_REQUIRED","Actual source producer unavailable"));source.requireEvidence(org,f.getOperationExecutionId(),value.sourceRef(),value.fieldCode(),value.value(),value.unitId());var before=model.render(f,c);transactions.appendSource(c,f,value,key);mutations.auditSnapshot(c,"EBR_SOURCE_VALUE","EBR_FORM_INSTANCE",f.getId(),before,model.render(f,c),value.reason(),key);return f;},row->model.render((RuntimeFormEntity)row,c),200);}
}
