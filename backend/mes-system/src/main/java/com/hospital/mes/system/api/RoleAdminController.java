package com.hospital.mes.system.api;

import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.hospital.mes.security.identity.LoginSnapshot;
import com.hospital.mes.system.application.PageResult;
import com.hospital.mes.system.application.RoleAdministration;
import com.hospital.mes.system.application.RoleAdministration.PermissionView;
import com.hospital.mes.system.application.RoleAdministration.RoleView;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
public class RoleAdminController {
    public record NameRequest(String displayName, Long expectedVersion) { }
    public record VersionRequest(Long expectedVersion) { }

    private final RoleAdministration roles;
    private final TraceIdProvider traces;

    public RoleAdminController(RoleAdministration roles, TraceIdProvider traces) {
        this.roles = roles;
        this.traces = traces;
    }

    @GetMapping("/api/v1/admin/roles")
    public ApiResponse<PageResult<RoleView>> list(@RequestParam(name = "page", defaultValue = "0") int page,
                                                  @RequestParam(name = "size", defaultValue = "20") int size) {
        return ApiResponse.success(roles.list(page, size), traces.currentTraceId());
    }

    @GetMapping("/api/v1/admin/roles/{roleId}")
    public ApiResponse<RoleView> get(@PathVariable("roleId") long roleId) {
        return ApiResponse.success(roles.get(roleId), traces.currentTraceId());
    }

    @GetMapping("/api/v1/admin/permissions")
    public ApiResponse<List<PermissionView>> catalog() {
        return ApiResponse.success(roles.catalog(), traces.currentTraceId());
    }

    @PostMapping("/api/v1/admin/roles")
    public ApiResponse<RoleView> create(@RequestBody RoleAdministration.CreateRole command,
                                        Authentication authentication) {
        return ApiResponse.success(roles.create(command, actorId(authentication), traces.currentTraceId()),
            traces.currentTraceId());
    }

    @PatchMapping("/api/v1/admin/roles/{roleId}/name")
    public ApiResponse<RoleView> rename(@PathVariable("roleId") long roleId,
                                        @RequestBody NameRequest request, Authentication authentication) {
        return ApiResponse.success(roles.rename(roleId, request.displayName(), version(request.expectedVersion()),
            actorId(authentication), traces.currentTraceId()), traces.currentTraceId());
    }

    @PostMapping("/api/v1/admin/roles/{roleId}/enable")
    public ApiResponse<RoleView> enable(@PathVariable("roleId") long roleId,
                                        @RequestBody VersionRequest request, Authentication authentication) {
        return setEnabled(roleId, true, request, authentication);
    }

    @PostMapping("/api/v1/admin/roles/{roleId}/disable")
    public ApiResponse<RoleView> disable(@PathVariable("roleId") long roleId,
                                         @RequestBody VersionRequest request, Authentication authentication) {
        return setEnabled(roleId, false, request, authentication);
    }

    private ApiResponse<RoleView> setEnabled(long roleId, boolean enabled, VersionRequest request,
                                             Authentication authentication) {
        return ApiResponse.success(roles.setEnabled(roleId, enabled, version(request.expectedVersion()),
            actorId(authentication), traces.currentTraceId()), traces.currentTraceId());
    }

    private static long actorId(Authentication authentication) {
        return ((LoginSnapshot) authentication.getPrincipal()).userId();
    }

    private static long version(Long value) {
        if (value == null || value < 0) throw new IllegalArgumentException("Invalid expected version");
        return value;
    }
}
