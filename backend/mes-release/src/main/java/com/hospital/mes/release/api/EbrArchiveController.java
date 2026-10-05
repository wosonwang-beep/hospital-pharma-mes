package com.hospital.mes.release.api;
import com.hospital.mes.release.application.EbrArchiveService;
import com.hospital.mes.release.domain.ArchiveCommand;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1") @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class EbrArchiveController {
 private final EbrArchiveService archive;private final TraceIdProvider traces;
 public EbrArchiveController(EbrArchiveService archive,TraceIdProvider traces){this.archive=archive;this.traces=traces;}
 @GetMapping("/main-batches/{id}/ebr")public ApiResponse<JsonNode> read(@PathVariable("id")String id){return ApiResponse.success(archive.get(id),traces.currentTraceId());}
 @PostMapping("/main-batches/{id}/ebr/pdf")public ApiResponse<JsonNode> generate(@PathVariable("id")String id,@RequestBody JsonNode body,@RequestHeader("If-Match")String match,@RequestHeader("Idempotency-Key")String key){return ApiResponse.success(archive.generate(id,command(body),match,key),traces.currentTraceId());}
 @GetMapping("/main-batches/{id}/ebr/pdf/{manifestId}")public ApiResponse<JsonNode> manifest(@PathVariable("id")String id,@PathVariable("manifestId")String manifest){return ApiResponse.success(archive.manifest(id,manifest),traces.currentTraceId());}
 private ArchiveCommand command(JsonNode body){if(!body.isObject()||body.size()!=3||!body.has("versionNo")||!body.has("archiveKind")||!body.has("reason")||!body.path("versionNo").isIntegralNumber()||!body.path("versionNo").canConvertToLong()||!body.path("archiveKind").isTextual()||!body.path("reason").isTextual())throw new IllegalArgumentException("Exact archive command required");return new ArchiveCommand(body.path("versionNo").asLong(),body.path("archiveKind").asText(),body.path("reason").asText());}
}
