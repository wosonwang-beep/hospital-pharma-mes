package com.hospital.mes.integration.api;import java.util.List;
public record IntegrationMessagePageResponse(List<IntegrationMessageResponse> items,int page,int size,long total){public IntegrationMessagePageResponse{items=List.copyOf(items);}}
