package com.hospital.mes.production.api;
import com.hospital.mes.production.application.NavigationQueryService;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/v1/navigation") @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class NavigationController {
 private final NavigationQueryService service;private final TraceIdProvider traces;
 public NavigationController(NavigationQueryService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 @GetMapping("/executions") public ApiResponse<?> executions(@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam Map<String,String> f){return ApiResponse.success(service.executions(page,size,f),traces.currentTraceId());}
 @GetMapping("/batches") public ApiResponse<?> batches(@RequestParam("context") String context,@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam Map<String,String> f){return ApiResponse.success(service.batches(context,page,size,f),traces.currentTraceId());}
 @GetMapping("/batches/{id}") public ApiResponse<?> batch(@PathVariable("id") String id,@RequestParam("context") String context,@RequestParam Map<String,String> f){if(!f.keySet().equals(java.util.Set.of("context")))throw new IllegalArgumentException("Unknown navigation filter");return ApiResponse.success(service.batch(context,id),traces.currentTraceId());}
}
