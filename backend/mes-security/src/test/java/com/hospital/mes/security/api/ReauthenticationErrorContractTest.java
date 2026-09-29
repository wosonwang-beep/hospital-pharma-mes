package com.hospital.mes.security.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.hospital.mes.security.reauth.ReauthenticationFailedException;
import com.hospital.mes.security.reauth.ReauthenticationTokenInvalidException;
import org.junit.jupiter.api.Test;

class ReauthenticationErrorContractTest {
    private final AuthExceptionAdvice advice = new AuthExceptionAdvice(() -> "request-1");

    @Test
    void distinguishesCredentialFailureFromInvalidConsumedOrExpiredToken() {
        var credential = advice.reauthenticationFailed(new ReauthenticationFailedException());
        var token = advice.reauthenticationTokenInvalid(new ReauthenticationTokenInvalidException());

        assertThat(credential.getBody().code()).isEqualTo("REAUTH_FAILED");
        assertThat(token.getBody().code()).isEqualTo("REAUTH_TOKEN_INVALID");
    }
}
