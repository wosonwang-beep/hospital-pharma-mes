package com.hospital.mes.foundation.api;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/foundation")
public class FoundationController {
 private final TraceIdProvider traces;
 public FoundationController(TraceIdProvider traces){this.traces=traces;}
 @GetMapping("/status") public ApiResponse<Map<String,String>> status(){return ApiResponse.success(Map.of("status","ready"),traces.currentTraceId());}
}
