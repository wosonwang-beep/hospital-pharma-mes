package com.hospital.mes.audit.signature;

public class SignatureMeaningNotAllowedException extends RuntimeException {
    public SignatureMeaningNotAllowedException(String objectType, SignatureMeaning meaning) {
        super("Signature meaning " + meaning + " is not allowed for " + objectType);
    }
}
