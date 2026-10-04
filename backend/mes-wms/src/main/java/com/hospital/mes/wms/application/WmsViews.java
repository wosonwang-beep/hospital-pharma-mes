package com.hospital.mes.wms.application;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import com.hospital.mes.wms.infrastructure.*;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
@org.springframework.stereotype.Component
public class WmsViews {
 private final ObjectMapper json;
 public WmsViews(ObjectMapper json){this.json=json;}
 public ObjectNode view(ScopedEntity e){
  ObjectNode n=json.valueToTree(e);var bean=new org.springframework.beans.BeanWrapperImpl(e);
  for(var pd:bean.getPropertyDescriptors()){
   String k=pd.getName();if(!n.hasNonNull(k))continue;Object v=bean.getPropertyValue(k);
   if(v instanceof Long && (k.equals("id")||k.endsWith("Id")||k.endsWith("By")))n.put(k,v.toString());
   if(v instanceof BigDecimal decimal)n.put(k,decimal.toPlainString());
   if(v instanceof LocalDateTime date)n.put(k,date.toInstant(ZoneOffset.UTC).toString());
   if(v instanceof LocalDate date)n.put(k,date.toString());
  }
  if(n.has("materialSnapshotJson")){String snapshot=n.path("materialSnapshotJson").asText(null);n.remove("materialSnapshotJson");try{n.set("materialSnapshot",snapshot==null?NullNode.instance:json.readTree(snapshot));}catch(Exception ex){throw new IllegalStateException("Invalid stored snapshot",ex);}}
  ArrayNode actions=n.putArray("allowedActions");
  if(e instanceof ReceiptEntity r){if("DRAFT".equals(r.getRecordStatus())){actions.add("UPDATE");actions.add("CONFIRM");}}
  else if(e instanceof WarehouseEntity||e instanceof LocationEntity||e instanceof ContainerEntity){actions.add("UPDATE");actions.add("ACTIVE".equals(n.path("status").asText())?"DISABLE":"ENABLE");}
  else if(e instanceof MaterialLotEntity l){try{com.hospital.mes.wms.domain.WmsRules.movable(l.getInventoryStatus(),l.getExpiryDate(),l.getRetestDate(),LocalDate.now(ZoneOffset.UTC));actions.add("MOVE");actions.add("ADJUST");}catch(com.hospital.mes.common.exception.ComplianceException ignored){}}
  if(e instanceof ReservationEntity||e instanceof MaterialIssueEntity||e instanceof MaterialIssueItemEntity)n.remove(List.of("orgId","createdBy","updatedBy"));
  if(e instanceof MaterialIssueEntity issue){if("DRAFT".equals(issue.getStatus())){actions.add("UPDATE");actions.add("CONFIRM");}else if("CONFIRMED".equals(issue.getStatus()))actions.add("RETURN");}
  return n;
 }
}
