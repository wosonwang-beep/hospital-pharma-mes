package com.hospital.mes.audit.signature;

public class UnsupportedSignableObjectException extends com.hospital.mes.common.exception.ComplianceException {
    public UnsupportedSignableObjectException(String objectType) {
        super("UNSUPPORTED_SIGNABLE_OBJECT", "Unsupported signable object: " + objectType);
    }
}
