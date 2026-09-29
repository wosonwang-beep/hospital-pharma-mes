package com.hospital.mes.security.api;

import java.time.Instant;

public final class ReauthenticationResponse {
    private final String reauthToken; private final Instant expiresAt;
    public ReauthenticationResponse(String token,Instant expiresAt){this.reauthToken=token;this.expiresAt=expiresAt;}
    public String getReauthToken(){return reauthToken;}public Instant getExpiresAt(){return expiresAt;}
    @Override public String toString(){return "ReauthenticationResponse[reauthToken=[REDACTED], expiresAt="+expiresAt+"]";}
}
