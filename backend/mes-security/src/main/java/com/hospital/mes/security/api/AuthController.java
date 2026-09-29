package com.hospital.mes.security.api;

import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.hospital.mes.security.application.AuthService;
import com.hospital.mes.security.identity.LoginSnapshot;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Duration;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class AuthController {
    private static final String COOKIE = "MES_RENEWAL";
    private static final String COOKIE_PATH = "/api/v1/auth";

    public record LoginRequest(String loginName, String password) {}
    public record PasswordChangeRequest(String oldPassword, String newPassword) {}
    public record TokenResponse(String accessToken, int expiresInSeconds) {}
    public record CurrentUserResponse(String userId, String organizationId, String loginName,
                                      String displayName, Set<String> roleCodes,
                                      Set<String> permissionCodes, boolean mustChangePassword) {
        static CurrentUserResponse from(LoginSnapshot value) {
            return new CurrentUserResponse(Long.toString(value.userId()), Long.toString(value.organizationId()),
                value.loginName(), value.displayName(), value.roleCodes(), value.permissionCodes(),
                value.mustChangePassword());
        }
    }

    private final AuthService auth;
    private final TraceIdProvider traces;
    private final boolean allowInsecureLocal;

    public AuthController(AuthService auth, TraceIdProvider traces,
                          @Value("${mes.security.cookie.allow-insecure-local:false}")
                          boolean allowInsecureLocal) {
        this.auth = auth;
        this.traces = traces;
        this.allowInsecureLocal = allowInsecureLocal;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody LoginRequest request,
                                                              HttpServletRequest servletRequest) {
        AuthService.AuthResult result = auth.login(request.loginName(), request.password(), traces.currentTraceId());
        return tokenResponse(result, servletRequest);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(HttpServletRequest request) {
        requireSameOrigin(request);
        String renewal = renewalCookie(request);
        return tokenResponse(auth.refresh(renewal), request);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication,
                                                     HttpServletRequest request) {
        auth.logout(sessionId(authentication));
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie("", request, Duration.ZERO).toString())
            .body(ApiResponse.success(null, traces.currentTraceId()));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(Authentication authentication) {
        return ApiResponse.success(CurrentUserResponse.from((LoginSnapshot) authentication.getPrincipal()),
            traces.currentTraceId());
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(Authentication authentication,
                                            @RequestBody PasswordChangeRequest request) {
        auth.changeOwnPassword((LoginSnapshot) authentication.getPrincipal(),
            request.oldPassword(), request.newPassword(), traces.currentTraceId());
        return ApiResponse.success(null, traces.currentTraceId());
    }

    private ResponseEntity<ApiResponse<TokenResponse>> tokenResponse(AuthService.AuthResult result,
                                                                       HttpServletRequest request) {
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie(result.renewalCookieValue(), request, Duration.ofHours(8)).toString())
            .body(ApiResponse.success(new TokenResponse(result.accessToken(), 15 * 60),
                traces.currentTraceId()));
    }

    private ResponseCookie cookie(String value, HttpServletRequest request, Duration age) {
        boolean loopback = "localhost".equalsIgnoreCase(request.getServerName())
            || "127.0.0.1".equals(request.getServerName())
            || "::1".equals(request.getServerName());
        return ResponseCookie.from(COOKIE, value).httpOnly(true)
            .secure(!(allowInsecureLocal && loopback)).sameSite("Strict")
            .path(COOKIE_PATH).maxAge(age).build();
    }

    private static String renewalCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (COOKIE.equals(cookie.getName())) return cookie.getValue();
            }
        }
        throw new BadCredentialsException("Invalid credentials");
    }

    private static String sessionId(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof String id))
            throw new BadCredentialsException("Invalid credentials");
        return id;
    }

    private static void requireSameOrigin(HttpServletRequest request) {
        String supplied = request.getHeader("Origin");
        if (supplied == null || supplied.isBlank()) throw new BadCredentialsException("Invalid credentials");
        URI origin;
        try { origin = URI.create(supplied); }
        catch (IllegalArgumentException ex) { throw new BadCredentialsException("Invalid credentials"); }
        int expectedPort = request.getServerPort();
        int originPort = origin.getPort();
        if (originPort == -1) originPort = "https".equalsIgnoreCase(origin.getScheme()) ? 443 : 80;
        if (!request.getScheme().equalsIgnoreCase(origin.getScheme())
            || origin.getHost() == null || !request.getServerName().equalsIgnoreCase(origin.getHost())
            || expectedPort != originPort || origin.getUserInfo() != null
            || (origin.getRawPath() != null && !origin.getRawPath().isEmpty())
            || origin.getRawQuery() != null || origin.getRawFragment() != null)
            throw new BadCredentialsException("Invalid credentials");
    }
}
