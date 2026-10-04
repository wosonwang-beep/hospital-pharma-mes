package com.hospital.mes.security.api;

import com.hospital.mes.common.api.ApiError;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Preserve the same forbidden envelope for method and filter authorization. */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MethodAuthorizationExceptionAdvice {
    private final TraceIdProvider traces;
    public MethodAuthorizationExceptionAdvice(TraceIdProvider traces) { this.traces = traces; }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> forbidden(AccessDeniedException ignored) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiError.of(traces.currentTraceId(), "FORBIDDEN", "Permission denied"));
    }
    @ExceptionHandler(com.hospital.mes.security.reauth.ReauthenticationTokenInvalidException.class)
    ResponseEntity<ApiError> invalidToken(com.hospital.mes.security.reauth.ReauthenticationTokenInvalidException ignored) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiError.of(traces.currentTraceId(), "REAUTH_TOKEN_INVALID", "Reauthentication token is invalid"));
    }
    @ExceptionHandler(com.hospital.mes.security.reauth.ReauthenticationFailedException.class)
    ResponseEntity<ApiError> failedReauthentication(com.hospital.mes.security.reauth.ReauthenticationFailedException ignored) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiError.of(traces.currentTraceId(), "REAUTH_FAILED", "Reauthentication failed"));
    }
}
