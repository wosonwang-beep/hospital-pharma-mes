package com.hospital.mes.equipment.application;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.masterdata.domain.*;
import com.hospital.mes.masterdata.infrastructure.*;
import com.hospital.mes.equipment.infrastructure.*;
import com.hospital.mes.common.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class EquipmentService {
 private final EquipmentStore store; private final MasterMutation mutations;
 public EquipmentService(EquipmentStore store,MasterMutation mutations){this.store=store;this.mutations=mutations;}
 public ScopedStore.PageData<JsonNode> list(int page,int size,String keyword,Map<String,String> params){var c=mutations.context("master:equipment:view");var p=store.list(c.organizationId(),page,size,keyword,params);return new ScopedStore.PageData<>(p.items().stream().map(mutations::view).toList(),p.total(),p.page(),p.size());}
 public JsonNode get(String id){var c=mutations.context("master:equipment:view");return mutations.view(store.get(c.organizationId(),MasterMutation.id(id)));}
 @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public JsonNode create(EquipmentCommands.Create r,String key){var c=mutations.context("master:equipment:create");return mutations.execute(c,"Equipment:CREATE",key,r,()->{var e=new EquipmentEntity();e.setEquipmentCode(MasterRules.text(r.equipmentCode(),64));e.setEquipmentName(MasterRules.text(r.equipmentName(),200));e.setEquipmentType(MasterRules.text(r.equipmentType(),64));e.setCalibrationDueDate(r.calibrationDueDate());e.setLocation(MasterRules.optional(r.location(),200));e.setStatus("ACTIVE");validate(e,c.organizationId());store.insert(e,c.organizationId(),c.actorId());return e;});}
 @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public JsonNode command(String id,EquipmentCommands.Update r,String header,String key){var c=mutations.context("master:equipment:update");long expected=0L;String reason="基础资料维护";return mutations.execute(c,"Equipment:"+id,key,Map.of("id",id,"version",expected,"body",r),()->{var e=store.lock(c.organizationId(),MasterMutation.id(id));var before=copy(e);switch(MasterRules.text(r.action(),40)){case "UPDATE" -> {e.setEquipmentName(MasterRules.text(r.equipmentName(),200));e.setEquipmentType(MasterRules.text(r.equipmentType(),64));e.setCalibrationDueDate(r.calibrationDueDate());e.setLocation(MasterRules.optional(r.location(),200));}case "ENABLE" -> e.setStatus(MasterRules.enable(e.getStatus()));case "DISABLE" -> {e.setStatus(MasterRules.disable(e.getStatus()));}case "BEGIN_MAINTENANCE" -> e.setStatus(com.hospital.mes.equipment.domain.EquipmentRules.beginMaintenance(e.getStatus()));case "RETURN_TO_SERVICE" -> e.setStatus(com.hospital.mes.equipment.domain.EquipmentRules.returnToService(e.getStatus()));default -> throw new IllegalArgumentException("Unknown action");}validate(e,c.organizationId());store.update(e,expected,c.actorId(),List.of("equipmentName","equipmentType","status","calibrationDueDate","location"));return e;});}
 private void validate(EquipmentEntity e,long org){}
 private EquipmentEntity copy(EquipmentEntity e){var x=new EquipmentEntity();org.springframework.beans.BeanUtils.copyProperties(e,x);return x;}
}
