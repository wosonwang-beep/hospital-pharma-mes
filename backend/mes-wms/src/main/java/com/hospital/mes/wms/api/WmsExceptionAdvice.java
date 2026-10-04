package com.hospital.mes.wms.api;
import com.hospital.mes.common.api.ApiError;
import com.hospital.mes.common.exception.*;
import com.hospital.mes.masterdata.domain.MasterGateException;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.core.annotation.Order;
import java.util.*;
@RestControllerAdvice(basePackages="com.hospital.mes.wms.api") @Order(-10)
public class WmsExceptionAdvice {
 private final TraceIdProvider traces;public WmsExceptionAdvice(TraceIdProvider traces){this.traces=traces;}
 @ExceptionHandler(org.springframework.dao.PessimisticLockingFailureException.class) public ResponseEntity<ApiError> concurrent(Exception e){return ResponseEntity.status(409).body(ApiError.of(traces.currentTraceId(),"CONCURRENT_MODIFICATION","Concurrent change; reload and retry"));}
 @ExceptionHandler(MasterGateException.class) public ResponseEntity<Map<String,Object>> gate(MasterGateException e){return ResponseEntity.unprocessableEntity().body(Map.of("requestId",traces.currentTraceId(),"code",e.code(),"message",e.getMessage(),"fieldErrors",List.of(),"allowedActions",List.of(),"requiredActions",e.requiredActions()));}
 @ExceptionHandler(NoSuchElementException.class) public ResponseEntity<ApiError> missing(Exception e){return ResponseEntity.status(404).body(ApiError.of(traces.currentTraceId(),"NOT_FOUND","Resource not found"));}
 @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class) public ResponseEntity<ApiError> duplicate(Exception e){return ResponseEntity.status(409).body(ApiError.of(traces.currentTraceId(),"DUPLICATE_CODE","Code or conversion already exists"));}
 @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class) public ResponseEntity<ApiError> reference(Exception e){return ResponseEntity.unprocessableEntity().body(ApiError.of(traces.currentTraceId(),"INVALID_REFERENCE","Referenced record is invalid"));}
 @ExceptionHandler({IllegalArgumentException.class,org.springframework.http.converter.HttpMessageNotReadableException.class,org.springframework.web.bind.MissingRequestHeaderException.class}) public ResponseEntity<ApiError> invalid(Exception e){return ResponseEntity.badRequest().body(ApiError.of(traces.currentTraceId(),"INVALID_REQUEST","Invalid fields, command or headers"));}
}
