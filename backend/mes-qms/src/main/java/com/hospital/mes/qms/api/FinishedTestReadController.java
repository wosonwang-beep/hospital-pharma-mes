package com.hospital.mes.qms.api;
import com.hospital.mes.qms.application.FinishedTestReadService;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/v1/quality/finished-tests") @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class FinishedTestReadController {
 private final FinishedTestReadService service;private final TraceIdProvider traces;
 public FinishedTestReadController(FinishedTestReadService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 @GetMapping public ApiResponse<?> list(@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam Map<String,String> f){return ApiResponse.success(service.list(page,size,f),traces.currentTraceId());}
 @GetMapping("/{id}") public ApiResponse<?> get(@PathVariable("id") String id,@RequestParam Map<String,String> f){if(!f.isEmpty())throw new IllegalArgumentException("Unknown finished test detail filter");return ApiResponse.success(service.get(id),traces.currentTraceId());}
}
