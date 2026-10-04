package com.hospital.mes.masterdata.api;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.hospital.mes.masterdata.application.ScopedStore;
import com.hospital.mes.masterdata.application.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.Map;
@RestController @RequestMapping("/api/v1/organizations") @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class OrganizationController {
 private final OrganizationService service;private final TraceIdProvider traces;
 public OrganizationController(OrganizationService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 @GetMapping public ApiResponse<ScopedStore.PageData<JsonNode>> list(@RequestParam(name="page",defaultValue="0")int page,@RequestParam(name="size",defaultValue="20")int size,@RequestParam(name="keyword",required=false)String keyword,@RequestParam Map<String,String> filters){return ApiResponse.success(service.list(page,size,keyword,filters),traces.currentTraceId());}
 @GetMapping("/{id}") public ApiResponse<JsonNode> get(@PathVariable("id")String id){return ApiResponse.success(service.get(id),traces.currentTraceId());}
 @PostMapping public ApiResponse<JsonNode> create(@RequestBody OrganizationCommands.Create body,@RequestHeader("Idempotency-Key")String key){return ApiResponse.success(service.create(body,key),traces.currentTraceId());}
 @PutMapping("/{id}") public ApiResponse<JsonNode> update(@PathVariable("id")String id,@RequestBody OrganizationCommands.Update body,@RequestHeader(name="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return ApiResponse.success(service.command(id,body,version,key),traces.currentTraceId());}
}
