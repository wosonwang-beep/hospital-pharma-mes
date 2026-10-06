package com.hospital.mes.wms.api;
import com.hospital.mes.wms.application.FinishedGoodsService;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.Map;
@RestController @RequestMapping("/api/v1") @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class FinishedGoodsController {
 private final FinishedGoodsService service;private final TraceIdProvider traces;
 public FinishedGoodsController(FinishedGoodsService service,TraceIdProvider traces){this.service=service;this.traces=traces;}
 private ApiResponse<?> ok(Object value){return ApiResponse.success(value,traces.currentTraceId());}
 @GetMapping("/finished-inbound-requests") public ApiResponse<?> inboundList(@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam Map<String,String> filters){return ok(service.listInbound(page,size,filters));}
 @PostMapping("/finished-inbound-requests") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<?> inboundCreate(@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key){return ok(service.createInbound(body,key));}
 @GetMapping("/finished-inbound-requests/{id}") public ApiResponse<?> inboundGet(@PathVariable("id") String id){return ok(service.getInbound(id));}
 @PostMapping("/finished-inbound-requests/{id}/{action:submit|cancel|confirm}") public ApiResponse<?> inboundAction(@PathVariable("id") String id,@PathVariable("action") String action,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ok(service.inboundAction(id,action,body,version,key));}
 @GetMapping("/finished-shipments") public ApiResponse<?> shipmentList(@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam Map<String,String> filters){return ok(service.listShipments(page,size,filters));}
 @PostMapping("/finished-shipments") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<?> shipmentCreate(@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key){return ok(service.createShipment(body,key));}
 @GetMapping("/finished-shipments/{id}") public ApiResponse<?> shipmentGet(@PathVariable("id") String id){return ok(service.getShipment(id));}
 @PostMapping("/finished-shipments/{id}/{action:confirm|cancel}") public ApiResponse<?> shipmentAction(@PathVariable("id") String id,@PathVariable("action") String action,@RequestBody JsonNode body,@RequestHeader("If-Match") String version,@RequestHeader("Idempotency-Key") String key){return ok(service.shipmentAction(id,action,body,version,key));}
}
