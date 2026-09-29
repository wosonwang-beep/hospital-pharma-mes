package com.hospital.mes.audit.idempotency;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(prefix="spring.datasource",name="url")
public class PlatformIdempotencyService {
    private static final Duration RECORD_LIFETIME = Duration.ofHours(24);
    private final IdempotencyRepository repository;
    private final Clock clock;

    public PlatformIdempotencyService(IdempotencyRepository repository, Clock clock) {
        this.repository = repository; this.clock = clock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public IdempotencyDecision begin(IdempotencyCommand command) {
        String digest = digest(command.canonicalRequest());
        IdempotencyRecord proposed = new IdempotencyRecord(0, command.organizationId(), command.actorId(),
            command.operationCode(), command.idempotencyKey(), digest, IdempotencyState.IN_PROGRESS,
            null, null, null, null, Instant.now(clock).plus(RECORD_LIFETIME), 0);
        if (repository.claim(proposed)) {
            IdempotencyRecord stored = repository.find(command.organizationId(), command.actorId(),
                command.operationCode(), command.idempotencyKey());
            return IdempotencyDecision.owner(new IdempotencyHandle(stored.id(), stored.versionNo()));
        }
        IdempotencyRecord existing = repository.find(command.organizationId(), command.actorId(),
            command.operationCode(), command.idempotencyKey());
        if (!existing.requestDigest().equals(digest)) {
            return new IdempotencyDecision(IdempotencyDecisionType.CONFLICT, null, null, null, null, null);
        }
        if (existing.state() == IdempotencyState.IN_PROGRESS) {
            return new IdempotencyDecision(IdempotencyDecisionType.IN_PROGRESS_CONFLICT, null, null, null, null, null);
        }
        return new IdempotencyDecision(IdempotencyDecisionType.REPLAY, null, existing.httpStatus(),
            existing.responseJson(), existing.resourceType(), existing.resourceId());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void complete(IdempotencyHandle handle, int httpStatus, String responseJson,
                         String resourceType, String resourceId) {
        if (!repository.complete(handle, httpStatus, responseJson, resourceType, resourceId)) {
            throw new IllegalStateException("idempotency completion conflict");
        }
    }

    public static String digest(String canonicalRequest) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(canonicalRequest.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
}
