package com.hospital.mes.masterdata.infrastructure;
import com.hospital.mes.masterdata.application.ScopedStore;
import org.springframework.stereotype.Repository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
@Repository @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class UnitConversionStore extends ScopedStore<UnitConversionEntity> {
 public boolean hasPair(com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UnitConversionEntity> q){return !mapper.selectList(q.last("FOR UPDATE")).isEmpty();}
 public boolean references(long org,long unit){var q=new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UnitConversionEntity>().eq("org_id",org).and(w->w.eq("from_unit_id",unit).or().eq("to_unit_id",unit)).last("FOR UPDATE");return !mapper.selectList(q).isEmpty();}
 public UnitConversionStore(UnitConversionMapper mapper){super(mapper,List.of("factor"),Map.of("fromUnitId","from_unit_id","toUnitId","to_unit_id","materialId","material_id"));}
}
