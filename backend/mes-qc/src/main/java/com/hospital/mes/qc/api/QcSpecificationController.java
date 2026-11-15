package com.hospital.mes.qc.api;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.qc.application.*;
import com.hospital.mes.qc.domain.QcCommands;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.*;
/** CRUD endpoints of the QC quality standard basic library (DCP-QC-SPEC-BASIC-LIBRARY-001). */
@RestController @RequestMapping("/api/v1/quality") @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class QcSpecificationController {
 private final QcSpecificationService service;private final TraceIdProvider traces;
 public QcSpecificationController(QcSpecificationService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 @GetMapping("/specifications") @PreAuthorize("hasAuthority('qms:specification:view')")
 public ApiResponse<QcViews.Page<QcViews.SpecificationSummary>> list(@RequestParam(name="page",defaultValue="0")int page,@RequestParam(name="size",defaultValue="20")int size,@RequestParam(name="keyword",required=false)String keyword,@RequestParam(name="basisType",required=false)String basisType,@RequestParam(name="materialId",required=false)String material,@RequestParam(name="productId",required=false)String product,@RequestParam(name="status",required=false)String status){return ok(service.list(page,size,keyword,basisType,material,product,status));}
 @PostMapping("/specifications") @PreAuthorize("hasAuthority('qms:specification:create')") @ResponseStatus(HttpStatus.CREATED)
 public ResponseEntity<ApiResponse<com.fasterxml.jackson.databind.JsonNode>> create(@RequestBody QcCommands.Create r,@RequestHeader("Idempotency-Key")String key){return tagged(service.create(r,key),201);}
 @GetMapping("/specifications/{id}") @PreAuthorize("hasAuthority('qms:specification:view')")
 public ResponseEntity<ApiResponse<com.fasterxml.jackson.databind.JsonNode>> get(@PathVariable("id")String id){return tagged(service.get(id),200);}
 @PutMapping("/specifications/{id}") @PreAuthorize("hasAuthority('qms:specification:edit')")
 public ResponseEntity<ApiResponse<com.fasterxml.jackson.databind.JsonNode>> edit(@PathVariable("id")String id,@RequestBody QcCommands.Edit r,@RequestHeader("If-Match")String expected,@RequestHeader("Idempotency-Key")String key){return tagged(service.edit(id,r,expected,key),200);}
 @PutMapping("/specifications/{id}/scope") @PreAuthorize("hasAuthority('qms:specification:edit')")
 public ResponseEntity<ApiResponse<com.fasterxml.jackson.databind.JsonNode>> replaceScope(@PathVariable("id")String id,@RequestBody QcCommands.ReplaceScope r,@RequestHeader("If-Match")String expected,@RequestHeader("Idempotency-Key")String key){return tagged(service.replaceScope(id,r,expected,key),200);}
 @PutMapping("/specifications/{id}/items") @PreAuthorize("hasAuthority('qms:specification:edit')")
 public ResponseEntity<ApiResponse<com.fasterxml.jackson.databind.JsonNode>> replaceItems(@PathVariable("id")String id,@RequestBody QcCommands.ReplaceItems r,@RequestHeader("If-Match")String expected,@RequestHeader("Idempotency-Key")String key){return tagged(service.replaceItems(id,r,expected,key),200);}
 @DeleteMapping("/specifications/{id}") @PreAuthorize("hasAuthority('qms:specification:delete')")
 public ApiResponse<JsonNode> delete(@PathVariable("id")String id,@RequestBody QcCommands.Remove r,@RequestHeader("If-Match")String expected,@RequestHeader("Idempotency-Key")String key){return ok(service.delete(id,r,expected,key));}
 private <T>ApiResponse<T> ok(T value){return ApiResponse.success(value,traces.currentTraceId());}
 private ResponseEntity<ApiResponse<com.fasterxml.jackson.databind.JsonNode>> tagged(com.fasterxml.jackson.databind.JsonNode v,int status){return ResponseEntity.status(status).eTag("\""+v.path("versionNo").asLong()+"\"").body(ok(v));}
}
