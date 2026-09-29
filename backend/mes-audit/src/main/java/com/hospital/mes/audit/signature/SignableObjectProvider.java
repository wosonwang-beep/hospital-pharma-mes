package com.hospital.mes.audit.signature;

import java.util.Set;

public interface SignableObjectProvider {
    String objectType();
    Set<SignatureMeaning> allowedMeanings();
    SignableObject loadForSignature(long organizationId, String objectId);
    void validateSignable(SignatureValidationContext context, SignableObject object);
}
