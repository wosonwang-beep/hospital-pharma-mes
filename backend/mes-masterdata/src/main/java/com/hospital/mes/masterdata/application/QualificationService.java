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
public class QualificationService {
 private final QualificationStore store; private final MasterMutation mutations;
 public QualificationService(QualificationStore store,MasterMutation mutations){this.store=store;this.mutations=mutations;}
 public ScopedStore.PageData<JsonNode> list(int page,int size,String keyword,Map<String,String> params){var c=mutations.context("master:qualification:view");var p=store.list(c.organizationId(),page,size,keyword,params);return new ScopedStore.PageData<>(p.items().stream().map(mutations::view).toList(),p.total(),p.page(),p.size());}
 public JsonNode get(String id){var c=mutations.context("master:qualification:view");return mutations.view(store.get(c.organizationId(),MasterMutation.id(id)));}
 @Transactional public JsonNode create(QualificationCommands.Create r,String key){var c=mutations.context("master:qualification:create");return mutations.execute(c,"Qualification:CREATE",key,r,()->{var e=new QualificationEntity();e.setUserId((r.userId()==null?null:MasterMutation.id(r.userId())));e.setQualificationCode(MasterRules.text(r.qualificationCode(),64));e.setValidFrom(r.validFrom());e.setValidTo(r.validTo());e.setStatus("ACTIVE");validate(e,c.organizationId());store.insert(e,c.organizationId(),c.actorId());mutations.audit(c,"Qualification:CREATE","Qualification",null,e,null,key);return e;});}
 @Transactional public JsonNode command(String id,QualificationCommands.Update r,String header,String key){var c=mutations.context("master:qualification:update");long expected=MasterMutation.version(header,r.versionNo());String reason=MasterRules.text(r.reason(),1000);return mutations.execute(c,"Qualification:"+id,key,Map.of("id",id,"version",expected,"body",r),()->{var e=store.lock(c.organizationId(),MasterMutation.id(id));if(e.getVersionNo()!=expected)throw new ResourceConflictException("VERSION_CONFLICT","Record changed; reload");var before=copy(e);switch(MasterRules.text(r.action(),40)){case "UPDATE" -> {e.setValidFrom(r.validFrom());e.setValidTo(r.validTo());}case "ENABLE" -> e.setStatus(MasterRules.enable(e.getStatus()));case "DISABLE" -> {e.setStatus(MasterRules.disable(e.getStatus()));}default -> throw new IllegalArgumentException("Unknown action");}validate(e,c.organizationId());store.update(e,expected,c.actorId(),List.of("validFrom","validTo","status"));mutations.audit(c,"Qualification:"+r.action(),"Qualification",before,e,reason,key);return e;});}
 private void validate(QualificationEntity e,long org){if(e.getUserId()==null)throw new IllegalArgumentException("User required");MasterRules.period(e.getValidFrom(),e.getValidTo());}
 private QualificationEntity copy(QualificationEntity e){var x=new QualificationEntity();org.springframework.beans.BeanUtils.copyProperties(e,x);return x;}
}
