package com.hospital.mes.system.api;

import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.hospital.mes.security.identity.LoginSnapshot;
import com.hospital.mes.system.application.PageResult;
import com.hospital.mes.system.application.AssignmentAdministration;
import com.hospital.mes.system.application.UserAdministration;
import com.hospital.mes.system.application.UserAdministration.CreatedUser;
import com.hospital.mes.system.application.UserAdministration.TemporaryPassword;
import com.hospital.mes.system.application.UserAdministration.UserView;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
public class UserAdminController {
    public record ProfileRequest(String displayName, Long expectedVersion) { }
    public record VersionRequest(Long expectedVersion) { }

    private final UserAdministration users;
    private final AssignmentAdministration assignments;
    private final TraceIdProvider traces;

    public UserAdminController(UserAdministration users, AssignmentAdministration assignments,
                               TraceIdProvider traces) {
        this.users = users;
        this.assignments = assignments;
        this.traces = traces;
    }

    @GetMapping
    public ApiResponse<PageResult<UserView>> list(@RequestParam(name = "page", defaultValue = "0") int page,
                                                  @RequestParam(name = "size", defaultValue = "20") int size,
                                                  @RequestParam(name = "keyword", required = false) String keyword,
                                                  @RequestParam(name = "enabled", required = false) Boolean enabled) {
        return ApiResponse.success(users.list(page, size, keyword, enabled), traces.currentTraceId());
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserView> get(@PathVariable("userId") long userId) {
        return ApiResponse.success(users.get(userId), traces.currentTraceId());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreatedUser>> create(
            @RequestBody UserAdministration.CreateUser command, Authentication authentication) {
        CreatedUser created = users.create(command, actorId(authentication), traces.currentTraceId());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .body(ApiResponse.success(created, traces.currentTraceId()));
    }

    @PatchMapping("/{userId}/profile")
    public ApiResponse<UserView> profile(@PathVariable("userId") long userId, @RequestBody ProfileRequest request,
                                         Authentication authentication) {
        return ApiResponse.success(users.profile(userId, request.displayName(), version(request.expectedVersion()),
            actorId(authentication), traces.currentTraceId()), traces.currentTraceId());
    }

    @PostMapping("/{userId}/enable")
    public ApiResponse<UserView> enable(@PathVariable("userId") long userId, @RequestBody VersionRequest request,
                                        Authentication authentication) {
        return setEnabled(userId, true, request, authentication);
    }

    @PostMapping("/{userId}/disable")
    public ApiResponse<UserView> disable(@PathVariable("userId") long userId, @RequestBody VersionRequest request,
                                         Authentication authentication) {
        return setEnabled(userId, false, request, authentication);
    }

    @PostMapping("/{userId}/password-reset")
    public ResponseEntity<ApiResponse<TemporaryPassword>> resetPassword(@PathVariable("userId") long userId,
                                                                          Authentication authentication) {
        TemporaryPassword secret = users.resetPassword(userId, actorId(authentication), traces.currentTraceId());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .body(ApiResponse.success(secret, traces.currentTraceId()));
    }

    @PostMapping("/{userId}/roles/{roleId}")
    public ApiResponse<Void> grantRole(@PathVariable("userId") long userId,
                                       @PathVariable("roleId") long roleId, Authentication authentication) {
        assignments.grantUserRole(userId, roleId, actorId(authentication), traces.currentTraceId());
        return ApiResponse.success(null, traces.currentTraceId());
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ApiResponse<Void> revokeRole(@PathVariable("userId") long userId,
                                        @PathVariable("roleId") long roleId, Authentication authentication) {
        assignments.revokeUserRole(userId, roleId, actorId(authentication), traces.currentTraceId());
        return ApiResponse.success(null, traces.currentTraceId());
    }

    private ApiResponse<UserView> setEnabled(long userId, boolean enabled, VersionRequest request,
                                             Authentication authentication) {
        return ApiResponse.success(users.setEnabled(userId, enabled, version(request.expectedVersion()),
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
