package com.hospital.mes.integration.application;

import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.integration.domain.InboxMessage;
import com.hospital.mes.integration.domain.OutboxMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Orchestrates worker I/O without holding a database transaction across the external call.
 * Claim, completion and failure each run through the transactional command service.
 */
@Service
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
public class IntegrationDispatchService {
    private final IntegrationClaimService claims;

    public IntegrationDispatchService(IntegrationClaimService claims) {
        this.claims = claims;
    }

    public void processInbox(CurrentPlatformContext technicalContext, long id, long expectedVersion,
                             InboxMessageHandler handler) {
        InboxMessage claimed = claims.claimInbox(technicalContext, id, expectedVersion);
        try {
            handler.handle(claimed);
        } catch (RuntimeException failure) {
            claims.failInbox(technicalContext, claimed, "HANDLER_FAILURE", "Inbox handler failed");
            return;
        }
        claims.completeInbox(technicalContext, claimed);
    }

    public void publishOutbox(CurrentPlatformContext technicalContext, long id, long expectedVersion,
                              ExternalMessagePublisher publisher) {
        OutboxMessage claimed = claims.claimOutbox(technicalContext, id, expectedVersion);
        try {
            publisher.publish(claimed);
        } catch (RuntimeException failure) {
            claims.failOutbox(technicalContext, claimed, "PUBLISH_FAILURE", "Outbox publisher failed");
            return;
        }
        claims.completeOutbox(technicalContext, claimed);
    }
}
