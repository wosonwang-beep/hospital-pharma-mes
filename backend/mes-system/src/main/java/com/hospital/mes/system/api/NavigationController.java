package com.hospital.mes.system.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.hospital.mes.system.application.NavigationTree;
import com.hospital.mes.system.infrastructure.SysMenuEntity;
import com.hospital.mes.system.infrastructure.SysMenuMapper;
import com.hospital.mes.system.infrastructure.SysRoleMenuMapper;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

@RestController("databaseMenuNavigationController")
@RequestMapping("/api/v1/auth")
@ConditionalOnProperty(prefix="spring.datasource", name="url")
public class NavigationController {
    private final SysMenuMapper menus; private final SysRoleMenuMapper roleMenus; private final CurrentPlatformContextResolver contexts; private final TraceIdProvider traces;
    public NavigationController(SysMenuMapper menus, SysRoleMenuMapper roleMenus, CurrentPlatformContextResolver contexts, TraceIdProvider traces) { this.menus=menus;this.roleMenus=roleMenus;this.contexts=contexts;this.traces=traces; }
    @GetMapping("/navigation")
    public ApiResponse<List<NavigationTree.Node>> navigation() {
        var context = contexts.current();
        return ApiResponse.success(NavigationTree.project(menus.selectList(new LambdaQueryWrapper<SysMenuEntity>().eq(SysMenuEntity::getOrgId,context.organizationId())),context.permissionCodes(),new java.util.HashSet<>(roleMenus.effectiveMenuCodes(context.actorId(),context.organizationId()))),traces.currentTraceId());
    }
}
