package com.hospital.mes.qms.api;
import com.hospital.mes.qms.application.FinishedInspectionService;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.Map;
@RestController @RequestMapping("/api/v1/quality") @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class FinishedInspectionController {
 private final FinishedInspectionService service;private final TraceIdProvider traces;
 public FinishedInspectionController(FinishedInspectionService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 private ApiResponse<?> ok(Object value){return ApiResponse.success(value,traces.currentTraceId());}
 @GetMapping("/finished-inspection-requests") public ApiResponse<?> requests(@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam Map<String,String> filters){return ok(service.list("requests",page,size,filters));}
 @PostMapping("/finished-inspection-requests") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<?> create(@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key){return ok(service.createRequest(body,key));}
 @GetMapping("/finished-inspection-requests/{id}") public ApiResponse<?> request(@PathVariable("id") String id){return ok(service.getRequest(id));}
 @PostMapping("/finished-inspection-requests/{id}/{action:submit|accept}") public ApiResponse<?> action(@PathVariable("id") String id,@PathVariable("action") String action,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ok(service.requestAction(id,action,body,version,key));}
 @PostMapping("/finished-inspection-requests/{id}/sample") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<?> sample(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ok(service.sample(id,body,version,key));}
 @PostMapping("/finished-inspection-requests/{id}/generate-report") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<?> generate(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ok(service.generateReport(id,body,version,key));}
 @GetMapping("/finished-sampling-records") public ApiResponse<?> sampling(@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam Map<String,String> filters){return ok(service.list("sampling",page,size,filters));}
 @GetMapping("/finished-sampling-records/{id}") public ApiResponse<?> sampling(@PathVariable("id") String id){return ok(service.getSampling(id));}
 @GetMapping("/finished-inspection-reports") public ApiResponse<?> reports(@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam Map<String,String> filters){return ok(service.list("reports",page,size,filters));}
 @GetMapping("/finished-inspection-reports/{id}") public ApiResponse<?> report(@PathVariable("id") String id){return ok(service.getReport(id));}
 @PostMapping("/finished-inspection-reports/{id}/approve") public ApiResponse<?> approve(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ok(service.approveReport(id,body,version,key));}
}
