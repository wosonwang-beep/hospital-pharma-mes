package com.hospital.mes.integration.application;

import com.hospital.mes.audit.application.AuditApplicationService;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.domain.AuditCommand;
import com.hospital.mes.audit.domain.AuditSource;
import com.hospital.mes.common.exception.ResourceConflictException;
import com.hospital.mes.integration.domain.InboxMessage;
import com.hospital.mes.integration.domain.InboxStatus;
import com.hospital.mes.integration.domain.OutboxMessage;
import com.hospital.mes.integration.domain.OutboxStatus;
import com.hospital.mes.integration.policy.IntegrationRetryPolicy;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
public class IntegrationClaimService {
    private final IntegrationMessageStore store;
    private final IntegrationRetryPolicy policy;
    private final AuditApplicationService audit;
    private final Clock clock;

    public IntegrationClaimService(IntegrationMessageStore store, IntegrationRetryPolicy policy,
                                   AuditApplicationService audit, Clock clock) {
        this.store = store;
        this.policy = policy;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    public InboxMessage claimInbox(CurrentPlatformContext context, long id, long version) {
        Instant now = clock.instant();
        if (!store.claimInbox(context.organizationId(), id, version, context.actorId(), now)) {
            conflict();
        }
        return store.findInbox(context.organizationId(), id);
    }

    @Transactional
    public OutboxMessage claimOutbox(CurrentPlatformContext context, long id, long version) {
        Instant now = clock.instant();
        if (!store.claimOutbox(context.organizationId(), id, version, context.actorId(), now)) {
            conflict();
        }
        return store.findOutbox(context.organizationId(), id);
    }

    @Transactional
    public InboxMessage completeInbox(CurrentPlatformContext context, InboxMessage claimed) {
        requireSameOrganization(context, claimed.organizationId());
        Instant now = clock.instant();
        InboxMessage next = claimed.processed(now);
        saveInbox(context, claimed, next, now);
        return next;
    }

    @Transactional
    public OutboxMessage completeOutbox(CurrentPlatformContext context, OutboxMessage claimed) {
        requireSameOrganization(context, claimed.organizationId());
        Instant now = clock.instant();
        OutboxMessage next = claimed.published(now);
        saveOutbox(context, claimed, next, now);
        return next;
    }

    @Transactional
    public InboxMessage failInbox(CurrentPlatformContext context, InboxMessage claimed,
                                  String code, String message) {
        requireSameOrganization(context, claimed.organizationId());
        Instant now = clock.instant();
        InboxMessage next = claimed.fail(code, message, now, policy);
        saveInbox(context, claimed, next, now);
        if (next.status() == InboxStatus.DEAD_LETTER) {
            appendDeadLetterAudit(context, "INBOX:" + next.id(), claimed.status().name(),
                claimed.retryCount(), next.retryCount(), code, now);
        }
        return next;
    }

    @Transactional
    public OutboxMessage failOutbox(CurrentPlatformContext context, OutboxMessage claimed,
                                    String code, String message) {
        requireSameOrganization(context, claimed.organizationId());
        Instant now = clock.instant();
        OutboxMessage next = claimed.fail(code, message, now, policy);
        saveOutbox(context, claimed, next, now);
        if (next.status() == OutboxStatus.DEAD_LETTER) {
            appendDeadLetterAudit(context, "OUTBOX:" + next.id(), claimed.status().name(),
                claimed.retryCount(), next.retryCount(), code, now);
        }
        return next;
    }

    @Transactional
    public InboxMessage recoverAbandonedInbox(CurrentPlatformContext context, long id, long expectedVersion) {
        InboxMessage claimed = store.findInbox(context.organizationId(), id);
        if (claimed.versionNo() != expectedVersion) {
            conflict();
        }
        Instant now = clock.instant();
        InboxMessage next = claimed.recoverAbandoned(now, policy);
        saveInbox(context, claimed, next, now);
        if (next.status() == InboxStatus.DEAD_LETTER) {
            appendDeadLetterAudit(context, "INBOX:" + next.id(), claimed.status().name(),
                claimed.retryCount(), next.retryCount(), next.lastErrorCode(), now);
        }
        return next;
    }

    @Transactional
    public OutboxMessage recoverAbandonedOutbox(CurrentPlatformContext context, long id, long expectedVersion) {
        OutboxMessage claimed = store.findOutbox(context.organizationId(), id);
        if (claimed.versionNo() != expectedVersion) {
            conflict();
        }
        Instant now = clock.instant();
        OutboxMessage next = claimed.recoverAbandoned(now, policy);
        saveOutbox(context, claimed, next, now);
        if (next.status() == OutboxStatus.DEAD_LETTER) {
            appendDeadLetterAudit(context, "OUTBOX:" + next.id(), claimed.status().name(),
                claimed.retryCount(), next.retryCount(), next.lastErrorCode(), now);
        }
        return next;
    }

    private void saveInbox(CurrentPlatformContext context, InboxMessage claimed,
                           InboxMessage next, Instant now) {
        if (!store.saveInbox(next, claimed.versionNo(), context.actorId(), now)) {
            conflict();
        }
    }

    private void saveOutbox(CurrentPlatformContext context, OutboxMessage claimed,
                            OutboxMessage next, Instant now) {
        if (!store.saveOutbox(next, claimed.versionNo(), context.actorId(), now)) {
            conflict();
        }
    }

    private void appendDeadLetterAudit(CurrentPlatformContext context, String messageRef,
                                       String previousStatus, int previousRetryCount,
                                       int retryCount, String errorCode, Instant now) {
        String transactionId = context.requestId() == null ? UUID.randomUUID().toString() : context.requestId();
        audit.append(new AuditCommand(
            context.organizationId(), context.actorId(), context.roleSnapshot(),
            "INTEGRATION_MESSAGE_DEAD_LETTERED", "INTEGRATION_MESSAGE", messageRef,
            digest(previousStatus + "|" + previousRetryCount),
            digest("DEAD_LETTER|" + retryCount),
            errorCode, null, now, transactionId, context.requestId(), AuditSource.SYSTEM, null));
    }

    private static void requireSameOrganization(CurrentPlatformContext context, long organizationId) {
        if (context.organizationId() != organizationId) {
            conflict();
        }
    }

    private static void conflict() {
        throw new ResourceConflictException(
            "INTEGRATION_CONCURRENT_UPDATE", "Integration message changed concurrently");
    }

    private static String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
