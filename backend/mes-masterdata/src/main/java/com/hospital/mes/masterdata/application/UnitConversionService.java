package com.hospital.mes.masterdata.application;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.masterdata.domain.*;
import com.hospital.mes.masterdata.infrastructure.*;
import com.hospital.mes.masterdata.infrastructure.*;
import com.hospital.mes.common.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class UnitConversionService {
 private final UnitConversionStore store; private final MasterMutation mutations;private final UnitStore units;private final MaterialStore materials;
 public UnitConversionService(UnitConversionStore store,MasterMutation mutations,UnitStore units,MaterialStore materials){this.store=store;this.mutations=mutations;this.units=units;this.materials=materials;}
 public ScopedStore.PageData<JsonNode> list(int page,int size,String keyword,Map<String,String> params){var c=mutations.context("master:uom:view");var p=store.list(c.organizationId(),page,size,keyword,params);return new ScopedStore.PageData<>(p.items().stream().map(mutations::view).toList(),p.total(),p.page(),p.size());}
 public JsonNode get(String id){var c=mutations.context("master:uom:view");return mutations.view(store.get(c.organizationId(),MasterMutation.id(id)));}
 @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public JsonNode create(UnitConversionCommands.Create r,String key){var c=mutations.context("master:uom:create");return mutations.execute(c,"UnitConversion:CREATE",key,r,()->{var e=new UnitConversionEntity();e.setFromUnitId((r.fromUnitId()==null?null:MasterMutation.id(r.fromUnitId())));e.setToUnitId((r.toUnitId()==null?null:MasterMutation.id(r.toUnitId())));e.setFactor(MasterRules.factor(r.factor()));e.setMaterialId((r.materialId()==null?null:MasterMutation.id(r.materialId())));validate(e,c.organizationId());store.insert(e,c.organizationId(),c.actorId());return e;});}
 @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public JsonNode command(String id,UnitConversionCommands.Update r,String header,String key){var c=mutations.context("master:uom:update");long expected=0L;String reason=MasterRules.text(r.reason()==null||r.reason().isBlank()?"基础资料维护（系统记录）":r.reason(),1000);return mutations.execute(c,"UnitConversion:"+id,key,Map.of("id",id,"version",expected,"body",r),()->{var e=store.lock(c.organizationId(),MasterMutation.id(id));var before=copy(e);switch(MasterRules.text(r.action(),40)){case "UPDATE" -> {e.setFromUnitId((r.fromUnitId()==null?null:MasterMutation.id(r.fromUnitId())));e.setToUnitId((r.toUnitId()==null?null:MasterMutation.id(r.toUnitId())));e.setFactor(MasterRules.factor(r.factor()));e.setMaterialId((r.materialId()==null?null:MasterMutation.id(r.materialId())));}default -> throw new IllegalArgumentException("Unknown action");}validate(e,c.organizationId());store.update(e,expected,c.actorId(),List.of("fromUnitId","toUnitId","factor","materialId"));return e;});}
 private void validate(UnitConversionEntity e,long org){
  if(e.getFromUnitId()==null||e.getToUnitId()==null||e.getFromUnitId().equals(e.getToUnitId()))throw new IllegalArgumentException("Two distinct units required");
  var lower=units.lock(org,Math.min(e.getFromUnitId(),e.getToUnitId()));var upper=units.lock(org,Math.max(e.getFromUnitId(),e.getToUnitId()));
  var from=e.getFromUnitId().equals(lower.getId())?lower:upper;var to=e.getToUnitId().equals(lower.getId())?lower:upper;
  if(e.getMaterialId()!=null)materials.get(org,e.getMaterialId());
  if(e.getMaterialId()==null&&!from.getDimension().equals(to.getDimension()))throw new MasterGateException("UNIT_DIMENSION_MISMATCH","DEFINE_MATERIAL_CONVERSION");
  var q=new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UnitConversionEntity>().eq("org_id",org).eq("from_unit_id",e.getFromUnitId()).eq("to_unit_id",e.getToUnitId());
  if(e.getMaterialId()==null)q.isNull("material_id");else q.eq("material_id",e.getMaterialId());
  if(e.getId()!=null)q.ne("id",e.getId());
  if(store.hasPair(q))throw new ResourceConflictException("DUPLICATE_CODE","Conversion already exists");
 }
 private UnitConversionEntity copy(UnitConversionEntity e){var x=new UnitConversionEntity();org.springframework.beans.BeanUtils.copyProperties(e,x);return x;}
}
