package com.hospital.mes.wms.application;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.masterdata.application.MasterMutation;
import com.hospital.mes.wms.infrastructure.*;
import com.hospital.mes.common.exception.ComplianceException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.transaction.annotation.*;
import java.time.*;
import java.util.*;
/** WMS owns final lot availability; QA orchestration must already hold the production root lock. */
@org.springframework.stereotype.Service @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class FinishedQualityDecisionService {
 private final WmsStore db;private final MasterMutation mutations;private final WmsViews views;
 public FinishedQualityDecisionService(WmsStore db,MasterMutation mutations,WmsViews views){this.db=db;this.mutations=mutations;this.views=views;}
 @Transactional(propagation=Propagation.MANDATORY) public void lockEvidence(long org,long lot){db.materialLot().lock(org,lot);db.ledgerMapper().selectList(new QueryWrapper<LedgerEntity>().eq("org_id",org).eq("material_lot_id",lot).orderByAsc("id").last("FOR UPDATE"));}
 @Transactional(propagation=Propagation.MANDATORY) public void decide(CurrentPlatformContext c,long lotId,long materialId,String decision,boolean superseding,String reason,String key){
  mutations.context("qa:release");var lot=db.materialLot().lock(c.organizationId(),lotId);
  gate(lot.getMaterialId()==materialId&&lot.getReceiptItemId()==null,"FINISHED_LOT_BATCH_MISMATCH","Actual finished output lot required");
  gate(Set.of("RELEASED","REJECTED").contains(decision),"FINISHED_DECISION_INVALID","Controlled decision required");
  gate(superseding?Set.of("RELEASED","REJECTED").contains(lot.getQualityStatus()):lot.getQualityStatus().equals("QUARANTINE"),"FINISHED_LOT_STATE_CONFLICT","Lot no longer has the displayed QA state");
  gate(Set.of("BLOCKED","AVAILABLE").contains(lot.getInventoryStatus()),"FINISHED_LOT_LOCKED","Locked inventory requires controlled investigation");
  var ledger=db.ledgerMapper().selectList(new QueryWrapper<LedgerEntity>().eq("org_id",c.organizationId()).eq("material_lot_id",lotId).orderByAsc("id").last("FOR UPDATE"));
  gate(ledger.stream().anyMatch(x->"PRODUCTION_OUTPUT".equals(x.getEventType())),"FINISHED_OUTPUT_REQUIRED","Actual physical output required");
  if(decision.equals("RELEASED")){gate(lot.getExpiryDate()!=null&&LocalDate.now(ZoneOffset.UTC).isBefore(lot.getExpiryDate()),"FINISHED_LOT_EXPIRED","Expired finished output cannot become available");gate(ledger.stream().map(LedgerEntity::getDeltaQty).reduce(java.math.BigDecimal.ZERO,java.math.BigDecimal::add).signum()>0,"FINISHED_OUTPUT_REQUIRED","Positive physical output required");}
  if(superseding&&decision.equals("REJECTED"))gate(ledger.stream().noneMatch(x->x.getDeltaQty().signum()<0&&!"PRODUCTION_REVERSAL".equals(x.getSourceType())&&!"MOVE_OUT".equals(x.getEventType())),"FINISHED_RELEASE_CONSUMED","Consumed released inventory requires controlled investigation/freeze");
  var before=views.view(lot);long v=lot.getVersionNo();lot.setQualityStatus(decision);lot.setInventoryStatus(decision.equals("RELEASED")?"AVAILABLE":"BLOCKED");db.materialLot().update(lot,v,c.actorId(),List.of("qualityStatus","inventoryStatus"));mutations.auditSnapshot(c,"FINISHED_QA_"+decision,"MaterialLot",lotId,before,views.view(lot),reason,key);
 }
 private static void gate(boolean ok,String code,String reason){if(!ok)throw new ComplianceException(code,reason);}
}
