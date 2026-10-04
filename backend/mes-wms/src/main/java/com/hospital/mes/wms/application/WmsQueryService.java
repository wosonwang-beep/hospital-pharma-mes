package com.hospital.mes.wms.application;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.wms.infrastructure.*;
import java.util.List;
/** Organization-scoped detached producer contracts for MES-008A. No release substitute. */
@org.springframework.stereotype.Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class WmsQueryService {
 @org.springframework.beans.factory.annotation.Autowired private InventoryDecisionService inventoryDecisions;
 private final WmsStore db;private final WmsViews views;
 public WmsQueryService(WmsStore db,WmsViews views){this.db=db;this.views=views;}
 public JsonNode receipt(long organizationId,long id){var e=db.receipt().get(organizationId,id);var result=views.view(e);var items=result.putArray("items");db.receiptItemMapper().selectList(new QueryWrapper<ReceiptItemEntity>().eq("org_id",organizationId).eq("receipt_id",id).orderByAsc("id")).forEach(i->items.add(views.view(i)));return result;}
 public JsonNode receiptItem(long organizationId,long id){return views.view(db.receiptItem().get(organizationId,id));}
 public JsonNode materialLot(long organizationId,long id){return inventoryDecisions.view(db.materialLot().get(organizationId,id));}
 public List<JsonNode> ledger(long organizationId,long materialLotId){db.materialLot().get(organizationId,materialLotId);return db.ledgerMapper().selectList(new QueryWrapper<LedgerEntity>().eq("org_id",organizationId).eq("material_lot_id",materialLotId).orderByAsc("id")).stream().map(x->(JsonNode)views.view(x)).toList();}
}
