package com.hospital.mes.wms.api;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.hospital.mes.wms.application.*;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1") @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class WmsManagementController {
 private final MaterialRequestService requests;private final WmsManagementQueryService query;private final TraceIdProvider traces;
 public WmsManagementController(MaterialRequestService requests,WmsManagementQueryService query,TraceIdProvider traces){this.requests=requests;this.query=query;this.traces=traces;}
 @GetMapping("/material-requests") public ApiResponse<?> list(@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam Map<String,String> filters){return ok(requests.list(page,size,filters));}
 @PostMapping("/material-requests") @ResponseStatus(org.springframework.http.HttpStatus.CREATED) public ApiResponse<?> create(@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key){return ok(requests.create(body,key));}
 @GetMapping("/material-requests/{id}") public ApiResponse<?> get(@PathVariable("id") String id){return ok(requests.get(id));}
 @PutMapping("/material-requests/{id}") public ApiResponse<?> edit(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(name="If-Match",required=false) String version){return ok(requests.edit(id,body,version,key));}
 @PostMapping("/material-requests/{id}/submit") public ApiResponse<?> submit(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(name="If-Match",required=false) String version){return ok(requests.submit(id,body,version,key));}
 @PostMapping("/material-requests/{id}/cancel") public ApiResponse<?> cancel(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(name="If-Match",required=false) String version){return ok(requests.cancel(id,body,version,key));}
 @GetMapping("/wms/inventory") public ApiResponse<?> inventory(@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam Map<String,String> filters){return ok(query.inventory(page,size,filters));}
 @GetMapping("/material-returns") public ApiResponse<?> returns(@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam Map<String,String> filters){return ok(query.returns(page,size,filters));}
 private ApiResponse<?> ok(Object value){return ApiResponse.success(value,traces.currentTraceId());}
}
