package com.hospital.mes.audit.signature;

import java.time.Instant;

public final class ReauthenticationChallenge {
    private final String token; private final Instant expiresAt;
    public ReauthenticationChallenge(String token, Instant expiresAt){this.token=token;this.expiresAt=expiresAt;}
    public String token(){return token;} public Instant expiresAt(){return expiresAt;}
    @Override public String toString(){return "ReauthenticationChallenge[token=[REDACTED], expiresAt="+expiresAt+"]";}
}
