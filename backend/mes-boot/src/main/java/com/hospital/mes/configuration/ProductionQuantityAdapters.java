package com.hospital.mes.configuration;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.signature.SignableObjectProvider;
import com.hospital.mes.masterdata.application.SignedRecordSupport;
import com.hospital.mes.execution.application.*;
import com.hospital.mes.wms.application.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.math.BigDecimal;
@Configuration @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ProductionQuantityAdapters {
 @Bean ExecutionOutputInventoryPort finishedOutputInventory(FinishedOutputInventoryService outputs,WmsQueryService query){return new ExecutionOutputInventoryPort(){
  public long receiveOutput(CurrentPlatformContext c,Output o,String key){return outputs.receive(c,o.batchId(),o.finishedLotId(),o.materialId(),o.amount(),o.unitId(),o.lotNo(),o.locationId(),o.productionDate(),o.expiryDate(),o.sourceRef(),o.reason(),key);}
  public void reverseOutput(CurrentPlatformContext c,long lot,String original,BigDecimal amount,long unit,String source,String reason,String key){outputs.reverse(c,lot,original,amount,unit,source,reason,key);}
  public JsonNode lot(long org,long lot){return query.materialLot(org,lot);}
  public java.util.List<LogisticsQuantity> confirmedLogistics(long org,long batch){return query.confirmedQuantitySources(org,batch).stream().map(x->new LogisticsQuantity(x.id(),x.type(),x.materialLotId(),x.amount(),x.unitId(),x.occurredAt())).toList();}
 };}
 @Bean SignableObjectProvider productionQuantitySignature(ObjectProvider<ProductionQuantityService> quantities){return SignedRecordSupport.provider("PRODUCTION_QUANTITY",(org,id)->quantities.getObject().envelopes(org,id));}
}
