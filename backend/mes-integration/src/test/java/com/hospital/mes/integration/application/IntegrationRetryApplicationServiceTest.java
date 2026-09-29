package com.hospital.mes.integration.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.audit.application.AuditApplicationService;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.idempotency.IdempotencyDecision;
import com.hospital.mes.audit.idempotency.IdempotencyHandle;
import com.hospital.mes.audit.idempotency.PlatformIdempotencyService;
import com.hospital.mes.common.exception.ComplianceException;
import com.hospital.mes.integration.domain.InboxMessage;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import org.junit.jupiter.api.Test;

class IntegrationRetryApplicationServiceTest {
    @Test
    void mapsAnIneligibleRetryStateToTheFrozenUnprocessableContract() {
        IntegrationMessageStore store = mock(IntegrationMessageStore.class);
        PlatformIdempotencyService idempotency = mock(PlatformIdempotencyService.class);
        when(idempotency.begin(any())).thenReturn(IdempotencyDecision.owner(new IdempotencyHandle(1, 0)));
        when(store.findInbox(11, 42)).thenReturn(InboxMessage.received(
            11, 5, "LIMS", "m1", "{}", Instant.parse("2026-09-29T00:00:00Z")));
        IntegrationRetryApplicationService service = new IntegrationRetryApplicationService(
            store, idempotency, mock(AuditApplicationService.class), new ObjectMapper(),
            Clock.fixed(Instant.parse("2026-09-29T00:00:00Z"), ZoneOffset.UTC));
        CurrentPlatformContext context = new CurrentPlatformContext(
            11, 7, Set.of("QA"), Set.of("integration:view", "integration:retry"), "session", "request");

        assertThatThrownBy(() -> service.manualRetry(context, "INBOX:42", "operator retry", 0, "idem"))
            .isInstanceOf(ComplianceException.class)
            .hasMessageContaining("not eligible");
    }
}
