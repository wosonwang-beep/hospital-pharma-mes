package com.hospital.mes.security.api;

import com.hospital.mes.common.api.ApiError;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import com.hospital.mes.security.reauth.ReauthenticationFailedException;
import com.hospital.mes.security.reauth.ReauthenticationTokenInvalidException;

@RestControllerAdvice(basePackageClasses = AuthController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionAdvice {
    private static final Logger log = LoggerFactory.getLogger(AuthExceptionAdvice.class);
    private final TraceIdProvider traces;

    public AuthExceptionAdvice(TraceIdProvider traces) { this.traces = traces; }

    @ExceptionHandler(ReauthenticationTokenInvalidException.class)
    ResponseEntity<ApiError> reauthenticationTokenInvalid(ReauthenticationTokenInvalidException ignored) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiError.of(traces.currentTraceId(), "REAUTH_TOKEN_INVALID",
                "Reauthentication token is invalid"));
    }

    @ExceptionHandler(ReauthenticationFailedException.class)
    ResponseEntity<ApiError> reauthenticationFailed(ReauthenticationFailedException ignored) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiError.of(traces.currentTraceId(), "REAUTH_FAILED", "Reauthentication failed"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiError> badCredentials(BadCredentialsException ignored) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiError.of(traces.currentTraceId(), "INVALID_CREDENTIALS", "Invalid credentials"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> invalidInput(IllegalArgumentException ignored) {
        return ResponseEntity.badRequest()
            .body(ApiError.of(traces.currentTraceId(), "INVALID_INPUT", "Invalid input"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> unreadableBody(HttpMessageNotReadableException ignored) {
        return ResponseEntity.badRequest()
            .body(ApiError.of(traces.currentTraceId(), "INVALID_INPUT", "Invalid input"));
    }

    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<ApiError> dependencyUnavailable(DataAccessException cause) {
        String traceId = traces.currentTraceId();
        log.error("Authentication dependency unavailable, traceId={}", traceId, cause);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiError.of(traceId, "DEPENDENCY_UNAVAILABLE",
                "Authentication temporarily unavailable"));
    }
}
