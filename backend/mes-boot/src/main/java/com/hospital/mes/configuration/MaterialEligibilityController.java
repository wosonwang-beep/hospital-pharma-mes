package com.hospital.mes.configuration;
import com.hospital.mes.audit.attachment.application.AttachmentService;
import com.hospital.mes.qms.application.IncomingQualityQueryService;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import java.time.Instant;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@RestController @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class MaterialEligibilityController {
 private final IncomingQualityQueryService quality;private final com.hospital.mes.audit.application.CurrentPlatformContextResolver contexts;private final TraceIdProvider traces;
 public MaterialEligibilityController(IncomingQualityQueryService quality,com.hospital.mes.audit.application.CurrentPlatformContextResolver contexts,TraceIdProvider traces){this.quality=quality;this.contexts=contexts;this.traces=traces;}
 @GetMapping("/api/v1/wms/material-lots/{id}/eligibility") public ApiResponse<?> eligibility(@PathVariable("id") long id){
  var c=contexts.current();if(!c.hasPermission("wms:inventory:view"))throw new com.hospital.mes.common.exception.PermissionException("PERMISSION_DENIED","Inventory view permission required");
  if(id<1)throw new IllegalArgumentException("Invalid material lot ID");return ApiResponse.success(quality.evaluate(c.organizationId(),id,"PRODUCTION",Instant.now()),traces.currentTraceId());
 }
}
