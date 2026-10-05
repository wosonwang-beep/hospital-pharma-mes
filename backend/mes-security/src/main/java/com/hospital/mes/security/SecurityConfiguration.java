package com.hospital.mes.security;

import com.hospital.mes.security.web.SecurityErrorWriter;
import com.hospital.mes.security.web.SessionAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SessionAuthenticationFilter sessions,
                                            SecurityErrorWriter errors) throws Exception {
        return http.csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e
                .authenticationEntryPoint((request, response, failure) -> errors.unauthorized(response))
                .accessDeniedHandler((request, response, failure) -> errors.forbidden(response)))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/v1/foundation/status", "/v3/api-docs/**", "/swagger-ui/**",
                    "/swagger-ui.html", "/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/change-password",
                    "/api/v1/auth/logout").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/reauth",
                    "/api/v1/records/{type}/{id}/sign").hasAuthority("ebr:sign")
                .requestMatchers(HttpMethod.GET, "/api/v1/audit-events").hasAuthority("audit:view")
                .requestMatchers(HttpMethod.GET, "/api/v1/integration/messages").hasAuthority("integration:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/integration/messages/{messageRef}/retry")
                    .access(AuthorizationManagers.allOf(
                        AuthorityAuthorizationManager.hasAuthority("integration:view"),
                        AuthorityAuthorizationManager.hasAuthority("integration:retry")))
                .requestMatchers(HttpMethod.GET, "/api/v1/organizations", "/api/v1/organizations/{id}").hasAuthority("master:org:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/organizations").hasAuthority("master:org:create")
                .requestMatchers(HttpMethod.PUT, "/api/v1/organizations/{id}").hasAuthority("master:org:update")
                .requestMatchers(HttpMethod.GET, "/api/v1/units", "/api/v1/units/{id}").hasAuthority("master:uom:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/units").hasAuthority("master:uom:create")
                .requestMatchers(HttpMethod.PUT, "/api/v1/units/{id}").hasAuthority("master:uom:update")
                .requestMatchers(HttpMethod.GET, "/api/v1/unit-conversions", "/api/v1/unit-conversions/{id}").hasAuthority("master:uom:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/unit-conversions").hasAuthority("master:uom:create")
                .requestMatchers(HttpMethod.PUT, "/api/v1/unit-conversions/{id}").hasAuthority("master:uom:update")
                .requestMatchers(HttpMethod.GET, "/api/v1/equipment", "/api/v1/equipment/{id}").hasAuthority("master:equipment:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/equipment").hasAuthority("master:equipment:create")
                .requestMatchers(HttpMethod.PUT, "/api/v1/equipment/{id}").hasAuthority("master:equipment:update")
                .requestMatchers(HttpMethod.GET, "/api/v1/qualifications", "/api/v1/qualifications/{id}").hasAuthority("master:qualification:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/qualifications").hasAuthority("master:qualification:create")
                .requestMatchers(HttpMethod.PUT, "/api/v1/qualifications/{id}").hasAuthority("master:qualification:update")
                .requestMatchers(HttpMethod.GET, "/api/v1/materials", "/api/v1/materials/{id}", "/api/v1/materials/{id}/suppliers").hasAuthority("master:material:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/materials").hasAuthority("master:material:create")
                .requestMatchers(HttpMethod.PUT, "/api/v1/materials/{id}", "/api/v1/materials/{id}/suppliers").hasAuthority("master:material:update")
                .requestMatchers(HttpMethod.POST, "/api/v1/materials/{id}/disable").hasAuthority("master:material:disable")
                .requestMatchers(HttpMethod.GET, "/api/v1/suppliers", "/api/v1/suppliers/{id}").hasAuthority("master:supplier:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/suppliers").hasAuthority("master:supplier:create")
                .requestMatchers(HttpMethod.PUT, "/api/v1/suppliers/{id}").hasAuthority("master:supplier:update")
                .requestMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/products/{id}").hasAuthority("master:product:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/products").hasAuthority("master:product:create")
                .requestMatchers(HttpMethod.PUT, "/api/v1/products/{id}").hasAuthority("master:product:update")
                .requestMatchers(HttpMethod.GET, "/api/v1/process-packages", "/api/v1/process-packages/{id}", "/api/v1/process-versions/{id}/formula", "/api/v1/process-versions/{id}/route").hasAuthority("process:package:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/process-packages").hasAuthority("process:package:create")
                .requestMatchers(HttpMethod.PUT, "/api/v1/process-packages/{id}").hasAuthority("process:package:update")
                .requestMatchers(HttpMethod.POST, "/api/v1/process-packages/{id}/versions", "/api/v1/process-versions/{id}/lint").hasAuthority("process:package:edit")
                .requestMatchers(HttpMethod.PUT, "/api/v1/process-versions/{id}/formula", "/api/v1/process-versions/{id}/route").hasAuthority("process:package:edit")
                .requestMatchers(HttpMethod.POST, "/api/v1/process-versions/{id}/submit").hasAuthority("process:package:submit")
                .requestMatchers(HttpMethod.POST, "/api/v1/process-versions/{id}/approve").hasAuthority("process:package:approve")
                .requestMatchers(HttpMethod.POST, "/api/v1/process-versions/{id}/publish").hasAuthority("process:package:publish")
                .requestMatchers(HttpMethod.GET, "/api/v1/ebr/templates").hasAuthority("ebr:template:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/ebr/templates").hasAuthority("ebr:template:create")
                .requestMatchers(HttpMethod.GET, "/api/v1/ebr/templates/{id}").hasAuthority("ebr:template:view")
                .requestMatchers(HttpMethod.PUT, "/api/v1/ebr/templates/{id}").hasAuthority("ebr:template:update")
                .requestMatchers(HttpMethod.POST, "/api/v1/ebr/templates/{id}/versions").hasAuthority("ebr:designer:edit")
                .requestMatchers(HttpMethod.POST, "/api/v1/ebr/versions/{id}/approve").hasAuthority("ebr:template:approve")
                .requestMatchers(HttpMethod.GET, "/api/v1/ebr/versions/{id}/compare").hasAuthority("ebr:template:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/ebr/versions/{id}/lint").hasAuthority("ebr:designer:edit")
                .requestMatchers(HttpMethod.POST, "/api/v1/ebr/versions/{id}/publish").hasAuthority("ebr:template:publish")
                .requestMatchers(HttpMethod.POST, "/api/v1/ebr/versions/{id}/simulate").hasAuthority("ebr:designer:edit")
                .requestMatchers(HttpMethod.POST, "/api/v1/ebr/versions/{id}/submit").hasAuthority("ebr:template:submit")
                .requestMatchers(HttpMethod.GET, "/api/v1/containers").hasAuthority("wms:inventory:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/containers").hasAuthority("wms:inventory:create")
                .requestMatchers(HttpMethod.GET, "/api/v1/containers/{id}").hasAuthority("wms:inventory:view")
                .requestMatchers(HttpMethod.PUT, "/api/v1/containers/{id}").hasAuthority("wms:inventory:update")
                .requestMatchers(HttpMethod.GET, "/api/v1/inventory").hasAuthority("wms:inventory:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/inventory/adjust").hasAuthority("wms:inventory:adjust")
                .requestMatchers(HttpMethod.POST, "/api/v1/inventory/move").hasAuthority("wms:inventory:move")
                .requestMatchers(HttpMethod.POST, "/api/v1/inventory/receive").hasAuthority("wms:receipt:create")
                .requestMatchers(HttpMethod.GET, "/api/v1/locations").hasAuthority("wms:inventory:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/locations").hasAuthority("wms:inventory:create")
                .requestMatchers(HttpMethod.GET, "/api/v1/locations/{id}").hasAuthority("wms:inventory:view")
                .requestMatchers(HttpMethod.PUT, "/api/v1/locations/{id}").hasAuthority("wms:inventory:update")
                .requestMatchers(HttpMethod.GET, "/api/v1/material-issues").hasAuthority("wms:issue:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/material-issues").hasAuthority("wms:issue:create")
                .requestMatchers(HttpMethod.GET, "/api/v1/material-issues/{id}").hasAuthority("wms:issue:view")
                .requestMatchers(HttpMethod.PUT, "/api/v1/material-issues/{id}").hasAuthority("wms:issue:update")
                .requestMatchers(HttpMethod.POST, "/api/v1/material-issues/{id}/confirm").hasAuthority("wms:issue:confirm")
                .requestMatchers(HttpMethod.POST, "/api/v1/material-issues/{id}/returns").hasAuthority("wms:issue:return")
                .requestMatchers(HttpMethod.GET, "/api/v1/material-lots").hasAuthority("wms:inventory:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/material-lots").hasAuthority("wms:inventory:create")
                .requestMatchers(HttpMethod.GET, "/api/v1/material-lots/{id}").hasAuthority("wms:inventory:view")
                .requestMatchers(HttpMethod.PUT, "/api/v1/material-lots/{id}").hasAuthority("wms:inventory:update")
                .requestMatchers(HttpMethod.GET, "/api/v1/warehouses").hasAuthority("wms:inventory:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/warehouses").hasAuthority("wms:inventory:create")
                .requestMatchers(HttpMethod.GET, "/api/v1/warehouses/{id}").hasAuthority("wms:inventory:view")
                .requestMatchers(HttpMethod.PUT, "/api/v1/warehouses/{id}").hasAuthority("wms:inventory:update")
                .requestMatchers(HttpMethod.GET, "/api/v1/wms/receipts").hasAuthority("wms:receipt:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/wms/receipts").hasAuthority("wms:receipt:create")
                .requestMatchers(HttpMethod.GET, "/api/v1/wms/receipts/{id}").hasAuthority("wms:receipt:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/wms/receipts/{id}/confirm").hasAuthority("wms:receipt:confirm")
                .requestMatchers(HttpMethod.GET, "/api/v1/wms/material-lots").hasAuthority("wms:inventory:view")
                .requestMatchers(HttpMethod.GET, "/api/v1/wms/material-lots/{id}").hasAuthority("wms:inventory:view")
                .requestMatchers(HttpMethod.PUT, "/api/v1/wms/receipts/{id}").hasAuthority("wms:receipt:update")
                .requestMatchers(HttpMethod.GET, "/api/v1/main-batches/{id}/reservations").hasAuthority("wms:reservation:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/main-batches/{id}/reservations").hasAuthority("wms:reservation:create")
                .requestMatchers(HttpMethod.GET, "/api/v1/users", "/api/v1/users/{id}")
                    .hasAuthority("iam:user:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/users").hasAuthority("iam:user:create")
                .requestMatchers(HttpMethod.PUT, "/api/v1/users/{id}")
                    .hasAuthority("iam:user:update")
                .requestMatchers(HttpMethod.POST, "/api/v1/users/{id}/roles")
                    .hasAuthority("iam:user:update")
                .requestMatchers(HttpMethod.GET, "/api/v1/roles", "/api/v1/roles/{id}")
                    .hasAuthority("iam:role:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/roles").hasAuthority("iam:role:create")
                .requestMatchers(HttpMethod.PUT, "/api/v1/roles/{id}")
                    .hasAuthority("iam:role:update")
                .requestMatchers(HttpMethod.POST, "/api/v1/roles/{id}/permissions")
                    .hasAuthority("iam:role:update")
                .requestMatchers(HttpMethod.GET, "/api/v1/permissions", "/api/v1/permissions/{id}")
                    .hasAuthority("iam:permission:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/permissions")
                    .hasAuthority("iam:permission:create")
                .requestMatchers(HttpMethod.PUT, "/api/v1/permissions/{id}")
                    .hasAuthority("iam:permission:update")
                .requestMatchers(HttpMethod.GET, "/api/v1/menus", "/api/v1/menus/{id}")
                    .hasAuthority("iam:menu:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/menus").hasAuthority("iam:menu:create")
                .requestMatchers(HttpMethod.PUT, "/api/v1/menus/{id}")
                    .hasAuthority("iam:menu:update")
                .requestMatchers(HttpMethod.GET, "/api/v1/admin/users",
                    "/api/v1/admin/users/{userId}").hasAuthority("menu:iam:users")
                .requestMatchers(HttpMethod.POST, "/api/v1/admin/users",
                    "/api/v1/admin/users/{userId}/enable",
                    "/api/v1/admin/users/{userId}/disable",
                    "/api/v1/admin/users/{userId}/password-reset",
                    "/api/v1/admin/users/{userId}/roles/{roleId}")
                    .access(AuthorizationManagers.allOf(
                        AuthorityAuthorizationManager.hasAuthority("menu:iam:users"),
                        AuthorityAuthorizationManager.hasAuthority("action:iam:user.manage")))
                .requestMatchers(HttpMethod.PATCH, "/api/v1/admin/users/{userId}/profile")
                    .access(AuthorizationManagers.allOf(
                        AuthorityAuthorizationManager.hasAuthority("menu:iam:users"),
                        AuthorityAuthorizationManager.hasAuthority("action:iam:user.manage")))
                .requestMatchers(HttpMethod.DELETE, "/api/v1/admin/users/{userId}/roles/{roleId}")
                    .access(AuthorizationManagers.allOf(
                        AuthorityAuthorizationManager.hasAuthority("menu:iam:users"),
                        AuthorityAuthorizationManager.hasAuthority("action:iam:user.manage")))
                .requestMatchers(HttpMethod.GET, "/api/v1/admin/roles",
                    "/api/v1/admin/roles/{roleId}", "/api/v1/admin/permissions")
                    .hasAuthority("menu:iam:roles")
                .requestMatchers(HttpMethod.POST, "/api/v1/admin/roles",
                    "/api/v1/admin/roles/{roleId}/enable",
                    "/api/v1/admin/roles/{roleId}/disable",
                    "/api/v1/admin/roles/{roleId}/permissions/{permissionCode}")
                    .access(AuthorizationManagers.allOf(
                        AuthorityAuthorizationManager.hasAuthority("menu:iam:roles"),
                        AuthorityAuthorizationManager.hasAuthority("action:iam:role.manage")))
                .requestMatchers(HttpMethod.PATCH, "/api/v1/admin/roles/{roleId}/name")
                    .access(AuthorizationManagers.allOf(
                        AuthorityAuthorizationManager.hasAuthority("menu:iam:roles"),
                        AuthorityAuthorizationManager.hasAuthority("action:iam:role.manage")))
                .requestMatchers(HttpMethod.DELETE, "/api/v1/admin/roles/{roleId}/permissions/{permissionCode}")
                    .access(AuthorizationManagers.allOf(
                        AuthorityAuthorizationManager.hasAuthority("menu:iam:roles"),
                        AuthorityAuthorizationManager.hasAuthority("action:iam:role.manage")))
                .requestMatchers(HttpMethod.GET, "/api/v1/quality/specifications").hasAuthority("qms:specification:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/specifications").hasAuthority("qms:specification:create")
                .requestMatchers(HttpMethod.GET, "/api/v1/quality/specifications/{id}").hasAuthority("qms:specification:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/specifications/{id}/versions").hasAuthority("qms:specification:create")
                .requestMatchers(HttpMethod.GET, "/api/v1/quality/specification-versions/{id}").hasAuthority("qms:specification:view")
                .requestMatchers(HttpMethod.PUT, "/api/v1/quality/specification-versions/{id}").hasAuthority("qms:specification:edit")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/specification-versions/{id}/approve").hasAuthority("qms:specification:approve")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/specification-versions/{id}/retire").hasAuthority("qms:specification:retire")
                .requestMatchers(HttpMethod.GET, "/api/v1/quality/inspection-requests").hasAuthority("qms:inspection-request:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/inspection-requests").hasAuthority("qms:inspection-request:create")
                .requestMatchers(HttpMethod.GET, "/api/v1/quality/inspection-requests/{id}").hasAuthority("qms:inspection-request:view")
                .requestMatchers(HttpMethod.GET, "/api/v1/quality/sampling-tasks").hasAuthority("qms:sampling:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/sampling-tasks").hasAuthority("qms:sampling:create")
                .requestMatchers(HttpMethod.GET, "/api/v1/quality/sampling-tasks/{id}").hasAuthority("qms:sampling:view")
                .requestMatchers(HttpMethod.GET, "/api/v1/quality/inspection-tasks").hasAuthority("qms:test:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/inspection-tasks").hasAuthority("qms:test:execute")
                .requestMatchers(HttpMethod.GET, "/api/v1/quality/inspection-tasks/{id}").hasAuthority("qms:test:view")
                .requestMatchers(HttpMethod.GET, "/api/v1/quality/inspection-reports").hasAuthority("qms:report:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/inspection-reports").hasAuthority("qms:report:create")
                .requestMatchers(HttpMethod.GET, "/api/v1/quality/inspection-reports/{id}").hasAuthority("qms:report:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/inspection-requests/{id}/submit").hasAuthority("qms:inspection-request:submit")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/inspection-requests/{id}/accept").hasAuthority("qms:inspection-request:accept")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/sampling-tasks/{id}/approve-plan").hasAuthority("qms:sampling:approve-plan")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/sampling-tasks/{id}/assign").hasAuthority("qms:sampling:assign")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/sampling-tasks/{id}/start").hasAuthority("qms:sampling:execute")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/sampling-tasks/{id}/details").hasAuthority("qms:sampling:execute")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/sampling-tasks/{id}/complete").hasAuthority("qms:sampling:complete")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/inspection-tasks/{id}/assign").hasAuthority("qms:test:execute")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/inspection-tasks/{id}/start").hasAuthority("qms:test:execute")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/inspection-tasks/{id}/submit-review").hasAuthority("qms:test:execute")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/inspection-tasks/{id}/review").hasAuthority("qms:test:review")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/inspection-reports/{id}/review").hasAuthority("qms:report:review")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/inspection-reports/{id}/approve").hasAuthority("qms:report:approve")
                .requestMatchers(HttpMethod.GET, "/api/v1/quality/samples").hasAuthority("qms:test:view")
                .requestMatchers(HttpMethod.GET, "/api/v1/quality/samples/{id}").hasAuthority("qms:test:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/samples/{id}/label").hasAuthority("qms:sampling:execute")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/samples/{id}/receive").hasAuthority("qms:sampling:execute")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/samples/{id}/retain").hasAuthority("qms:sampling:execute")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/samples/{id}/dispose").hasAuthority("qms:sampling:complete")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/inspection-items/{id}/executions").hasAuthority("qms:test:execute")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/inspection-items/{id}/approved-retests").hasAuthority("qms:test:execute")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/inspection-items/{id}/results").hasAuthority("qms:test:execute")
                .requestMatchers(HttpMethod.POST, "/api/v1/quality/test-results/{revisionId}/revisions").hasAuthority("qms:test:correct")
                .requestMatchers(HttpMethod.GET, "/api/v1/qa/material-lots/{lotId}/release-review").hasAuthority("qa:material-release:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/qa/material-lots/{lotId}/release-decisions").hasAuthority("qa:material-release:decide")
                .requestMatchers(HttpMethod.GET, "/api/v1/deviations").hasAuthority("qms:deviation:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/deviations").hasAuthority("qms:deviation:create")
                .requestMatchers(HttpMethod.GET, "/api/v1/deviations/{id}").hasAuthority("qms:deviation:view")
                .requestMatchers(HttpMethod.PUT, "/api/v1/deviations/{id}").hasAuthority("qms:deviation:update")
                .requestMatchers(HttpMethod.POST, "/api/v1/deviations/{id}/investigate").hasAuthority("qms:deviation:investigate")
                .requestMatchers(HttpMethod.POST, "/api/v1/deviations/{id}/decide").hasAuthority("qms:deviation:decide")
                .requestMatchers(HttpMethod.POST, "/api/v1/deviations/{id}/close").hasAuthority("qms:deviation:close")
                .requestMatchers(HttpMethod.GET, "/api/v1/quality/signature-evidence/{signatureId}").hasAuthority("audit:view")
                .requestMatchers(HttpMethod.GET, "/api/v1/execution-units/{id}").hasAuthority("mes:execution:view")
                .requestMatchers(HttpMethod.GET, "/api/v1/execution-units/{id}/material-charges").hasAuthority("mes:charge:view")
                .requestMatchers(HttpMethod.GET, "/api/v1/execution-units/{id}/operations").hasAuthority("mes:operation:view")
                .requestMatchers(HttpMethod.GET, "/api/v1/execution-units/{id}/weighings").hasAuthority("mes:weigh:view")
                .requestMatchers(HttpMethod.GET, "/api/v1/main-batches").hasAuthority("production:batch:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/main-batches").hasAuthority("production:batch:create")
                .requestMatchers(HttpMethod.GET, "/api/v1/main-batches/{id}").hasAuthority("production:batch:view")
                .requestMatchers(HttpMethod.PUT, "/api/v1/main-batches/{id}").hasAuthority("production:batch:update")
                .requestMatchers(HttpMethod.POST, "/api/v1/main-batches/{id}/complete-production").hasAuthority("production:batch:complete")
                .requestMatchers(HttpMethod.GET, "/api/v1/main-batches/{id}/execution-units").hasAuthority("mes:execution:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/main-batches/{id}/release").hasAuthority("production:batch:release")
                .requestMatchers(HttpMethod.POST, "/api/v1/main-batches/{id}/start").hasAuthority("production:batch:start")
                .requestMatchers(HttpMethod.GET, "/api/v1/main-batches/{id}/sub-batches").hasAuthority("production:batch:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/main-batches/{id}/sub-batches").hasAuthority("production:subbatch:create")
                .requestMatchers(HttpMethod.POST, "/api/v1/main-batches/{id}/submit-qa").hasAuthority("qa:batch-review")
                .requestMatchers(HttpMethod.POST, "/api/v1/material-charges").hasAuthority("mes:charge:create")
                .requestMatchers(HttpMethod.POST, "/api/v1/material-charges/{id}/reverse").hasAuthority("mes:charge:reverse")
                .requestMatchers(HttpMethod.POST, "/api/v1/operations/{id}/complete").hasAuthority("mes:operation:complete")
                .requestMatchers(HttpMethod.POST, "/api/v1/operations/{id}/equipment-usages").hasAuthority("mes:equipment:bind")
                .requestMatchers(HttpMethod.POST, "/api/v1/operations/{id}/parameter-values").hasAuthority("mes:param:record")
                .requestMatchers(HttpMethod.POST, "/api/v1/operations/{id}/pause").hasAuthority("mes:operation:pause")
                .requestMatchers(HttpMethod.POST, "/api/v1/operations/{id}/resume").hasAuthority("mes:operation:start")
                .requestMatchers(HttpMethod.POST, "/api/v1/operations/{id}/start").hasAuthority("mes:operation:start")
                .requestMatchers(HttpMethod.GET, "/api/v1/production-orders").hasAuthority("production:order:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/production-orders").hasAuthority("production:order:create")
                .requestMatchers(HttpMethod.GET, "/api/v1/production-orders/{id}").hasAuthority("production:order:view")
                .requestMatchers(HttpMethod.PUT, "/api/v1/production-orders/{id}").hasAuthority("production:order:update")
                .requestMatchers(HttpMethod.GET, "/api/v1/trace").hasAuthority("trace:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/weighings").hasAuthority("mes:weigh:create")
                .requestMatchers(HttpMethod.POST, "/api/v1/weighings/{id}/verify").hasAuthority("mes:weigh:verify")
                .requestMatchers(HttpMethod.POST, "/api/v1/attachments").hasAuthority("attachment:upload")
                .requestMatchers(HttpMethod.GET, "/api/v1/attachments/{id}").hasAnyAuthority("attachment:view","ebr:form:view","qa:batch-review")
                .requestMatchers(HttpMethod.GET, "/api/v1/attachments/{id}/content").hasAnyAuthority("attachment:view","ebr:form:view","qa:batch-review")
                .requestMatchers(HttpMethod.GET, "/api/v1/wms/receipts/{id}/attachments").hasAuthority("wms:receipt:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/wms/receipts/{id}/attachments").hasAuthority("wms:receipt:update")
                .requestMatchers(HttpMethod.GET, "/api/v1/wms/receipts/{id}/attachments/{attachmentId}/content").hasAuthority("wms:receipt:view")
                .requestMatchers(HttpMethod.GET, "/api/v1/execution-units/{id}/forms").hasAuthority("ebr:form:view")
                .requestMatchers(HttpMethod.GET, "/api/v1/forms/{id}/render-model").hasAuthority("ebr:form:view")
                .requestMatchers(HttpMethod.PUT, "/api/v1/forms/{id}/draft-values").hasAuthority("ebr:form:edit")
                .requestMatchers(HttpMethod.POST, "/api/v1/forms/{id}/submit").hasAuthority("ebr:form:submit")
                .requestMatchers(HttpMethod.POST, "/api/v1/field-values/{id}/corrections").hasAuthority("ebr:record:correct")
                .requestMatchers(HttpMethod.POST, "/api/v1/forms/{id}/reviews").hasAnyAuthority("ebr:review:verify","ebr:review:approve")
                .requestMatchers(HttpMethod.GET, "/api/v1/main-batches/{id}/ebr").hasAuthority("ebr:form:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/main-batches/{id}/ebr/pdf").hasAuthority("ebr:pdf:generate")
                .requestMatchers(HttpMethod.GET, "/api/v1/main-batches/{id}/ebr/pdf/{manifestId}").hasAuthority("ebr:form:view")
                .requestMatchers(HttpMethod.GET, "/api/v1/qa/batches/{id}/review-model").hasAuthority("qa:batch-review")
                .requestMatchers(HttpMethod.POST, "/api/v1/release-decisions").hasAuthority("qa:release")
                .requestMatchers(HttpMethod.GET, "/api/v1/release-decisions/{id}").hasAuthority("qa:batch-review")
                .requestMatchers(HttpMethod.GET, "/api/v1/wms/material-lots/{id}/eligibility").hasAuthority("wms:inventory:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/material-lots/{id}/freeze").hasAuthority("qa:material-inventory:freeze")
                .requestMatchers(HttpMethod.POST, "/api/v1/material-lots/{id}/unfreeze").hasAuthority("qa:material-inventory:unfreeze")
                .requestMatchers(HttpMethod.POST, "/api/v1/operations/{id}/record-clearance").hasAuthority("mes:clearance:record")
                .requestMatchers(HttpMethod.POST, "/api/v1/operations/{id}/review-clearance").hasAuthority("mes:clearance:review")
                .requestMatchers(HttpMethod.GET, "/api/v1/ipc", "/api/v1/ipc/{id}").hasAuthority("qms:ipc:view")
                .requestMatchers(HttpMethod.POST, "/api/v1/ipc", "/api/v1/ipc/{id}/submit-result").hasAuthority("qms:ipc:record")
                .requestMatchers(HttpMethod.POST, "/api/v1/ipc/{id}/review-result").hasAuthority("qms:ipc:review")
                .anyRequest().denyAll())
            .addFilterBefore(sessions, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    FilterRegistrationBean<SessionAuthenticationFilter> sessionFilterRegistration(
        SessionAuthenticationFilter sessions) {
        FilterRegistrationBean<SessionAuthenticationFilter> registration = new FilterRegistrationBean<>(sessions);
        registration.setEnabled(false);
        return registration;
    }
}
