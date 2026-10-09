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
public class OrganizationService {
 private final OrganizationStore store; private final MasterMutation mutations;
 public OrganizationService(OrganizationStore store,MasterMutation mutations){this.store=store;this.mutations=mutations;}
 public ScopedStore.PageData<JsonNode> list(int page,int size,String keyword,Map<String,String> params){var c=mutations.context("master:org:view");var p=store.list(c.organizationId(),page,size,keyword,params);return new ScopedStore.PageData<>(p.items().stream().map(mutations::view).toList(),p.total(),p.page(),p.size());}
 public JsonNode get(String id){var c=mutations.context("master:org:view");return mutations.view(store.get(c.organizationId(),MasterMutation.id(id)));}
 @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public JsonNode create(OrganizationCommands.Create r,String key){var c=mutations.context("master:org:create");return mutations.execute(c,"Organization:CREATE",key,r,()->{var e=new OrganizationEntity();e.setParentId((r.parentId()==null?null:MasterMutation.id(r.parentId())));e.setOrgCode(MasterRules.text(r.orgCode(),64));e.setOrgName(MasterRules.text(r.orgName(),200));e.setOrgType(MasterRules.text(r.orgType(),200));e.setStatus("ACTIVE");validate(e,c.organizationId());store.insert(e,c.organizationId(),c.actorId());return e;});}
 @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public JsonNode command(String id,OrganizationCommands.Update r,String header,String key){var c=mutations.context("master:org:update");long expected=0L;String reason=MasterRules.text(r.reason()==null||r.reason().isBlank()?"基础资料维护（系统记录）":r.reason(),1000);return mutations.execute(c,"Organization:"+id,key,Map.of("id",id,"version",expected,"body",r),()->{var e=store.lock(c.organizationId(),MasterMutation.id(id));var before=copy(e);switch(MasterRules.text(r.action(),40)){case "UPDATE" -> {e.setParentId((r.parentId()==null?null:MasterMutation.id(r.parentId())));e.setOrgName(MasterRules.text(r.orgName(),200));}case "ENABLE" -> e.setStatus(MasterRules.enable(e.getStatus()));case "DISABLE" -> {if(store.count(c.organizationId(),"parent_id",e.getId(),"ACTIVE")>0)throw new MasterGateException("ORG_ACTIVE_CHILDREN","DISABLE_CHILDREN_FIRST");e.setStatus(MasterRules.disable(e.getStatus()));}default -> throw new IllegalArgumentException("Unknown action");}validate(e,c.organizationId());store.update(e,expected,c.actorId(),List.of("parentId","orgName","status"));return e;});}
 private void validate(OrganizationEntity e,long org){if(e.getParentId()==null)MasterRules.hierarchy(e.getOrgType(),null,null);else{if(e.getParentId().equals(e.getId()))throw new MasterGateException("ORG_HIERARCHY_INVALID","SELECT_COMPATIBLE_ACTIVE_PARENT");var p=store.lock(org,e.getParentId());MasterRules.hierarchy(e.getOrgType(),p.getOrgType(),p.getStatus());}}
 private OrganizationEntity copy(OrganizationEntity e){var x=new OrganizationEntity();org.springframework.beans.BeanUtils.copyProperties(e,x);return x;}
}
