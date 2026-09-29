package com.hospital.mes.integration.application;
import com.hospital.mes.integration.api.IntegrationMessagePageResponse;
public interface IntegrationMessageQueryRepository{IntegrationMessagePageResponse query(long organizationId,IntegrationMessageQuery query);}
