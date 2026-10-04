package com.hospital.mes.audit.attachment.api;

import com.hospital.mes.audit.attachment.application.AttachmentService;
import com.hospital.mes.audit.attachment.domain.AttachmentRules;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController @RequestMapping("/api/v1/attachments")
@ConditionalOnProperty(prefix="spring.datasource",name="url")
public class AttachmentController {
    private final AttachmentService service;private final TraceIdProvider traces;
    public AttachmentController(AttachmentService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
    @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AttachmentService.Metadata> upload(@RequestPart("file") MultipartFile file,@RequestHeader("Idempotency-Key") String key)throws IOException{
        service.require("attachment:upload");AttachmentRules.validateSize(file.getSize());
        return ApiResponse.success(service.upload(file.getOriginalFilename(),file.getContentType(),file.getBytes(),key),traces.currentTraceId());
    }
    @GetMapping("/{id}") public ApiResponse<AttachmentService.Metadata> get(@PathVariable("id") String id){return ApiResponse.success(service.get(id),traces.currentTraceId());}
    @GetMapping("/{id}/content") public ResponseEntity<byte[]> content(@PathVariable("id") String id){return download(service.download(id));}
    public static ResponseEntity<byte[]> download(AttachmentService.Download download){
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(download.metadata().mediaType()))
            .header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(download.metadata().fileName(),StandardCharsets.UTF_8).build().toString())
            .header("X-Content-Type-Options","nosniff").contentLength(download.metadata().byteLength()).body(download.content());
    }
}
