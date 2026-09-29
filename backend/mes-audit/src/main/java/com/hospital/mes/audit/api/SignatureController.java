package com.hospital.mes.audit.api;

import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.audit.signature.SignCommand;
import com.hospital.mes.audit.signature.SignatureApplicationService;
import com.hospital.mes.audit.signature.SignatureResponseData;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.exception.ValidationException;
import com.hospital.mes.common.trace.TraceIdProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class SignatureController {
    private final SignatureApplicationService service;private final CurrentPlatformContextResolver contexts;private final TraceIdProvider traces;
    public SignatureController(SignatureApplicationService service,CurrentPlatformContextResolver contexts,TraceIdProvider traces){this.service=service;this.contexts=contexts;this.traces=traces;}
    @PostMapping("/api/v1/records/{type}/{id}/sign") @PreAuthorize("hasAuthority('ebr:sign')")
    public ApiResponse<SignatureResponseData> sign(@PathVariable String type,@PathVariable String id,
        @RequestHeader("Idempotency-Key")String key,@RequestHeader("If-Match")String ifMatch,@RequestBody SignRecordRequest request){
        return ApiResponse.success(service.sign(new SignCommand(contexts.current(),type,id,request.getMeaning(),version(ifMatch),request.getReauthToken(),key,null)),traces.currentTraceId());
    }
    private static long version(String value){try{return Long.parseLong(value.replace("\"", ""));}catch(Exception e){throw new ValidationException("INVALID_IF_MATCH","If-Match must be a record version");}}
}
