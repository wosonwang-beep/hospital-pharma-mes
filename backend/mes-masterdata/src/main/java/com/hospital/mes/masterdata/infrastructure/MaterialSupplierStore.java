package com.hospital.mes.masterdata.infrastructure;
@org.springframework.stereotype.Repository @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class MaterialSupplierStore extends com.hospital.mes.masterdata.application.ScopedStore<MaterialSupplierEntity> {
 public MaterialSupplierStore(MaterialSupplierMapper mapper){super(false,mapper,java.util.List.of("id"),java.util.Map.of("materialId","material_id"));}
 public java.util.List<MaterialSupplierEntity> relationships(long org,long material,boolean lock){var q=new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MaterialSupplierEntity>().eq("org_id",org).eq("material_id",material).orderByAsc("supplier_id");if(lock)q.last("FOR UPDATE");return mapper.selectList(q);}
}
