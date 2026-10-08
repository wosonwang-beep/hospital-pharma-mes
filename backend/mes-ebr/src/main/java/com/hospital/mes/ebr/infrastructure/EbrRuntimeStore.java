package com.hospital.mes.ebr.infrastructure;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospital.mes.masterdata.application.ScopedStore;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.util.*;
@org.springframework.stereotype.Repository @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class EbrRuntimeStore {
 private final Map<Class<?>,BaseMapper<?>> mappers=new HashMap<>();private final Map<Class<?>,ScopedStore<?>> stores=new HashMap<>();
 public EbrRuntimeStore(RuntimeBatchSnapshotMapper snapshots,RuntimeFormMapper forms,RuntimeValueMapper values,RuntimeRuleMapper rules,RuntimeReviewMapper reviews){add(RuntimeBatchSnapshotEntity.class,snapshots);add(RuntimeFormEntity.class,forms);add(RuntimeValueEntity.class,values);add(RuntimeRuleEntity.class,rules);add(RuntimeReviewEntity.class,reviews);}
 private <T extends ScopedEntity>void add(Class<T> c,BaseMapper<T> m){mappers.put(c,m);stores.put(c,new ScopedStore<>(m,List.of(),Map.of()));}
 @SuppressWarnings("unchecked")public <T extends ScopedEntity>ScopedStore<T> store(Class<T> c){return (ScopedStore<T>)stores.get(c);}
 @SuppressWarnings("unchecked")public <T extends ScopedEntity>List<T> rows(Class<T> c,long org,String column,Object id){return ((BaseMapper<T>)mappers.get(c)).selectList(new QueryWrapper<T>().eq("org_id",org).eq(column,id).orderByAsc("id"));}
 public RuntimeBatchSnapshotEntity snapshot(long org,long batch){return rows(RuntimeBatchSnapshotEntity.class,org,"main_batch_id",batch).stream().findFirst().orElseThrow(()->new NoSuchElementException("eBR batch snapshot not initialized"));}
 public List<RuntimeFormEntity> shared(long org,long batch){return rows(RuntimeFormEntity.class,org,"shared_main_batch_id",batch);}
 public List<RuntimeFormEntity> forms(long org,long operation){return rows(RuntimeFormEntity.class,org,"operation_execution_id",operation);}
 public List<RuntimeValueEntity> values(RuntimeFormEntity f){return rows(RuntimeValueEntity.class,f.getOrgId(),"form_instance_id",f.getId());}
 public List<RuntimeValueEntity> current(RuntimeFormEntity f){Map<String,RuntimeValueEntity> latest=new LinkedHashMap<>();for(var v:values(f))latest.put(v.getFieldCode()+"|"+v.getOccurrencePath(),v);return List.copyOf(latest.values());}
 public List<RuntimeReviewEntity> reviews(RuntimeFormEntity f){return rows(RuntimeReviewEntity.class,f.getOrgId(),"object_id",f.getId()).stream().filter(r->r.getObjectType().equals("EBR_FORM_INSTANCE")).toList();}
}
