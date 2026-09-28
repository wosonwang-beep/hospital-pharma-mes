package com.hospital.mes.system.infrastructure;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("sys_user")
public class SysUserEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String loginName;
    private String loginNameNormalized;
    private String displayName;
    private String passwordHash;
    private Boolean enabled;
    private Integer failedLoginCount;
    private LocalDateTime lockedUntil;
    private Boolean mustChangePassword;
    private Long version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLoginName() { return loginName; }
    public void setLoginName(String loginName) { this.loginName = loginName; }
    public String getLoginNameNormalized() { return loginNameNormalized; }
    public void setLoginNameNormalized(String value) { this.loginNameNormalized = value; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String value) { this.displayName = value; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String value) { this.passwordHash = value; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean value) { this.enabled = value; }
    public Integer getFailedLoginCount() { return failedLoginCount; }
    public void setFailedLoginCount(Integer value) { this.failedLoginCount = value; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime value) { this.lockedUntil = value; }
    public Boolean getMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(Boolean value) { this.mustChangePassword = value; }
    public Long getVersion() { return version; }
    public void setVersion(Long value) { this.version = value; }
}
