package com.hospital.mes.wms.api;
import com.hospital.mes.wms.application.ReceiptNumberingService;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@RestController @RequestMapping("/api/v1/wms/receipts") @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ReceiptSourceController {
 private final MasterMutation auth;private final MaterialQueryService materials;private final ApprovedSupplierQueryService sources;private final ReceiptNumberingService numbers;private final TraceIdProvider traces;private final ObjectMapper json;
 public ReceiptSourceController(MasterMutation auth,MaterialQueryService materials,ApprovedSupplierQueryService sources,ReceiptNumberingService numbers,TraceIdProvider traces,ObjectMapper json){this.auth=auth;this.materials=materials;this.sources=sources;this.numbers=numbers;this.traces=traces;this.json=json;}
 @GetMapping("/source-context") public ApiResponse<?> source(@RequestParam(name="materialId") String materialId,@RequestParam(name="supplierId") String supplierId){var c=auth.context("wms:receipt:view");long mid=MasterMutation.id(materialId),sid=MasterMutation.id(supplierId);var out=json.createObjectNode();out.set("material",materials.requireUsable(c.organizationId(),mid,java.time.Instant.now()));out.set("source",sources.requireApproved(c.organizationId(),mid,sid,java.time.Instant.now()));return ApiResponse.success(out,traces.currentTraceId());}
 @GetMapping("/number-rule") public ApiResponse<?> numberRule(){return ApiResponse.success(numbers.configuration(),traces.currentTraceId());}
 @PostMapping("/number-rule/monthly-four-digit") public ApiResponse<?> configure(@RequestBody ReceiptNumberingService.ConfigureMonthly body,@RequestHeader("Idempotency-Key")String key){return ApiResponse.success(numbers.configureMonthly(body,key),traces.currentTraceId());}
 @PostMapping("/number-reservations") public ApiResponse<?> reserve(@RequestBody ReceiptNumberingService.Reserve body,@RequestHeader("Idempotency-Key")String key){return ApiResponse.success(numbers.reserve(body,key),traces.currentTraceId());}
}
