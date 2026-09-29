package com.hospital.mes.audit.signature;

public interface SignatureCanonicalizer {
    String canonicalJson(SignableObject object);
    String digest(SignableObject object);
}
