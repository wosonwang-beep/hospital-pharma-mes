package com.hospital.mes.production.api;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.hospital.mes.production.application.ProductionService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/v1") @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ProductionController {
 private final ProductionService service;private final TraceIdProvider traces;
 public ProductionController(ProductionService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 @GetMapping("/production-orders") public ApiResponse<?> orders(@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam(name="keyword",required=false) String keyword,@RequestParam Map<String,String> filters){return ApiResponse.success(service.orders(page,size,keyword,filters),traces.currentTraceId());}
 @GetMapping("/main-batches") public ApiResponse<?> batches(@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam(name="keyword",required=false) String keyword,@RequestParam Map<String,String> filters){return ApiResponse.success(service.batches(page,size,keyword,filters),traces.currentTraceId());}
 @GetMapping("/production-orders/{id}") public ApiResponse<?> order(@PathVariable("id") String id){return ApiResponse.success(service.order(id),traces.currentTraceId());}
 @GetMapping("/main-batches/{id}") public ApiResponse<?> batch(@PathVariable("id") String id){return ApiResponse.success(service.batch(id),traces.currentTraceId());}
 @GetMapping("/main-batches/{id}/sub-batches") public ApiResponse<?> subBatches(@PathVariable("id") String id){return ApiResponse.success(service.subBatches(id),traces.currentTraceId());}
 @GetMapping("/main-batches/{id}/execution-units") public ApiResponse<?> executions(@PathVariable("id") String id){return ApiResponse.success(service.executions(id),traces.currentTraceId());}
 @GetMapping("/execution-units/{id}") public ApiResponse<?> execution(@PathVariable("id") String id){return ApiResponse.success(service.execution(id),traces.currentTraceId());}
 @PostMapping("/production-orders") public ApiResponse<?> createOrder(@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.createOrder(body,key),traces.currentTraceId());}
 @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
 @PostMapping("/main-batches") public ApiResponse<?> createBatch(@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.createBatch(body,key),traces.currentTraceId());}
 @PutMapping("/production-orders/{id}") public ApiResponse<?> updateOrder(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader(value="If-Match",required=false) String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.updateOrder(id,body,version,key),traces.currentTraceId());}
 @PutMapping("/main-batches/{id}") public ApiResponse<?> updateBatch(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader(value="If-Match",required=false) String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.updateBatch(id,body,version,key),traces.currentTraceId());}
 @PostMapping("/main-batches/{id}/sub-batches") public ApiResponse<?> createSubBatch(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader(value="If-Match",required=false) String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.createSubBatch(id,body,version,key),traces.currentTraceId());}
 @PostMapping("/main-batches/{id}/release") public ApiResponse<?> releaseBatch(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader(value="If-Match",required=false) String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.releaseBatch(id,body,version,key),traces.currentTraceId());}
 @PostMapping("/main-batches/{id}/start") public ApiResponse<?> startBatch(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader(value="If-Match",required=false) String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.startBatch(id,body,version,key),traces.currentTraceId());}
 @PostMapping("/main-batches/{id}/complete-production") public ApiResponse<?> completeProduction(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader(value="If-Match",required=false) String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.completeProduction(id,body,version,key),traces.currentTraceId());}
 @PostMapping("/main-batches/{id}/submit-qa") public ApiResponse<?> submitQa(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader(value="If-Match",required=false) String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.submitQa(id,body,version,key),traces.currentTraceId());}
}
