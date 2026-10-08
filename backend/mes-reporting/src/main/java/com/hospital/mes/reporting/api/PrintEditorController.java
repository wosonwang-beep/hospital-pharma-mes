package com.hospital.mes.reporting.api;
import com.hospital.mes.reporting.application.PrintEditorService;
import com.hospital.mes.common.api.ApiResponse;import com.hospital.mes.common.trace.TraceIdProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;import org.springframework.http.*;import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;import java.util.*;
@RestController @RequestMapping("/api/v1/printing/editor") @ConditionalOnProperty(prefix="mes.print.editor",name="enabled",havingValue="true")
public class PrintEditorController {
 private final PrintEditorService service;private final TraceIdProvider traces;
 public PrintEditorController(PrintEditorService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 public record OpenCommand(String templateVersionId){}
 @PostMapping("/sessions") public ApiResponse<?> open(@RequestBody OpenCommand c){return ApiResponse.success(service.open(c.templateVersionId()),traces.currentTraceId());}
 @GetMapping("/sessions/{id}") public ApiResponse<?> status(@PathVariable("id") String id){return ApiResponse.success(service.status(id),traces.currentTraceId());}
 @GetMapping("/transfer/{id}/document") public ResponseEntity<byte[]> document(@PathVariable("id") String id,@RequestParam("token") String token){return docx(service.document(id,token));}
 @GetMapping("/transfer/{id}/dictionary") public Object dictionary(@PathVariable("id") String id,@RequestParam("token") String token){return service.dictionary(id,token);}
 @GetMapping("/transfer/{id}/loop") public ResponseEntity<byte[]> loop(@PathVariable("id") String id,@RequestParam("token") String token,@RequestParam("columns") List<String> columns){return docx(service.loop(id,token,columns));}
 @GetMapping("/transfer/{id}/plugin-config") public Object plugin(@PathVariable("id") String id,@RequestParam("token") String token){return service.plugin(id,token);}
 @PostMapping("/transfer/{id}/callback") public Map<String,Integer> callback(@PathVariable("id") String id,@RequestParam("token") String token,@RequestHeader(value="Authorization",required=false)String authorization,@RequestBody JsonNode body){return service.callback(id,token,authorization,body);}
 private ResponseEntity<byte[]> docx(byte[] bytes){return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL,"no-store").header("X-Content-Type-Options","nosniff").contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")).body(bytes);}
}
