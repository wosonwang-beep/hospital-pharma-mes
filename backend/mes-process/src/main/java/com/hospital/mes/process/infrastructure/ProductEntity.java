package com.hospital.mes.process.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("md_product")
public class ProductEntity extends ScopedEntity {
 private String productCode;
 public String getProductCode(){return productCode;} public void setProductCode(String v){productCode=v;}
 private String productName;
 public String getProductName(){return productName;} public void setProductName(String v){productName=v;}
 private String dosageForm;
 public String getDosageForm(){return dosageForm;} public void setDosageForm(String v){dosageForm=v;}
 private String specification;
 public String getSpecification(){return specification;} public void setSpecification(String v){specification=v;}
 private Long baseUnitId;
 public Long getBaseUnitId(){return baseUnitId;} public void setBaseUnitId(Long v){baseUnitId=v;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
 @Override public java.util.List<String> allowedActions(){return status.equals("ACTIVE")?java.util.List.of("UPDATE","DISABLE"):java.util.List.of("UPDATE");}
}
