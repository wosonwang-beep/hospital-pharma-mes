package com.hospital.mes.qms.application;
import java.math.BigDecimal;
import java.time.LocalDate;
/** Boot adapter owns WMS state changes and receipt facts; QMS never writes WMS tables. */
public interface IncomingMaterialPort {
 enum IncomingLotTransition { SUBMIT_INSPECTION, START_SAMPLING, COMPLETE_SAMPLING, START_TESTING, SUBMIT_QC_REVIEW, PASS_QC, FAIL_QC, OPEN_DISPOSITION, APPROVE_REPORT, RELEASE, REJECT, OPEN_RETEST }
 record LotFacts(long id,long materialId,long receiptId,long receiptItemId,long supplierId,long materialSupplierId,
  BigDecimal quantity,long unitId,int packageCount,boolean requiresIncomingInspection,boolean receiptChecksPassed,
  boolean supplierApproved,boolean materialSupplierApproved,String qualityStatus,String inventoryStatus,
  LocalDate expiryDate,LocalDate retestDate,long versionNo){}
 LotFacts lock(long orgId,long lotId);
 LotFacts read(long orgId,long lotId);
 void applyTransition(long orgId,long lotId,long expectedVersion,IncomingLotTransition transition,long actorId);
}
