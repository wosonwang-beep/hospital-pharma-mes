package com.hospital.mes.system.application;

import com.hospital.mes.system.infrastructure.SysMenuEntity;
import java.util.*;

/** A single database menu tree projected against effective permissions. Never grants permissions. */
public final class NavigationTree {
    private NavigationTree() { }
    public record Node(String id, String menuCode, String title, String path, String permissionCode,
                       List<String> requiredPermissions, List<Node> children) { }
    public static List<String> required(String csv) {
        return csv == null || csv.isBlank() ? List.of() : Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
    }
    public static List<Node> project(List<SysMenuEntity> menus, Set<String> permissions) {
        return project(menus, permissions, menus.stream().map(SysMenuEntity::getMenuCode).collect(java.util.stream.Collectors.toSet()));
    }
    public static List<Node> project(List<SysMenuEntity> menus, Set<String> permissions, Set<String> authorizedMenus) {
        Map<Long,List<SysMenuEntity>> children = new HashMap<>();
        for (SysMenuEntity menu : menus) children.computeIfAbsent(menu.getParentId() == null ? 0L : menu.getParentId(), k -> new ArrayList<>()).add(menu);
        return branch(0L, children, permissions, authorizedMenus, new HashSet<>());
    }
    private static List<Node> branch(long parent, Map<Long,List<SysMenuEntity>> children, Set<String> permissions, Set<String> authorizedMenus, Set<Long> ancestors) {
        List<Node> result = new ArrayList<>();
        for (SysMenuEntity menu : children.getOrDefault(parent, List.of()).stream().sorted(Comparator.comparing(SysMenuEntity::getSortNo).thenComparing(SysMenuEntity::getId)).toList()) {
            if (!"ACTIVE".equals(menu.getStatus()) || !ancestors.add(menu.getId())) continue;
            List<Node> nested = branch(menu.getId(), children, permissions, authorizedMenus, ancestors); ancestors.remove(menu.getId());
            boolean directory = menu.getRoutePath() == null || menu.getRoutePath().isBlank();
            List<String> required = required(menu.getRequiredPermissions());
            boolean allowed = authorizedMenus.contains(menu.getMenuCode()) && menu.getPermissionCode() != null && permissions.contains(menu.getPermissionCode()) && permissions.containsAll(required);
            if ((directory && !nested.isEmpty()) || (!directory && allowed)) result.add(new Node(String.valueOf(menu.getId()), menu.getMenuCode(), menu.getMenuName(), menu.getRoutePath(), menu.getPermissionCode(), required, nested));
        }
        return List.copyOf(result);
    }
}
