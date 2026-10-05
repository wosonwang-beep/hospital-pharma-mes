package com.hospital.mes.production.application;
import com.fasterxml.jackson.databind.JsonNode;
/** Dispatch consumes actual approved quality evidence without a module dependency cycle. */
public interface ProductionQualityPlanPort {
 JsonNode frozenPlan(long organizationId,long mainBatchId,JsonNode dispatchedRoute);
}
