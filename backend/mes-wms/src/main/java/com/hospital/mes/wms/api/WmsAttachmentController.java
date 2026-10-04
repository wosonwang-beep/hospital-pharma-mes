package com.hospital.mes.wms.api;
import com.hospital.mes.wms.application.WmsAttachmentService;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.nio.charset.StandardCharsets;
@RestController @RequestMapping("/api/v1/wms/receipts/{id}/attachments")
@ConditionalOnProperty(prefix="spring.datasource",name="url")
public class WmsAttachmentController {
 private final WmsAttachmentService service;private final TraceIdProvider traces;
 public WmsAttachmentController(WmsAttachmentService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 @GetMapping public ApiResponse<?> list(@PathVariable("id") String id){return ApiResponse.success(service.list(id),traces.currentTraceId());}
 @PostMapping public ApiResponse<?> link(@PathVariable("id") String id,@RequestBody WmsAttachmentService.Link body,
  @RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.link(id,body,version,key),traces.currentTraceId());}
 @GetMapping("/{attachmentId}/content") public ResponseEntity<byte[]> download(@PathVariable("id") String id,@PathVariable("attachmentId") String attachment){
  var file=service.download(id,attachment);
  return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.metadata().mediaType()))
   .header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(file.metadata().fileName(),StandardCharsets.UTF_8).build().toString())
   .header("X-Content-Type-Options","nosniff").contentLength(file.metadata().byteLength()).body(file.content());
 }
}
