package com.hospital.mes.masterdata.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("md_unit") public class UnitEntity extends ScopedEntity {
    private String unitCode;
    private String unitName;
    private String dimension;
    private Integer scale;
    public String getUnitCode(){return unitCode;} public void setUnitCode(String v){unitCode=v;}
    public String getUnitName(){return unitName;} public void setUnitName(String v){unitName=v;}
    public String getDimension(){return dimension;} public void setDimension(String v){dimension=v;}
    public Integer getScale(){return scale;} public void setScale(Integer v){scale=v;}
}
