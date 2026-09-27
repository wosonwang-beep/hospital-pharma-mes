package com.hospital.mes.security;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
@Configuration public class SecurityConfiguration {
 @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
  return http.csrf(csrf->csrf.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a->a.requestMatchers("/api/v1/foundation/status","/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html","/actuator/health","/actuator/info").permitAll().anyRequest().denyAll())
   .build();
 }
}
