package com.hospital.mes.wms.application;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospital.mes.wms.infrastructure.*;
import com.hospital.mes.wms.domain.WmsRules;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.masterdata.domain.MasterRules;
import com.hospital.mes.audit.signature.SignatureMeaning;
import com.hospital.mes.common.exception.ComplianceException;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@org.springframework.stereotype.Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class InventoryDecisionService {
 private final WmsViews views;private final WmsStore db;private final InventoryDecisionMapper records;private final ScopedStore<InventoryDecisionEntity> store;private final MasterMutation mutations;private final SignedRecordSupport signing;private final org.springframework.beans.factory.ObjectProvider<InventoryUnfreezeGate> gate;
 public InventoryDecisionService(WmsViews views,WmsStore db,InventoryDecisionMapper records,MasterMutation mutations,SignedRecordSupport signing,org.springframework.beans.factory.ObjectProvider<InventoryUnfreezeGate> gate){this.views=views;this.db=db;this.records=records;this.store=new ScopedStore<>(records,List.of(),Map.of());this.mutations=mutations;this.signing=signing;this.gate=gate;}
 public List<JsonNode> history(long org,long lot){db.materialLot().get(org,lot);return records.selectList(new QueryWrapper<InventoryDecisionEntity>().eq("org_id",org).eq("material_lot_id",lot).orderByAsc("id")).stream().map(this::recordView).toList();}
 private JsonNode recordView(InventoryDecisionEntity row){var n=views.view(row);n.remove(List.of("signatureEvidenceJson","updatedBy","updatedAt","versionNo"));for(String field:List.of("materialLotId","previousDecisionId","decidedBy","signatureId"))if(n.hasNonNull(field))n.put(field,n.get(field).asText());return n;}
 public JsonNode view(MaterialLotEntity lot){var n=views.view(lot);n.set("inventoryDecisions",new ObjectMapper().valueToTree(history(lot.getOrgId(),lot.getId())));var actions=n.withArray("allowedActions");if("AVAILABLE".equals(lot.getInventoryStatus()))actions.add("FREEZE");if("FROZEN".equals(lot.getInventoryStatus()))actions.add("UNFREEZE");return n;}
 @Transactional public JsonNode decide(String target,boolean freeze,JsonNode body,String header,String key){
  if(body==null||!body.isObject())throw new IllegalArgumentException("Object required");body.fieldNames().forEachRemaining(f->{if(!Set.of("versionNo","reason","signature").contains(f))throw new IllegalArgumentException("Unknown field: "+f);});
  var c=mutations.context("qa:material-inventory:"+(freeze?"freeze":"unfreeze"));long expected=SignedRecordSupport.expected(body,header);String reason=MasterRules.text(body.path("reason").asText(null),1000);
  return mutations.execute(c,"Inventory:"+(freeze?"FREEZE:":"UNFREEZE:")+target,key,Map.of("target",target,"body",body),()->{
   var lot=db.materialLot().lock(c.organizationId(),MasterMutation.id(target));WmsRules.version(lot.getVersionNo(),expected);String from=freeze?"AVAILABLE":"FROZEN",to=freeze?"FROZEN":"AVAILABLE";
   if(!from.equals(lot.getInventoryStatus()))throw new ComplianceException("INVENTORY_DECISION_NOT_ALLOWED","Current inventory status does not allow this action");
   if(!freeze){var today=java.time.LocalDate.now(java.time.ZoneOffset.UTC);if(!"RELEASED".equals(lot.getQualityStatus())||(lot.getExpiryDate()!=null&&!today.isBefore(lot.getExpiryDate()))||(lot.getRetestDate()!=null&&!today.isBefore(lot.getRetestDate())))throw new ComplianceException("UNFREEZE_GATE_FAILED","Current locked lot must remain released and within expiry/retest dates");gate.getObject().requireUnfreeze(c.organizationId(),lot.getId());}
   var predecessors=records.selectList(new QueryWrapper<InventoryDecisionEntity>().eq("org_id",c.organizationId()).eq("material_lot_id",lot.getId()).orderByDesc("id").last("FOR UPDATE"));
   var record=new InventoryDecisionEntity();record.setMaterialLotId(lot.getId());record.setPreviousDecisionId(predecessors.isEmpty()?null:predecessors.getFirst().getId());record.setAction(freeze?"FREEZE":"UNFREEZE");record.setPreviousInventoryStatus(from);record.setResultingInventoryStatus(to);record.setReason(reason);record.setDecidedBy(c.actorId());record.setDecidedAt(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC));
   var evidence=signing.sign(c,"INVENTORY_DECISION",target+":"+expected,expected,SignatureMeaning.APPROVE,record,body.get("signature"),key,List.of("MaterialLot:"+target));record.setSignatureId(evidence.id());record.setSignatureEvidenceJson(evidence.envelope());store.insert(record,c.organizationId(),c.actorId());
   var before=mutations.view(lot);lot.setInventoryStatus(to);db.materialLot().update(lot,expected,c.actorId(),List.of("inventoryStatus"));mutations.auditSnapshot(c,"INVENTORY_"+record.getAction(),"MaterialLot",lot.getId(),before,view(lot),reason,key);return lot;
  },row->view((MaterialLotEntity)row),200);
 }
}
