package com.hospital.mes.integration.application;

import com.hospital.mes.common.exception.ResourceConflictException;
import com.hospital.mes.integration.domain.*;
import com.hospital.mes.integration.policy.IntegrationRetryPolicy;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.transaction.annotation.Transactional;

@Service @ConditionalOnBean(IntegrationMessageStore.class) public class IntegrationClaimService{
    private final IntegrationMessageStore store;private final IntegrationRetryPolicy policy;private final Clock clock;
    public IntegrationClaimService(IntegrationMessageStore store,IntegrationRetryPolicy policy,Clock clock){this.store=store;this.policy=policy;this.clock=clock;}
    @Transactional public InboxMessage claimInbox(long org,long id,long version){if(!store.claimInbox(org,id,version,clock.instant()))conflict();return store.findInbox(org,id);}
    @Transactional public OutboxMessage claimOutbox(long org,long id,long version){if(!store.claimOutbox(org,id,version,clock.instant()))conflict();return store.findOutbox(org,id);}
    @Transactional public InboxMessage failInbox(InboxMessage claimed,String code,String message){var next=claimed.fail(code,message,clock.instant(),policy);if(!store.saveInbox(next,claimed.versionNo()))conflict();return next;}
    @Transactional public OutboxMessage failOutbox(OutboxMessage claimed,String code,String message){var next=claimed.fail(code,message,clock.instant(),policy);if(!store.saveOutbox(next,claimed.versionNo()))conflict();return next;}
    private static void conflict(){throw new ResourceConflictException("INTEGRATION_CONCURRENT_UPDATE","Integration message changed concurrently");}
}
