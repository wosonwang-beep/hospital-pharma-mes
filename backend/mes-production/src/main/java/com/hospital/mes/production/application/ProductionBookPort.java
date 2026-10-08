package com.hospital.mes.production.application;
import com.fasterxml.jackson.databind.JsonNode;
/** Optional additive producer; an absent mapping leaves legacy release unchanged. */
public interface ProductionBookPort {JsonNode freeze(long organizationId,long productId,JsonNode process,JsonNode ebr);}
