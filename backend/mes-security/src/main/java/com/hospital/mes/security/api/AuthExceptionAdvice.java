package com.hospital.mes.security.api;

import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = AuthController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionAdvice {
    private final TraceIdProvider traces;

    public AuthExceptionAdvice(TraceIdProvider traces) { this.traces = traces; }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiResponse<Void>> badCredentials(BadCredentialsException ignored) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse<>("INVALID_CREDENTIALS", "Invalid credentials", null,
                traces.currentTraceId()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiResponse<Void>> invalidInput(IllegalArgumentException ignored) {
        return ResponseEntity.badRequest()
            .body(new ApiResponse<>("INVALID_INPUT", "Invalid input", null, traces.currentTraceId()));
    }

    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<ApiResponse<Void>> dependencyUnavailable(DataAccessException ignored) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ApiResponse<>("DEPENDENCY_UNAVAILABLE", "Authentication temporarily unavailable",
                null, traces.currentTraceId()));
    }
}
