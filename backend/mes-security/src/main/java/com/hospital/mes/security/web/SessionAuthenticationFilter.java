package com.hospital.mes.security.web;

import com.hospital.mes.security.identity.LoginSnapshot;
import com.hospital.mes.security.jwt.AccessTokenCodec;
import com.hospital.mes.security.session.SessionSnapshot;
import com.hospital.mes.security.session.SessionStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {
    private final AccessTokenCodec tokens;
    private final SessionStore sessions;
    private final SecurityErrorWriter errors;

    public SessionAuthenticationFilter(AccessTokenCodec tokens, SessionStore sessions,
                                       SecurityErrorWriter errors) {
        this.tokens = tokens;
        this.sessions = sessions;
        this.errors = errors;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            chain.doFilter(request, response);
            return;
        }
        if (!authorization.startsWith("Bearer ") || authorization.length() <= 7) {
            errors.unauthorized(response);
            return;
        }
        try {
            AccessTokenCodec.TokenClaims claims = tokens.verify(authorization.substring(7));
            SessionSnapshot session = sessions.touch(claims.sessionId())
                .orElseThrow(() -> new BadCredentialsException("Session unavailable"));
            LoginSnapshot identity = session.identity();
            if (identity.userId() != claims.userId()) throw new BadCredentialsException("Session mismatch");
            var authorities = identity.permissionCodes().stream().map(SimpleGrantedAuthority::new).toList();
            var authentication = UsernamePasswordAuthenticationToken.authenticated(identity, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            if (identity.mustChangePassword() && !allowedBeforePasswordChange(request)) {
                errors.forbidden(response);
                return;
            }
            chain.doFilter(request, response);
        } catch (BadCredentialsException | DataAccessException | IllegalStateException ex) {
            SecurityContextHolder.clearContext();
            errors.unauthorized(response);
        }
    }

    private boolean allowedBeforePasswordChange(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        return ("GET".equals(method) && "/api/v1/auth/me".equals(path))
            || ("POST".equals(method) && ("/api/v1/auth/change-password".equals(path)
                || "/api/v1/auth/logout".equals(path)
                || "/api/v1/auth/refresh".equals(path)));
    }
}
