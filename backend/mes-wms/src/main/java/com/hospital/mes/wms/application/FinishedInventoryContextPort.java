package com.hospital.mes.wms.application;

/** Read labels only; boot composes actual same-org batch and product relationships. */
public interface FinishedInventoryContextPort {
 record Context(long mainBatchId,String batchNo,long productId,String productCode,String productName,String productSpecification) {}
 Context forLot(long organizationId,long materialLotId);
}
