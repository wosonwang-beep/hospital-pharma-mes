package com.hospital.mes.reporting.api;
import com.hospital.mes.reporting.application.*;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
@RestController @RequestMapping("/api/v1/printing") @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class PrintController {
 private final PrintService service;private final TraceIdProvider traces;
 public PrintController(PrintService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 private ApiResponse<?> ok(Object data){return ApiResponse.success(data,traces.currentTraceId());}
 public record VersionCommand(long versionNo,String reason){}
 public record BindCommand(String businessType,boolean enabled,String reason){}
 public record GenerateCommand(String businessType,String businessId,String templateVersionId,boolean formal){}
 public record NativeDesignCommand(String templateCode,String templateName,String businessType,String reason,com.fasterxml.jackson.databind.JsonNode design){}
 @GetMapping("/types") public ApiResponse<?> types(){return ok(service.types());}
 @GetMapping("/fields") public ApiResponse<?> fields(@RequestParam("businessType") String businessType){return ok(service.fields(businessType));}
 @GetMapping("/designer/default") public ApiResponse<?> nativeDefaults(@RequestParam("businessType") String type){return ok(service.nativeDesignDefaults(type));}
 @GetMapping("/templates/{id}/design") public ApiResponse<?> nativeDesign(@PathVariable("id") String id){return ok(service.nativeDesign(id));}
 @PostMapping("/designer") public ApiResponse<?> createNativeDesign(@RequestBody NativeDesignCommand c){return ok(service.createNativeDesign(c.templateCode(),c.templateName(),c.businessType(),c.design(),c.reason()));}
 @GetMapping("/templates") public ApiResponse<?> list(@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam(name="keyword",required=false) String keyword,@RequestParam Map<String,String> filters){return ok(service.list(page,size,keyword,filters));}
 @GetMapping("/applicable") public ApiResponse<?> applicable(@RequestParam("businessType") String businessType){return ok(service.applicable(businessType));}
 @PostMapping(value="/templates",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public ApiResponse<?> upload(@RequestParam("templateCode") String templateCode,@RequestParam("templateName") String templateName,@RequestParam("businessType") String businessType,@RequestParam("reason") String reason,@RequestPart("file") MultipartFile file) throws java.io.IOException {if(file.getSize()>DocxGuard.MAX_UPLOAD||file.getOriginalFilename()==null||!file.getOriginalFilename().toLowerCase(Locale.ROOT).endsWith(".docx"))throw new IllegalArgumentException("仅支持5MB以内DOCX");return ok(service.upload(templateCode,templateName,businessType,file.getBytes(),reason));}
 @PostMapping("/templates/{id}/validate") public ApiResponse<?> validate(@PathVariable("id") String id,@RequestBody VersionCommand c){return ok(service.validate(id,c.versionNo(),c.reason()));}
 @PostMapping("/templates/{id}/publish") public ApiResponse<?> publish(@PathVariable("id") String id,@RequestBody VersionCommand c){return ok(service.publish(id,c.versionNo(),c.reason()));}
 @PostMapping("/templates/{id}/deactivate") public ApiResponse<?> deactivate(@PathVariable("id") String id,@RequestBody VersionCommand c){return ok(service.deactivate(id,c.versionNo(),c.reason()));}
 @PostMapping("/templates/{id}/bind") public ApiResponse<?> bind(@PathVariable("id") String id,@RequestBody BindCommand c){return ok(service.bind(id,c.businessType(),c.enabled(),c.reason()));}
 @PostMapping("/artifacts") public ApiResponse<?> generate(@RequestBody GenerateCommand c){return ok(service.generate(c.businessType(),c.businessId(),c.templateVersionId(),c.formal()));}
 @GetMapping("/artifacts") public ApiResponse<?> history(@RequestParam("businessType") String businessType,@RequestParam("businessId") String businessId){return ok(service.history(businessType,businessId));}
 @GetMapping("/artifacts/{id}/pdf") public ResponseEntity<byte[]> pdf(@PathVariable("id") String id,@RequestParam(name="download",defaultValue="false") boolean download){return pdf(service.pdf(id),"print-"+id+".pdf",download);}
 @GetMapping("/templates/{id}/preview") public ResponseEntity<byte[]> preview(@PathVariable("id") String id){return pdf(service.preview(id),"template-preview.pdf",false);}
 @GetMapping("/templates/{id}/docx") public ResponseEntity<byte[]> docx(@PathVariable("id") String id){return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL,"no-store").header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=template.docx").contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")).body(service.template(id));}
 @GetMapping("/sample.docx") public ResponseEntity<byte[]> sample(){service.fields("INSPECTION_REPORT");return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=inspection-report-template.docx").contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")).body(InspectionSampleTemplate.create());}
 private ResponseEntity<byte[]> pdf(byte[] bytes,String name,boolean download){return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CACHE_CONTROL,"no-store").header("X-Content-Type-Options","nosniff").header(HttpHeaders.CONTENT_DISPOSITION,(download?"attachment":"inline")+"; filename="+name).body(bytes);}
}
