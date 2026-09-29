package com.hospital.mes.audit.signature;

import java.time.Instant;

public record SignatureResponseData(String id, String signerId, SignatureMeaning meaning, String objectType,
                                    String objectId, String recordDigest, SignatureStatus status, Instant signedAt,
                                    String revokedSignatureId, long versionNo) {
    static SignatureResponseData from(SignatureRecord r){return new SignatureResponseData(Long.toString(r.id()),
        Long.toString(r.signerId()),r.meaning(),r.objectType(),r.objectId(),r.recordDigest(),r.status(),r.signedAt(),
        r.revokedSignatureId()==null?null:Long.toString(r.revokedSignatureId()),r.versionNo());}
}
