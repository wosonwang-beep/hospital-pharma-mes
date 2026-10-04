package com.hospital.mes.masterdata.infrastructure;
@org.springframework.stereotype.Repository @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class MaterialStore extends com.hospital.mes.masterdata.application.ScopedStore<MaterialEntity> {
 public MaterialStore(MaterialMapper mapper){super(mapper,java.util.List.of("material_code","material_name","material_type","specification","manufacturer_name"),java.util.Map.of("status","status","materialType","material_type","baseUnitId","base_unit_id"));}
 @Override protected void filter(com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MaterialEntity> query,String key,String column,String value){
  if(key.equals("status")&&value.equals("ACTIVE"))query.in(column,java.util.List.of("ACTIVE","DRAFT","APPROVED"));else super.filter(query,key,column,value);
 }
}
