package com.hospital.mes.qms.api;
import com.hospital.mes.qms.application.MaterialBalanceService;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1") @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class MaterialBalanceController {
 private final MaterialBalanceService service;private final TraceIdProvider traces;
 public MaterialBalanceController(MaterialBalanceService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 @GetMapping("/main-batches/{id}/material-balance") public ApiResponse<?> get(@PathVariable("id") String id){return ApiResponse.success(service.get(id),traces.currentTraceId());}
 @PostMapping("/main-batches/{id}/material-balance/recalculate") public ApiResponse<?> recalculate(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.recalculate(id,body,version,key),traces.currentTraceId());}
 @PostMapping("/balances/{id}/investigations") public ApiResponse<?> createInvestigation(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.createInvestigation(id,body,version,key),traces.currentTraceId());}
 @PostMapping("/balance-investigations/{id}/investigate") public ApiResponse<?> investigate(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.investigate(id,body,version,key),traces.currentTraceId());}
 @PostMapping("/balance-investigations/{id}/approve") public ApiResponse<?> approve(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.approve(id,body,version,key),traces.currentTraceId());}
}
