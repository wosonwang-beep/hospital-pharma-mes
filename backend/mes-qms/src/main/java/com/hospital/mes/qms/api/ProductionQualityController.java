package com.hospital.mes.qms.api;
import com.hospital.mes.qms.application.ProductionQualityService;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import java.util.Map;
@RestController @RequestMapping("/api/v1") @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ProductionQualityController {
 private final ProductionQualityService service;private final TraceIdProvider traces;
 public ProductionQualityController(ProductionQualityService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 private ApiResponse<?> response(Object data){return ApiResponse.success(data,traces.currentTraceId());}
 @PostMapping("/samples") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<?> createSample(@RequestBody JsonNode b,@RequestHeader("Idempotency-Key") String key){return response(service.createSample(b,key));}
 @GetMapping("/samples") public ApiResponse<?> samples(@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam Map<String,String> filters){return response(service.list(ProductionQualityService.SAMPLE,page,size,filters));}
 @GetMapping("/samples/{id}") public ApiResponse<?> sample(@PathVariable("id") String id){return response(service.get(ProductionQualityService.SAMPLE,id));}
 @PostMapping("/samples/{id}/receive") public ApiResponse<?> receive(@PathVariable("id") String id,@RequestBody JsonNode b,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return response(service.receiveSample(id,b,version,key));}
 @PostMapping("/quality/production-tests") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<?> createTest(@RequestBody JsonNode b,@RequestHeader("Idempotency-Key") String key){return response(service.createTest(b,key));}
 @GetMapping("/quality/production-tests") public ApiResponse<?> tests(@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam Map<String,String> filters){return response(service.list(ProductionQualityService.TEST,page,size,filters));}
 @GetMapping("/quality/production-tests/{id}") public ApiResponse<?> test(@PathVariable("id") String id){return response(service.get(ProductionQualityService.TEST,id));}
 @PostMapping("/quality/production-tests/{id}/results") public ApiResponse<?> result(@PathVariable("id") String id,@RequestBody JsonNode b,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return response(service.result(id,false,b,version,key));}
 @PostMapping("/quality/production-tests/{id}/revisions") public ApiResponse<?> revise(@PathVariable("id") String id,@RequestBody JsonNode b,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return response(service.result(id,true,b,version,key));}
 @PostMapping("/quality/production-tests/{id}/review") public ApiResponse<?> review(@PathVariable("id") String id,@RequestBody JsonNode b,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return response(service.review(id,b,version,key));}
 @PostMapping("/quality/production-tests/{id}/retest") public ApiResponse<?> retest(@PathVariable("id") String id,@RequestBody JsonNode b,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return response(service.retest(id,b,version,key));}
 @PostMapping("/deviations/{id}/capas") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<?> capa(@PathVariable("id") String id,@RequestBody JsonNode b,@RequestHeader("Idempotency-Key") String key){return response(service.createCapa(id,b,key));}
 @GetMapping("/deviations/{id}/capas") public ApiResponse<?> capas(@PathVariable("id") String id,@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size){return response(service.capas(id,page,size));}
 @PostMapping("/capas/{id}/{action:start|complete|verify}") public ApiResponse<?> capaAction(@PathVariable("id") String id,@PathVariable("action") String action,@RequestBody JsonNode b,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return response(service.capaAction(id,action,b,version,key));}
}
