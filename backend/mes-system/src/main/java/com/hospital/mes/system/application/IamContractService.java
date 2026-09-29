package com.hospital.mes.system.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.common.exception.ResourceConflictException;
import com.hospital.mes.security.session.SessionStore;
import com.hospital.mes.system.infrastructure.SysMenuEntity;
import com.hospital.mes.system.infrastructure.SysMenuMapper;
import com.hospital.mes.system.infrastructure.SysPermissionEntity;
import com.hospital.mes.system.infrastructure.SysPermissionMapper;
import com.hospital.mes.system.infrastructure.SysRoleEntity;
import com.hospital.mes.system.infrastructure.SysRoleMapper;
import com.hospital.mes.system.infrastructure.SysRoleMenuMapper;
import com.hospital.mes.system.infrastructure.SysRolePermissionMapper;
import com.hospital.mes.system.infrastructure.SysUserEntity;
import com.hospital.mes.system.infrastructure.SysUserMapper;
import com.hospital.mes.system.infrastructure.SysUserRoleMapper;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
public class IamContractService {
    private static final Pattern ROLE_CODE = Pattern.compile("[A-Z][A-Z0-9_]{1,127}");
    private static final Pattern PERMISSION_CODE = Pattern.compile("[a-z][a-z0-9-]*(?::[a-z][a-z0-9-]*){2}");

    public record CreateUserRequest(String username, String displayName, List<String> roleIds, String reason) { }
    public record UpdateUserRequest(String displayName, String status, String reason) { }
    public record AssignUserRolesRequest(List<String> roleIds, String reason) { }
    public record IamUserResponse(String id, String username, String displayName, String status,
                           long version, List<String> roleIds, List<String> roleNames,
                           String lastLoginAt, String updatedAt) { }
    public record CreatedUserView(IamUserResponse user, String temporaryPassword) { }

    public record CreateRoleRequest(String roleCode, String roleName, String reason) { }
    public record UpdateRoleRequest(String roleName, String status, String reason) { }
    public record AssignRolePermissionsRequest(List<String> permissionCodes, String reason) { }
    public record IamRoleResponse(String id, String roleCode, String roleName, String status,
                           long version, List<String> permissionCodes, List<String> menuCodes) { }

    public record CreatePermissionRequest(String permissionCode, String permissionName,
                                          String permissionType, String routePath, String status,
                                          String reason) { }
    public record UpdatePermissionRequest(String permissionName, String permissionType,
                                          String routePath, String status, String reason) { }
    public record IamPermissionResponse(String id, String permissionCode, String permissionName,
                                 String permissionType, String routePath, String status, long version) { }

    public record CreateMenuRequest(String menuCode, String menuName, String routePath,
                                    String parentId, Integer sortNo, String status, String reason) { }
    public record UpdateMenuRequest(String menuName, String routePath, String parentId,
                                    Integer sortNo, String status, String reason) { }
    public record IamMenuResponse(String id, String menuCode, String menuName, String routePath,
                           String parentId, int sortNo, String status, long version) { }

    private final SysUserMapper users;
    private final SysRoleMapper roles;
    private final SysPermissionMapper permissions;
    private final SysMenuMapper menus;
    private final SysUserRoleMapper userRoles;
    private final SysRolePermissionMapper rolePermissions;
    private final SysRoleMenuMapper roleMenus;
    private final UserAdministration userAdministration;
    private final SessionStore sessions;
    private final AdminSafetyGuard safety;
    private final IamAuditWriter audit;

    public IamContractService(SysUserMapper users, SysRoleMapper roles, SysPermissionMapper permissions,
                              SysMenuMapper menus, SysUserRoleMapper userRoles,
                              SysRolePermissionMapper rolePermissions, SysRoleMenuMapper roleMenus,
                              UserAdministration userAdministration, SessionStore sessions,
                              AdminSafetyGuard safety, IamAuditWriter audit) {
        this.users = users; this.roles = roles; this.permissions = permissions; this.menus = menus;
        this.userRoles = userRoles; this.rolePermissions = rolePermissions; this.roleMenus = roleMenus;
        this.userAdministration = userAdministration; this.sessions = sessions; this.safety = safety;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public PageResult<IamUserResponse> listUsers(int page, int size, String keyword, String status, String roleId) {
        page(page, size);
        Long selectedRole = optionalId(roleId);
        LambdaQueryWrapper<SysUserEntity> query = new LambdaQueryWrapper<SysUserEntity>()
            .and(text(keyword) != null, q -> q.like(SysUserEntity::getLoginName, text(keyword))
                .or().like(SysUserEntity::getDisplayName, text(keyword)))
            .eq(status(status) != null, SysUserEntity::getEnabled, enabled(status))
            .inSql(selectedRole != null, SysUserEntity::getId,
                selectedRole == null ? "SELECT user_id FROM sys_user_role WHERE 1=0"
                    : "SELECT user_id FROM sys_user_role WHERE role_id = " + selectedRole)
            .orderByAsc(SysUserEntity::getId);
        long total = users.selectCount(query);
        query.last("LIMIT " + size + " OFFSET " + Math.multiplyExact((long) page, size));
        return new PageResult<>(users.selectList(query).stream().map(this::userView).toList(), total, page, size);
    }

    @Transactional(readOnly = true)
    public IamUserResponse getUser(long id) { return userView(requireUser(id)); }

    @Transactional
    public CreatedUserView createUser(CreateUserRequest request, CurrentPlatformContext context, String key) {
        require(request, "User request is required");
        var created = userAdministration.create(new UserAdministration.CreateUser(
            request.username(), request.displayName()), context.actorId(), context.requestId());
        for (long roleId : ids(request.roleIds())) {
            requireRole(roleId);
            userRoles.grant(created.user().id(), roleId);
        }
        IamUserResponse view = userView(requireUser(created.user().id()));
        audit.append(context, "IAM_USER_CREATED", "USER", view.id(), null, view, request.reason(), key);
        return new CreatedUserView(view, created.temporaryPassword());
    }

    @Transactional
    public IamUserResponse updateUser(long id, long version, UpdateUserRequest request,
                               CurrentPlatformContext context, String key) {
        require(request, "User request is required");
        IamUserResponse before = getUser(id);
        String displayName = requiredText(request.displayName(), 128);
        boolean enabled = enabledRequired(request.status());
        Runnable update = () -> {
            if (users.updateContract(id, version, displayName, enabled, context.actorId()) != 1) throw conflict();
        };
        if (!enabled) safety.runPreservingEffectiveAdmin(update); else update.run();
        if (!enabled) sessions.revokeAllForUser(id);
        IamUserResponse after = getUser(id);
        audit.append(context, "IAM_USER_UPDATED", "USER", after.id(), before, after, request.reason(), key);
        return after;
    }

    @Transactional
    public IamUserResponse assignUserRoles(long id, long version, AssignUserRolesRequest request,
                                    CurrentPlatformContext context, String key) {
        require(request, "Role assignment is required");
        IamUserResponse before = getUser(id);
        Set<Long> roleIds = ids(request.roleIds());
        roleIds.forEach(this::requireRole);
        safety.runPreservingEffectiveAdmin(() -> {
            if (users.touchVersion(id, version, context.actorId()) != 1) throw conflict();
            userRoles.deleteForUser(id);
            roleIds.forEach(roleId -> userRoles.grant(id, roleId));
        });
        IamUserResponse after = getUser(id);
        audit.append(context, "IAM_USER_ROLES_ASSIGNED", "USER", after.id(), before, after, request.reason(), key);
        return after;
    }

    @Transactional(readOnly = true)
    public PageResult<IamRoleResponse> listRoles(int page, int size, String keyword, String status) {
        page(page, size);
        LambdaQueryWrapper<SysRoleEntity> query = new LambdaQueryWrapper<SysRoleEntity>()
            .and(text(keyword) != null, q -> q.like(SysRoleEntity::getRoleCode, text(keyword))
                .or().like(SysRoleEntity::getDisplayName, text(keyword)))
            .eq(status(status) != null, SysRoleEntity::getEnabled, enabled(status))
            .orderByAsc(SysRoleEntity::getId);
        long total = roles.selectCount(query);
        query.last("LIMIT " + size + " OFFSET " + Math.multiplyExact((long) page, size));
        return new PageResult<>(roles.selectList(query).stream().map(this::roleView).toList(), total, page, size);
    }

    @Transactional(readOnly = true)
    public IamRoleResponse getRole(long id) { return roleView(requireRole(id)); }

    @Transactional
    public IamRoleResponse createRole(CreateRoleRequest request, CurrentPlatformContext context, String key) {
        require(request, "Role request is required");
        if (request.roleCode() == null || !ROLE_CODE.matcher(request.roleCode()).matches())
            throw new IllegalArgumentException("Invalid role code");
        SysRoleEntity role = new SysRoleEntity();
        role.setRoleCode(request.roleCode()); role.setDisplayName(requiredText(request.roleName(), 128));
        role.setEnabled(true); role.setVersion(0L);
        roles.insert(role);
        IamRoleResponse view = roleView(role);
        audit.append(context, "IAM_ROLE_CREATED", "ROLE", view.id(), null, view, request.reason(), key);
        return view;
    }

    @Transactional
    public IamRoleResponse updateRole(long id, long version, UpdateRoleRequest request,
                               CurrentPlatformContext context, String key) {
        require(request, "Role request is required");
        IamRoleResponse before = getRole(id);
        boolean enabled = enabledRequired(request.status());
        Runnable update = () -> {
            if (roles.updateContract(id, version, requiredText(request.roleName(), 128), enabled,
                context.actorId()) != 1) throw conflict();
        };
        if (!enabled) safety.runPreservingEffectiveAdmin(update); else update.run();
        IamRoleResponse after = getRole(id);
        audit.append(context, "IAM_ROLE_UPDATED", "ROLE", after.id(), before, after, request.reason(), key);
        return after;
    }

    @Transactional
    public IamRoleResponse assignRolePermissions(long id, long version, AssignRolePermissionsRequest request,
                                          CurrentPlatformContext context, String key) {
        require(request, "Permission assignment is required");
        IamRoleResponse before = getRole(id);
        Set<String> codes = strings(request.permissionCodes(), 160);
        codes.forEach(this::requirePermission);
        safety.runPreservingEffectiveAdmin(() -> {
            if (roles.touchVersion(id, version, context.actorId()) != 1) throw conflict();
            rolePermissions.deleteForRole(id);
            codes.forEach(code -> rolePermissions.grant(id, requirePermission(code).getId()));
            roleMenus.deleteForRole(id);
            codes.forEach(code -> {
                if (menus.selectCount(new LambdaQueryWrapper<SysMenuEntity>()
                    .eq(SysMenuEntity::getOrgId, context.organizationId())
                    .eq(SysMenuEntity::getMenuCode, code)) > 0) {
                    roleMenus.grant(context.organizationId(), id, code, context.actorId());
                }
            });
        });
        IamRoleResponse after = getRole(id);
        audit.append(context, "IAM_ROLE_PERMISSIONS_ASSIGNED", "ROLE", after.id(), before, after,
            request.reason(), key);
        return after;
    }

    @Transactional(readOnly = true)
    public PageResult<IamPermissionResponse> listPermissions(int page, int size, String keyword, String status) {
        page(page, size);
        LambdaQueryWrapper<SysPermissionEntity> query = new LambdaQueryWrapper<SysPermissionEntity>()
            .and(text(keyword) != null, q -> q.like(SysPermissionEntity::getPermissionCode, text(keyword))
                .or().like(SysPermissionEntity::getDisplayName, text(keyword)))
            .eq(status(status) != null, SysPermissionEntity::getEnabled, enabled(status))
            .orderByAsc(SysPermissionEntity::getId);
        long total = permissions.selectCount(query);
        query.last("LIMIT " + size + " OFFSET " + Math.multiplyExact((long) page, size));
        return new PageResult<>(permissions.selectList(query).stream().map(this::permissionView).toList(), total, page, size);
    }

    @Transactional(readOnly = true)
    public IamPermissionResponse getPermission(long id) { return permissionView(requirePermission(id)); }

    @Transactional
    public IamPermissionResponse createPermission(CreatePermissionRequest request, CurrentPlatformContext context, String key) {
        require(request, "Permission request is required");
        validatePermissionCode(request.permissionCode());
        SysPermissionEntity permission = new SysPermissionEntity();
        permission.setPermissionCode(request.permissionCode());
        permission.setDisplayName(requiredText(request.permissionName(), 128));
        permission.setPermissionType(permissionType(request.permissionType()));
        permission.setMenuRoute(route(request.routePath()));
        permission.setEnabled(enabledRequired(request.status())); permission.setVersion(0L);
        permissions.insert(permission);
        IamPermissionResponse view = permissionView(permission);
        audit.append(context, "IAM_PERMISSION_CREATED", "PERMISSION", view.id(), null, view, request.reason(), key);
        return view;
    }

    @Transactional
    public IamPermissionResponse updatePermission(long id, long version, UpdatePermissionRequest request,
                                           CurrentPlatformContext context, String key) {
        require(request, "Permission request is required");
        IamPermissionResponse before = getPermission(id);
        if (permissions.updateContract(id, version, permissionType(request.permissionType()),
            requiredText(request.permissionName(), 128), route(request.routePath()), null,
            enabledRequired(request.status())) != 1) throw conflict();
        IamPermissionResponse after = getPermission(id);
        audit.append(context, "IAM_PERMISSION_UPDATED", "PERMISSION", after.id(), before, after,
            request.reason(), key);
        return after;
    }

    @Transactional(readOnly = true)
    public PageResult<IamMenuResponse> listMenus(int page, int size, String keyword, String status, long orgId) {
        page(page, size);
        LambdaQueryWrapper<SysMenuEntity> query = new LambdaQueryWrapper<SysMenuEntity>()
            .eq(SysMenuEntity::getOrgId, orgId)
            .and(text(keyword) != null, q -> q.like(SysMenuEntity::getMenuCode, text(keyword))
                .or().like(SysMenuEntity::getMenuName, text(keyword)))
            .eq(status(status) != null, SysMenuEntity::getStatus, status(status))
            .orderByAsc(SysMenuEntity::getSortNo).orderByAsc(SysMenuEntity::getId);
        long total = menus.selectCount(query);
        query.last("LIMIT " + size + " OFFSET " + Math.multiplyExact((long) page, size));
        return new PageResult<>(menus.selectList(query).stream().map(IamContractService::menuView).toList(), total, page, size);
    }

    @Transactional(readOnly = true)
    public IamMenuResponse getMenu(long id, long orgId) { return menuView(requireMenu(id, orgId)); }

    @Transactional
    public IamMenuResponse createMenu(CreateMenuRequest request, CurrentPlatformContext context, String key) {
        require(request, "Menu request is required");
        SysMenuEntity menu = new SysMenuEntity();
        menu.setOrgId(context.organizationId()); menu.setParentId(optionalId(request.parentId()));
        validateParent(menu.getParentId(), context.organizationId(), null);
        menu.setMenuCode(requiredText(request.menuCode(), 64)); menu.setMenuName(requiredText(request.menuName(), 100));
        menu.setRoutePath(route(request.routePath())); menu.setSortNo(sort(request.sortNo()));
        menu.setStatus(statusRequired(request.status())); menu.setCreatedBy(context.actorId());
        menu.setUpdatedBy(context.actorId()); menu.setVersionNo(0L);
        menus.insert(menu);
        IamMenuResponse view = menuView(menu);
        audit.append(context, "IAM_MENU_CREATED", "MENU", view.id(), null, view, request.reason(), key);
        return view;
    }

    @Transactional
    public IamMenuResponse updateMenu(long id, long version, UpdateMenuRequest request,
                               CurrentPlatformContext context, String key) {
        require(request, "Menu request is required");
        IamMenuResponse before = getMenu(id, context.organizationId());
        Long parentId = optionalId(request.parentId());
        validateParent(parentId, context.organizationId(), id);
        if (menus.updateContract(id, context.organizationId(), version, parentId,
            requiredText(request.menuName(), 100), route(request.routePath()), sort(request.sortNo()),
            statusRequired(request.status()), context.actorId()) != 1) throw conflict();
        IamMenuResponse after = getMenu(id, context.organizationId());
        audit.append(context, "IAM_MENU_UPDATED", "MENU", after.id(), before, after, request.reason(), key);
        return after;
    }

    private IamUserResponse userView(SysUserEntity user) {
        return new IamUserResponse(Long.toString(user.getId()), user.getLoginName(), user.getDisplayName(),
            state(Boolean.TRUE.equals(user.getEnabled())), value(user.getVersion()),
            userRoles.activeRoleIds(user.getId()).stream().map(String::valueOf).toList(),
            userRoles.activeRoleNames(user.getId()), time(users.lastSuccessfulLoginAt(user.getId())),
            time(user.getUpdatedAt()));
    }
    private IamRoleResponse roleView(SysRoleEntity role) {
        return new IamRoleResponse(Long.toString(role.getId()), role.getRoleCode(), role.getDisplayName(),
            state(Boolean.TRUE.equals(role.getEnabled())), value(role.getVersion()),
            rolePermissions.permissionCodes(role.getId()), roleMenus.menuCodes(role.getId()));
    }
    private IamPermissionResponse permissionView(SysPermissionEntity permission) {
        return new IamPermissionResponse(Long.toString(permission.getId()), permission.getPermissionCode(),
            permission.getDisplayName(), permission.getPermissionType(), permission.getMenuRoute(),
            state(Boolean.TRUE.equals(permission.getEnabled())), value(permission.getVersion()));
    }
    private static IamMenuResponse menuView(SysMenuEntity menu) {
        return new IamMenuResponse(Long.toString(menu.getId()), menu.getMenuCode(), menu.getMenuName(),
            menu.getRoutePath(), menu.getParentId() == null ? null : Long.toString(menu.getParentId()),
            menu.getSortNo(), menu.getStatus(), value(menu.getVersionNo()));
    }

    private SysUserEntity requireUser(long id) { return required(id, () -> users.selectById(id), "User"); }
    private SysRoleEntity requireRole(long id) { return required(id, () -> roles.selectById(id), "Role"); }
    private SysPermissionEntity requirePermission(long id) { return required(id, () -> permissions.selectById(id), "Permission"); }
    private SysPermissionEntity requirePermission(String code) {
        SysPermissionEntity value = permissions.selectOne(new LambdaQueryWrapper<SysPermissionEntity>()
            .eq(SysPermissionEntity::getPermissionCode, code));
        if (value == null) throw new NoSuchElementException("Permission not found");
        return value;
    }
    private SysMenuEntity requireMenu(long id, long orgId) {
        if (id < 1) throw new NoSuchElementException("Menu not found");
        SysMenuEntity value = menus.selectOne(new LambdaQueryWrapper<SysMenuEntity>()
            .eq(SysMenuEntity::getId, id).eq(SysMenuEntity::getOrgId, orgId));
        if (value == null) throw new NoSuchElementException("Menu not found");
        return value;
    }
    private void validateParent(Long parentId, long orgId, Long selfId) {
        if (parentId == null) return;
        if (parentId.equals(selfId)) throw new IllegalArgumentException("Menu cannot parent itself");
        requireMenu(parentId, orgId);
    }
    private static <T> T required(long id, Supplier<T> supplier, String name) {
        if (id < 1) throw new NoSuchElementException(name + " not found");
        T value = supplier.get();
        if (value == null) throw new NoSuchElementException(name + " not found");
        return value;
    }
    private static void require(Object value, String message) { if (value == null) throw new IllegalArgumentException(message); }
    private static void page(int page, int size) { if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid page"); }
    private static String text(String value) { return value == null || value.isBlank() ? null : value.strip(); }
    private static String requiredText(String value, int max) {
        String result = text(value);
        if (result == null || result.length() > max || result.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Invalid text");
        return result;
    }
    private static String route(String value) {
        if (value == null || value.isBlank()) return null;
        String route = requiredText(value, 255);
        if (!route.startsWith("/")) throw new IllegalArgumentException("Invalid route");
        return route;
    }
    private static String status(String value) {
        if (value == null || value.isBlank()) return null;
        if (!value.equals("ACTIVE") && !value.equals("INACTIVE")) throw new IllegalArgumentException("Invalid status");
        return value;
    }
    private static String statusRequired(String value) {
        String result = status(value); if (result == null) throw new IllegalArgumentException("Status is required"); return result;
    }
    private static boolean enabled(String status) { return "ACTIVE".equals(status); }
    private static boolean enabledRequired(String status) { return enabled(statusRequired(status)); }
    private static String state(boolean enabled) { return enabled ? "ACTIVE" : "INACTIVE"; }
    private static String permissionType(String value) {
        if (!"MENU".equals(value) && !"ACTION".equals(value)) throw new IllegalArgumentException("Invalid permission type");
        return value;
    }
    private static void validatePermissionCode(String value) {
        if (value == null || value.length() > 160 || !PERMISSION_CODE.matcher(value).matches())
            throw new IllegalArgumentException("Invalid permission code");
    }
    private static Set<Long> ids(List<String> values) {
        if (values == null) return Set.of();
        LinkedHashSet<Long> result = new LinkedHashSet<>();
        for (String value : values) {
            try { long id = Long.parseLong(value); if (id < 1 || !result.add(id)) throw new IllegalArgumentException("Invalid IDs"); }
            catch (NumberFormatException ex) { throw new IllegalArgumentException("Invalid IDs", ex); }
        }
        return result;
    }
    private static Set<String> strings(List<String> values, int max) {
        if (values == null) return Set.of();
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String value : values) {
            String item = requiredText(value, max);
            if (!result.add(item)) throw new IllegalArgumentException("Duplicate value");
        }
        return result;
    }
    private static Long optionalId(String value) {
        if (value == null || value.isBlank()) return null;
        try { long id = Long.parseLong(value); if (id < 1) throw new IllegalArgumentException("Invalid ID"); return id; }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("Invalid ID", ex); }
    }
    private static int sort(Integer value) { if (value == null || value < 0) throw new IllegalArgumentException("Invalid sort number"); return value; }
    private static long value(Long value) { return value == null ? 0 : value; }
    private static String time(java.time.LocalDateTime value) { return value == null ? null : value.toString(); }
    private static ResourceConflictException conflict() {
        return new ResourceConflictException("IAM_CONFLICT", "IAM resource conflicts with current state");
    }
}
