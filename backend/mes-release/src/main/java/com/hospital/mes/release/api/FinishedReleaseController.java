package com.hospital.mes.release.api;
import com.hospital.mes.release.application.FinishedReleaseService;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
@RestController @RequestMapping("/api/v1") @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class FinishedReleaseController {
 private final FinishedReleaseService service;private final TraceIdProvider traces;
 public FinishedReleaseController(FinishedReleaseService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 @GetMapping("/qa/batches/{id}/review-model") public ApiResponse<?> review(@PathVariable("id") String id){return ApiResponse.success(service.review(id),traces.currentTraceId());}
 @PostMapping("/release-decisions") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<?> decide(@RequestBody JsonNode b,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.decide(b,version,key),traces.currentTraceId());}
 @GetMapping("/release-decisions/{id}") public ApiResponse<?> decision(@PathVariable("id") String id){return ApiResponse.success(service.get(id),traces.currentTraceId());}
}
