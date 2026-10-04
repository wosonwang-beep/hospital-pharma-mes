package com.hospital.mes.ebr.infrastructure;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospital.mes.masterdata.application.ScopedStore;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.util.*;
@org.springframework.stereotype.Repository @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class EbrStore {
private final Map<Class<?>,BaseMapper<?>> mappers=new HashMap<>();
private final Map<Class<?>,ScopedStore<?>> stores=new HashMap<>();
public EbrStore(TemplateMapper template,SectionMapper section,GroupMapper group,FormMapper form,FieldMapper field,OptionMapper option,RuleMapper rule,SignatureRuleMapper signatureRule,ReviewRuleMapper reviewRule){
mappers.put(TemplateEntity.class,template);stores.put(TemplateEntity.class,new ScopedStore<>(template,List.of("template_code"),Map.of("status","status","packageVersionId","package_version_id")));
mappers.put(SectionEntity.class,section);stores.put(SectionEntity.class,new ScopedStore<>(section,List.of(),Map.of()));
mappers.put(GroupEntity.class,group);stores.put(GroupEntity.class,new ScopedStore<>(group,List.of(),Map.of()));
mappers.put(FormEntity.class,form);stores.put(FormEntity.class,new ScopedStore<>(form,List.of(),Map.of()));
mappers.put(FieldEntity.class,field);stores.put(FieldEntity.class,new ScopedStore<>(field,List.of(),Map.of()));
mappers.put(OptionEntity.class,option);stores.put(OptionEntity.class,new ScopedStore<>(option,List.of(),Map.of()));
mappers.put(RuleEntity.class,rule);stores.put(RuleEntity.class,new ScopedStore<>(rule,List.of(),Map.of()));
mappers.put(SignatureRuleEntity.class,signatureRule);stores.put(SignatureRuleEntity.class,new ScopedStore<>(signatureRule,List.of(),Map.of()));
mappers.put(ReviewRuleEntity.class,reviewRule);stores.put(ReviewRuleEntity.class,new ScopedStore<>(reviewRule,List.of(),Map.of()));
}
@SuppressWarnings("unchecked") public <T extends ScopedEntity> ScopedStore<T> store(Class<T> type){return (ScopedStore<T>)stores.get(type);}
@SuppressWarnings("unchecked") public <T extends ScopedEntity> List<T> rows(Class<T> type,long org,String column,Object value){return ((BaseMapper<T>)mappers.get(type)).selectList(new QueryWrapper<T>().eq("org_id",org).eq(column,value).orderByAsc("id"));}
public List<TemplateEntity> family(long org,String code){return rows(TemplateEntity.class,org,"template_code",code);}
public TemplateEntity lock(long org,long id){var v=store(TemplateEntity.class).get(org,id);var family=family(org,v.getTemplateCode());store(TemplateEntity.class).lock(org,family.getFirst().getId());return store(TemplateEntity.class).lock(org,id);}
}
