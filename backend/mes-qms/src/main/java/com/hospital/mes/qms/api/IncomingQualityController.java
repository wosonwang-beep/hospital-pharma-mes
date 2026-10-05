package com.hospital.mes.qms.api;
import com.hospital.mes.qms.application.IncomingQualityService;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import java.util.*;
@RestController @RequestMapping("/api/v1") @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class IncomingQualityController {
 private final IncomingQualityService service;private final TraceIdProvider traces;private final com.hospital.mes.qms.application.ProductionQualityService production;
 public IncomingQualityController(IncomingQualityService service,TraceIdProvider traces,com.hospital.mes.qms.application.ProductionQualityService production){this.service=service;this.traces=traces;this.production=production;}
 @GetMapping("/quality/inspection-requests")
 public ApiResponse<?> listInspectionRequests(@RequestParam(defaultValue="0",name="page") int page,@RequestParam(defaultValue="20",name="size") int size,@RequestParam(required=false,name="keyword") String keyword,@RequestParam Map<String,String> filters){return ApiResponse.success(service.list("qms_inspection_request","qms:inspection-request:view",page,size,keyword,filters),traces.currentTraceId());}
 @PostMapping("/quality/inspection-requests")
 @ResponseStatus(HttpStatus.CREATED)
 public ApiResponse<?> createInspectionRequest(@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.createRequest(body,key),traces.currentTraceId());}
 @GetMapping("/quality/inspection-requests/{id}")
 public ApiResponse<?> getInspectionRequest(@PathVariable("id") String target){return ApiResponse.success(service.get("qms_inspection_request","qms:inspection-request:view",target),traces.currentTraceId());}
 @GetMapping("/quality/sampling-tasks")
 public ApiResponse<?> listSamplingTasks(@RequestParam(defaultValue="0",name="page") int page,@RequestParam(defaultValue="20",name="size") int size,@RequestParam(required=false,name="keyword") String keyword,@RequestParam Map<String,String> filters){return ApiResponse.success(service.list("qms_sampling_task","qms:sampling:view",page,size,keyword,filters),traces.currentTraceId());}
 @PostMapping("/quality/sampling-tasks")
 @ResponseStatus(HttpStatus.CREATED)
 public ApiResponse<?> createSamplingTask(@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.createSampling(body,key),traces.currentTraceId());}
 @GetMapping("/quality/sampling-tasks/{id}")
 public ApiResponse<?> getSamplingTask(@PathVariable("id") String target){return ApiResponse.success(service.get("qms_sampling_task","qms:sampling:view",target),traces.currentTraceId());}
 @GetMapping("/quality/inspection-tasks")
 public ApiResponse<?> listInspectionTasks(@RequestParam(defaultValue="0",name="page") int page,@RequestParam(defaultValue="20",name="size") int size,@RequestParam(required=false,name="keyword") String keyword,@RequestParam Map<String,String> filters){return ApiResponse.success(service.list("qms_inspection_task","qms:test:view",page,size,keyword,filters),traces.currentTraceId());}
 @PostMapping("/quality/inspection-tasks")
 @ResponseStatus(HttpStatus.CREATED)
 public ApiResponse<?> createInspectionTask(@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.createTask(body,key),traces.currentTraceId());}
 @GetMapping("/quality/inspection-tasks/{id}")
 public ApiResponse<?> getInspectionTask(@PathVariable("id") String target){return ApiResponse.success(service.get("qms_inspection_task","qms:test:view",target),traces.currentTraceId());}
 @GetMapping("/quality/inspection-reports")
 public ApiResponse<?> listInspectionReports(@RequestParam(defaultValue="0",name="page") int page,@RequestParam(defaultValue="20",name="size") int size,@RequestParam(required=false,name="keyword") String keyword,@RequestParam Map<String,String> filters){return ApiResponse.success(service.list("qms_inspection_report","qms:report:view",page,size,keyword,filters),traces.currentTraceId());}
 @PostMapping("/quality/inspection-reports")
 @ResponseStatus(HttpStatus.CREATED)
 public ApiResponse<?> createInspectionReport(@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.createReport(body,key),traces.currentTraceId());}
 @GetMapping("/quality/inspection-reports/{id}")
 public ApiResponse<?> getInspectionReport(@PathVariable("id") String target){return ApiResponse.success(service.get("qms_inspection_report","qms:report:view",target),traces.currentTraceId());}
 @PostMapping("/quality/inspection-requests/{id}/submit")
 public ApiResponse<?> submitInspectionRequest(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.requestAction(target,"submit",body,version,key),traces.currentTraceId());}
 @PostMapping("/quality/inspection-requests/{id}/accept")
 public ApiResponse<?> acceptInspectionRequest(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.requestAction(target,"accept",body,version,key),traces.currentTraceId());}
 @PostMapping("/quality/sampling-tasks/{id}/approve-plan")
 public ApiResponse<?> approve_planSamplingTask(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.samplingAction(target,"approve-plan",body,version,key),traces.currentTraceId());}
 @PostMapping("/quality/sampling-tasks/{id}/assign")
 public ApiResponse<?> assignSamplingTask(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.samplingAction(target,"assign",body,version,key),traces.currentTraceId());}
 @PostMapping("/quality/sampling-tasks/{id}/start")
 public ApiResponse<?> startSamplingTask(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.samplingAction(target,"start",body,version,key),traces.currentTraceId());}
 @PostMapping("/quality/sampling-tasks/{id}/details")
 @ResponseStatus(HttpStatus.CREATED)
 public ApiResponse<?> recordSamplingDetail(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.samplingAction(target,"details",body,version,key),traces.currentTraceId());}
 @PostMapping("/quality/sampling-tasks/{id}/complete")
 public ApiResponse<?> completeSamplingTask(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.samplingAction(target,"complete",body,version,key),traces.currentTraceId());}
 @PostMapping("/quality/inspection-tasks/{id}/assign")
 public ApiResponse<?> assignInspectionTask(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.taskAction(target,"assign",body,version,key),traces.currentTraceId());}
 @PostMapping("/quality/inspection-tasks/{id}/start")
 public ApiResponse<?> startInspectionTask(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.taskAction(target,"start",body,version,key),traces.currentTraceId());}
 @PostMapping("/quality/inspection-tasks/{id}/submit-review")
 public ApiResponse<?> submitInspectionReview(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.taskAction(target,"submit-review",body,version,key),traces.currentTraceId());}
 @PostMapping("/quality/inspection-tasks/{id}/review")
 public ApiResponse<?> reviewInspectionTask(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.taskAction(target,"review",body,version,key),traces.currentTraceId());}
 @PostMapping("/quality/inspection-reports/{id}/review")
 public ApiResponse<?> reviewInspectionReport(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.reportAction(target,"review",body,version,key),traces.currentTraceId());}
 @PostMapping("/quality/inspection-reports/{id}/approve")
 public ApiResponse<?> approveInspectionReport(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.reportAction(target,"approve",body,version,key),traces.currentTraceId());}
 @GetMapping("/quality/samples")
 public ApiResponse<?> listIncomingSamples(@RequestParam(defaultValue="0",name="page") int page,@RequestParam(defaultValue="20",name="size") int size,@RequestParam(required=false,name="keyword") String keyword,@RequestParam Map<String,String> filters){return ApiResponse.success(service.list("qms_sample","qms:test:view",page,size,keyword,filters),traces.currentTraceId());}
 @GetMapping("/quality/samples/{id}")
 public ApiResponse<?> getIncomingSample(@PathVariable("id") String target){return ApiResponse.success(service.get("qms_sample","qms:test:view",target),traces.currentTraceId());}
 @PostMapping("/quality/samples/{id}/label")
 public ApiResponse<?> printSampleLabel(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.sampleLabel(target,body,version,key),traces.currentTraceId());}
 @PostMapping("/quality/samples/{id}/receive")
 public ApiResponse<?> receiveIncomingSample(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.sampleAction(target,"receive",body,version,key),traces.currentTraceId());}
 @PostMapping("/quality/samples/{id}/retain")
 public ApiResponse<?> retainIncomingSample(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.sampleAction(target,"retain",body,version,key),traces.currentTraceId());}
 @PostMapping("/quality/samples/{id}/dispose")
 public ApiResponse<?> disposeIncomingSample(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.sampleAction(target,"dispose",body,version,key),traces.currentTraceId());}
 @PostMapping("/quality/inspection-items/{id}/executions")
 @ResponseStatus(HttpStatus.CREATED)
 public ApiResponse<?> recordTestExecution(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.execution(target,false,body,version,key),traces.currentTraceId());}
 @PostMapping("/quality/inspection-items/{id}/approved-retests")
 @ResponseStatus(HttpStatus.CREATED)
 public ApiResponse<?> recordApprovedRetest(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.execution(target,true,body,version,key),traces.currentTraceId());}
 @PostMapping("/quality/inspection-items/{id}/results")
 @ResponseStatus(HttpStatus.CREATED)
 public ApiResponse<?> recordInspectionResult(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.result(target,false,body,version,key),traces.currentTraceId());}
 @PostMapping("/quality/test-results/{revisionId}/revisions")
 @ResponseStatus(HttpStatus.CREATED)
 public ApiResponse<?> reviseInspectionResult(@PathVariable("revisionId") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.result(target,true,body,version,key),traces.currentTraceId());}
 @GetMapping("/qa/material-lots/{lotId}/release-review")
 public ApiResponse<?> getIncomingMaterialReleaseReview(@PathVariable("lotId") String target){return ApiResponse.success(service.releaseReview(target),traces.currentTraceId());}
 @PostMapping("/qa/material-lots/{lotId}/release-decisions")
 @ResponseStatus(HttpStatus.CREATED)
 public ApiResponse<?> decideIncomingMaterialRelease(@PathVariable("lotId") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(service.decideRelease(target,body,version,key),traces.currentTraceId());}
 @GetMapping("/deviations")
 public ApiResponse<?> listDeviations(@RequestParam(defaultValue="0",name="page") int page,@RequestParam(defaultValue="20",name="size") int size,@RequestParam(required=false,name="keyword") String keyword,@RequestParam Map<String,String> filters){return ApiResponse.success(("INCOMING_MATERIAL".equals(filters.get("investigationScope"))?service.list("qms_deviation","qms:deviation:view",page,size,keyword,filters):production.list("qms_deviation",page,size,filters)),traces.currentTraceId());}
 @PostMapping("/deviations")
 @ResponseStatus(HttpStatus.CREATED)
 public ApiResponse<?> createDeviations(@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success(("INCOMING_MATERIAL".equals(body.path("investigationScope").asText())?service.createDeviation(body,key):production.createDeviation(body,key)),traces.currentTraceId());}
 @GetMapping("/deviations/{id}")
 public ApiResponse<?> getDeviations(@PathVariable("id") String target){return ApiResponse.success((production.productionDeviation(target)?production.get("qms_deviation",target):service.get("qms_deviation","qms:deviation:view",target)),traces.currentTraceId());}
 @PutMapping("/deviations/{id}")
 public ApiResponse<?> updateDeviations(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success((production.productionDeviation(target,"qms:deviation:update")?production.deviationAction(target,"update",body,version,key):service.deviationAction(target,"update",body,version,key)),traces.currentTraceId());}
 @PostMapping("/deviations/{id}/investigate")
 public ApiResponse<?> investigateDeviation(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success((production.productionDeviation(target,"qms:deviation:investigate")?production.deviationAction(target,"investigate",body,version,key):service.deviationAction(target,"investigate",body,version,key)),traces.currentTraceId());}
 @PostMapping("/deviations/{id}/decide")
 public ApiResponse<?> decideDeviation(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success((production.productionDeviation(target,"qms:deviation:decide")?production.deviationAction(target,"decide",body,version,key):service.deviationAction(target,"decide",body,version,key)),traces.currentTraceId());}
 @PostMapping("/deviations/{id}/close")
 public ApiResponse<?> closeDeviation(@PathVariable("id") String target,@RequestBody JsonNode body,@RequestHeader("Idempotency-Key") String key,@RequestHeader(value="If-Match",required=false) String version){return ApiResponse.success((production.productionDeviation(target,"qms:deviation:close")?production.deviationAction(target,"close",body,version,key):service.deviationAction(target,"close",body,version,key)),traces.currentTraceId());}
 @GetMapping("/quality/signature-evidence/{signatureId}")
 public ApiResponse<?> getIncomingSignatureEvidence(@PathVariable("signatureId") String target){return ApiResponse.success(service.signatureEvidence(target),traces.currentTraceId());}
}
