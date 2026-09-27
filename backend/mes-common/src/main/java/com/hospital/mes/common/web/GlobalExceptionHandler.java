package com.hospital.mes.common.web;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.exception.*;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice public class GlobalExceptionHandler {
    private final TraceIdProvider traces;
    public GlobalExceptionHandler(TraceIdProvider traces) { this.traces=traces; }
    @ExceptionHandler(MesException.class) ResponseEntity<ApiResponse<Void>> mes(MesException ex) {
        HttpStatus status = ex instanceof PermissionException ? HttpStatus.FORBIDDEN : ex instanceof ResourceConflictException || ex instanceof StateTransitionException ? HttpStatus.CONFLICT : ex instanceof ComplianceException ? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(new ApiResponse<>(ex.code(),ex.getMessage(),null,traces.currentTraceId()));
    }
    @ExceptionHandler(Exception.class) ResponseEntity<ApiResponse<Void>> unexpected(Exception ex) {
        return ResponseEntity.internalServerError().body(new ApiResponse<>("INTERNAL_ERROR","Internal server error",null,traces.currentTraceId()));
    }
}
