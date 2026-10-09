package com.hospital.mes.process.api;
import com.hospital.mes.process.application.PrescriptionService;
import com.hospital.mes.process.domain.ProcessCommands.*;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/v1/production-prescriptions")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class PrescriptionController {
 private final PrescriptionService service;private final TraceIdProvider traces;
 public PrescriptionController(PrescriptionService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 private <T>ApiResponse<T> response(T data){return ApiResponse.success(data,traces.currentTraceId());}
 @GetMapping public Object list(@RequestParam(name="page",defaultValue="0")int page,@RequestParam(name="size",defaultValue="20")int size,@RequestParam(name="keyword",required=false)String keyword,@RequestParam Map<String,String> params){var filters=new java.util.HashMap<>(params);filters.remove("page");filters.remove("size");filters.remove("keyword");return response(service.list(page,size,keyword,filters));}
 @GetMapping("/{id}") public Object get(@PathVariable("id")String id){return response(service.get(id));}
 @PostMapping public Object create(@RequestBody PrescriptionCreate body,@RequestHeader("Idempotency-Key")String key){return response(service.create(body,key));}
 @PutMapping("/{id}") public Object update(@PathVariable("id")String id,@RequestBody PrescriptionUpdate body,@RequestHeader(value="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return response(service.update(id,body,version,key));}
 @PostMapping("/{id}/activate") public Object activate(@PathVariable("id")String id,@RequestBody PrescriptionAction body,@RequestHeader(value="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return response(service.activate(id,body,version,key));}
 @PostMapping("/{id}/deactivate") public Object deactivate(@PathVariable("id")String id,@RequestBody PrescriptionAction body,@RequestHeader(value="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return response(service.deactivate(id,body,version,key));}
}