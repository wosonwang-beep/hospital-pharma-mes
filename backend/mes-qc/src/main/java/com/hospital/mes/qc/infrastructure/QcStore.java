package com.hospital.mes.qc.infrastructure;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hospital.mes.masterdata.application.ScopedStore;
import com.hospital.mes.qc.domain.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Repository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@Repository @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class QcStore {
 private final SpecificationMapper specifications; private final SpecificationVersionMapper versions; private final SpecificationItemMapper items;
 private final ScopedStore<SpecificationEntity> roots; private final ScopedStore<SpecificationVersionEntity> revisions; private final ScopedStore<SpecificationItemEntity> lines;
 public QcStore(SpecificationMapper specifications,SpecificationVersionMapper versions,SpecificationItemMapper items){this.specifications=specifications;this.versions=versions;this.items=items;roots=new ScopedStore<>(specifications,List.of("specification_code","specification_name"),Map.of());revisions=new ScopedStore<>(versions,List.of(),Map.of());lines=new ScopedStore<>(items,List.of(),Map.of());}
 public SpecificationEntity root(long org,long id){try{return roots.get(org,id);}catch(NoSuchElementException e){throw new QcNotFoundException();}}
 public SpecificationVersionEntity version(long org,long id,boolean lock){try{return lock?revisions.lock(org,id):revisions.get(org,id);}catch(NoSuchElementException e){throw new QcNotFoundException();}}
 public List<SpecificationVersionEntity> versions(long org,long root){return versions.selectList(new QueryWrapper<SpecificationVersionEntity>().eq("org_id",org).eq("specification_id",root).orderByDesc("version_no_business","id"));}
 public List<SpecificationItemEntity> items(long org,long version,boolean activeOnly){var q=new QueryWrapper<SpecificationItemEntity>().eq("org_id",org).eq("specification_version_id",version);if(activeOnly)q.eq("active",true);return items.selectList(q.orderByAsc("item_code","id"));}
 public ScopedStore.PageData<SpecificationEntity> list(long org,int page,int size,String keyword,Long material,String state){if(page<0||page>1000000||size<1||size>100)throw QcSpecificationRules.invalid("Invalid pagination");var q=new QueryWrapper<SpecificationEntity>().eq("org_id",org);if(keyword!=null&&!keyword.isBlank()){String k=QcSpecificationRules.text(keyword,200);q.and(w->w.like("specification_code",k).or().like("specification_name",k));}if(material!=null)q.eq("material_id",material);if(state!=null){if(!Set.of("DRAFT","APPROVED","RETIRED").contains(state))throw QcSpecificationRules.invalid("Invalid version status");var ids=versions.selectList(new QueryWrapper<SpecificationVersionEntity>().select("specification_id").eq("org_id",org).eq("status",state)).stream().map(SpecificationVersionEntity::getSpecificationId).distinct().toList();if(ids.isEmpty())return new ScopedStore.PageData<>(List.of(),0,page,size);q.in("id",ids);}long count=specifications.selectCount(q);q.orderByDesc("id").last("LIMIT "+((long)page*size)+","+size);return new ScopedStore.PageData<>(specifications.selectList(q),count,page,size);}
 public void insertRoot(SpecificationEntity e,long org,long actor){roots.insert(e,org,actor);}
 public void insertVersion(SpecificationVersionEntity e,long org,long actor){revisions.insert(e,org,actor);}
 public void insertItem(SpecificationItemEntity e,long org,long actor){lines.insert(e,org,actor);}
 public void updateItem(SpecificationItemEntity e,long actor){lines.update(e,e.getVersionNo(),actor,List.of("itemName","required","resultType","lowerLimit","upperLimit","unitId","textAcceptanceCriteria","methodCode","methodVersion","active"));}
 public void updateVersion(SpecificationVersionEntity e,long expected,String previousState,long actor){var u=new UpdateWrapper<SpecificationVersionEntity>().eq("org_id",e.getOrgId()).eq("id",e.getId()).eq("version_no",expected).eq("status",previousState);e.setUpdatedBy(actor);e.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MILLIS));e.setVersionNo(expected+1);u.set("status",e.getStatus()).set("content_hash",e.getContentHash()).set("approved_by",e.getApprovedBy()).set("approved_at",e.getApprovedAt()).set("approval_signature_id",e.getApprovalSignatureId()).set("approval_reason",e.getApprovalReason()).set("retired_by",e.getRetiredBy()).set("retired_at",e.getRetiredAt()).set("retirement_signature_id",e.getRetirementSignatureId()).set("retirement_reason",e.getRetirementReason()).set("updated_by",actor).set("updated_at",e.getUpdatedAt()).set("version_no",expected+1);if(versions.update(null,u)!=1)throw QcSpecificationRules.conflict("RECORD_CHANGED","Record changed; reload before retrying");}
}
