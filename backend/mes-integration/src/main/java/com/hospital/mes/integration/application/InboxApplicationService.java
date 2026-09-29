package com.hospital.mes.integration.application;

import com.hospital.mes.integration.domain.InboxMessage;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.transaction.annotation.Transactional;

@Service @ConditionalOnBean(IntegrationMessageStore.class) public class InboxApplicationService{
    private final IntegrationMessageStore store;private final Clock clock;
    public InboxApplicationService(IntegrationMessageStore store,Clock clock){this.store=store;this.clock=clock;}
    @Transactional public InboxMessage receive(long org,long actor,String source,String messageId,String payload){return store.receive(InboxMessage.received(org,actor,source,messageId,payload,clock.instant()));}
}
