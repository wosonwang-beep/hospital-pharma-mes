package com.hospital.mes.integration.application;
import com.hospital.mes.integration.api.IntegrationMessagePageResponse;import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url") public class IntegrationMessageQueryService{
 private final IntegrationMessageQueryRepository repository;public IntegrationMessageQueryService(IntegrationMessageQueryRepository r){repository=r;}
 @Transactional(readOnly=true)public IntegrationMessagePageResponse query(long org,IntegrationMessageQuery q){return repository.query(org,q);}
}
