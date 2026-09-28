package com.hospital.mes.system.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.mes.common.exception.ResourceConflictException;
import com.hospital.mes.system.infrastructure.SysPermissionEntity;
import com.hospital.mes.system.infrastructure.SysPermissionMapper;
import com.hospital.mes.system.infrastructure.SysRoleEntity;
import com.hospital.mes.system.infrastructure.SysRoleMapper;
import com.hospital.mes.system.infrastructure.SysRolePermissionMapper;
import com.hospital.mes.system.infrastructure.SysUserMapper;
import com.hospital.mes.system.infrastructure.SysUserRoleMapper;
import java.util.NoSuchElementException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

@Service
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
public class AssignmentAdministration {
    private final SysUserMapper users;
    private final SysRoleMapper roles;
    private final SysPermissionMapper permissions;
    private final SysUserRoleMapper userRoles;
    private final SysRolePermissionMapper rolePermissions;
    private final AdminSecurityEventWriter events;
    private final AdminSafetyGuard safety;

    public AssignmentAdministration(SysUserMapper users, SysRoleMapper roles,
                                    SysPermissionMapper permissions, SysUserRoleMapper userRoles,
                                    SysRolePermissionMapper rolePermissions,
                                    AdminSecurityEventWriter events, AdminSafetyGuard safety) {
        this.users = users;
        this.roles = roles;
        this.permissions = permissions;
        this.userRoles = userRoles;
        this.rolePermissions = rolePermissions;
        this.events = events;
        this.safety = safety;
    }

    @Transactional
    public void grantUserRole(long userId, long roleId, long actorId, String traceId) {
        requireUser(userId);
        requireRole(roleId);
        if (userRoles.pairCount(userId, roleId) != 0) throw conflict();
        safety.runUnderAdministratorLock(() -> userRoles.grant(userId, roleId));
        events.append("USER_ROLE_GRANTED", actorId, userId, roleId, null, traceId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void revokeUserRole(long userId, long roleId, long actorId, String traceId) {
        requireUser(userId);
        requireRole(roleId);
        Runnable change = () -> {
            if (userRoles.revoke(userId, roleId) != 1) throw conflict();
        };
        safety.runPreservingEffectiveAdmin(change);
        events.append("USER_ROLE_REVOKED", actorId, userId, roleId, null, traceId);
    }

    @Transactional
    public void grantRolePermission(long roleId, String permissionCode, long actorId, String traceId) {
        requireRole(roleId);
        SysPermissionEntity permission = requirePermission(permissionCode);
        if (rolePermissions.pairCount(roleId, permission.getId()) != 0) throw conflict();
        rolePermissions.grant(roleId, permission.getId());
        events.append("ROLE_PERMISSION_GRANTED", actorId, null, roleId, permission.getId(), traceId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void revokeRolePermission(long roleId, String permissionCode, long actorId, String traceId) {
        requireRole(roleId);
        SysPermissionEntity permission = requirePermission(permissionCode);
        Runnable change = () -> {
            if (rolePermissions.revoke(roleId, permission.getId()) != 1) throw conflict();
        };
        safety.runPreservingEffectiveAdmin(change);
        events.append("ROLE_PERMISSION_REVOKED", actorId, null, roleId, permission.getId(), traceId);
    }

    private void requireUser(long userId) {
        if (userId < 1 || users.selectById(userId) == null) throw new NoSuchElementException("User not found");
    }

    private SysRoleEntity requireRole(long roleId) {
        if (roleId < 1) throw new NoSuchElementException("Role not found");
        SysRoleEntity role = roles.selectById(roleId);
        if (role == null) throw new NoSuchElementException("Role not found");
        return role;
    }

    private SysPermissionEntity requirePermission(String code) {
        if (code == null || code.isBlank() || code.length() > 160)
            throw new IllegalArgumentException("Invalid permission code");
        SysPermissionEntity permission = permissions.selectOne(new LambdaQueryWrapper<SysPermissionEntity>()
            .eq(SysPermissionEntity::getPermissionCode, code));
        if (permission == null) throw new NoSuchElementException("Permission not found");
        return permission;
    }

    private static ResourceConflictException conflict() {
        return new ResourceConflictException("ADMIN_CONFLICT", "Assignment conflicts with current state");
    }

}
