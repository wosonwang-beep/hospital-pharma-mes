package com.hospital.mes.integration.api;

import com.hospital.mes.audit.application.CurrentPlatformContextResolver;import com.hospital.mes.common.api.ApiResponse;import com.hospital.mes.common.exception.ValidationException;import com.hospital.mes.common.trace.TraceIdProvider;import com.hospital.mes.integration.application.*;import com.hospital.mes.integration.domain.IntegrationDirection;
import java.time.Instant;import java.util.List;import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.web.bind.annotation.*;

@RestController @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class IntegrationMessageController{
 private final IntegrationMessageQueryService queries;private final IntegrationRetryApplicationService retries;private final CurrentPlatformContextResolver contexts;private final TraceIdProvider traces;
 public IntegrationMessageController(IntegrationMessageQueryService q,IntegrationRetryApplicationService r,CurrentPlatformContextResolver c,TraceIdProvider t){queries=q;retries=r;contexts=c;traces=t;}
 @GetMapping("/api/v1/integration/messages") @PreAuthorize("hasAuthority('integration:view')")
 public ApiResponse<IntegrationMessagePageResponse> query(@RequestParam(required=false)IntegrationDirection direction,@RequestParam(required=false)String system,@RequestParam(required=false)String messageId,@RequestParam(required=false)String eventType,@RequestParam(required=false)String aggregateType,@RequestParam(required=false)String aggregateId,@RequestParam(required=false)List<String> status,@RequestParam(required=false)Instant occurredFrom,@RequestParam(required=false)Instant occurredTo,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="50")int size){return ApiResponse.success(queries.query(contexts.current().organizationId(),new IntegrationMessageQuery(direction,system,messageId,eventType,aggregateType,aggregateId,status,occurredFrom,occurredTo,page,size)),traces.currentTraceId());}
 @PostMapping("/api/v1/integration/messages/{messageRef}/retry") @PreAuthorize("hasAuthority('integration:view') and hasAuthority('integration:retry')")
 public ApiResponse<IntegrationMessageResponse> retry(@PathVariable String messageRef,@RequestHeader("Idempotency-Key")String key,@RequestHeader("If-Match")String version,@RequestBody RetryIntegrationMessageRequest request){return ApiResponse.success(retries.manualRetry(contexts.current(),messageRef,request.reason(),version(version),key),traces.currentTraceId());}
 private static long version(String value){try{return Long.parseLong(value.replace("\"",""));}catch(Exception e){throw new ValidationException("INVALID_IF_MATCH","If-Match must be a record version");}}
}
