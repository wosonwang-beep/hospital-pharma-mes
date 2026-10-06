package com.hospital.mes.wms.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.*;
import com.hospital.mes.wms.infrastructure.*;
import com.hospital.mes.masterdata.application.MasterMutation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/** Existing handover facts; never assigns an aggregate entitlement to a particular charge. */
@org.springframework.stereotype.Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class WmsTraceQueryService {
 private final WmsStore db;private final IssueReturnMapper returns;private final MaterialRequestService requests;private final MasterMutation mutations;private final ObjectMapper json;
 public WmsTraceQueryService(WmsStore db,IssueReturnMapper returns,MaterialRequestService requests,MasterMutation mutations,ObjectMapper json){this.db=db;this.returns=returns;this.requests=requests;this.mutations=mutations;this.json=json;}
 @Transactional(readOnly=true) public JsonNode forLot(long org,long lotId){
  var c=mutations.context("trace:view");if(c.organizationId()!=org)throw new NoSuchElementException("Scoped trace lot not found");
  var lot=db.materialLot().get(org,lotId);var result=json.createObjectNode();
  if(c.hasPermission("wms:receipt:view")&&lot.getReceiptItemId()!=null){
   var item=db.receiptItem().get(org,lot.getReceiptItemId());var source=item.getSourceSnapshotJson();
   if(source!=null)try{result.set("source",json.readTree(source));}catch(java.io.IOException ex){throw new IllegalStateException("Frozen source evidence corrupt",ex);}
  }
  var out=result.putArray("issues");if(!c.hasPermission("wms:issue:view"))return result;
  var byIssue=new TreeMap<Long,List<MaterialIssueItemEntity>>();
  for(var item:db.materialIssueItemMapper().selectList(new QueryWrapper<MaterialIssueItemEntity>().eq("org_id",org).eq("material_lot_id",lotId).orderByAsc("id")))byIssue.computeIfAbsent(item.getIssueId(),x->new ArrayList<>()).add(item);
  for(var entry:byIssue.entrySet()){
   var issue=db.materialIssue().get(org,entry.getKey());var n=out.addObject();n.put("id",issue.getId().toString());n.put("issueNo",issue.getIssueNo());n.put("mainBatchId",issue.getMainBatchId().toString());n.put("status",issue.getStatus());
   if(issue.getMaterialRequestId()!=null&&c.hasPermission("wms:request:view"))n.set("request",requests.get(issue.getMaterialRequestId().toString()));
   var items=n.putArray("items");for(var item:entry.getValue()){
    var line=items.addObject();line.put("id",item.getId().toString());line.put("materialLotId",item.getMaterialLotId().toString());var history=line.putArray("returns");
    for(var r:returns.selectList(new QueryWrapper<IssueReturnEntity>().eq("org_id",org).eq("issue_item_id",item.getId()).orderByAsc("id"))){var fact=history.addObject();fact.put("id",r.getId().toString());fact.put("quantity",r.getQuantity().toPlainString());fact.put("unitId",r.getUnitId().toString());fact.put("reason",r.getReason());fact.put("returnedAt",r.getReturnedAt().toString());}
   }
  }return result;
 }
}
