package com.hospital.mes.qms.api;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.hospital.mes.qms.application.ProductionQualityPlanService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.Map;
@RestController @RequestMapping("/api/v1/quality/production-plans")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ProductionQualityPlanController {
 private final ProductionQualityPlanService service;private final TraceIdProvider trace;
 public ProductionQualityPlanController(ProductionQualityPlanService service,TraceIdProvider trace){this.service=service;this.trace=trace;}
 @GetMapping public ApiResponse<?> list(@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam Map<String,String> filters){return ApiResponse.success(service.list(page,size,filters),trace.currentTraceId());}
 @GetMapping("/{id}") public ApiResponse<?> get(@PathVariable("id") String id){return ApiResponse.success(service.get(id),trace.currentTraceId());}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public ApiResponse<?> create(@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.create(body,key),trace.currentTraceId());}
 @PutMapping("/{id}") public ApiResponse<?> update(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.update(id,body,version,key),trace.currentTraceId());}
 @PostMapping("/{id}/approve") public ApiResponse<?> approve(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.approve(id,body,version,key),trace.currentTraceId());}
}
