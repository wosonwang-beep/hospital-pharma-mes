package com.hospital.mes.security.api;

import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.audit.signature.ReauthenticationPort;
import com.hospital.mes.audit.signature.ReauthenticationRequest;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/auth") @ConditionalOnBean(CurrentPlatformContextResolver.class)
public class ReauthenticationController {
    private final ReauthenticationPort service;private final CurrentPlatformContextResolver contexts;private final TraceIdProvider traces;
    public ReauthenticationController(ReauthenticationPort service,CurrentPlatformContextResolver contexts,TraceIdProvider traces){this.service=service;this.contexts=contexts;this.traces=traces;}
    @PostMapping("/reauth") @PreAuthorize("hasAuthority('ebr:sign')")
    public ApiResponse<ReauthenticationResponse> reauthenticate(@RequestBody ReauthenticateForSignatureRequest request){
        var c=service.issue(new ReauthenticationRequest(request.getObjectType(),request.getObjectId(),request.getMeaning(),request.getRecordVersion(),request.getCredential()),contexts.current());
        return ApiResponse.success(new ReauthenticationResponse(c.token(),c.expiresAt()),traces.currentTraceId());
    }
}
