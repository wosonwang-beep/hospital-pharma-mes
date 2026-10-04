package com.hospital.mes.wms.application;
import java.time.Instant;
/** Consumer port to be wired only to real MES-008A eligibility and MES-009 MainBatch contracts.
 * No permissive implementation is installed in the sequencing phase. */
public interface ProductionMaterialGate {
 void requireEligible(long organizationId,long mainBatchId,long materialLotId,Instant at);
}
