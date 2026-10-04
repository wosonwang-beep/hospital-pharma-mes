package com.hospital.mes.masterdata.application;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.mes.masterdata.infrastructure.*;
import com.hospital.mes.masterdata.domain.*;
import com.hospital.mes.common.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
import java.time.Instant;

@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class MaterialService {
 private final MaterialStore materials;
 private final UnitStore units;
 private final MasterMutation mutations;
 private final org.springframework.context.ApplicationEventPublisher events;
 public MaterialService(MaterialStore materials,UnitStore units,MasterMutation mutations,org.springframework.context.ApplicationEventPublisher events){this.materials=materials;this.units=units;this.mutations=mutations;this.events=events;}

 public ScopedStore.PageData<JsonNode> list(int page,int size,String keyword,Map<String,String> filters){
  var c=mutations.context("master:material:view");
  var p=materials.list(c.organizationId(),page,size,keyword,filters);
  return new ScopedStore.PageData<>(p.items().stream().map(this::view).toList(),p.total(),page,size);
 }
 public JsonNode get(String id){var c=mutations.context("master:material:view");return snapshot(c.organizationId(),MasterMutation.id(id));}
 public JsonNode snapshot(long org,long id){return view(materials.get(org,id));}
 public JsonNode requireUsable(long org,long id,Instant at){var m=materials.get(org,id);MaterialRules.usable(m.getStatus(),m.getEffectiveFrom(),m.getEffectiveTo(),at);return view(m);}
 private JsonNode view(MaterialEntity m){
  var n=(ObjectNode)mutations.view(m);
  n.retain(List.of("id","orgId","createdBy","createdAt","updatedBy","updatedAt","versionNo","allowedActions","status","materialCode","materialName","materialType","specification","gradePurity","appearance","baseUnitId","packSpec","packUnitId","manufacturerName","lotControlled","effectiveFrom","effectiveTo","remark","requiresIncomingInspection"));
  if(MaterialRules.enabled(m.getStatus()))n.put("status","ACTIVE");
  var u=units.get(m.getOrgId(),m.getBaseUnitId());n.put("baseUnitName",u.getUnitName());n.put("baseUnitCode",u.getUnitCode());
  if(m.getPackUnitId()!=null)n.put("packUnitName",units.get(m.getOrgId(),m.getPackUnitId()).getUnitName());
  return n;
 }
 @Transactional public JsonNode create(MaterialCommands.Create r,String key){
  var c=mutations.context("master:material:create");
  return mutations.execute(c,"Material:CREATE",key,r,()->{
   var e=new MaterialEntity();e.setMaterialCode(MasterRules.text(r.materialCode(),50));
   if(materials.count(c.organizationId(),"material_code",e.getMaterialCode(),null)>0)throw new ResourceConflictException("MATERIAL_CODE_EXISTS","Material code already exists");
   apply(e,new MaterialCommands.Update(null,null,r.materialName(),r.materialType(),r.specification(),r.gradePurity(),r.appearance(),r.baseUnitId(),r.packSpec(),r.packUnitId(),r.manufacturerName(),r.lotControlled(),r.effectiveFrom(),r.effectiveTo(),r.remark(),r.requiresIncomingInspection()),c.organizationId());
   e.setStatus("ACTIVE");materials.insert(e,c.organizationId(),c.actorId());
   mutations.auditSnapshot(c,"Material:CREATE","Material",e.getId(),null,mutations.view(e),null,key);
   events.publishEvent(new MaterialEvents.Created(c.organizationId(),e.getId()));return e;
  },x->view((MaterialEntity)x),201);
 }
 @Transactional public JsonNode update(String id,MaterialCommands.Update r,String header,String key){
  var c=mutations.context("master:material:update");long expected=MasterMutation.version(header,r.versionNo());String reason=MasterRules.text(r.reason(),1000);
  return mutations.execute(c,"Material:UPDATE:"+id,key,Map.of("id",id,"version",expected,"body",r),()->{
   var e=materials.lock(c.organizationId(),MasterMutation.id(id));requireVersion(e,expected);var before=mutations.view(e);
   apply(e,r,c.organizationId());if(MaterialRules.enabled(e.getStatus()))e.setStatus("ACTIVE");
   materials.update(e,expected,c.actorId(),List.of("materialName","materialType","specification","gradePurity","appearance","baseUnitId","packSpec","packUnitId","manufacturerName","lotControlled","effectiveFrom","effectiveTo","remark","requiresIncomingInspection","status"));
   mutations.auditSnapshot(c,"Material:UPDATE","Material",e.getId(),before,mutations.view(e),reason,key);
   events.publishEvent(new MaterialEvents.Updated(c.organizationId(),e.getId()));return e;
  },x->view((MaterialEntity)x),200);
 }
 @Transactional public JsonNode disable(String id,MaterialCommands.Disable r,String header,String key){
  var c=mutations.context("master:material:disable");long expected=MasterMutation.version(header,r.versionNo());String reason=MasterRules.text(r.reason(),1000);
  return mutations.execute(c,"Material:DISABLE:"+id,key,Map.of("id",id,"version",expected,"body",r),()->{
   var e=materials.lock(c.organizationId(),MasterMutation.id(id));requireVersion(e,expected);var before=mutations.view(e);
   e.setStatus(MaterialRules.disable(e.getStatus()));materials.update(e,expected,c.actorId(),List.of("status"));
   mutations.auditSnapshot(c,"Material:DISABLE","Material",e.getId(),before,mutations.view(e),reason,key);
   events.publishEvent(new MaterialEvents.Disabled(c.organizationId(),e.getId()));return e;
  },x->view((MaterialEntity)x),200);
 }
 private void requireVersion(MaterialEntity m,long expected){if(m.getVersionNo()!=expected)throw new ResourceConflictException("VERSION_CONFLICT","Reload material before saving");}
 private void apply(MaterialEntity e,MaterialCommands.Update r,long org){
  e.setMaterialName(MasterRules.text(r.materialName(),200));
e.setMaterialType(MasterRules.text(r.materialType(),30));
e.setSpecification(MasterRules.optional(r.specification(),200));
e.setGradePurity(MasterRules.optional(r.gradePurity(),100));
e.setAppearance(MasterRules.optional(r.appearance(),500));
e.setBaseUnitId(MasterMutation.id(r.baseUnitId()));
e.setPackSpec(MasterRules.optional(r.packSpec(),200));
e.setPackUnitId((r.packUnitId()==null||r.packUnitId().isBlank()?null:MasterMutation.id(r.packUnitId())));
e.setManufacturerName(MasterRules.optional(r.manufacturerName(),200));
e.setLotControlled(MaterialRules.required(r.lotControlled()));
e.setEffectiveFrom(MaterialRules.time(r.effectiveFrom()));
e.setEffectiveTo(MaterialRules.time(r.effectiveTo()));
e.setRemark(MasterRules.optional(r.remark(),1000));
e.setRequiresIncomingInspection(r.requiresIncomingInspection()==null?true:r.requiresIncomingInspection());
  units.get(org,e.getBaseUnitId());if(e.getPackUnitId()!=null)units.get(org,e.getPackUnitId());MaterialRules.period(e.getEffectiveFrom(),e.getEffectiveTo());
 }
}
