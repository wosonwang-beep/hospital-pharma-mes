package com.hospital.mes.wms.api;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.wms.application.InventoryDecisionService;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/material-lots")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class InventoryDecisionController {
 private final InventoryDecisionService service;private final TraceIdProvider traces;
 public InventoryDecisionController(InventoryDecisionService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 @PostMapping("/{id}/freeze") public ApiResponse<?> freeze(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.decide(id,true,body,version,key),traces.currentTraceId());}
 @PostMapping("/{id}/unfreeze") public ApiResponse<?> unfreeze(@PathVariable("id") String id,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ApiResponse.success(service.decide(id,false,body,version,key),traces.currentTraceId());}
}
