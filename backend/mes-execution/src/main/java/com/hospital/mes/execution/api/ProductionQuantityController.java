package com.hospital.mes.execution.api;
import com.hospital.mes.execution.application.ProductionQuantityService;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
@RestController @RequestMapping("/api/v1") @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ProductionQuantityController {
 private final ProductionQuantityService service;private final TraceIdProvider traces;
 public ProductionQuantityController(ProductionQuantityService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 @GetMapping("/main-batches/{id}/quantity-events") public ApiResponse<?> list(@PathVariable("id") String id,@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size){return ApiResponse.success(service.list(id,page,size),traces.currentTraceId());}
 @PostMapping("/main-batches/{id}/quantity-events") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<?> record(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.record(id,body,version,key),traces.currentTraceId());}
 @PostMapping("/quantity-events/{id}/reverse") public ApiResponse<?> reverse(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.reverse(id,body,version,key),traces.currentTraceId());}
}
