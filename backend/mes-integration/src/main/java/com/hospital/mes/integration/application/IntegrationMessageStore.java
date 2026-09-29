package com.hospital.mes.integration.application;

import com.hospital.mes.integration.domain.InboxMessage;
import com.hospital.mes.integration.domain.OutboxMessage;
import java.time.Instant;

public interface IntegrationMessageStore {
    InboxMessage receive(InboxMessage message);
    InboxMessage findInbox(long organizationId,long id);
    OutboxMessage enqueue(OutboxMessage message);
    OutboxMessage findOutbox(long organizationId,long id);
    boolean claimInbox(long organizationId,long id,long versionNo,Instant now);
    boolean claimOutbox(long organizationId,long id,long versionNo,Instant now);
    boolean saveInbox(InboxMessage message,long previousVersion);
    boolean saveOutbox(OutboxMessage message,long previousVersion);
}
