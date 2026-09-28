package com.hospital.mes.security;

import com.hospital.mes.security.web.SecurityErrorWriter;
import com.hospital.mes.security.web.SessionAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
