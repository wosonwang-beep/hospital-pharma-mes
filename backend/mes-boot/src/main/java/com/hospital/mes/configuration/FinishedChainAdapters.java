package com.hospital.mes.configuration;
import com.hospital.mes.qms.application.FinishedWarehousePort;
import com.hospital.mes.wms.application.FinishedGoodsService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.context.annotation.*;
@Configuration @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class FinishedChainAdapters {
 @Bean FinishedWarehousePort finishedWarehouse(FinishedGoodsService goods){return new FinishedWarehousePort(){
  public JsonNode confirmed(long org,long batch){return goods.confirmedInbound(org,batch);}
  public JsonNode inbound(long org,long id){return goods.inboundFact(org,id);}
 };}
 @org.springframework.context.annotation.Bean com.hospital.mes.traceability.application.FinishedTracePort finishedTrace(FinishedChainQuery chain,com.hospital.mes.production.application.ProductionQueryService batches){return new com.hospital.mes.traceability.application.FinishedTracePort(){public com.fasterxml.jackson.databind.JsonNode forLot(long org,long lot){return chain.forLot(org,lot);}public com.fasterxml.jackson.databind.JsonNode forBatch(long org,long batch){var fact=batches.batchFact(org,batch);return fact.path("productionSchemaVersion").asInt()==2?chain.forBatch(org,batch):com.hospital.mes.traceability.application.FinishedTracePort.super.forBatch(org,batch);}public Long lotForBatch(long org,long batch){return batches.qualityIdentity(org,batch).finishedLotId();}};}
}
