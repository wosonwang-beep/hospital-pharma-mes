package com.hospital.mes.qms.api;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.qms.application.IpcService;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/v1/ipc") @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class IpcController {
 private final IpcService service;private final TraceIdProvider traces;
 public IpcController(IpcService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 @GetMapping public ApiResponse<?> list(@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam Map<String,String> filters){return ApiResponse.success(service.list(page,size,filters),traces.currentTraceId());}
 @PostMapping public ApiResponse<?> create(@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.create(body,key),traces.currentTraceId());}
 @GetMapping("/{id}") public ApiResponse<?> get(@PathVariable("id") String id){return ApiResponse.success(service.get(id),traces.currentTraceId());}
 @PostMapping("/{id}/submit-result") public ApiResponse<?> submit(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.submit(id,body,version,key),traces.currentTraceId());}
 @PostMapping("/{id}/review-result") public ApiResponse<?> review(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.reviewResult(id,body,version,key),traces.currentTraceId());}
}
