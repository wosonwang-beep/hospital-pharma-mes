package com.hospital.mes.masterdata.api;
import com.fasterxml.jackson.databind.JsonNode;import com.hospital.mes.common.api.ApiResponse;import com.hospital.mes.common.trace.TraceIdProvider;import com.hospital.mes.masterdata.application.*;import org.springframework.web.bind.annotation.*;import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;import java.util.Map;
@RestController @RequestMapping("/api/v1/materials") @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class MaterialController {
 private final MaterialService service;private final MaterialSupplierService suppliers;private final TraceIdProvider traces;
 public MaterialController(MaterialService service,MaterialSupplierService suppliers,TraceIdProvider traces){this.service=service;this.suppliers=suppliers;this.traces=traces;}
 @GetMapping public ApiResponse<ScopedStore.PageData<JsonNode>> list(@RequestParam(name="page",defaultValue="0")int page,@RequestParam(name="size",defaultValue="20")int size,@RequestParam(name="keyword",required=false)String keyword,@RequestParam Map<String,String> filters){return ApiResponse.success(service.list(page,size,keyword,filters),traces.currentTraceId());}
 @GetMapping("/{id}") public ApiResponse<JsonNode> get(@PathVariable("id")String id){return response(service.get(id));}
 @PostMapping @ResponseStatus(org.springframework.http.HttpStatus.CREATED) public ApiResponse<JsonNode> create(@RequestBody MaterialCommands.Create r,@RequestHeader("Idempotency-Key")String key){return response(service.create(r,key));}
 @PutMapping("/{id}") public ApiResponse<JsonNode> update(@PathVariable("id")String id,@RequestBody MaterialCommands.Update r,@RequestHeader("Idempotency-Key")String key){return response(service.update(id,r,null,key));}
 @PostMapping("/{id}/disable") public ApiResponse<JsonNode> disable(@PathVariable("id")String id,@RequestBody MaterialCommands.Disable r,@RequestHeader("Idempotency-Key")String key){return response(service.disable(id,r,null,key));}
 @GetMapping("/{id}/suppliers") public ApiResponse<JsonNode> suppliers(@PathVariable("id")String id){return response(suppliers.get(id));}
 @PutMapping("/{id}/suppliers") public ApiResponse<JsonNode> assign(@PathVariable("id")String id,@RequestBody SupplierCommands.Assign r,@RequestHeader("Idempotency-Key")String key){return response(suppliers.assign(id,r,null,key));}
 private ApiResponse<JsonNode> response(JsonNode n){return ApiResponse.success(n,traces.currentTraceId());}
}
