package com.hospital.mes.system.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.mes.common.exception.ResourceConflictException;
import com.hospital.mes.system.infrastructure.SysPermissionEntity;
import com.hospital.mes.system.infrastructure.SysPermissionMapper;
import com.hospital.mes.system.infrastructure.SysRoleEntity;
import com.hospital.mes.system.infrastructure.SysRoleMapper;
import com.hospital.mes.system.infrastructure.SysRolePermissionMapper;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
public class RoleAdministration {
    public record CreateRole(String roleCode, String displayName) { }
    public record RoleView(long id, String roleCode, String displayName, boolean enabled,
                           long version, List<String> permissionCodes) { }
    public record PermissionView(long id, String permissionCode, String permissionType,
                                 String module, String displayName, boolean enabled) { }

    private final SysRoleMapper roles;
    private final SysRolePermissionMapper rolePermissions;
    private final SysPermissionMapper permissions;
    private final AdminSecurityEventWriter events;
    private final AdminSafetyGuard safety;

    public RoleAdministration(SysRoleMapper roles, SysRolePermissionMapper rolePermissions,
                              SysPermissionMapper permissions, AdminSecurityEventWriter events,
                              AdminSafetyGuard safety) {
        this.roles = roles;
        this.rolePermissions = rolePermissions;
        this.permissions = permissions;
        this.events = events;
        this.safety = safety;
    }

    @Transactional(readOnly = true)
    public PageResult<RoleView> list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid page");
        long offset = Math.multiplyExact((long) page, size);
        List<RoleView> items = roles.selectList(new LambdaQueryWrapper<SysRoleEntity>()
                .orderByAsc(SysRoleEntity::getId).last("LIMIT " + size + " OFFSET " + offset))
            .stream().map(this::view).toList();
        return new PageResult<>(items, roles.selectCount(null), page, size);
    }

    @Transactional(readOnly = true)
    public RoleView get(long roleId) { return view(required(roleId)); }

    @Transactional(readOnly = true)
    public List<PermissionView> catalog() {
        return permissions.selectList(new LambdaQueryWrapper<SysPermissionEntity>()
                .orderByAsc(SysPermissionEntity::getId)).stream()
            .map(p -> new PermissionView(p.getId(), p.getPermissionCode(), p.getPermissionType(),
                module(p.getPermissionCode()), p.getDisplayName(), Boolean.TRUE.equals(p.getEnabled())))
            .toList();
    }

    @Transactional
    public RoleView create(CreateRole command, long actorId, String traceId) {
        if (command == null || command.roleCode() == null
            || !command.roleCode().matches("[A-Z][A-Z0-9_]{1,127}"))
            throw new IllegalArgumentException("Invalid role code");
        String display = displayName(command.displayName());
        SysRoleEntity role = new SysRoleEntity();
        role.setRoleCode(command.roleCode());
        role.setDisplayName(display);
        role.setEnabled(true);
        role.setVersion(0L);
        roles.insert(role);
        events.append("ROLE_CREATED", actorId, null, role.getId(), null, traceId);
        return view(role);
    }

    @Transactional
    public RoleView rename(long roleId, String displayName, long expectedVersion,
                           long actorId, String traceId) {
        required(roleId);
        String display = displayName(displayName);
        if (roles.updateDisplayName(roleId, expectedVersion, display, actorId) != 1) throw conflict();
        events.append("ROLE_RENAMED", actorId, null, roleId, null, traceId);
        return view(required(roleId));
    }

    @Transactional
    public RoleView setEnabled(long roleId, boolean enabled, long expectedVersion,
                               long actorId, String traceId) {
        SysRoleEntity role = required(roleId);
        Runnable change = () -> {
            if (roles.updateEnabled(roleId, expectedVersion, enabled, actorId) != 1) throw conflict();
        };
        if (!enabled && "SYSTEM_ADMIN".equals(role.getRoleCode()))
            safety.runPreservingEffectiveAdmin(change);
        else change.run();
        events.append(enabled ? "ROLE_ENABLED" : "ROLE_DISABLED", actorId, null, roleId, null, traceId);
        return view(required(roleId));
    }

    private SysRoleEntity required(long roleId) {
        if (roleId < 1) throw new NoSuchElementException("Role not found");
        SysRoleEntity role = roles.selectById(roleId);
        if (role == null) throw new NoSuchElementException("Role not found");
        return role;
    }

    private RoleView view(SysRoleEntity role) {
        return new RoleView(role.getId(), role.getRoleCode(), role.getDisplayName(),
            Boolean.TRUE.equals(role.getEnabled()), role.getVersion(),
            rolePermissions.permissionCodes(role.getId()));
    }

    private static String displayName(String value) {
        if (value == null || value.isBlank() || value.length() > 128)
            throw new IllegalArgumentException("Invalid name");
        String stripped = value.strip();
        if (stripped.isBlank() || stripped.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Invalid name");
        return stripped;
    }

    private static String module(String code) {
        String[] parts = code.split(":", 3);
        return parts.length < 2 ? "" : parts[1];
    }

    private static ResourceConflictException conflict() {
        return new ResourceConflictException("ADMIN_CONFLICT", "Administrative change conflicts with current state");
    }
}
