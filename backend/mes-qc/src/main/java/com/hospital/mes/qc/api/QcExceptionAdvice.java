package com.hospital.mes.qc.api;
import com.hospital.mes.common.api.ApiError;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.hospital.mes.qc.domain.QcNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
@RestControllerAdvice(basePackageClasses=QcSpecificationController.class) @org.springframework.core.annotation.Order(-20)
public class QcExceptionAdvice {
 private final TraceIdProvider traces;public QcExceptionAdvice(TraceIdProvider traces){this.traces=traces;}
 @ExceptionHandler(QcNotFoundException.class) public ResponseEntity<ApiError> missing(Exception e){return ResponseEntity.status(404).body(ApiError.of(traces.currentTraceId(),"QC_SPEC_NOT_FOUND","QC specification not found"));}
 @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class,org.springframework.web.bind.MissingRequestHeaderException.class,IllegalArgumentException.class}) public ResponseEntity<ApiError> invalid(Exception e){return ResponseEntity.badRequest().body(ApiError.of(traces.currentTraceId(),"VALIDATION_ERROR","Invalid QC fields or headers"));}
 @ExceptionHandler(org.springframework.dao.PessimisticLockingFailureException.class) public ResponseEntity<ApiError> concurrent(Exception e){return ResponseEntity.status(409).body(ApiError.of(traces.currentTraceId(),"RECORD_CHANGED","Concurrent QC record change; retry"));}
 @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class) public ResponseEntity<ApiError> integrity(Exception e){return ResponseEntity.badRequest().body(ApiError.of(traces.currentTraceId(),"VALIDATION_ERROR","Invalid QC definition or reference"));}
}
