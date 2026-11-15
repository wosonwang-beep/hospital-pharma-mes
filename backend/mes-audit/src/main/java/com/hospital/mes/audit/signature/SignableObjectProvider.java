package com.hospital.mes.audit.signature;

import java.util.Set;

public interface SignableObjectProvider {
    String objectType();
    Set<SignatureMeaning> allowedMeanings();
    SignableObject loadForSignature(long organizationId, String objectId);
    void validateSignable(SignatureValidationContext context, SignableObject object);
    default java.util.Map<String,Object> signatureContext(SignatureValidationContext context, SignableObject object) { return java.util.Map.of(); }
}
