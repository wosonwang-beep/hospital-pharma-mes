package com.hospital.mes.system.infrastructure;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_role")
public class SysRoleEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String roleCode;
    private String displayName;
    private Boolean enabled;
    private Long version;

    public Long getId() { return id; }
    public void setId(Long value) { id = value; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String value) { roleCode = value; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String value) { displayName = value; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean value) { enabled = value; }
    public Long getVersion() { return version; }
    public void setVersion(Long value) { version = value; }
}
