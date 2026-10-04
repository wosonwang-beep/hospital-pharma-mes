package com.hospital.mes.masterdata.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("md_organization") public class OrganizationEntity extends ScopedEntity {
    private Long parentId;
    private String orgCode;
    private String orgName;
    private String orgType;
    private String status;
    public Long getParentId(){return parentId;} public void setParentId(Long v){parentId=v;}
    public String getOrgCode(){return orgCode;} public void setOrgCode(String v){orgCode=v;}
    public String getOrgName(){return orgName;} public void setOrgName(String v){orgName=v;}
    public String getOrgType(){return orgType;} public void setOrgType(String v){orgType=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of("UPDATE", "INACTIVE".equals(status)?"ENABLE":"DISABLE");}
}
