package com.hospital.mes.qc.infrastructure;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hospital.mes.masterdata.application.ScopedStore;
import com.hospital.mes.qc.domain.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Repository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
/** Persistence of the QC quality standard basic library (DCP-QC-SPEC-BASIC-LIBRARY-001). */
@Repository @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class QcStore {
 private final SpecificationMapper specifications; private final SpecificationItemMapper items; private final SpecificationScopeMapper scopes; private final SpecificationRevisionMapper revisions;
 private final ScopedStore<SpecificationEntity> roots; private final ScopedStore<SpecificationItemEntity> lines; private final ScopedStore<SpecificationScopeEntity> links; private final ScopedStore<SpecificationRevisionEntity> edits;
 public QcStore(SpecificationMapper specifications,SpecificationItemMapper items,SpecificationScopeMapper scopes,SpecificationRevisionMapper revisions){this.specifications=specifications;this.items=items;this.scopes=scopes;this.revisions=revisions;roots=new ScopedStore<>(specifications,List.of("specification_code","specification_name"),Map.of());lines=new ScopedStore<>(items,List.of(),Map.of());links=new ScopedStore<>(scopes,List.of(),Map.of());edits=new ScopedStore<>(revisions,List.of(),Map.of());}
 public SpecificationEntity root(long org,long id){try{return roots.get(org,id);}catch(NoSuchElementException e){throw new QcNotFoundException();}}
 public SpecificationEntity lockRoot(long org,long id){try{return roots.lock(org,id);}catch(NoSuchElementException e){throw new QcNotFoundException();}}
 public List<SpecificationItemEntity> items(long org,long specification){return items.selectList(new QueryWrapper<SpecificationItemEntity>().eq("org_id",org).eq("specification_id",specification).orderByAsc("item_code","id"));}
 public List<SpecificationScopeEntity> scopes(long org,long specification){return scopes.selectList(new QueryWrapper<SpecificationScopeEntity>().eq("org_id",org).eq("specification_id",specification).orderByAsc("subject_type","subject_id"));}
 public long scopeCount(long org,long specification){return scopes.selectCount(new QueryWrapper<SpecificationScopeEntity>().eq("org_id",org).eq("specification_id",specification));}
 public List<SpecificationRevisionEntity> revisions(long org,long specification,int limit){return revisions.selectList(new QueryWrapper<SpecificationRevisionEntity>().eq("org_id",org).eq("specification_id",specification).orderByDesc("revision_no","id").last("LIMIT "+limit));}
 public int nextRevisionNo(long org,long specification){var list=revisions.selectList(new QueryWrapper<SpecificationRevisionEntity>().select("revision_no").eq("org_id",org).eq("specification_id",specification).orderByDesc("revision_no").last("LIMIT 1"));return list.isEmpty()?1:list.get(0).getRevisionNo()+1;}
 /**
  * Page of standards. {@code material}/{@code product} filter through the applicability table so one
  * standard bound to several subjects is returned once; keyword matches code, name or basis reference.
  */
 public ScopedStore.PageData<SpecificationEntity> list(long org,int page,int size,String keyword,String basisType,Long material,Long product,String status){if(page<0||page>1000000||size<1||size>100)throw QcSpecificationRules.invalid("Invalid pagination");var q=new QueryWrapper<SpecificationEntity>().eq("org_id",org);if(keyword!=null&&!keyword.isBlank()){String k=QcSpecificationRules.text(keyword,200);q.and(w->w.like("specification_code",k).or().like("specification_name",k).or().like("basis_reference",k));}if(basisType!=null&&!basisType.isBlank())q.eq("basis_type",QcSpecificationRules.basisType(basisType));if(status!=null)q.eq("status",QcSpecificationRules.status(status));for(var subject:List.of(new Object[]{"MATERIAL",material},new Object[]{"PRODUCT",product})){if(subject[1]==null)continue;var matched=scopes.selectList(new QueryWrapper<SpecificationScopeEntity>().eq("org_id",org).eq("subject_type",subject[0]).eq("subject_id",subject[1])).stream().map(SpecificationScopeEntity::getSpecificationId).distinct().toList();if(matched.isEmpty())return new ScopedStore.PageData<>(List.of(),0,page,size);q.in("id",matched);}long count=specifications.selectCount(q);q.orderByDesc("id").last("LIMIT "+((long)page*size)+","+size);return new ScopedStore.PageData<>(specifications.selectList(q),count,page,size);}
 public void insertRoot(SpecificationEntity e,long org,long actor){roots.insert(e,org,actor);}
 public void insertItem(SpecificationItemEntity e,long org,long actor){lines.insert(e,org,actor);}
 public void insertScope(SpecificationScopeEntity e,long org,long actor){links.insert(e,org,actor);}
 public void insertRevision(SpecificationRevisionEntity e,long org,long actor){edits.insert(e,org,actor);}
 public void deleteScope(long org,long id){scopes.delete(new QueryWrapper<SpecificationScopeEntity>().eq("org_id",org).eq("id",id));}
 public void deleteItem(long org,long id){items.delete(new QueryWrapper<SpecificationItemEntity>().eq("org_id",org).eq("id",id));}
 public void deleteRoot(long org,long id){specifications.delete(new QueryWrapper<SpecificationEntity>().eq("org_id",org).eq("id",id));}
 public void updateItem(SpecificationItemEntity e,long actor){lines.update(e,e.getVersionNo(),actor,List.of("itemCode","itemName","required","resultType","lowerLimit","upperLimit","unitId","textAcceptanceCriteria","methodCode","methodVersion"));}
 /** Compare-and-swap on the record token; a stale client token fails closed instead of overwriting. */
 public void updateRoot(SpecificationEntity e,long expected,long actor){var u=new UpdateWrapper<SpecificationEntity>().eq("org_id",e.getOrgId()).eq("id",e.getId()).eq("version_no",expected);e.setUpdatedBy(actor);e.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MILLIS));e.setVersionNo(expected+1);u.set("specification_code",e.getSpecificationCode()).set("specification_name",e.getSpecificationName()).set("basis_type",e.getBasisType()).set("basis_reference",e.getBasisReference()).set("effective_date",e.getEffectiveDate()).set("remark",e.getRemark()).set("status",e.getStatus()).set("updated_by",actor).set("updated_at",e.getUpdatedAt()).set("version_no",expected+1);if(specifications.update(null,u)!=1)throw QcSpecificationRules.conflict("RECORD_CHANGED","Record changed; reload before retrying");}
 /** Bumps the record token after child rows changed so a concurrent editor is detected. */
 public void touchRoot(SpecificationEntity e,long expected,long actor){var u=new UpdateWrapper<SpecificationEntity>().eq("org_id",e.getOrgId()).eq("id",e.getId()).eq("version_no",expected);e.setUpdatedBy(actor);e.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MILLIS));e.setVersionNo(expected+1);u.set("updated_by",actor).set("updated_at",e.getUpdatedAt()).set("version_no",expected+1);if(specifications.update(null,u)!=1)throw QcSpecificationRules.conflict("RECORD_CHANGED","Record changed; reload before retrying");}
}
