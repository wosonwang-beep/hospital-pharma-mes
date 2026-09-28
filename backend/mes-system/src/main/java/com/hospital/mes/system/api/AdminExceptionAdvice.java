package com.hospital.mes.system.api;

import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.exception.ResourceConflictException;
import com.hospital.mes.common.trace.TraceIdProvider;
import java.util.NoSuchElementException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = UserAdminController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminExceptionAdvice {
    private final TraceIdProvider traces;

    public AdminExceptionAdvice(TraceIdProvider traces) {
        this.traces = traces;
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<ApiResponse<Void>> notFound(NoSuchElementException ignored) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "Resource not found");
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ApiResponse<Void>> invalid(Exception ignored) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "Invalid input");
    }

    @ExceptionHandler(ResourceConflictException.class)
    ResponseEntity<ApiResponse<Void>> conflict(ResourceConflictException ex) {
        return error(HttpStatus.CONFLICT, ex.code(), ex.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    ResponseEntity<ApiResponse<Void>> duplicate(DuplicateKeyException ignored) {
        return error(HttpStatus.CONFLICT, "ADMIN_CONFLICT", "Resource already exists");
    }

    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<ApiResponse<Void>> dependencyUnavailable(DataAccessException ignored) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "DEPENDENCY_UNAVAILABLE", "Administrative service unavailable");
    }

    private ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
            .body(new ApiResponse<>(code, message, null, traces.currentTraceId()));
    }
}
