package com.hospital.mes.wms.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.wms.infrastructure.*;
import com.fasterxml.jackson.databind.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.transaction.annotation.*;

/** Finished stock uses the product/batch identity; raw material stock is a separate ledger. */
@org.springframework.stereotype.Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class FinishedInventoryService {
 private final FinishedInventoryLedgerMapper mapper;private final MasterMutation mutations;private final ObjectMapper json;
 public FinishedInventoryService(FinishedInventoryLedgerMapper mapper,MasterMutation mutations,ObjectMapper json){this.mapper=mapper;this.mutations=mutations;this.json=json;}
 @Transactional(propagation=Propagation.MANDATORY) public List<FinishedInventoryLedgerEntity> locked(long org,long batch){return mapper.selectList(new QueryWrapper<FinishedInventoryLedgerEntity>().eq("org_id",org).eq("main_batch_id",batch).orderByAsc("id").last("FOR UPDATE"));}
 public List<JsonNode> facts(long org,long batch){return mapper.selectList(new QueryWrapper<FinishedInventoryLedgerEntity>().eq("org_id",org).eq("main_batch_id",batch).orderByAsc("id")).stream().map(x->(JsonNode)SignedRecordSupport.publicView(x,json)).toList();}
 public BigDecimal balance(List<FinishedInventoryLedgerEntity> rows,long product,long unit,Long location){BigDecimal total=BigDecimal.ZERO;for(var row:rows){if(row.getProductId()!=product||row.getUnitId()!=unit)throw new com.hospital.mes.common.exception.ComplianceException("FINISHED_LEDGER_IDENTITY_CONFLICT","Frozen product/base unit required");if(location==null||location.equals(row.getLocationId()))total=total.add(row.getDeltaQty());}return total;}
 @Transactional(propagation=Propagation.MANDATORY) public long receipt(CurrentPlatformContext c,long batch,long product,long location,long unit,BigDecimal amount,long source,String reason,String key){return append(c,FinishedInventoryLedgerEntity.receipt(batch,product,location,unit,amount,source,key(c,key,"RECEIPT",source),now()),reason,key);}
 @Transactional(propagation=Propagation.MANDATORY) public long shipment(CurrentPlatformContext c,long batch,long product,long location,long unit,BigDecimal amount,long source,String reason,String key){return append(c,FinishedInventoryLedgerEntity.shipment(batch,product,location,unit,amount,source,key(c,key,"SHIPMENT",source),now()),reason,key);}
 private String key(CurrentPlatformContext c,String key,String action,long source){return mutations.digest(Map.of("org",c.organizationId(),"key",key,"action",action,"source",source));}
 private long append(CurrentPlatformContext c,FinishedInventoryLedgerEntity row,String reason,String key){row.setOrgId(c.organizationId());row.setCreatedBy(c.actorId());row.setUpdatedBy(c.actorId());row.setCreatedAt(now());row.setUpdatedAt(row.getCreatedAt());row.setVersionNo(0L);mapper.insert(row);mutations.auditSnapshot(c,row.getEventType(),"FinishedInventoryLedger",row.getId(),null,SignedRecordSupport.publicView(row,json),reason,key);return row.getId();}
 private static LocalDateTime now(){return LocalDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MILLIS);}
}
