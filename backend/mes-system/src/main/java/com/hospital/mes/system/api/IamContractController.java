package com.hospital.mes.system.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.hospital.mes.system.application.IamContractService;
import com.hospital.mes.system.application.IamContractService.AssignRolePermissionsRequest;
import com.hospital.mes.system.application.IamContractService.AssignUserRolesRequest;
import com.hospital.mes.system.application.IamContractService.CreateMenuRequest;
import com.hospital.mes.system.application.IamContractService.CreatePermissionRequest;
import com.hospital.mes.system.application.IamContractService.CreateRoleRequest;
import com.hospital.mes.system.application.IamContractService.CreateUserRequest;
import com.hospital.mes.system.application.IamContractService.CreatedUserView;
import com.hospital.mes.system.application.IamContractService.IamMenuResponse;
import com.hospital.mes.system.application.IamContractService.IamPermissionResponse;
import com.hospital.mes.system.application.IamContractService.IamRoleResponse;
import com.hospital.mes.system.application.IamContractService.UpdateMenuRequest;
import com.hospital.mes.system.application.IamContractService.UpdatePermissionRequest;
import com.hospital.mes.system.application.IamContractService.UpdateRoleRequest;
import com.hospital.mes.system.application.IamContractService.UpdateUserRequest;
import com.hospital.mes.system.application.IamContractService.IamUserResponse;
import com.hospital.mes.system.application.IamMutationExecutor;
import com.hospital.mes.system.application.PageResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
public class IamContractController {
    private record MutationEnvelope(String id, Long version, Object body) { }

    private final IamContractService iam;
    private final IamMutationExecutor mutations;
    private final CurrentPlatformContextResolver contexts;
    private final TraceIdProvider traces;

    public IamContractController(IamContractService iam, IamMutationExecutor mutations,
                                 CurrentPlatformContextResolver contexts, TraceIdProvider traces) {
        this.iam = iam; this.mutations = mutations; this.contexts = contexts; this.traces = traces;
    }

    @GetMapping("/users")
    public ApiResponse<PageResult<IamUserResponse>> listUsers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "roleId", required = false) String roleId) {
        return success(iam.listUsers(page, size, keyword, status, roleId));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<JsonNode>> createUsers(
            @RequestHeader("Idempotency-Key") String key, @RequestBody CreateUserRequest request) {
        CurrentPlatformContext context = contexts.current();
        JsonNode result = mutations.execute(context, "createUsers", key, request, "USER",
            () -> iam.createUser(request, context, key), value -> value.user().id(),
            value -> new CreatedUserView(value.user(), null));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(success(result));
    }

    @GetMapping("/users/{id}")
    public ApiResponse<IamUserResponse> getUsers(@PathVariable("id") String id) {
        return success(iam.getUser(id(id)));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<JsonNode> updateUsers(@PathVariable("id") String id,
            @RequestHeader("Idempotency-Key") String key, @RequestHeader("If-Match") String ifMatch,
            @RequestBody UpdateUserRequest request) {
        long resourceId = id(id); long version = IamRequestVersion.parse(ifMatch);
        CurrentPlatformContext context = contexts.current();
        return success(mutations.execute(context, "updateUsers", key,
            new MutationEnvelope(id, version, request), "USER",
            () -> iam.updateUser(resourceId, version, request, context, key), IamUserResponse::id));
    }

    @PostMapping("/users/{id}/roles")
    public ApiResponse<JsonNode> assignUserRoles(@PathVariable("id") String id,
            @RequestHeader("Idempotency-Key") String key, @RequestHeader("If-Match") String ifMatch,
            @RequestBody AssignUserRolesRequest request) {
        long resourceId = id(id); long version = IamRequestVersion.parse(ifMatch);
        CurrentPlatformContext context = contexts.current();
        return success(mutations.execute(context, "assignUserRoles", key,
            new MutationEnvelope(id, version, request), "USER",
            () -> iam.assignUserRoles(resourceId, version, request, context, key), IamUserResponse::id));
    }

    @GetMapping("/roles")
    public ApiResponse<PageResult<IamRoleResponse>> listRoles(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status) {
        return success(iam.listRoles(page, size, keyword, status));
    }

    @PostMapping("/roles")
    public ApiResponse<JsonNode> createRoles(@RequestHeader("Idempotency-Key") String key,
                                              @RequestBody CreateRoleRequest request) {
        CurrentPlatformContext context = contexts.current();
        return success(mutations.execute(context, "createRoles", key, request, "ROLE",
            () -> iam.createRole(request, context, key), IamRoleResponse::id));
    }

    @GetMapping("/roles/{id}")
    public ApiResponse<IamRoleResponse> getRoles(@PathVariable("id") String id) { return success(iam.getRole(id(id))); }

    @PutMapping("/roles/{id}")
    public ApiResponse<JsonNode> updateRoles(@PathVariable("id") String id,
            @RequestHeader("Idempotency-Key") String key, @RequestHeader("If-Match") String ifMatch,
            @RequestBody UpdateRoleRequest request) {
        long resourceId = id(id); long version = IamRequestVersion.parse(ifMatch);
        CurrentPlatformContext context = contexts.current();
        return success(mutations.execute(context, "updateRoles", key,
            new MutationEnvelope(id, version, request), "ROLE",
            () -> iam.updateRole(resourceId, version, request, context, key), IamRoleResponse::id));
    }

    @PostMapping("/roles/{id}/permissions")
    public ApiResponse<JsonNode> assignRolePermissions(@PathVariable("id") String id,
            @RequestHeader("Idempotency-Key") String key, @RequestHeader("If-Match") String ifMatch,
            @RequestBody AssignRolePermissionsRequest request) {
        long resourceId = id(id); long version = IamRequestVersion.parse(ifMatch);
        CurrentPlatformContext context = contexts.current();
        return success(mutations.execute(context, "assignRolePermissions", key,
            new MutationEnvelope(id, version, request), "ROLE",
            () -> iam.assignRolePermissions(resourceId, version, request, context, key), IamRoleResponse::id));
    }

    @GetMapping("/permissions")
    public ApiResponse<PageResult<IamPermissionResponse>> listPermissions(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "100") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status) {
        return success(iam.listPermissions(page, size, keyword, status));
    }

    @PostMapping("/permissions")
    public ApiResponse<JsonNode> createPermissions(@RequestHeader("Idempotency-Key") String key,
                                                    @RequestBody CreatePermissionRequest request) {
        CurrentPlatformContext context = contexts.current();
        return success(mutations.execute(context, "createPermissions", key, request, "PERMISSION",
            () -> iam.createPermission(request, context, key), IamPermissionResponse::id));
    }

    @GetMapping("/permissions/{id}")
    public ApiResponse<IamPermissionResponse> getPermissions(@PathVariable("id") String id) {
        return success(iam.getPermission(id(id)));
    }

    @PutMapping("/permissions/{id}")
    public ApiResponse<JsonNode> updatePermissions(@PathVariable("id") String id,
            @RequestHeader("Idempotency-Key") String key, @RequestHeader("If-Match") String ifMatch,
            @RequestBody UpdatePermissionRequest request) {
        long resourceId = id(id); long version = IamRequestVersion.parse(ifMatch);
        CurrentPlatformContext context = contexts.current();
        return success(mutations.execute(context, "updatePermissions", key,
            new MutationEnvelope(id, version, request), "PERMISSION",
            () -> iam.updatePermission(resourceId, version, request, context, key), IamPermissionResponse::id));
    }

    @GetMapping("/menus")
    public ApiResponse<PageResult<IamMenuResponse>> listMenus(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "100") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status) {
        return success(iam.listMenus(page, size, keyword, status, contexts.current().organizationId()));
    }

    @PostMapping("/menus")
    public ApiResponse<JsonNode> createMenus(@RequestHeader("Idempotency-Key") String key,
                                              @RequestBody CreateMenuRequest request) {
        CurrentPlatformContext context = contexts.current();
        return success(mutations.execute(context, "createMenus", key, request, "MENU",
            () -> iam.createMenu(request, context, key), IamMenuResponse::id));
    }

    @GetMapping("/menus/{id}")
    public ApiResponse<IamMenuResponse> getMenus(@PathVariable("id") String id) {
        return success(iam.getMenu(id(id), contexts.current().organizationId()));
    }
    @PostMapping("/menus/{id}/permissions")
    public ApiResponse<JsonNode> assignMenuPermissions(@PathVariable("id") String id,
            @RequestHeader("Idempotency-Key") String key,@RequestHeader("If-Match") String ifMatch,
            @RequestBody IamContractService.AssignMenuPermissionsRequest request) {
        long resourceId=id(id),version=IamRequestVersion.parse(ifMatch);var context=contexts.current();
        return success(mutations.execute(context,"assignMenuPermissions",key,new MutationEnvelope(id,version,request),"MENU",
            ()->iam.assignMenuPermissions(resourceId,version,request,context,key),IamMenuResponse::id));
    }

    @PutMapping("/menus/{id}")
    public ApiResponse<JsonNode> updateMenus(@PathVariable("id") String id,
            @RequestHeader("Idempotency-Key") String key, @RequestHeader("If-Match") String ifMatch,
            @RequestBody UpdateMenuRequest request) {
        long resourceId = id(id); long version = IamRequestVersion.parse(ifMatch);
        CurrentPlatformContext context = contexts.current();
        return success(mutations.execute(context, "updateMenus", key,
            new MutationEnvelope(id, version, request), "MENU",
            () -> iam.updateMenu(resourceId, version, request, context, key), IamMenuResponse::id));
    }

    private <T> ApiResponse<T> success(T value) { return ApiResponse.success(value, traces.currentTraceId()); }
    private static long id(String value) {
        try { long id = Long.parseLong(value); if (id < 1) throw new IllegalArgumentException("Invalid ID"); return id; }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("Invalid ID", ex); }
    }
}
