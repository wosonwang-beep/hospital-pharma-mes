package com.hospital.mes.equipment.api;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.hospital.mes.masterdata.application.ScopedStore;
import com.hospital.mes.equipment.application.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.Map;
@RestController @RequestMapping("/api/v1/equipment") @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class EquipmentController {
 private final EquipmentService service;private final TraceIdProvider traces;
 public EquipmentController(EquipmentService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 @GetMapping public ApiResponse<ScopedStore.PageData<JsonNode>> list(@RequestParam(name="page",defaultValue="0")int page,@RequestParam(name="size",defaultValue="20")int size,@RequestParam(name="keyword",required=false)String keyword,@RequestParam Map<String,String> filters){return ApiResponse.success(service.list(page,size,keyword,filters),traces.currentTraceId());}
 @GetMapping("/{id}") public ApiResponse<JsonNode> get(@PathVariable("id")String id){return ApiResponse.success(service.get(id),traces.currentTraceId());}
 @PostMapping public ApiResponse<JsonNode> create(@RequestBody EquipmentCommands.Create body,@RequestHeader("Idempotency-Key")String key){return ApiResponse.success(service.create(body,key),traces.currentTraceId());}
 @PutMapping("/{id}") public ApiResponse<JsonNode> update(@PathVariable("id")String id,@RequestBody EquipmentCommands.Update body,@RequestHeader("Idempotency-Key")String key){return ApiResponse.success(service.command(id,body,null,key),traces.currentTraceId());}
}
