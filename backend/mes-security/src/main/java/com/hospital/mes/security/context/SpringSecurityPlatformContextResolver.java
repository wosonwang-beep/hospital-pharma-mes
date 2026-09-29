package com.hospital.mes.security.context;

import com.hospital.mes.audit.application.*;import com.hospital.mes.common.trace.TraceIdProvider;import org.springframework.security.authentication.BadCredentialsException;import org.springframework.security.core.context.SecurityContextHolder;import org.springframework.stereotype.Component;
@Component public class SpringSecurityPlatformContextResolver implements CurrentPlatformContextResolver{
 private final TraceIdProvider traces;public SpringSecurityPlatformContextResolver(TraceIdProvider traces){this.traces=traces;}
 @Override public CurrentPlatformContext current(){var auth=SecurityContextHolder.getContext().getAuthentication();if(auth==null||!auth.isAuthenticated()||!(auth.getPrincipal() instanceof PlatformPrincipal p)||!(auth.getDetails() instanceof String session)||session.isBlank())throw new BadCredentialsException("Authenticated platform context unavailable");return new CurrentPlatformContext(p.organizationId(),p.userId(),p.roleCodes(),p.permissionCodes(),session,traces.currentTraceId());}
}
