package com.hospital.mes.system.infrastructure;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_user_role")
public class SysUserRoleEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private Long roleId;

    public Long getId() { return id; }
    public void setId(Long value) { id = value; }
    public Long getUserId() { return userId; }
    public void setUserId(Long value) { userId = value; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long value) { roleId = value; }
}
