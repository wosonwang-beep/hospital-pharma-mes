package com.hospital.mes.ebr.api;
import com.hospital.mes.ebr.application.EbrRuntimeService;
import com.hospital.mes.ebr.domain.EbrRuntimeCommands.*;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1") @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class EbrRuntimeController {
 private final EbrRuntimeService service;private final TraceIdProvider traces;
 public EbrRuntimeController(EbrRuntimeService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 private ApiResponse<JsonNode> response(JsonNode body){return ApiResponse.success(body,traces.currentTraceId());}
 @GetMapping("/execution-units/{id}/forms")public ApiResponse<JsonNode> forms(@PathVariable("id")String id){return response(service.forms(id));}
 @GetMapping("/forms/{id}/render-model")public ApiResponse<JsonNode> render(@PathVariable("id")String id){return response(service.render(id));}
 @PutMapping("/forms/{id}/draft-values")public ApiResponse<JsonNode> save(@PathVariable("id")String id,@RequestBody Save body,@RequestHeader("If-Match")String match,@RequestHeader("Idempotency-Key")String key){return response(service.command("SAVE",id,body,match,key));}
 @PostMapping("/forms/{id}/submit")public ApiResponse<JsonNode> submit(@PathVariable("id")String id,@RequestBody Command body,@RequestHeader("If-Match")String match,@RequestHeader("Idempotency-Key")String key){return response(service.command("SUBMIT",id,body,match,key));}
 @PostMapping("/field-values/{id}/corrections")public ApiResponse<JsonNode> correct(@PathVariable("id")String id,@RequestBody Correction body,@RequestHeader("If-Match")String match,@RequestHeader("Idempotency-Key")String key){return response(service.command("CORRECT",id,body,match,key));}
 @PostMapping("/forms/{id}/reviews")public ApiResponse<JsonNode> review(@PathVariable("id")String id,@RequestBody Review body,@RequestHeader("If-Match")String match,@RequestHeader("Idempotency-Key")String key){return response(service.command("REVIEW",id,body,match,key));}
 @GetMapping("/main-batches/{id}/ebr")public ApiResponse<JsonNode> batch(@PathVariable("id")String id){return response(service.batch(id));}
}
