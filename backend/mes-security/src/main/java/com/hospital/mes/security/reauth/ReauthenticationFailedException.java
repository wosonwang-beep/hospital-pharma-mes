package com.hospital.mes.security.reauth;

import org.springframework.security.authentication.BadCredentialsException;

public class ReauthenticationFailedException extends BadCredentialsException {
    public ReauthenticationFailedException(){super("Reauthentication failed");}
}
