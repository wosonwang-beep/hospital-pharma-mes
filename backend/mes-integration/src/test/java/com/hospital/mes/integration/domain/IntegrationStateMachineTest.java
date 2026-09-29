package com.hospital.mes.integration.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hospital.mes.integration.policy.ControlledIntegrationRetryPolicy;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class IntegrationStateMachineTest {
    private final ControlledIntegrationRetryPolicy policy = new ControlledIntegrationRetryPolicy();

    @Test
    void freezesCentralRetryScheduleAndEighthFailure() {
        assertThat(policy.maximumAttempts()).isEqualTo(8);
        assertThat(java.util.stream.IntStream.rangeClosed(1,7).mapToObj(policy::delayAfterFailure).toList())
            .containsExactly(Duration.ofMinutes(1),Duration.ofMinutes(5),Duration.ofMinutes(15),
                Duration.ofHours(1),Duration.ofHours(4),Duration.ofHours(12),Duration.ofHours(24));
        assertThat(policy.claimTimeout()).isEqualTo(Duration.ofMinutes(15));
        assertThatThrownBy(() -> policy.delayAfterFailure(8)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void inboxUsesExplicitTransitionsAndManualRetryNeverResetsEvidence() {
        Instant now=Instant.parse("2026-09-29T00:00:00Z");
        InboxMessage message=InboxMessage.received(1,2,"LIMS","m1","{}",now).claim(now);
        for(int failure=1;failure<=8;failure++) {
            message=message.fail("TIMEOUT","failed",now,policy);
            if(failure<8) { now=message.nextRetryAt(); message=message.claim(now); }
        }
        assertThat(message.status()).isEqualTo(InboxStatus.DEAD_LETTER);
        assertThat(message.retryCount()).isEqualTo(8);
        InboxMessage retried=message.manualRetry();
        assertThat(retried.status()).isEqualTo(InboxStatus.RECEIVED);
        assertThat(retried.retryCount()).isEqualTo(8);
        assertThat(retried.lastErrorCode()).isEqualTo("TIMEOUT");
    }

    @Test
    void outboxKeepsStableMessageIdAcrossClaimsAndRetries() {
        Instant now=Instant.parse("2026-09-29T00:00:00Z");
        OutboxMessage message=OutboxMessage.pending(1,2,"stable-id","ERP","BATCH_RELEASED","BATCH","9","{}",now);
        message=message.claim(now).fail("HTTP_503","unavailable",now,policy).manualRetry();
        assertThat(message.messageId()).isEqualTo("stable-id");
        assertThat(message.status()).isEqualTo(OutboxStatus.PENDING);
        assertThat(message.retryCount()).isEqualTo(1);
    }
}
