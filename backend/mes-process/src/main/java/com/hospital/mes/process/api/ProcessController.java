package com.hospital.mes.process.api;
import com.hospital.mes.process.application.ProcessService;
import com.hospital.mes.process.domain.ProcessCommands.*;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/v1")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ProcessController {
 private final ProcessService service;private final TraceIdProvider traces;public ProcessController(ProcessService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 private <T>ApiResponse<T> response(T data){return ApiResponse.success(data,traces.currentTraceId());}
 @GetMapping("/products") public Object products(@RequestParam(name="page",defaultValue="0")int page,@RequestParam(name="size",defaultValue="20")int size,@RequestParam(name="keyword",required=false)String keyword,@RequestParam Map<String,String> params){return response(service.products(page,size,keyword,params));}
 @GetMapping("/products/{id}") public Object product(@PathVariable("id") String id){return response(service.product(id));}
 @PostMapping("/products") public Object createProduct(@RequestBody ProductCreate r,@RequestHeader("Idempotency-Key")String key){return response(service.createProduct(r,key));}
 @PutMapping("/products/{id}") public Object updateProduct(@PathVariable("id") String id,@RequestBody ProductUpdate r,@RequestHeader("Idempotency-Key")String key){return response(service.updateProduct(id,r,null,key));}
 @GetMapping("/process-packages") public Object packages(@RequestParam(name="page",defaultValue="0")int page,@RequestParam(name="size",defaultValue="20")int size,@RequestParam(name="keyword",required=false)String keyword,@RequestParam Map<String,String> params){return response(service.packages(page,size,keyword,params));}
 @GetMapping("/process-packages/{id}") public Object getPackage(@PathVariable("id") String id){return response(service.getPackage(id,null));}
 @PostMapping("/process-packages") public Object createPackage(@RequestBody PackageCreate r,@RequestHeader("Idempotency-Key")String key){return response(service.createPackage(r,key));}
 @PutMapping("/process-packages/{id}") public Object updatePackage(@PathVariable("id") String id,@RequestBody PackageUpdate r,@RequestHeader("Idempotency-Key")String key){return response(service.updatePackage(id,r,null,key));}
 @PutMapping("/process-packages/{id}/current-definition") public Object saveCurrentDefinition(@PathVariable("id")String id,@RequestBody CurrentDefinitionSave r,@RequestHeader("Idempotency-Key")String key){return response(service.saveCurrentDefinition(id,r,null,key));}
}
