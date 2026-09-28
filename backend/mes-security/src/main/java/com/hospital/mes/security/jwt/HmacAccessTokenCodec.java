package com.hospital.mes.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class HmacAccessTokenCodec implements AccessTokenCodec {
    private static final String ISSUER = "hospital-pharma-mes";
    private static final Pattern KID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    private final String activeKid;
    private final Map<String, byte[]> keys;
    private final Clock clock;

    public HmacAccessTokenCodec(@Value("${mes.security.jwt.active-kid:}") String activeKid,
                                @Value("${mes.security.jwt.keys:}") String keyRing,
                                Clock clock) {
        if (activeKid == null || !KID.matcher(activeKid).matches())
            throw new IllegalArgumentException("A JWT signing key identifier is required");
        this.keys = parseKeys(keyRing);
        if (!keys.containsKey(activeKid))
            throw new IllegalArgumentException("Active JWT signing key is missing");
        this.activeKid = activeKid;
        this.clock = clock;
    }

    @Override
    public String issue(long userId, String sessionId) {
        if (userId <= 0 || !validSessionId(sessionId))
            throw new IllegalArgumentException("Invalid token subject or session");
        Instant now = clock.instant();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
            .issuer(ISSUER).subject(Long.toString(userId)).claim("sid", sessionId)
            .issueTime(Date.from(now)).expirationTime(Date.from(now.plusSeconds(15 * 60)))
            .build();
        SignedJWT token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256)
            .keyID(activeKid).build(), claims);
        try {
            token.sign(new MACSigner(keys.get(activeKid)));
            return token.serialize();
        } catch (JOSEException ex) {
            throw new IllegalStateException("Access-token signing failed", ex);
        }
    }

    @Override
    public TokenClaims verify(String serialized) {
        try {
            SignedJWT token = SignedJWT.parse(serialized);
            if (!JWSAlgorithm.HS256.equals(token.getHeader().getAlgorithm()))
                throw invalidToken();
            String kid = token.getHeader().getKeyID();
            byte[] key = keys.get(kid);
            if (key == null || !token.verify(new MACVerifier(key))) throw invalidToken();
            JWTClaimsSet claims = token.getJWTClaimsSet();
            Instant now = clock.instant();
            Date issued = claims.getIssueTime();
            Date expiry = claims.getExpirationTime();
            String sessionId = claims.getStringClaim("sid");
            if (!ISSUER.equals(claims.getIssuer()) || issued == null || expiry == null
                || issued.toInstant().isAfter(now) || !expiry.toInstant().isAfter(now)
                || !expiry.toInstant().isAfter(issued.toInstant()) || !validSessionId(sessionId))
                throw invalidToken();
            long userId = Long.parseLong(claims.getSubject());
            if (userId <= 0) throw invalidToken();
            return new TokenClaims(userId, sessionId, issued.toInstant(), expiry.toInstant(), kid);
        } catch (ParseException | JOSEException | NumberFormatException | NullPointerException ex) {
            throw new BadCredentialsException("Invalid access token", ex);
        }
    }

    private static Map<String, byte[]> parseKeys(String keyRing) {
        if (keyRing == null || keyRing.isBlank())
            throw new IllegalArgumentException("JWT signing key ring is required");
        Map<String, byte[]> result = new HashMap<>();
        for (String item : keyRing.split(",")) {
            int separator = item.indexOf(':');
            if (separator <= 0 || separator == item.length() - 1)
                throw new IllegalArgumentException("Invalid JWT key entry");
            String kid = item.substring(0, separator).trim();
            if (!KID.matcher(kid).matches() || result.containsKey(kid))
                throw new IllegalArgumentException("Invalid or duplicate JWT key identifier");
            byte[] decoded;
            try { decoded = Base64.getDecoder().decode(item.substring(separator + 1).trim()); }
            catch (IllegalArgumentException ex) { throw new IllegalArgumentException("Invalid JWT key encoding", ex); }
            if (decoded.length < 32)
                throw new IllegalArgumentException("JWT signing key must be at least 256 bits");
            result.put(kid, decoded);
        }
        return Map.copyOf(result);
    }

    private static boolean validSessionId(String sessionId) {
        try { UUID.fromString(sessionId); return true; }
        catch (IllegalArgumentException | NullPointerException ex) { return false; }
    }

    private static BadCredentialsException invalidToken() {
        return new BadCredentialsException("Invalid access token");
    }
}
