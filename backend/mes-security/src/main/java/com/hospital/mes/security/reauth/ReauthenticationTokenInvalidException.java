package com.hospital.mes.security.reauth;

import org.springframework.security.authentication.BadCredentialsException;

public class ReauthenticationTokenInvalidException extends BadCredentialsException {
    public ReauthenticationTokenInvalidException() {
        super("Reauthentication token is invalid");
    }
}
