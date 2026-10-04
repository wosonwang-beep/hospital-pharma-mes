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
 @PutMapping("/products/{id}") public Object updateProduct(@PathVariable("id") String id,@RequestBody ProductUpdate r,@RequestHeader(name="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return response(service.updateProduct(id,r,version,key));}
 @GetMapping("/process-packages") public Object packages(@RequestParam(name="page",defaultValue="0")int page,@RequestParam(name="size",defaultValue="20")int size,@RequestParam(name="keyword",required=false)String keyword,@RequestParam Map<String,String> params){return response(service.packages(page,size,keyword,params));}
 @GetMapping("/process-packages/{id}") public Object getPackage(@PathVariable("id") String id,@RequestParam(name="versionId",required=false)String versionId){return response(service.getPackage(id,versionId));}
 @PostMapping("/process-packages") public Object createPackage(@RequestBody PackageCreate r,@RequestHeader("Idempotency-Key")String key){return response(service.createPackage(r,key));}
 @PutMapping("/process-packages/{id}") public Object updatePackage(@PathVariable("id") String id,@RequestBody PackageUpdate r,@RequestHeader(name="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return response(service.updatePackage(id,r,version,key));}
 @PostMapping("/process-packages/{id}/versions") public Object createVersion(@PathVariable("id") String id,@RequestBody VersionCreate r,@RequestHeader(name="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return response(service.createVersion(id,r,version,key));}
 @GetMapping("/process-versions/{id}/formula") public Object formula(@PathVariable("id") String id){return response(service.formula(id));}
 @PutMapping("/process-versions/{id}/formula") public Object formula(@PathVariable("id") String id,@RequestBody FormulaSave r,@RequestHeader(name="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return response(service.saveFormula(id,r,version,key));}
 @GetMapping("/process-versions/{id}/route") public Object route(@PathVariable("id") String id){return response(service.route(id));}
 @PutMapping("/process-versions/{id}/route") public Object route(@PathVariable("id") String id,@RequestBody RouteSave r,@RequestHeader(name="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return response(service.saveRoute(id,r,version,key));}
 @PostMapping("/process-versions/{id}/lint") public Object lint(@PathVariable("id") String id,@RequestBody Transition r,@RequestHeader(name="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return response(service.lint(id,r,version,key));}
 @PostMapping("/process-versions/{id}/submit") public Object submit(@PathVariable("id") String id,@RequestBody Transition r,@RequestHeader(name="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return response(service.transition(id,"SUBMIT",r,version,key));}
 @PostMapping("/process-versions/{id}/approve") public Object approve(@PathVariable("id") String id,@RequestBody Transition r,@RequestHeader(name="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return response(service.transition(id,"APPROVE",r,version,key));}
 @PostMapping("/process-versions/{id}/publish") public Object publish(@PathVariable("id") String id,@RequestBody Transition r,@RequestHeader(name="If-Match",required=false)String version,@RequestHeader("Idempotency-Key")String key){return response(service.transition(id,"PUBLISH",r,version,key));}
}
