package com.hospital.mes.security.reauth;

import com.hospital.mes.audit.signature.ExpectedReauthenticationBinding;
import java.time.Instant;

public record StoredReauthentication(ExpectedReauthenticationBinding binding, Instant issuedAt, Instant expiresAt,
                                     String method) { }
