package com.hospital.mes.integration.application;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.integration.domain.InboxMessage;
import com.hospital.mes.integration.domain.OutboxMessage;
import java.util.Set;
import org.junit.jupiter.api.Test;

class IntegrationDispatchServiceTest {
    private static final CurrentPlatformContext CONTEXT = new CurrentPlatformContext(
        11, 7, Set.of(), Set.of(), "worker", null);

    @Test
    void performsNetworkWorkAfterClaimAndCompletesInASeparateCommand() {
        IntegrationClaimService claims = mock(IntegrationClaimService.class);
        InboxMessage inbox = mock(InboxMessage.class);
        OutboxMessage outbox = mock(OutboxMessage.class);
        when(claims.claimInbox(CONTEXT, 1, 2)).thenReturn(inbox);
        when(claims.claimOutbox(CONTEXT, 3, 4)).thenReturn(outbox);
        InboxMessageHandler handler = mock(InboxMessageHandler.class);
        ExternalMessagePublisher publisher = mock(ExternalMessagePublisher.class);
        IntegrationDispatchService service = new IntegrationDispatchService(claims);

        service.processInbox(CONTEXT, 1, 2, handler);
        service.publishOutbox(CONTEXT, 3, 4, publisher);

        verify(handler).handle(inbox);
        verify(publisher).publish(outbox);
        verify(claims).completeInbox(CONTEXT, inbox);
        verify(claims).completeOutbox(CONTEXT, outbox);
    }

    @Test
    void recordsAControlledFailureWithoutPersistingExceptionText() {
        IntegrationClaimService claims = mock(IntegrationClaimService.class);
        InboxMessage inbox = mock(InboxMessage.class);
        when(claims.claimInbox(CONTEXT, 1, 2)).thenReturn(inbox);
        InboxMessageHandler handler = ignored -> {
            throw new IllegalStateException("secret external response");
        };
        IntegrationDispatchService service = new IntegrationDispatchService(claims);

        service.processInbox(CONTEXT, 1, 2, handler);

        verify(claims).failInbox(CONTEXT, inbox, "HANDLER_FAILURE", "Inbox handler failed");
    }
}
