package com.hospital.mes.process.infrastructure;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospital.mes.masterdata.application.ScopedStore;
import java.util.*;
@org.springframework.stereotype.Repository
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class PrescriptionStore {
 private final ScopedStore<PrescriptionEntity> prescriptions; private final ScopedStore<PrescriptionItemEntity> items;
 private final PrescriptionMapper pm; private final PrescriptionItemMapper im;
 public PrescriptionStore(PrescriptionMapper pm,PrescriptionItemMapper im){
  this.pm=pm;this.im=im;
  prescriptions=new ScopedStore<>(pm,List.of("prescription_code","prescription_name"),Map.of("status","status","productId","product_id","processPackageId","process_package_id"));
  items=new ScopedStore<>(im,List.of(),Map.of());
 }
 public ScopedStore<PrescriptionEntity> prescriptions(){return prescriptions;}
 public ScopedStore<PrescriptionItemEntity> items(){return items;}
 public ScopedStore.PageData<PrescriptionEntity> page(long org,int page,int size,String keyword,Map<String,String> filters){return prescriptions.list(org,page,size,keyword,filters);}
 public List<PrescriptionItemEntity> items(long org,long prescriptionId){return im.selectList(new QueryWrapper<PrescriptionItemEntity>().eq("org_id",org).eq("prescription_id",prescriptionId).orderByAsc("line_no"));}
 public void deleteItems(long org,long prescriptionId){im.delete(new QueryWrapper<PrescriptionItemEntity>().eq("org_id",org).eq("prescription_id",prescriptionId));}
 public PrescriptionEntity active(long org,long productId){
  return pm.selectOne(new QueryWrapper<PrescriptionEntity>().eq("org_id",org).eq("product_id",productId).eq("status","ACTIVE"));
 }
 public long activeCount(long org,long productId,long excludeId){
  return pm.selectCount(new QueryWrapper<PrescriptionEntity>().eq("org_id",org).eq("product_id",productId).eq("status","ACTIVE").ne("id",excludeId));
 }
}