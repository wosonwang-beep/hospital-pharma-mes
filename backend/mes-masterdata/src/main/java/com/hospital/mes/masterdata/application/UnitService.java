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
public class UnitService {
 private final UnitStore store; private final MasterMutation mutations; private final UnitConversionStore conversions;
 public UnitService(UnitStore store,MasterMutation mutations,UnitConversionStore conversions){this.store=store;this.mutations=mutations;this.conversions=conversions;}
 public ScopedStore.PageData<JsonNode> list(int page,int size,String keyword,Map<String,String> params){var c=mutations.context("master:uom:view");var p=store.list(c.organizationId(),page,size,keyword,params);return new ScopedStore.PageData<>(p.items().stream().map(mutations::view).toList(),p.total(),p.page(),p.size());}
 public JsonNode get(String id){var c=mutations.context("master:uom:view");return mutations.view(store.get(c.organizationId(),MasterMutation.id(id)));}
 @Transactional public JsonNode create(UnitCommands.Create r,String key){var c=mutations.context("master:uom:create");return mutations.execute(c,"Unit:CREATE",key,r,()->{var e=new UnitEntity();e.setUnitCode(MasterRules.text(r.unitCode(),32));e.setUnitName(MasterRules.text(r.unitName(),64));e.setDimension(MasterRules.text(r.dimension(),30));e.setScale(MasterRules.scale(r.scale()));validate(e,c.organizationId());store.insert(e,c.organizationId(),c.actorId());mutations.audit(c,"Unit:CREATE","Unit",null,e,null,key);return e;});}
 @Transactional public JsonNode command(String id,UnitCommands.Update r,String header,String key){var c=mutations.context("master:uom:update");long expected=MasterMutation.version(header,r.versionNo());String reason=MasterRules.text(r.reason(),1000);return mutations.execute(c,"Unit:"+id,key,Map.of("id",id,"version",expected,"body",r),()->{var e=store.lock(c.organizationId(),MasterMutation.id(id));if(e.getVersionNo()!=expected)throw new ResourceConflictException("VERSION_CONFLICT","Record changed; reload");var before=copy(e);switch(MasterRules.text(r.action(),40)){case "UPDATE" -> {e.setUnitName(MasterRules.text(r.unitName(),64));e.setDimension(MasterRules.text(r.dimension(),30));e.setScale(MasterRules.scale(r.scale()));}default -> throw new IllegalArgumentException("Unknown action");}if(!before.getDimension().equals(e.getDimension())&&conversions.references(c.organizationId(),e.getId()))throw new MasterGateException("UNIT_DIMENSION_IN_USE","PRESERVE_REFERENCED_DIMENSION");validate(e,c.organizationId());store.update(e,expected,c.actorId(),List.of("unitName","dimension","scale"));mutations.audit(c,"Unit:"+r.action(),"Unit",before,e,reason,key);return e;});}
 private void validate(UnitEntity e,long org){MasterRules.scale(e.getScale());}
 private UnitEntity copy(UnitEntity e){var x=new UnitEntity();org.springframework.beans.BeanUtils.copyProperties(e,x);return x;}
}
