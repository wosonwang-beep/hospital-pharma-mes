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
