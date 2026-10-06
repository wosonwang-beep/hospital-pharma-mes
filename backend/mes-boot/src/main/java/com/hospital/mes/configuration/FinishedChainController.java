package com.hospital.mes.configuration;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.web.bind.annotation.*;
@RestController @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class FinishedChainController {
 private final FinishedChainQuery query;private final TraceIdProvider traces;
 public FinishedChainController(FinishedChainQuery query,TraceIdProvider traces){this.query=query;this.traces=traces;}
 @GetMapping("/api/v1/finished-material-lots/{id}/chain") public ApiResponse<?> chain(@PathVariable("id") String id){return ApiResponse.success(query.get(id),traces.currentTraceId());}
}
