package com.hospital.mes.audit.signature;

public class SignatureMeaningNotAllowedException extends com.hospital.mes.common.exception.ComplianceException {
    public SignatureMeaningNotAllowedException(String objectType, SignatureMeaning meaning) {
        super("SIGNATURE_MEANING_NOT_ALLOWED",
            "Signature meaning " + meaning + " is not allowed for " + objectType);
    }
}
