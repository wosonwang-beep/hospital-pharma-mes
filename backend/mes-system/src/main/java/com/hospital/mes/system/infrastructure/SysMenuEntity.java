package com.hospital.mes.system.infrastructure;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_menu")
public class SysMenuEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private Long orgId;
    private Long parentId;
    private String menuCode;
    private String menuName;
    private String routePath;
    private Integer sortNo;
    private String status;
    private Long createdBy;
    private Long updatedBy;
    private Long versionNo;

    public Long getId() { return id; } public void setId(Long value) { id = value; }
    public Long getOrgId() { return orgId; } public void setOrgId(Long value) { orgId = value; }
    public Long getParentId() { return parentId; } public void setParentId(Long value) { parentId = value; }
    public String getMenuCode() { return menuCode; } public void setMenuCode(String value) { menuCode = value; }
    public String getMenuName() { return menuName; } public void setMenuName(String value) { menuName = value; }
    public String getRoutePath() { return routePath; } public void setRoutePath(String value) { routePath = value; }
    public Integer getSortNo() { return sortNo; } public void setSortNo(Integer value) { sortNo = value; }
    public String getStatus() { return status; } public void setStatus(String value) { status = value; }
    public Long getCreatedBy() { return createdBy; } public void setCreatedBy(Long value) { createdBy = value; }
    public Long getUpdatedBy() { return updatedBy; } public void setUpdatedBy(Long value) { updatedBy = value; }
    public Long getVersionNo() { return versionNo; } public void setVersionNo(Long value) { versionNo = value; }
}
