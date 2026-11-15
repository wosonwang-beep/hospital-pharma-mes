package com.hospital.mes.wms.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.common.exception.ComplianceException;
import com.hospital.mes.masterdata.application.MasterMutation;
import com.hospital.mes.masterdata.application.MasterQueryService;
import com.hospital.mes.wms.domain.WmsRules;
import com.hospital.mes.wms.infrastructure.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class WmsSamplingOutboundService {
 public record Line(long detailId,long lotId,Long locationId,Long containerId,long unitId,BigDecimal quantity){}
 public record Result(long detailId,long ledgerId,long locationId,Long containerId,long unitId,BigDecimal quantity){}
 private record Stock(long lot,long location,Long container,long unit){}
 private final WmsStore db;private final MasterMutation mutations;private final MasterQueryService units;private final WmsViews views;
 public WmsSamplingOutboundService(WmsStore db,MasterMutation mutations,MasterQueryService units,WmsViews views){this.db=db;this.mutations=mutations;this.units=units;this.views=views;}
 @Transactional(propagation=Propagation.MANDATORY)
 public List<Result> outbound(long org,long actor,List<Line> requested){
  CurrentPlatformContext c=mutations.context("wms:inventory:sample-outbound");
  if(c.organizationId()!=org||c.actorId()!=actor)throw new ComplianceException("ACTOR_MISMATCH","Authenticated warehouse actor required");
  if(requested==null||requested.isEmpty())throw new ComplianceException("SAMPLING_INCOMPLETE","Sampling details required");
  var lines=new ArrayList<Line>();
  for(Line input:requested){var lot=db.materialLot().lock(org,input.lotId());var receipt=db.receiptItem().get(org,lot.getReceiptItemId());if(input.locationId()==null&&input.containerId()!=null)throw new ComplianceException("SAMPLE_SOURCE_MISMATCH","Container requires an explicit source location");Long location=input.locationId()==null?receipt.getLocationId():input.locationId();Long container=input.locationId()==null?receipt.getContainerId():input.containerId();var locationRow=db.location().lock(org,location);if(!"ACTIVE".equals(locationRow.getStatus()))throw new ComplianceException("STORAGE_INACTIVE","Sampling source location is inactive");if(container!=null&& !"ACTIVE".equals(db.container().lock(org,container).getStatus()))throw new ComplianceException("STORAGE_INACTIVE","Sampling source container is inactive");
   long targetUnit=views.view(lot).path("materialSnapshot").path("baseUnitId").asLong();var converted=units.convert(org,input.unitId(),targetUnit,lot.getMaterialId(),input.quantity());BigDecimal qty=WmsRules.exact(input.quantity(),converted.factor(),converted.convertedValue());lines.add(new Line(input.detailId(),input.lotId(),location,container,targetUnit,qty));}
  lines.sort(Comparator.comparingLong(Line::lotId).thenComparingLong(x->x.locationId()).thenComparing(x->x.containerId()==null?Long.MIN_VALUE:x.containerId()).thenComparingLong(Line::detailId));
  var grouped=new LinkedHashMap<Stock,BigDecimal>();for(Line line:lines)grouped.merge(new Stock(line.lotId(),line.locationId(),line.containerId(),line.unitId()),line.quantity(),BigDecimal::add);
  for(var e:grouped.entrySet()){var k=e.getKey();var q=new QueryWrapper<LedgerEntity>().eq("org_id",org).eq("material_lot_id",k.lot()).eq("location_id",k.location()).eq("unit_id",k.unit());if(k.container()==null)q.isNull("container_id");else q.eq("container_id",k.container());BigDecimal balance=db.ledgerMapper().selectList(q.orderByAsc("id").last("FOR UPDATE")).stream().map(LedgerEntity::getDeltaQty).reduce(BigDecimal.ZERO,BigDecimal::add);WmsRules.balance(balance,e.getValue().negate());}
  var result=new ArrayList<Result>();for(Line line:lines){String stable="SamplingDetail/"+line.detailId();String idem=mutations.digest(Map.of("org",org,"source",stable));var existing=db.ledgerMapper().selectOne(new QueryWrapper<LedgerEntity>().eq("org_id",org).eq("idempotency_key",idem));if(existing!=null){if(!stable.equals(existing.getSourceRef()))throw new ComplianceException("SAMPLE_OUT_IDEMPOTENCY_CONFLICT","Ledger source conflict");result.add(new Result(line.detailId(),existing.getId(),line.locationId(),line.containerId(),line.unitId(),existing.getDeltaQty().abs()));continue;}
   var row=new LedgerEntity();row.setMaterialLotId(line.lotId());row.setLocationId(line.locationId());row.setContainerId(line.containerId());row.setEventType("SAMPLE_OUT");row.setDeltaQty(line.quantity().negate());row.setUnitId(line.unitId());row.setSourceType("SAMPLING_DETAIL");row.setSourceRef(stable);row.setIdempotencyKey(idem);row.setOccurredAt(LocalDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MILLIS));db.ledger().insert(row,org,actor);mutations.auditSnapshot(c,"InventoryLedger:SAMPLE_OUT","InventoryLedger",row.getId(),null,views.view(row),stable,stable);result.add(new Result(line.detailId(),row.getId(),line.locationId(),line.containerId(),line.unitId(),line.quantity()));}
  return List.copyOf(result);
 }
}
