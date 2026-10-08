package com.hospital.mes.configuration;
import org.springframework.context.annotation.*;import org.springframework.core.annotation.Order;import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;import org.springframework.security.config.annotation.web.builders.HttpSecurity;import org.springframework.security.config.http.SessionCreationPolicy;import org.springframework.security.web.SecurityFilterChain;
@Configuration @ConditionalOnProperty(prefix="mes.print.editor",name="enabled",havingValue="true")
public class PrintEditorTransferSecurity {
 @Bean @Order(0) SecurityFilterChain editorTransferChain(HttpSecurity http)throws Exception {return http.securityMatcher("/api/v1/printing/editor/transfer/**").csrf(c->c.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(a->a.anyRequest().permitAll()).build();}
}
