package com.hospital.mes.common.web;
import com.hospital.mes.common.api.ApiError;
import com.hospital.mes.common.exception.*;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice public class GlobalExceptionHandler {
    private final TraceIdProvider traces;
    public GlobalExceptionHandler(TraceIdProvider traces) { this.traces=traces; }
    @ExceptionHandler(MesException.class) ResponseEntity<ApiError> mes(MesException ex) {
        HttpStatus status = ex instanceof PermissionException ? HttpStatus.FORBIDDEN : ex instanceof ResourceConflictException || ex instanceof StateTransitionException ? HttpStatus.CONFLICT : ex instanceof ComplianceException ? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(ApiError.of(traces.currentTraceId(),ex.code(),ex.getMessage()));
    }
    @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<ApiError> invalidArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
            ApiError.of(traces.currentTraceId(), "INVALID_REQUEST", ex.getMessage()));
    }
    @ExceptionHandler(java.util.NoSuchElementException.class) ResponseEntity<ApiError> notFound(java.util.NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of(traces.currentTraceId(),"NOT_FOUND","Resource not found"));
    }
    @ExceptionHandler(org.springframework.web.bind.MissingRequestHeaderException.class)
    ResponseEntity<ApiError> missingHeader(org.springframework.web.bind.MissingRequestHeaderException ex) {
        return ResponseEntity.badRequest().body(ApiError.of(traces.currentTraceId(),"INVALID_REQUEST","Required header: "+ex.getHeaderName()));
    }
    @ExceptionHandler(Exception.class) ResponseEntity<ApiError> unexpected(Exception ex) {
        return ResponseEntity.internalServerError().body(ApiError.of(traces.currentTraceId(),"INTERNAL_ERROR","Internal server error"));
    }
}
