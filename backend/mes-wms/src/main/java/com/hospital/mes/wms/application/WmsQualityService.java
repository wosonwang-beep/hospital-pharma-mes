package com.hospital.mes.wms.application;
import com.hospital.mes.wms.infrastructure.*;
import com.hospital.mes.wms.domain.WmsRules;
import com.hospital.mes.common.exception.StateTransitionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
/** Internal owner-side transitions; callers are the validated quality application commands. */
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class WmsQualityService {
 public enum Action {SUBMIT_INSPECTION,START_SAMPLING,COMPLETE_SAMPLING,START_TESTING,SUBMIT_QC_REVIEW,PASS_QC,FAIL_QC,OPEN_DISPOSITION,APPROVE_REPORT,RELEASE,REJECT,OPEN_RETEST}
 private final WmsStore store;private final com.hospital.mes.masterdata.application.MasterMutation mutations;private final com.hospital.mes.audit.application.CurrentPlatformContextResolver contexts;
 public WmsQualityService(WmsStore store,com.hospital.mes.masterdata.application.MasterMutation mutations,com.hospital.mes.audit.application.CurrentPlatformContextResolver contexts){this.store=store;this.mutations=mutations;this.contexts=contexts;}
 @Transactional public void apply(long org,long id,long version,Action action,long actor){
  var context=contexts.current();if(context.organizationId()!=org||context.actorId()!=actor)throw new com.hospital.mes.common.exception.PermissionException("QUALITY_CONTEXT_MISMATCH","Quality transition actor and organization must match current context");
  var lot=store.materialLot().lock(org,id);WmsRules.version(lot.getVersionNo(),version);var original=mutations.view(lot);
  String before=lot.getQualityStatus();Set<String> allowed;String after;
  switch(action){
   case SUBMIT_INSPECTION -> {allowed=Set.of("QUARANTINE");after="PENDING_SAMPLING";}
   case START_SAMPLING -> {allowed=Set.of("PENDING_SAMPLING","SAMPLING","SAMPLED");after="SAMPLING";}
   case COMPLETE_SAMPLING -> {allowed=Set.of("SAMPLING","SAMPLED");after="SAMPLED";}
   case START_TESTING -> {allowed=Set.of("SAMPLED","TESTING","PENDING_QC_REVIEW","QC_PASSED","QC_FAILED","PENDING_DISPOSITION");after="TESTING";}
   case SUBMIT_QC_REVIEW -> {allowed=Set.of("TESTING","PENDING_QC_REVIEW");after="PENDING_QC_REVIEW";}
   case PASS_QC -> {allowed=Set.of("PENDING_QC_REVIEW","QC_PASSED","QC_FAILED","PENDING_DISPOSITION");after="QC_PASSED";}
   case FAIL_QC -> {allowed=Set.of("TESTING","PENDING_QC_REVIEW","QC_PASSED","QC_FAILED");after="QC_FAILED";}
   case OPEN_DISPOSITION -> {allowed=Set.of("QC_FAILED","PENDING_DISPOSITION","TESTING","PENDING_QC_REVIEW","PENDING_QA_RELEASE","RELEASED");after="PENDING_DISPOSITION";}
   case APPROVE_REPORT -> {allowed=Set.of("QC_PASSED","PENDING_QA_RELEASE");after="PENDING_QA_RELEASE";}
   case RELEASE -> {allowed=Set.of("PENDING_QA_RELEASE","QUARANTINE","RELEASED");after="RELEASED";}
   case REJECT -> {allowed=Set.of("QC_FAILED","PENDING_DISPOSITION","PENDING_QA_RELEASE","QUARANTINE","RELEASED");after="REJECTED";}
   case OPEN_RETEST -> {allowed=Set.of("QC_FAILED","PENDING_DISPOSITION","RELEASED");after="PENDING_SAMPLING";}
   default -> throw new IllegalArgumentException("Unsupported quality action");
  }
  if(!allowed.contains(before))throw new StateTransitionException("MATERIAL_LOT_TRANSITION_REJECTED","Quality action is not valid for the current lot state");
  if(after.equals("RELEASED")&&"FROZEN".equals(lot.getInventoryStatus()))throw new com.hospital.mes.common.exception.ComplianceException("INVENTORY_FROZEN","Frozen material cannot be released");
  lot.setQualityStatus(after);lot.setInventoryStatus(after.equals("RELEASED")?"AVAILABLE":"FROZEN".equals(lot.getInventoryStatus())?"FROZEN":"BLOCKED");
  store.materialLot().update(lot,version,actor,List.of("qualityStatus","inventoryStatus"));
  mutations.auditSnapshot(context,"INCOMING_LOT_"+action.name(),"MaterialLot",lot.getId(),original,mutations.view(lot),"Quality command: "+action.name(),null);
 }
}
