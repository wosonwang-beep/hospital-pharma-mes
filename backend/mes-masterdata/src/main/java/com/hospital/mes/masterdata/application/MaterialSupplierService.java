package com.hospital.mes.masterdata.application;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.mes.masterdata.infrastructure.*;
import com.hospital.mes.masterdata.domain.*;
import com.hospital.mes.common.exception.*;
import java.util.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class MaterialSupplierService {
 private final MaterialStore materials;
 private final SupplierStore suppliers;
 private final MaterialSupplierStore store;
 private final MasterMutation mutations;
 public MaterialSupplierService(MaterialStore materials,SupplierStore suppliers,MaterialSupplierStore store,MasterMutation mutations){this.materials=materials;this.suppliers=suppliers;this.store=store;this.mutations=mutations;}
 public JsonNode get(String id){var c=mutations.context("master:material:view");return view(materials.get(c.organizationId(),MasterMutation.id(id)));}
 private JsonNode view(MaterialEntity m){
  var n=(ObjectNode)mutations.view(m);n.retain("id","versionNo");var a=n.putArray("suppliers");
  for(var row:store.relationships(m.getOrgId(),m.getId(),false)){
   var item=(ObjectNode)mutations.view(row);var supplier=suppliers.get(m.getOrgId(),row.getSupplierId());
   item.put("supplierCode",supplier.getSupplierCode());item.put("supplierName",supplier.getSupplierName());item.put("qualificationStatus",supplier.getQualificationStatus());a.add(item);
  }
  return n;
 }
 @Transactional public JsonNode assign(String id,SupplierCommands.Assign r,String header,String key){
  var c=mutations.context("master:material:update");long expected=MasterMutation.version(header,r.versionNo());String reason=MasterRules.text(r.reason(),1000);
  if(r.suppliers()==null||r.suppliers().size()>500)throw new IllegalArgumentException("Supplier list required, maximum 500");
  validatePreferred(r.suppliers());
  return mutations.execute(c,"Material:SUPPLIERS:"+id,key,Map.of("id",id,"version",expected,"body",r),()->{
   var m=materials.lock(c.organizationId(),MasterMutation.id(id));if(m.getVersionNo()!=expected)throw new ResourceConflictException("VERSION_CONFLICT","Reload material relationships");
   if(!MaterialRules.enabled(m.getStatus()))throw new StateTransitionException("INVALID_STATE","Inactive material cannot change relationships");
   var before=view(m);var current=store.relationships(c.organizationId(),m.getId(),true);
   var input=r.suppliers().stream().sorted(Comparator.comparingLong(x->MasterMutation.id(x.supplierId()))).toList();
   var seen=new HashSet<Long>();
   // Lock all supplier references in a stable order before validation/write.
   for(var item:input){
    long sid=MasterMutation.id(item.supplierId());if(!seen.add(sid))throw new IllegalArgumentException("Duplicate supplier assignment");
    var supplier=suppliers.lock(c.organizationId(),sid);
    if(Boolean.TRUE.equals(item.approved()))SupplierRules.usable(supplier.getQualificationStatus(),supplier.getValidTo(),true,item.validTo(),Instant.now());
   }
   // Clear a revoked preferred first; then apply non-preferred before preferred to avoid transient unique-key collisions.
   for(var row:current)if(!seen.contains(row.getSupplierId())&&(Boolean.TRUE.equals(row.getApproved())||Boolean.TRUE.equals(row.getPreferred()))){
    row.setApproved(false);row.setPreferred(false);store.update(row,row.getVersionNo(),c.actorId(),List.of("approved","preferred"));
   }
   var ordered=input.stream().sorted(Comparator.comparing(x->Boolean.TRUE.equals(x.preferred()))).toList();
   for(var item:ordered){
    long sid=MasterMutation.id(item.supplierId());var row=current.stream().filter(e->e.getSupplierId()==sid).findFirst().orElse(null);
    if(row==null){row=new MaterialSupplierEntity();row.setMaterialId(m.getId());row.setSupplierId(sid);row.setApproved(item.approved());row.setPreferred(item.preferred());row.setValidTo(item.validTo());store.insert(row,c.organizationId(),c.actorId());}
    else{row.setApproved(item.approved());row.setPreferred(item.preferred());row.setValidTo(item.validTo());store.update(row,row.getVersionNo(),c.actorId(),List.of("approved","preferred","validTo"));}
   }
   materials.update(m,expected,c.actorId(),List.of());mutations.auditSnapshot(c,"Material:SUPPLIERS","Material",m.getId(),before,view(m),reason,key);return m;
  },x->view((MaterialEntity)x),200);
 }
 private void validatePreferred(List<SupplierCommands.Relationship> rows){
  long active=0,preferred=0;
  for(var item:rows){boolean linked=MaterialRules.required(item.approved()),first=MaterialRules.required(item.preferred());if(first&&!linked)throw new MasterGateException("PREFERRED_SUPPLIER_INVALID","SELECT_ACTIVE_SUPPLIER");if(linked)active++;if(first)preferred++;}
  if(preferred!=(active>0?1:0))throw new MasterGateException("PREFERRED_SUPPLIER_REQUIRED","SELECT_EXACTLY_ONE_PREFERRED_SUPPLIER");
 }
 public JsonNode requireApproved(long org,long materialId,long supplierId,Instant at){
  var m=materials.get(org,materialId);MaterialRules.usable(m.getStatus(),m.getEffectiveFrom(),m.getEffectiveTo(),at);
  var supplier=suppliers.get(org,supplierId);
  var row=store.relationships(org,materialId,false).stream().filter(e->e.getSupplierId()==supplierId).findFirst().orElseThrow(()->new MasterGateException("SUPPLIER_NOT_APPROVED","LINK_APPROVED_SUPPLIER"));
  SupplierRules.usable(supplier.getQualificationStatus(),supplier.getValidTo(),Boolean.TRUE.equals(row.getApproved()),row.getValidTo(),at);return mutations.view(row);
 }
 public JsonNode requirePreferred(long org,long materialId,Instant at){
  var rows=store.relationships(org,materialId,false).stream().filter(e->Boolean.TRUE.equals(e.getPreferred())&&Boolean.TRUE.equals(e.getApproved())).toList();
  if(rows.size()!=1)throw new MasterGateException("PREFERRED_SUPPLIER_REQUIRED","SELECT_EXACTLY_ONE_PREFERRED_SUPPLIER");
  return requireApproved(org,materialId,rows.get(0).getSupplierId(),at);
 }
}
