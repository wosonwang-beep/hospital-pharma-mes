package com.hospital.mes.masterdata.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("md_unit_conversion") public class UnitConversionEntity extends ScopedEntity {
    private Long fromUnitId;
    private Long toUnitId;
    private java.math.BigDecimal factor;
    private Long materialId;
    public Long getFromUnitId(){return fromUnitId;} public void setFromUnitId(Long v){fromUnitId=v;}
    public Long getToUnitId(){return toUnitId;} public void setToUnitId(Long v){toUnitId=v;}
    public java.math.BigDecimal getFactor(){return factor;} public void setFactor(java.math.BigDecimal v){factor=v;}
    public Long getMaterialId(){return materialId;} public void setMaterialId(Long v){materialId=v;}
}
