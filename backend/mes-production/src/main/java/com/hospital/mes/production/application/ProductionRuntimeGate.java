package com.hospital.mes.production.application;
import java.util.List;
/** Boot adapter verifies real synchronous execution/eBR producers and completion facts. */
public interface ProductionRuntimeGate {
 void requireInitialized(long org,long batchId,List<Long> executionIds);
 void requireProductionComplete(long org,long batchId);
 void requireQaSubmissionReady(long org,long batchId);
}
