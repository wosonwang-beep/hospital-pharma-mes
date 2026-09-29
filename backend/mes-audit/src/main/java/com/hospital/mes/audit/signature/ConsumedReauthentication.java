package com.hospital.mes.audit.signature;

import java.time.Instant;

public record ConsumedReauthentication(Instant reauthenticatedAt, String method) { }
