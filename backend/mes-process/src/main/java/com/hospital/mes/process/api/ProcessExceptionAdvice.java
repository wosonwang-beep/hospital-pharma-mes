package com.hospital.mes.process.api;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.hospital.mes.masterdata.api.MasterExceptionAdvice;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RestControllerAdvice;
/** Reuses the existing API error mapping for typed CRUD, references and concurrency. */
@RestControllerAdvice(basePackageClasses=ProcessController.class) @Order(-10)
public class ProcessExceptionAdvice extends MasterExceptionAdvice {
 public ProcessExceptionAdvice(TraceIdProvider traces){super(traces);}
 @org.springframework.web.bind.annotation.ExceptionHandler(java.time.DateTimeException.class)
 public org.springframework.http.ResponseEntity<com.hospital.mes.common.api.ApiError> invalidDate(java.time.DateTimeException error){return invalid(error);}
}
