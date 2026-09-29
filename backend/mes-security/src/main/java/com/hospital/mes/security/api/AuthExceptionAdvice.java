package com.hospital.mes.security.api;

import com.hospital.mes.common.api.ApiError;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice(basePackageClasses = AuthController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionAdvice {
    private final TraceIdProvider traces;

    public AuthExceptionAdvice(TraceIdProvider traces) { this.traces = traces; }

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
    ResponseEntity<ApiError> dependencyUnavailable(DataAccessException ignored) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiError.of(traces.currentTraceId(), "DEPENDENCY_UNAVAILABLE",
                "Authentication temporarily unavailable"));
    }
}
