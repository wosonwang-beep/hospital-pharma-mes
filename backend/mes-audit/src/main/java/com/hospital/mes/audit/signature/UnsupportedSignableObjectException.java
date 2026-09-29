package com.hospital.mes.audit.signature;

public class UnsupportedSignableObjectException extends RuntimeException {
    public UnsupportedSignableObjectException(String objectType) { super("Unsupported signable object: " + objectType); }
}
