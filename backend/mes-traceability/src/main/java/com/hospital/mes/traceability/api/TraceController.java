package com.hospital.mes.traceability.api;
import com.hospital.mes.traceability.application.TraceService;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
@org.springframework.web.bind.annotation.RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class TraceController {
 private final TraceService service;private final TraceIdProvider traces;
 public TraceController(TraceService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 @org.springframework.web.bind.annotation.GetMapping("/api/v1/trace")
 public ApiResponse<?> trace(@org.springframework.web.bind.annotation.RequestParam(name="mainBatchId",required=false) String mainBatchId,@org.springframework.web.bind.annotation.RequestParam(name="materialLotId",required=false) String materialLotId){return ApiResponse.success(service.trace(mainBatchId,materialLotId),traces.currentTraceId());}
}
