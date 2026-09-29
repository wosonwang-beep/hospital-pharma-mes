package com.hospital.mes.system.infrastructure;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_permission")
public class SysPermissionEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String permissionCode;
    private String permissionType;
    private String displayName;
    private String menuRoute;
    private Long parentPermissionId;
    private Boolean enabled;
    private Long version;

    public Long getId() { return id; }
    public void setId(Long value) { id = value; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String value) { permissionCode = value; }
    public String getPermissionType() { return permissionType; }
    public void setPermissionType(String value) { permissionType = value; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String value) { displayName = value; }
    public String getMenuRoute() { return menuRoute; }
    public void setMenuRoute(String value) { menuRoute = value; }
    public Long getParentPermissionId() { return parentPermissionId; }
    public void setParentPermissionId(Long value) { parentPermissionId = value; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean value) { enabled = value; }
    public Long getVersion() { return version; }
    public void setVersion(Long value) { version = value; }
}
