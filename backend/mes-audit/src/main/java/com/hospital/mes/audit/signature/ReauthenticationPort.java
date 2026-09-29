package com.hospital.mes.audit.signature;

import com.hospital.mes.audit.application.CurrentPlatformContext;

public interface ReauthenticationPort {
    ReauthenticationChallenge issue(ReauthenticationRequest request, CurrentPlatformContext context);
    ConsumedReauthentication consume(String token, ExpectedReauthenticationBinding expected);
}
