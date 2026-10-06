package com.hospital.mes.configuration;

import com.hospital.mes.wms.application.*;
import com.hospital.mes.production.application.ProductionQueryService;
import com.hospital.mes.process.application.ProcessQueryService;
import com.hospital.mes.traceability.application.WmsTracePort;
import org.springframework.context.annotation.*;

@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class TraceInventoryAdapters {
 @Bean FinishedInventoryContextPort finishedInventoryContext(ProductionQueryService production,ProcessQueryService process){return (org,lot)->{
  Long batch=production.finishedBatchForLot(org,lot);if(batch==null)return null;
  var identity=production.qualityIdentity(org,batch);var labels=process.productLabels(org,identity.productId());
  return new FinishedInventoryContextPort.Context(batch,production.batchNumber(org,batch),identity.productId(),labels.productCode(),labels.productName(),labels.specification());
 };}
 @Bean WmsTracePort wmsTrace(WmsTraceQueryService query){return query::forLot;}
}
