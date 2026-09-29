package com.hospital.mes.security.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hospital.mes.common.trace.TraceIdProvider;
import com.hospital.mes.security.identity.LoginSnapshot;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class SpringSecurityPlatformContextResolverTest {
    @AfterEach void clear(){SecurityContextHolder.clearContext();}
    @Test void resolvesOrganizationOnlyFromAuthenticatedSessionPrincipal(){
        var identity=new LoginSnapshot(7,11,"staff","Staff",Set.of("QA"),Set.of("audit:view"),false);
        var auth=UsernamePasswordAuthenticationToken.authenticated(identity,null,ListAuthorities.of("audit:view"));auth.setDetails("session-1");SecurityContextHolder.getContext().setAuthentication(auth);
        var context=new SpringSecurityPlatformContextResolver(() -> "request-1").current();
        assertThat(context.organizationId()).isEqualTo(11);assertThat(context.actorId()).isEqualTo(7);assertThat(context.sessionId()).isEqualTo("session-1");
    }
    @Test void failsClosedWithoutAuthenticatedOrganizationContext(){
        assertThatThrownBy(() -> new SpringSecurityPlatformContextResolver(() -> "r").current()).isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class);
    }
    private static final class ListAuthorities{static java.util.List<org.springframework.security.core.GrantedAuthority> of(String v){return java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(v));}}
}
