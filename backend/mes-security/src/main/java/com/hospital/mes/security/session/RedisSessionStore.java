package com.hospital.mes.security.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.security.identity.LoginSnapshot;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class RedisSessionStore implements SessionStore {
    private static final String SESSION_PREFIX = "mes:iam:session:";
    private static final String USER_PREFIX = "mes:iam:user:";
    private static final TypeReference<Set<String>> CODE_SET = new TypeReference<>() {};
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final DefaultRedisScript<Long> CREATE = script("""
        redis.call('HSET', KEYS[1], 'userId', ARGV[1])
        redis.call('HSET', KEYS[1], 'organizationId', ARGV[2])
        redis.call('HSET', KEYS[1], 'loginName', ARGV[3])
        redis.call('HSET', KEYS[1], 'displayName', ARGV[4])
        redis.call('HSET', KEYS[1], 'roleCodes', ARGV[5])
        redis.call('HSET', KEYS[1], 'permissionCodes', ARGV[6])
        redis.call('HSET', KEYS[1], 'mustChange', ARGV[7])
        redis.call('HSET', KEYS[1], 'createdAt', ARGV[8])
        redis.call('HSET', KEYS[1], 'absoluteExpiresAt', ARGV[9])
        redis.call('HSET', KEYS[1], 'idleExpiresAt', ARGV[10])
        redis.call('HSET', KEYS[1], 'renewDigest', ARGV[11])
        redis.call('PEXPIRE', KEYS[1], ARGV[12])
        redis.call('SADD', KEYS[2], ARGV[13])
        redis.call('PEXPIRE', KEYS[2], 28800000)
        return 1
        """);
    private static final DefaultRedisScript<Long> TOUCH = script("""
        local values = redis.call('HMGET', KEYS[1], 'absoluteExpiresAt', 'idleExpiresAt')
        if not values[1] or tonumber(ARGV[1]) >= tonumber(values[1])
          or tonumber(ARGV[1]) >= tonumber(values[2]) then return 0 end
        local nextIdle = math.min(tonumber(values[1]), tonumber(ARGV[1]) + 1800000)
        redis.call('HSET', KEYS[1], 'idleExpiresAt', tostring(nextIdle))
        redis.call('PEXPIRE', KEYS[1], nextIdle - tonumber(ARGV[1]))
        return 1
        """);
    private static final DefaultRedisScript<Long> RENEW = script("""
        local values = redis.call('HMGET', KEYS[1], 'absoluteExpiresAt', 'idleExpiresAt', 'renewDigest')
        if not values[1] or tonumber(ARGV[1]) >= tonumber(values[1])
          or tonumber(ARGV[1]) >= tonumber(values[2]) or values[3] ~= ARGV[2] then return 0 end
        local nextIdle = math.min(tonumber(values[1]), tonumber(ARGV[1]) + 1800000)
        redis.call('HSET', KEYS[1], 'renewDigest', ARGV[3])
        redis.call('HSET', KEYS[1], 'idleExpiresAt', tostring(nextIdle))
        redis.call('PEXPIRE', KEYS[1], nextIdle - tonumber(ARGV[1]))
        return 1
        """);
    private static final DefaultRedisScript<Long> REVOKE = script("""
        local userId = redis.call('HGET', KEYS[1], 'userId')
        redis.call('DEL', KEYS[1])
        if userId then redis.call('SREM', ARGV[1] .. userId .. ':sessions', ARGV[2]) end
        return 1
        """);
    private static final DefaultRedisScript<Long> REVOKE_ALL = script("""
        local ids = redis.call('SMEMBERS', KEYS[1])
        for _, id in ipairs(ids) do redis.call('DEL', ARGV[1] .. id) end
        redis.call('DEL', KEYS[1])
        return #ids
        """);

    private final StringRedisTemplate redis;
    private final ObjectMapper json;
    private final Clock clock;

    public RedisSessionStore(StringRedisTemplate redis, ObjectMapper json, Clock clock) {
        this.redis = redis;
        this.json = json;
        this.clock = clock;
    }

    @Override
    public SessionLease create(LoginSnapshot identity) {
        Instant now = clock.instant();
        String sessionId = UUID.randomUUID().toString();
        String credential = newCredential();
        SessionSnapshot snapshot = SessionSnapshot.start(sessionId, identity, now);
        Long created = redis.execute(CREATE, List.of(sessionKey(sessionId), userKey(identity.userId())),
            Long.toString(identity.userId()), Long.toString(identity.organizationId()), identity.loginName(), identity.displayName(),
            toJson(identity.roleCodes()), toJson(identity.permissionCodes()),
            Boolean.toString(identity.mustChangePassword()), Long.toString(now.toEpochMilli()),
            Long.toString(snapshot.absoluteExpiresAt().toEpochMilli()),
            Long.toString(snapshot.idleExpiresAt().toEpochMilli()), digest(credential),
            Long.toString(30 * 60 * 1000L), sessionId);
        if (!Long.valueOf(1).equals(created)) throw new IllegalStateException("Session creation failed");
        return new SessionLease(sessionId, credential, snapshot);
    }

    @Override
    public Optional<SessionSnapshot> findById(String sessionId) {
        if (!validId(sessionId)) return Optional.empty();
        Map<Object, Object> fields = redis.opsForHash().entries(sessionKey(sessionId));
        if (fields.isEmpty()) return Optional.empty();
        SessionSnapshot snapshot = fromHash(sessionId, fields);
        if (snapshot.isExpired(clock.instant())) {
            revoke(sessionId);
            return Optional.empty();
        }
        return Optional.of(snapshot);
    }

    @Override
    public Optional<SessionSnapshot> touch(String sessionId) {
        if (!validId(sessionId)) return Optional.empty();
        Long changed = redis.execute(TOUCH, List.of(sessionKey(sessionId)),
            Long.toString(clock.instant().toEpochMilli()));
        return Long.valueOf(1).equals(changed) ? findById(sessionId) : Optional.empty();
    }

    @Override
    public Optional<SessionLease> renew(String sessionId, String renewalCredential) {
        if (!validId(sessionId) || renewalCredential == null || renewalCredential.isBlank())
            return Optional.empty();
        String nextCredential = newCredential();
        Long changed = redis.execute(RENEW, List.of(sessionKey(sessionId)),
            Long.toString(clock.instant().toEpochMilli()), digest(renewalCredential),
            digest(nextCredential));
        if (!Long.valueOf(1).equals(changed)) return Optional.empty();
        return findById(sessionId).map(snapshot -> new SessionLease(sessionId, nextCredential, snapshot));
    }

    @Override
    public void revoke(String sessionId) {
        if (validId(sessionId)) redis.execute(REVOKE, List.of(sessionKey(sessionId)), USER_PREFIX, sessionId);
    }

    @Override
    public void revokeAllForUser(long userId) {
        redis.execute(REVOKE_ALL, List.of(userKey(userId)), SESSION_PREFIX);
    }

    private SessionSnapshot fromHash(String sessionId, Map<Object, Object> fields) {
        try {
            long userId = Long.parseLong(value(fields, "userId"));
            LoginSnapshot identity = new LoginSnapshot(userId, Long.parseLong(value(fields, "organizationId")), value(fields, "loginName"),
                value(fields, "displayName"), json.readValue(value(fields, "roleCodes"), CODE_SET),
                json.readValue(value(fields, "permissionCodes"), CODE_SET),
                Boolean.parseBoolean(value(fields, "mustChange")));
            return new SessionSnapshot(sessionId, identity,
                Instant.ofEpochMilli(Long.parseLong(value(fields, "createdAt"))),
                Instant.ofEpochMilli(Long.parseLong(value(fields, "absoluteExpiresAt"))),
                Instant.ofEpochMilli(Long.parseLong(value(fields, "idleExpiresAt"))));
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            throw new IllegalStateException("Invalid session record", ex);
        }
    }

    private String toJson(Set<String> codes) {
        try { return json.writeValueAsString(codes); }
        catch (JsonProcessingException ex) { throw new IllegalStateException("Session encoding failed", ex); }
    }

    private static String value(Map<Object, Object> fields, String key) {
        Object result = fields.get(key);
        if (!(result instanceof String text)) throw new IllegalStateException("Invalid session record");
        return text;
    }

    private static String newCredential() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String digest(String credential) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(credential.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private static boolean validId(String sessionId) {
        if (sessionId == null) return false;
        try { UUID.fromString(sessionId); return true; }
        catch (IllegalArgumentException ex) { return false; }
    }

    private static String sessionKey(String sessionId) { return SESSION_PREFIX + sessionId; }
    private static String userKey(long userId) { return USER_PREFIX + userId + ":sessions"; }
    private static DefaultRedisScript<Long> script(String source) {
        return new DefaultRedisScript<>(source, Long.class);
    }
}
