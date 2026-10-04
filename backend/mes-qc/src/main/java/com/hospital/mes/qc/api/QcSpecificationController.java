package com.hospital.mes.qc.api;
import com.hospital.mes.qc.application.*;
import com.hospital.mes.qc.domain.QcCommands;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.*;
@RestController @RequestMapping("/api/v1/quality") @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class QcSpecificationController {
 private final QcSpecificationService service;private final TraceIdProvider traces;
 public QcSpecificationController(QcSpecificationService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 @GetMapping("/specifications") @PreAuthorize("hasAuthority('qms:specification:view')")
 public ApiResponse<QcViews.Page<QcViews.SpecificationSummary>> list(@RequestParam(name="page",defaultValue="0")int page,@RequestParam(name="size",defaultValue="20")int size,@RequestParam(name="keyword",required=false)String keyword,@RequestParam(name="materialId",required=false)String material,@RequestParam(name="versionStatus",required=false)String state){return ok(service.list(page,size,keyword,material,state));}
 @PostMapping("/specifications") @PreAuthorize("hasAuthority('qms:specification:create')") @ResponseStatus(HttpStatus.CREATED)
 public ApiResponse<QcViews.SpecificationDetail> create(@RequestBody QcCommands.CreateSpecification r,@RequestHeader("Idempotency-Key")String key){return ok(service.create(r,key));}
 @GetMapping("/specifications/{id}") @PreAuthorize("hasAuthority('qms:specification:view')")
 public ApiResponse<QcViews.SpecificationDetail> get(@PathVariable("id")String id){return ok(service.get(id));}
 @PostMapping("/specifications/{id}/versions") @PreAuthorize("hasAuthority('qms:specification:create')")
 public ResponseEntity<ApiResponse<QcViews.SpecificationVersionDetail>> createVersion(@PathVariable("id")String id,@RequestBody QcCommands.CreateVersion r,@RequestHeader("Idempotency-Key")String key){return version(service.createVersion(id,r,key),201);}
 @GetMapping("/specification-versions/{id}") @PreAuthorize("hasAuthority('qms:specification:view')")
 public ResponseEntity<ApiResponse<QcViews.SpecificationVersionDetail>> getVersion(@PathVariable("id")String id){return version(service.getVersion(id),200);}
 @PutMapping("/specification-versions/{id}") @PreAuthorize("hasAuthority('qms:specification:edit')")
 public ResponseEntity<ApiResponse<QcViews.SpecificationVersionDetail>> editVersion(@PathVariable("id")String id,@RequestBody QcCommands.EditVersion r,@RequestHeader("If-Match")String expected,@RequestHeader("Idempotency-Key")String key){return version(service.editVersion(id,r,expected,key),200);}
 @PostMapping("/specification-versions/{id}/approve") @PreAuthorize("hasAuthority('qms:specification:approve') and hasAuthority('ebr:sign')")
 public ResponseEntity<ApiResponse<QcViews.SpecificationVersionDetail>> approve(@PathVariable("id")String id,@RequestBody QcCommands.SignVersion r,@RequestHeader("If-Match")String expected,@RequestHeader("Idempotency-Key")String key){return version(service.approveVersion(id,r,expected,key),200);}
 @PostMapping("/specification-versions/{id}/retire") @PreAuthorize("hasAuthority('qms:specification:retire') and hasAuthority('ebr:sign')")
 public ResponseEntity<ApiResponse<QcViews.SpecificationVersionDetail>> retire(@PathVariable("id")String id,@RequestBody QcCommands.SignVersion r,@RequestHeader("If-Match")String expected,@RequestHeader("Idempotency-Key")String key){return version(service.retireVersion(id,r,expected,key),200);}
 private <T>ApiResponse<T> ok(T value){return ApiResponse.success(value,traces.currentTraceId());}
 private ResponseEntity<ApiResponse<QcViews.SpecificationVersionDetail>> version(QcViews.SpecificationVersionDetail v,int status){return ResponseEntity.status(status).eTag("\""+v.versionNo()+"\"").body(ok(v));}
}
