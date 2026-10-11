package com.hospital.mes.reporting.api;
import com.hospital.mes.reporting.application.PrintDataSourceService;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@RestController @RequestMapping("/api/v1/printing") @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class PrintDataSourceController {
 private final PrintDataSourceService service;private final TraceIdProvider traces;
 public PrintDataSourceController(PrintDataSourceService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 private ApiResponse<?> ok(Object value){return ApiResponse.success(value,traces.currentTraceId());}
 @GetMapping("/data-sources") public ApiResponse<?> list(@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam(name="keyword",required=false) String keyword,@RequestParam(name="businessType",required=false) String businessType,@RequestParam(name="status",required=false) String status,@RequestParam(name="includeDeleted",defaultValue="false") boolean includeDeleted){return ok(service.list(page,size,keyword,businessType,status,includeDeleted));}
 @GetMapping("/data-sources/metadata/catalogs") public ApiResponse<?> catalogs(){return ok(service.catalogs());}
 @GetMapping("/data-sources/metadata/document-types") public ApiResponse<?> types(@RequestParam(name="sourceId",required=false) String sourceId){return ok(service.documentTypes(sourceId));}
 @GetMapping("/data-sources/metadata/authorization-policies") public ApiResponse<?> policies(){return ok(service.policies());}
 @GetMapping("/document-types/{businessType}/metadata") public ApiResponse<?> metadata(@PathVariable("businessType") String businessType){return ok(service.getMetadata(businessType));}
 @PutMapping("/document-types/{businessType}/metadata") public ApiResponse<?> metadata(@PathVariable("businessType") String businessType,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ok(service.putMetadata(businessType,body,version,key));}
 @GetMapping("/data-sources/{id}") public ApiResponse<?> detail(@PathVariable("id") String id){return ok(service.detail(id));}
 @GetMapping("/data-sources/{id}/revisions/{revisionId}") public ApiResponse<?> revision(@PathVariable("id") String id,@PathVariable("revisionId") String revisionId){return ok(service.revision(id,revisionId));}
 @PostMapping("/data-sources") public ApiResponse<?> create(@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key){return ok(service.save(null,body,null,key));}
 @PutMapping("/data-sources/{id}/draft") public ApiResponse<?> draft(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ok(service.save(id,body,version,key));}
 @PostMapping("/data-sources/{id}/validate") public ApiResponse<?> validate(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ok(service.validate(id,body,version,key));}
 @PostMapping("/data-sources/{id}/publish") public ApiResponse<?> publish(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ok(service.publish(id,body,version,key));}
 @PostMapping("/data-sources/{id}/deactivate") public ApiResponse<?> deactivate(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ok(service.lifecycle(id,"deactivate",body,version,key));}
 @PostMapping("/data-sources/{id}/reactivate") public ApiResponse<?> reactivate(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ok(service.lifecycle(id,"reactivate",body,version,key));}
 @PostMapping("/data-sources/{id}/restore") public ApiResponse<?> restore(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ok(service.lifecycle(id,"restore",body,version,key));}
 @DeleteMapping("/data-sources/{id}") public ApiResponse<?> delete(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ok(service.lifecycle(id,"delete",body,version,key));}
}
