package com.hospital.mes.security.reauth;

public interface ReauthenticationTokenStore {
    void put(String token, StoredReauthentication value);
    StoredReauthentication consume(String token);
}
