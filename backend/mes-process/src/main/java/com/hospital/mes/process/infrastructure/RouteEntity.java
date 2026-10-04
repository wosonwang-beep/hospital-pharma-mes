package com.hospital.mes.process.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("proc_route_version")
public class RouteEntity extends ScopedEntity {
 private Long packageVersionId;
 public Long getPackageVersionId(){return packageVersionId;} public void setPackageVersionId(Long v){packageVersionId=v;}
 private String routeCode;
 public String getRouteCode(){return routeCode;} public void setRouteCode(String v){routeCode=v;}
 @TableField("version")
 private Integer businessVersion;
 public Integer getBusinessVersion(){return businessVersion;} public void setBusinessVersion(Integer v){businessVersion=v;}
}
