package com.hospital.mes.integration.application;

import com.hospital.mes.integration.domain.OutboxMessage;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service @ConditionalOnProperty(prefix="spring.datasource",name="url") public class OutboxApplicationService{
    private final IntegrationMessageStore store;private final Clock clock;
    public OutboxApplicationService(IntegrationMessageStore store,Clock clock){this.store=store;this.clock=clock;}
    @Transactional(propagation=Propagation.MANDATORY)
    public OutboxMessage enqueue(long org,long actor,String messageId,String target,String eventType,String aggregateType,String aggregateId,String payload){
        return store.enqueue(OutboxMessage.pending(org,actor,messageId,target,eventType,aggregateType,aggregateId,payload,clock.instant()));}
}
