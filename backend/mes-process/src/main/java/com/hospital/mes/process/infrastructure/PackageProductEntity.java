package com.hospital.mes.process.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("proc_package_product")
public class PackageProductEntity extends ScopedEntity {
 private Long packageId;
 private Long productId;
 public Long getPackageId(){return packageId;} public void setPackageId(Long value){packageId=value;}
 public Long getProductId(){return productId;} public void setProductId(Long value){productId=value;}
}
