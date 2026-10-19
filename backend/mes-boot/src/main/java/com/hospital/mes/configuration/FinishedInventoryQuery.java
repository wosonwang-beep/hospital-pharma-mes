package com.hospital.mes.configuration;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.production.infrastructure.*;
import com.hospital.mes.wms.application.FinishedInventoryService;
import com.hospital.mes.wms.infrastructure.WmsStore;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Permission-filtered, paginated read projection of the independent product stock ledger. */
@org.springframework.stereotype.Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class FinishedInventoryQuery {
 private final ProductionStore batches;private final WmsStore wms;private final FinishedInventoryService ledger;private final MasterQueryService units;private final MasterMutation mutations;private final ObjectMapper json;
 public FinishedInventoryQuery(ProductionStore batches,WmsStore wms,FinishedInventoryService ledger,MasterQueryService units,MasterMutation mutations,ObjectMapper json){this.batches=batches;this.wms=wms;this.ledger=ledger;this.units=units;this.mutations=mutations;this.json=json;}
 @org.springframework.transaction.annotation.Transactional(readOnly=true)
 public ScopedStore.PageData<JsonNode> list(int page,int size,Map<String,String> filters){
  var c=mutations.context("wms:inventory:view");if(page<0||page>1000000||size<1||size>100)throw new IllegalArgumentException("Invalid pagination");
  for(String key:filters.keySet())if(!Set.of("page","size","keyword","mainBatchId","productId","warehouseId","locationId","qualityStatus","inventoryStatus").contains(key))throw new IllegalArgumentException("Unknown finished inventory filter: "+key);
  var q=new QueryWrapper<BatchEntity>().eq("org_id",c.organizationId()).eq("production_schema_version",2).isNotNull("finished_base_unit_id");
  for(var pair:Map.of("mainBatchId","id","productId","product_id","qualityStatus","finished_quality_status","inventoryStatus","finished_inventory_status").entrySet())if(filters.containsKey(pair.getKey())&&!filters.get(pair.getKey()).isBlank())q.eq(pair.getValue(),pair.getKey().endsWith("Id")?MasterMutation.id(filters.get(pair.getKey())):filters.get(pair.getKey()));
  String keyword=filters.get("keyword");if(keyword!=null&&!keyword.isBlank())q.and(x->x.like("batch_no",com.hospital.mes.masterdata.domain.MasterRules.text(keyword,200)).or().like("finished_product_snapshot_json",keyword));
  // Bounded inventory report predicate; values are bound, never interpolated SQL.
  String exists="EXISTS (SELECT 1 FROM wms_finished_inventory_ledger f JOIN wms_location l ON l.id=f.location_id AND l.org_id=f.org_id WHERE f.org_id=prd_main_batch.org_id AND f.main_batch_id=prd_main_batch.id";
  var values=new ArrayList<Object>();for(String key:List.of("warehouseId","locationId"))if(filters.containsKey(key)&&!filters.get(key).isBlank()){exists+=(key.equals("warehouseId")?" AND l.warehouse_id=":" AND f.location_id=")+"{"+values.size()+"}";values.add(MasterMutation.id(filters.get(key)));}q.apply(exists+")",values.toArray());
  long total=batches.batchMapper.selectCount(q);var rows=batches.batchMapper.selectList(q.orderByDesc("id").last("LIMIT "+((long)page*size)+","+size));var items=new ArrayList<JsonNode>();for(var row:rows)items.add(view(row));return new ScopedStore.PageData<>(items,total,page,size);
 }
 private JsonNode view(BatchEntity batch){
  JsonNode product;try{product=json.readTree(batch.getFinishedProductSnapshotJson());}catch(Exception ex){throw new IllegalStateException("Invalid frozen product snapshot",ex);}var n=json.createObjectNode();n.put("finishedSchemaVersion",2);n.put("mainBatchId",batch.getId().toString());n.put("batchNo",batch.getBatchNo());n.put("productId",batch.getProductId().toString());n.set("productName",product.path("productName"));n.set("productCode",product.path("productCode"));n.set("productSpecification",product.path("specification"));n.put("unitId",batch.getFinishedBaseUnitId().toString());n.put("unitName",units.unitName(batch.getOrgId(),batch.getFinishedBaseUnitId()));n.put("qualityStatus",batch.getFinishedQualityStatus());n.put("inventoryStatus",batch.getFinishedInventoryStatus());n.put("manufactureDate",batch.getFinishedManufactureDate().toString());n.put("expiryDate",batch.getFinishedExpiryDate().toString());
  var totals=new LinkedHashMap<Long,BigDecimal>();for(var row:ledger.facts(batch.getOrgId(),batch.getId())){if(row.path("productId").asLong()!=batch.getProductId()||row.path("unitId").asLong()!=batch.getFinishedBaseUnitId())throw new IllegalStateException("Finished stock identity mismatch");totals.merge(row.path("locationId").asLong(),new BigDecimal(row.path("deltaQty").asText()),BigDecimal::add);}BigDecimal total=totals.values().stream().reduce(BigDecimal.ZERO,BigDecimal::add);n.put("onHandQty",total.toPlainString());n.put("reservedQty","0");n.put("availableQty","RELEASED".equals(batch.getFinishedQualityStatus())&&"AVAILABLE".equals(batch.getFinishedInventoryStatus())&&LocalDate.now(ZoneOffset.UTC).isBefore(batch.getFinishedExpiryDate())?total.toPlainString():"0");var locations=n.putArray("locationBalances");for(var entry:totals.entrySet()){var location=wms.location().get(batch.getOrgId(),entry.getKey());var warehouse=wms.warehouse().get(batch.getOrgId(),location.getWarehouseId());var l=locations.addObject();l.put("locationId",entry.getKey().toString());l.put("warehouseId",warehouse.getId().toString());l.put("locationName",location.getLocationName());l.put("warehouseName",warehouse.getWarehouseName());l.put("onHandQty",entry.getValue().toPlainString());l.set("unitName",n.path("unitName"));}return n;
 }
}
