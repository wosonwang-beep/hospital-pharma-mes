package com.hospital.mes.security.reauth;

import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.signature.ConsumedReauthentication;
import com.hospital.mes.audit.signature.ExpectedReauthenticationBinding;
import com.hospital.mes.audit.signature.ReauthenticationChallenge;
import com.hospital.mes.audit.signature.ReauthenticationPort;
import com.hospital.mes.audit.signature.ReauthenticationRequest;
import com.hospital.mes.security.identity.IdentityDirectory;
import com.hospital.mes.security.identity.LoginIdentity;
import com.hospital.mes.security.password.PasswordService;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Service
@ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ReauthenticationService implements ReauthenticationPort {
    public static final Duration TTL = Duration.ofMinutes(5);
    private static final SecureRandom RANDOM = new SecureRandom();
    private final IdentityDirectory identities; private final PasswordService passwords;
    private final ReauthenticationTokenStore tokens; private final Clock clock;
    public ReauthenticationService(IdentityDirectory identities, PasswordService passwords,
                                   ReauthenticationTokenStore tokens, Clock clock) {
        this.identities=identities;this.passwords=passwords;this.tokens=tokens;this.clock=clock;
    }
    @Override public ReauthenticationChallenge issue(ReauthenticationRequest request, CurrentPlatformContext context) {
        LoginIdentity identity = identities.findForLogin(context.actorId() > 0 ? currentLoginName(context.actorId()) : "")
            .orElseThrow(ReauthenticationFailedException::new);
        if (identity.userId()!=context.actorId() || !passwords.canAuthenticate(identity, request.credential(), clock.instant()))
            throw new ReauthenticationFailedException();
        Instant issued = clock.instant(); String token = newToken();
        var binding = new ExpectedReauthenticationBinding(context.actorId(), context.sessionId(), context.organizationId(),
            request.objectType(), request.objectId(), request.meaning(), request.recordVersion());
        tokens.put(token, new StoredReauthentication(binding, issued, issued.plus(TTL), "PASSWORD"));
        return new ReauthenticationChallenge(token, issued.plus(TTL));
    }
    private String currentLoginName(long userId) {
        return identities.loadLoginSnapshot(userId).loginName();
    }
    @Override public ConsumedReauthentication consume(String token, ExpectedReauthenticationBinding expected) {
        StoredReauthentication stored = tokens.consume(token);
        if (stored == null || !stored.expiresAt().isAfter(clock.instant()) || !stored.binding().equals(expected))
            throw new ReauthenticationTokenInvalidException();
        return new ConsumedReauthentication(stored.issuedAt(), stored.method());
    }
    private static String newToken(){byte[] b=new byte[32];RANDOM.nextBytes(b);return Base64.getUrlEncoder().withoutPadding().encodeToString(b);}
}
