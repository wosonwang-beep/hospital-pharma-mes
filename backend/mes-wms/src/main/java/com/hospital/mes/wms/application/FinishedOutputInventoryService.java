package com.hospital.mes.wms.application;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.wms.infrastructure.*;
import com.hospital.mes.common.exception.ComplianceException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.transaction.annotation.*;
import java.math.*;
import java.time.*;
import java.util.*;

/** Delegated MES output transaction; stock remains WMS-owned and append-only. */
@org.springframework.stereotype.Service @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class FinishedOutputInventoryService {
 private final WmsStore store;private final MaterialQueryService materials;private final MasterQueryService units;private final MasterMutation mutations;private final WmsViews views;
 public FinishedOutputInventoryService(WmsStore store,MaterialQueryService materials,MasterQueryService units,MasterMutation mutations,WmsViews views){this.store=store;this.materials=materials;this.units=units;this.mutations=mutations;this.views=views;}
 private static void gate(boolean ok,String code,String why){if(!ok)throw new ComplianceException(code,why);}
 @Transactional(propagation=Propagation.MANDATORY)
 public long receive(CurrentPlatformContext c,long batch,Long existing,long material,BigDecimal amount,long unit,String lotNo,long location,LocalDate productionDate,LocalDate expiryDate,String source,String reason,String key){
  mutations.context("mes:quantity:record");var today=LocalDate.now(ZoneOffset.UTC);
  gate(productionDate!=null&&expiryDate!=null&&!productionDate.isAfter(today)&&expiryDate.isAfter(today)&&expiryDate.isAfter(productionDate),"OUTPUT_DATES_INVALID","Actual production/expiry dates required");
  var destination=store.location().get(c.organizationId(),location);gate(destination.getStatus().equals("ACTIVE")&&store.warehouse().get(c.organizationId(),destination.getWarehouseId()).getStatus().equals("ACTIVE"),"OUTPUT_DESTINATION_INVALID","Actual active destination required");
  var materialSnapshot=materials.requireUsable(c.organizationId(),material,Instant.now());gate(materialSnapshot.path("materialType").asText().equals("FINISHED"),"OUTPUT_MATERIAL_INVALID","Finished material required");
  var lot=existing==null?new MaterialLotEntity():store.materialLot().lock(c.organizationId(),existing);
  if(existing==null){lot.setMaterialId(material);lot.setLotNo(lotNo);lot.setManufactureDate(productionDate);lot.setExpiryDate(expiryDate);lot.setMaterialSnapshotJson(materialSnapshot.toString());lot.setRequiresIncomingInspectionSnapshot(false);lot.setQualityStatus("QUARANTINE");lot.setInventoryStatus("BLOCKED");store.materialLot().insert(lot,c.organizationId(),c.actorId());}
  else gate(lot.getMaterialId()==material&&lot.getLotNo().equals(lotNo)&&Objects.equals(lot.getManufactureDate(),productionDate)&&Objects.equals(lot.getExpiryDate(),expiryDate)&&lot.getQualityStatus().equals("QUARANTINE")&&lot.getInventoryStatus().equals("BLOCKED"),"FINISHED_LOT_IDENTITY_CONFLICT","Actual batch-owned output lot identity/status must remain unchanged");
  long base;try{base=new com.fasterxml.jackson.databind.ObjectMapper().readTree(lot.getMaterialSnapshotJson()).path("baseUnitId").asLong();}catch(java.io.IOException e){throw new IllegalStateException(e);}
  var conversion=units.convert(c.organizationId(),unit,base,material,amount);BigDecimal converted=amount.multiply(new BigDecimal(conversion.factor()));
  gate(converted.compareTo(new BigDecimal(conversion.convertedValue()))==0,"QUANTITY_PRECISION_LOSS","Output conversion must be exact");validateQuantity(converted);
  append(c,lot.getId(),location,base,converted,"PRODUCTION_OUTPUT","PRODUCTION_OBSERVATION",source,reason,mutations.digest(Map.of("key",key,"batch",batch,"action","OUTPUT")));
  mutations.auditSnapshot(c,"FINISHED_OUTPUT_RECEIVED","MaterialLot",lot.getId(),null,views.view(lot),reason,key);return lot.getId();
 }
 @Transactional(propagation=Propagation.MANDATORY)
 public void reverse(CurrentPlatformContext c,long lotId,String originalSource,BigDecimal amount,long unit,String source,String reason,String key){
  mutations.context("mes:quantity:reverse");var lot=store.materialLot().lock(c.organizationId(),lotId);
  gate(lot.getQualityStatus().equals("QUARANTINE")&&lot.getInventoryStatus().equals("BLOCKED"),"OUTPUT_REVERSAL_BLOCKED","Released/rejected/frozen output cannot be reversed normally");
  var all=store.ledgerMapper().selectList(new QueryWrapper<LedgerEntity>().eq("org_id",c.organizationId()).eq("material_lot_id",lotId).orderByAsc("id").last("FOR UPDATE"));
  gate(all.stream().noneMatch(x->x.getDeltaQty().signum()<0&&!x.getSourceType().equals("PRODUCTION_REVERSAL")),"OUTPUT_REVERSAL_CONSUMED","Physically consumed output cannot be reversed");
  var candidates=all.stream().filter(x->x.getSourceType().equals("PRODUCTION_OBSERVATION")&&x.getSourceRef().equals(originalSource)&&x.getEventType().equals("PRODUCTION_OUTPUT")).toList();
  gate(candidates.size()==1,"OUTPUT_LEDGER_MISSING","Exact original output stock fact required");var original=candidates.getFirst();
  var conversion=units.convert(c.organizationId(),unit,original.getUnitId(),lot.getMaterialId(),amount);
  gate(amount.multiply(new BigDecimal(conversion.factor())).compareTo(original.getDeltaQty())==0,"OUTPUT_LEDGER_MISMATCH","Reversal must exactly negate original physical evidence");
  BigDecimal available=all.stream().filter(x->x.getLocationId().equals(original.getLocationId())&&Objects.equals(x.getContainerId(),original.getContainerId())&&x.getUnitId().equals(original.getUnitId())).map(LedgerEntity::getDeltaQty).reduce(BigDecimal.ZERO,BigDecimal::add);
  gate(available.compareTo(original.getDeltaQty())>=0,"OUTPUT_REVERSAL_STOCK_MISSING","Reversal would make physical stock negative");
  append(c,lotId,original.getLocationId(),original.getUnitId(),original.getDeltaQty().negate(),"PRODUCTION_OUTPUT_REVERSAL","PRODUCTION_REVERSAL",source,reason,mutations.digest(Map.of("key",key,"lot",lotId,"action","REVERSE")));
 }
 private static void validateQuantity(BigDecimal amount){try{amount.setScale(6,RoundingMode.UNNECESSARY);}catch(ArithmeticException e){throw new ComplianceException("QUANTITY_PRECISION_LOSS","Output ledger requires exact DECIMAL(18,6)");}gate(amount.signum()>0&&amount.precision()-amount.scale()<=12,"OUTPUT_QUANTITY_INVALID","Positive bounded physical output required");}
 private void append(CurrentPlatformContext c,long lot,long location,long unit,BigDecimal delta,String type,String sourceType,String source,String reason,String key){
  var row=new LedgerEntity();row.setMaterialLotId(lot);row.setLocationId(location);row.setUnitId(unit);row.setDeltaQty(delta);row.setEventType(type);row.setSourceType(sourceType);row.setSourceRef(source);row.setIdempotencyKey(key);row.setOccurredAt(LocalDateTime.now(ZoneOffset.UTC));store.ledger().insert(row,c.organizationId(),c.actorId());mutations.auditSnapshot(c,type,"InventoryLedger",row.getId(),null,views.view(row),reason,key);
 }
}
