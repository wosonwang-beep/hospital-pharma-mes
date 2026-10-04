package com.hospital.mes.configuration;
import com.hospital.mes.qms.application.IncomingMaterialPort;
import com.hospital.mes.wms.application.WmsQualityService;
import com.hospital.mes.wms.infrastructure.*;
import com.hospital.mes.masterdata.application.ApprovedSupplierQueryService;
import com.hospital.mes.masterdata.domain.MasterGateException;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@Component @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class IncomingMaterialAdapter implements IncomingMaterialPort {
 private final WmsStore store;private final WmsQualityService quality;private final ApprovedSupplierQueryService suppliers;
 public IncomingMaterialAdapter(WmsStore store,WmsQualityService quality,ApprovedSupplierQueryService suppliers){this.store=store;this.quality=quality;this.suppliers=suppliers;}
 public LotFacts lock(long org,long id){return facts(org,store.materialLot().lock(org,id));}
 public LotFacts read(long org,long id){return facts(org,store.materialLot().get(org,id));}
 public void applyTransition(long org,long id,long version,IncomingLotTransition transition,long actor){quality.apply(org,id,version,WmsQualityService.Action.valueOf(transition.name()),actor);}
 private LotFacts facts(long org,MaterialLotEntity lot){
  if(lot.getReceiptItemId()==null)throw new IllegalStateException("Material lot lacks receiving lineage");
  var item=store.receiptItem().get(org,lot.getReceiptItemId());var receipt=store.receipt().get(org,item.getReceiptId());
  long relationship=0;boolean approved=false;
  try{var source=suppliers.requireApproved(org,lot.getMaterialId(),receipt.getSupplierId(),Instant.now());relationship=source.path("id").asLong();approved=relationship>0;}catch(MasterGateException ignored){}
  boolean checks="APPROVED".equals(receipt.getRecordStatus())&&Boolean.TRUE.equals(receipt.getTransportCheckPassed())
   &&Boolean.TRUE.equals(item.getPackageCheckPassed())&&Boolean.TRUE.equals(item.getSealCheckPassed())
   &&Boolean.TRUE.equals(item.getLabelCheckPassed())&&Boolean.TRUE.equals(item.getDamageCheckPassed())&&Boolean.TRUE.equals(item.getContaminationCheckPassed());
  return new LotFacts(lot.getId(),lot.getMaterialId(),receipt.getId(),item.getId(),receipt.getSupplierId(),relationship,
   item.getReceivedQty(),item.getUnitId(),item.getPackageCount(),Boolean.TRUE.equals(lot.getRequiresIncomingInspectionSnapshot()),checks,
   approved,approved,lot.getQualityStatus(),lot.getInventoryStatus(),lot.getExpiryDate(),lot.getRetestDate(),lot.getVersionNo());
 }
}
