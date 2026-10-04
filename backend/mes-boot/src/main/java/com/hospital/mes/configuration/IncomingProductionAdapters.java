package com.hospital.mes.configuration;
import com.hospital.mes.wms.application.*;
import com.hospital.mes.production.application.ProductionQueryService;
import com.hospital.mes.execution.application.*;
import com.hospital.mes.qms.application.IncomingQualityQueryService;
import com.hospital.mes.traceability.application.IncomingTracePort;
import org.springframework.context.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import com.hospital.mes.common.exception.ComplianceException;
import java.math.BigDecimal;
import java.time.Instant;
@Configuration @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class IncomingProductionAdapters {
 @Bean WmsProductionContextPort wmsProductionContext(ProductionQueryService production,ExecutionQueryService execution){return new WmsProductionContextPort(){
  public BatchContext lockBatch(long org,long id){var b=production.lockBatch(org,id);return new BatchContext(b.mainBatchId(),b.status(),b.snapshot(),b.versionNo());}
  public BigDecimal chargedQuantity(long org,long batch,long lot,long unit){return execution.chargedQuantity(org,batch,lot,unit);}
 };}
 @Bean ProductionMaterialGate productionMaterialGate(IncomingQualityQueryService quality){return (org,batch,lot,at)->quality.requireEligible(org,lot,"PRODUCTION",at);}
 @Bean ChargeStockPort chargeStock(WmsProductionService wms){return new ChargeStockPort(){
  public void lockLot(long org,long lot){wms.lockLot(org,lot);}
  public void consume(com.hospital.mes.audit.application.CurrentPlatformContext c,long batch,long lot,long charge,BigDecimal quantity,long unit,String key){wms.consume(c,batch,lot,charge,quantity,unit,key);}
  public void reverse(com.hospital.mes.audit.application.CurrentPlatformContext c,long batch,long lot,long charge,String key){wms.reverseConsumption(c,batch,lot,charge,key);}
 };}
 @Bean IncomingTracePort incomingTrace(IncomingQualityQueryService quality,IncomingMaterialAdapter lots,WmsAttachmentService attachments,com.hospital.mes.wms.application.InventoryDecisionService inventoryDecisions,com.fasterxml.jackson.databind.ObjectMapper json){return (org,lot)->{
  var result=(com.fasterxml.jackson.databind.node.ObjectNode)quality.getLotChain(org,lot).deepCopy();
  result.set("inventoryDecisions",json.valueToTree(inventoryDecisions.history(org,lot)));var linked=result.putArray("deliveryAttachments");for(var association:attachments.forReceipt(org,lots.read(org,lot).receiptId())){
   var file=json.valueToTree(association.attachment());((com.fasterxml.jackson.databind.node.ObjectNode)file).put("attachmentId",association.attachment().id());linked.add(file);
  }return result;
 };}
 @Bean ExecutionMaterialPort executionMaterial(WmsQueryService query,WmsProductionService wms,IncomingQualityQueryService quality){return new ExecutionMaterialPort(){
  public void requireOperationMaterials(long org,long batch){wms.requireOperationMaterials(org,batch);}
  public com.fasterxml.jackson.databind.JsonNode lot(long org,long id){return query.materialLot(org,id);}
  public void requireEligible(long org,long batch,long lot,long material,Instant at){if(query.materialLot(org,lot).path("materialId").asLong()!=material)throw new ComplianceException("MATERIAL_MISMATCH","Material lot does not match the frozen formula material");quality.requireEligible(org,lot,"PRODUCTION",at);}
 };}
}
