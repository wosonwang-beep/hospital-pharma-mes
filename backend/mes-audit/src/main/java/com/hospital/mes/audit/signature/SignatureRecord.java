package com.hospital.mes.audit.signature;

import java.time.Instant;

public record SignatureRecord(long id, long organizationId, long signerId, SignatureMeaning meaning,
                              String objectType, String objectId, String recordDigest, Instant signedAt,
                              SignatureStatus status, Instant invalidatedAt, String invalidationReason,
                              String authContextJson, Long revokedSignatureId, long versionNo) { }
