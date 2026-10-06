package com.hospital.mes.wms.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.common.exception.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.masterdata.domain.MasterRules;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import com.hospital.mes.wms.domain.*;
import com.hospital.mes.wms.domain.WmsCommands.*;
import com.hospital.mes.wms.infrastructure.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;

@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class WmsService {
 @org.springframework.beans.factory.annotation.Autowired private org.springframework.beans.factory.ObjectProvider<InventoryDecisionService> inventoryDecisions;
 @org.springframework.beans.factory.annotation.Autowired private org.springframework.beans.factory.ObjectProvider<WmsProductionService> productionIssues;
 private final org.springframework.context.ApplicationEventPublisher events;
 private final WmsStore db; private final MasterMutation mutations; private final WmsViews views; private final ObjectMapper json;
 private final MaterialQueryService materials; private final ApprovedSupplierQueryService suppliers; private final MasterQueryService units;
 public WmsService(WmsStore db,MasterMutation mutations,WmsViews views,ObjectMapper json,MaterialQueryService materials,ApprovedSupplierQueryService suppliers,MasterQueryService units,org.springframework.context.ApplicationEventPublisher events){this.events=events;this.db=db;this.mutations=mutations;this.views=views;this.json=json;this.materials=materials;this.suppliers=suppliers;this.units=units;}
 private static long id(String id){return MasterMutation.id(id);} private static Long nullableId(String id){return id==null||id.isBlank()?null:id(id);}
 private static LocalDate date(String value){
  if(value==null||value.isBlank())return null;
  try{return LocalDate.parse(value);}catch(java.time.format.DateTimeParseException e){throw new IllegalArgumentException("Invalid lot date",e);}
 }
 private static LocalDateTime now(){return LocalDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MILLIS);}
 private static String reason(String text){return MasterRules.text(text,1000);}
 private static <E extends ScopedEntity> E copy(E e,Class<E> type){try{var c=type.getDeclaredConstructor().newInstance();org.springframework.beans.BeanUtils.copyProperties(e,c);return c;}catch(Exception ex){throw new IllegalStateException(ex);}}
 @SuppressWarnings("unchecked") private ScopedStore<ScopedEntity> store(String type){return (ScopedStore<ScopedEntity>)(ScopedStore<?>)switch(type){case "Warehouse"->db.warehouse();case "Location"->db.location();case "Container"->db.container();case "MaterialLot"->db.materialLot();case "MaterialIssue"->db.materialIssue();case "Receipt"->db.receipt();default->throw new IllegalArgumentException("Unknown resource");};}
 private String readPermission(String type){return type.equals("Receipt")?"wms:receipt:view":type.equals("MaterialIssue")?"wms:issue:view":"wms:inventory:view";}
 public ScopedStore.PageData<JsonNode> list(String type,int page,int size,String keyword,Map<String,String> filters){var c=mutations.context(readPermission(type));var rows=store(type).list(c.organizationId(),page,size,keyword,filters);return new ScopedStore.PageData<>(rows.items().stream().map(x->(JsonNode)view(x)).toList(),rows.total(),page,size);}
 public JsonNode get(String type,String id){var c=mutations.context(readPermission(type));return view(store(type).get(c.organizationId(),id(id)));}
 private ObjectNode view(ScopedEntity entity){
  var out=views.view(entity);
  if(entity instanceof MaterialLotEntity lot){out.set("inventoryDecisions",json.valueToTree(inventoryDecisions.getObject().history(lot.getOrgId(),lot.getId())));var actions=out.withArray("allowedActions");if("AVAILABLE".equals(lot.getInventoryStatus()))actions.add("FREEZE");if("FROZEN".equals(lot.getInventoryStatus()))actions.add("UNFREEZE");}
  if(entity instanceof ReceiptEntity r){var a=out.putArray("items");for(var item:items(r)){var n=views.view(item);var lot=db.materialLotMapper().selectOne(new QueryWrapper<MaterialLotEntity>().eq("org_id",r.getOrgId()).eq("receipt_item_id",item.getId()));if(lot!=null)n.put("materialLotId",lot.getId().toString());a.add(n);}}
  if(entity instanceof MaterialIssueEntity issue){var a=out.putArray("items");db.materialIssueItemMapper().selectList(new QueryWrapper<MaterialIssueItemEntity>().eq("org_id",issue.getOrgId()).eq("issue_id",issue.getId()).orderByAsc("id")).forEach(i->a.add(views.view(i)));out.set("returns",productionIssues.getObject().returnFacts(issue.getOrgId(),issue.getId()));}
  return out;
 }
 private List<ReceiptItemEntity> items(ReceiptEntity receipt){return db.receiptItemMapper().selectList(new QueryWrapper<ReceiptItemEntity>().eq("org_id",receipt.getOrgId()).eq("receipt_id",receipt.getId()).orderByAsc("id"));}

 @Transactional public JsonNode maintenance(String type,String target,Object request,String header,String key){
  boolean create=target==null;var c=mutations.context("wms:inventory:"+(create?"create":"update"));JsonNode body=json.valueToTree(request);
  long expected=create?0:MasterMutation.version(header,body.hasNonNull("versionNo")?body.get("versionNo").asLong():null);String why=create?null:reason(body.path("reason").asText(null));
  return mutations.execute(c,type+":"+(create?"CREATE":target),key,Map.of("target",target==null?"":target,"version",expected,"body",request),()->{
   ScopedEntity e=create?switch(type){case "Warehouse"->new WarehouseEntity();case "Location"->new LocationEntity();case "Container"->new ContainerEntity();default->throw new IllegalArgumentException("Unknown maintenance type");}:store(type).lock(c.organizationId(),id(target));
   JsonNode before=create?null:view(e);if(!create)WmsRules.version(e.getVersionNo(),expected);
   var bean=new org.springframework.beans.BeanWrapperImpl(e);String action=create?"UPDATE":MasterRules.text(body.path("action").asText(null),30);
   List<String> fields=new ArrayList<>();
   if(action.equals("UPDATE")){
    switch(type){
     case "Warehouse"->{set(bean,fields,"warehouseCode",MasterRules.text(body.path("warehouseCode").asText(null),64));set(bean,fields,"warehouseName",MasterRules.text(body.path("warehouseName").asText(null),200));set(bean,fields,"warehouseType",MasterRules.text(body.path("warehouseType").asText(null),30));}
     case "Location"->{long warehouse=id(body.path("warehouseId").asText());active(db.warehouse().get(c.organizationId(),warehouse).getStatus());if(!create&&!Objects.equals(bean.getPropertyValue("warehouseId"),warehouse)&&locationUsed(c.organizationId(),e.getId()))throw new ComplianceException("LOCATION_IN_USE","Referenced location cannot change warehouse");set(bean,fields,"warehouseId",warehouse);set(bean,fields,"locationCode",MasterRules.text(body.path("locationCode").asText(null),64));set(bean,fields,"locationName",MasterRules.optional(body.path("locationName").asText(null),200));}
     case "Container"->{set(bean,fields,"containerCode",MasterRules.text(body.path("containerCode").asText(null),100));set(bean,fields,"containerType",MasterRules.optional(body.path("containerType").asText(null),30));}
     default->throw new IllegalArgumentException("Unknown maintenance type");
    }
    if(create)set(bean,fields,"status","ACTIVE");
   }else if(action.equals("ENABLE"))set(bean,fields,"status",MasterRules.enable((String)bean.getPropertyValue("status")));
   else if(action.equals("DISABLE")){if(inUse(type,c.organizationId(),e.getId()))throw new ComplianceException("STORAGE_IN_USE","Storage with nonzero inventory cannot be disabled");set(bean,fields,"status",MasterRules.disable((String)bean.getPropertyValue("status")));}
   else throw new IllegalArgumentException("Unknown command");
   if(create)store(type).insert(e,c.organizationId(),c.actorId());else store(type).update(e,expected,c.actorId(),fields);
   mutations.auditSnapshot(c,type+":"+(create?"CREATE":action),type,e.getId(),before,view(e),why,key);return e;
  },x->view(x),200);
 }
 private void set(org.springframework.beans.BeanWrapper bean,List<String> fields,String field,Object value){bean.setPropertyValue(field,value);fields.add(field);}
 private boolean locationUsed(long org,long location){return db.receiptItemMapper().selectCount(new QueryWrapper<ReceiptItemEntity>().eq("org_id",org).eq("location_id",location))>0||db.ledgerMapper().selectCount(new QueryWrapper<LedgerEntity>().eq("org_id",org).eq("location_id",location))>0;}
 private boolean inUse(String type,long org,long target){
  Set<Long> locations=new HashSet<>();if(type.equals("Warehouse"))db.locationMapper().selectList(new QueryWrapper<LocationEntity>().eq("org_id",org).eq("warehouse_id",target)).forEach(l->locations.add(l.getId()));
  var q=new QueryWrapper<LedgerEntity>().eq("org_id",org);if(type.equals("Container"))q.eq("container_id",target);else if(type.equals("Location"))q.eq("location_id",target);else{if(locations.isEmpty())return false;q.in("location_id",locations);}
  return balances(db.ledgerMapper().selectList(q.last("FOR UPDATE"))).values().stream().anyMatch(v->v.signum()!=0);
 }
 private static void active(String status){if(!"ACTIVE".equals(status))throw new ComplianceException("STORAGE_INACTIVE","Choose active warehouse, location and container");}

 @Transactional public JsonNode createReceipt(ReceiptCreate r,String key,boolean direct){
  var c=mutations.context("wms:receipt:create");return mutations.execute(c,direct?"Inventory:RECEIVE":"Receipt:CREATE",key,r,()->{
   ReceiptEntity e=new ReceiptEntity();applyReceipt(e,r,c);e.setRecordStatus("DRAFT");e.setReceivedBy(c.actorId());e.setReceivedAt(now());db.receipt().insert(e,c.organizationId(),c.actorId());saveItems(e,r.items(),c);
   mutations.auditSnapshot(c,"Receipt:CREATE","MaterialReceipt",e.getId(),null,view(e),null,key);
   if(direct)confirm(e,c,key,"Direct physical receipt",0,true);return e;
  },this::view,200);
 }
 @Transactional public JsonNode updateReceipt(String target,ReceiptUpdate r,String header,String key){
  var c=mutations.context("wms:receipt:update");long expected=MasterMutation.version(header,r.versionNo());String why=reason(r.reason());
  return mutations.execute(c,"Receipt:"+target+":UPDATE",key,Map.of("id",target,"version",expected,"body",r),()->{var e=db.receipt().lock(c.organizationId(),id(target));WmsRules.version(e.getVersionNo(),expected);WmsRules.draft(e.getRecordStatus());var before=view(e);applyReceipt(e,new ReceiptCreate(r.receiptNo(),r.supplierId(),r.purchaseOrderNo(),r.deliveryNoteNo(),r.warehouseId(),r.transportCheckPassed(),r.items()),c);saveItems(e,r.items(),c);db.receipt().update(e,expected,c.actorId(),List.of("receiptNo","supplierId","purchaseOrderNo","deliveryNoteNo","warehouseId","transportCheckPassed"));mutations.auditSnapshot(c,"Receipt:UPDATE","MaterialReceipt",e.getId(),before,view(e),why,key);return e;},this::view,200);
 }
 @Transactional public JsonNode confirmReceipt(String target,Command r,String header,String key){var c=mutations.context("wms:receipt:confirm");long expected=MasterMutation.version(header,r.versionNo());String why=reason(r.reason());return mutations.execute(c,"Receipt:"+target+":CONFIRM",key,Map.of("id",target,"version",expected,"body",r),()->{var e=db.receipt().lock(c.organizationId(),id(target));WmsRules.version(e.getVersionNo(),expected);confirm(e,c,key,why,expected,false);return e;},this::view,200);}
 private void applyReceipt(ReceiptEntity e,ReceiptCreate r,CurrentPlatformContext c){
  e.setReceiptNo(MasterRules.text(r.receiptNo(),80));e.setSupplierId(id(r.supplierId()));e.setPurchaseOrderNo(MasterRules.optional(r.purchaseOrderNo(),100));e.setDeliveryNoteNo(MasterRules.optional(r.deliveryNoteNo(),100));e.setWarehouseId(id(r.warehouseId()));active(db.warehouse().get(c.organizationId(),e.getWarehouseId()).getStatus());WmsRules.check(r.transportCheckPassed());e.setTransportCheckPassed(r.transportCheckPassed());
  if(r.items()==null||r.items().isEmpty()||r.items().size()>200)throw new IllegalArgumentException("Receipt needs 1..200 items");
 }
 private void saveItems(ReceiptEntity receipt,List<ReceiptItemInput> inputs,CurrentPlatformContext c){
  var old=items(receipt);if(inputs.size()<old.size())throw new ComplianceException("RECEIPT_ITEMS_RETAINED","Saved receipt items must be retained");Set<String> lotNos=new HashSet<>();
  for(int i=0;i<inputs.size();i++){
   var r=inputs.get(i);var e=i<old.size()?old.get(i):new ReceiptItemEntity();long version=e.getId()==null?0:e.getVersionNo();
   e.setReceiptId(receipt.getId());e.setMaterialId(id(r.materialId()));e.setLotNo(MasterRules.text(r.lotNo(),100));if(!lotNos.add(e.getLotNo()))throw new IllegalArgumentException("Duplicate lot number in receipt");
   e.setSupplierLotNo(MasterRules.text(r.supplierLotNo(),100));e.setManufacturerLotNo(MasterRules.optional(r.manufacturerLotNo(),100));e.setManufactureDate(date(r.manufactureDate()));e.setExpiryDate(date(r.expiryDate()));e.setRetestDate(date(r.retestDate()));WmsRules.dates(e.getManufactureDate(),e.getExpiryDate(),e.getRetestDate(),LocalDate.now(ZoneOffset.UTC));
   e.setReceivedQty(WmsRules.quantity(r.receivedQty(),false));e.setUnitId(id(r.unitId()));e.setPackageSpec(MasterRules.optional(r.packageSpec(),200));if(r.packageCount()==null||r.packageCount()<1||r.packageCount()>Integer.MAX_VALUE)throw new IllegalArgumentException("Positive package count required");e.setPackageCount(r.packageCount().intValue());e.setLocationId(id(r.locationId()));e.setContainerId(nullableId(r.containerId()));
   for(Boolean check:Arrays.asList(r.packageCheckPassed(),r.sealCheckPassed(),r.labelCheckPassed(),r.damageCheckPassed(),r.contaminationCheckPassed()))WmsRules.check(check);
   e.setPackageCheckPassed(r.packageCheckPassed());e.setSealCheckPassed(r.sealCheckPassed());e.setLabelCheckPassed(r.labelCheckPassed());e.setDamageCheckPassed(r.damageCheckPassed());e.setContaminationCheckPassed(r.contaminationCheckPassed());
   var snapshot=materials.requireUsable(c.organizationId(),e.getMaterialId(),Instant.now());suppliers.requireApproved(c.organizationId(),e.getMaterialId(),receipt.getSupplierId(),Instant.now());storage(c.organizationId(),receipt.getWarehouseId(),e.getLocationId(),e.getContainerId());convert(c.organizationId(),e.getMaterialId(),e.getUnitId(),snapshot.path("baseUnitId").asLong(),e.getReceivedQty());
   e.setMaterialSnapshotJson(snapshot.toString());e.setRequiresIncomingInspectionSnapshot(snapshot.path("requiresIncomingInspection").asBoolean(true));
   if(e.getId()==null)db.receiptItem().insert(e,c.organizationId(),c.actorId());else db.receiptItem().update(e,version,c.actorId(),List.of("materialId","lotNo","supplierLotNo","manufacturerLotNo","manufactureDate","expiryDate","retestDate","receivedQty","unitId","packageSpec","packageCount","locationId","containerId","packageCheckPassed","sealCheckPassed","labelCheckPassed","damageCheckPassed","contaminationCheckPassed","materialSnapshotJson","requiresIncomingInspectionSnapshot"));
  }
 }
 private void confirm(ReceiptEntity receipt,CurrentPlatformContext c,String key,String why,long expected,boolean directReceive){
  var exemptionLots=new ArrayList<Long>();
  WmsRules.draft(receipt.getRecordStatus());JsonNode before=view(receipt);WmsRules.passed(receipt.getTransportCheckPassed());active(db.warehouse().get(c.organizationId(),receipt.getWarehouseId()).getStatus());
  var receiptItems=items(receipt);suppliers.lockSources(c.organizationId(),receiptItems.stream().map(ReceiptItemEntity::getMaterialId).toList(),receipt.getSupplierId());
  for(var item:receiptItems){
   for(Boolean check:Arrays.asList(item.getPackageCheckPassed(),item.getSealCheckPassed(),item.getLabelCheckPassed(),item.getDamageCheckPassed(),item.getContaminationCheckPassed()))WmsRules.passed(check);
   var snapshot=materials.freezeUsable(c.organizationId(),item.getMaterialId(),Instant.now());var source=suppliers.freezeSource(c.organizationId(),item.getMaterialId(),receipt.getSupplierId(),Instant.now());storage(c.organizationId(),receipt.getWarehouseId(),item.getLocationId(),item.getContainerId());WmsRules.movable("BLOCKED",item.getExpiryDate(),item.getRetestDate(),LocalDate.now(ZoneOffset.UTC));
   long unit=snapshot.path("baseUnitId").asLong();BigDecimal qty=convert(c.organizationId(),item.getMaterialId(),item.getUnitId(),unit,item.getReceivedQty());
   item.setSourceSnapshotJson(source.toString());item.setMaterialSnapshotJson(snapshot.toString());item.setRequiresIncomingInspectionSnapshot(snapshot.path("requiresIncomingInspection").asBoolean(true));db.receiptItem().update(item,item.getVersionNo(),c.actorId(),List.of("materialSnapshotJson","requiresIncomingInspectionSnapshot","sourceSnapshotJson"));
   var lot=new MaterialLotEntity();lot.setMaterialId(item.getMaterialId());lot.setLotNo(item.getLotNo());lot.setSupplierLotNo(item.getSupplierLotNo());lot.setManufactureDate(item.getManufactureDate());lot.setExpiryDate(item.getExpiryDate());lot.setRetestDate(item.getRetestDate());lot.setReceiptItemId(item.getId());lot.setMaterialSnapshotJson(snapshot.toString());lot.setRequiresIncomingInspectionSnapshot(item.getRequiresIncomingInspectionSnapshot());lot.setQualityStatus("QUARANTINE");lot.setInventoryStatus("BLOCKED");db.materialLot().insert(lot,c.organizationId(),c.actorId());
   if(!Boolean.TRUE.equals(lot.getRequiresIncomingInspectionSnapshot()))exemptionLots.add(lot.getId());
   append(c,lot,item.getLocationId(),item.getContainerId(),"RECEIVE",qty,unit,"MaterialReceiptItem",item.getId().toString(),key+":"+item.getId());mutations.auditSnapshot(c,"MaterialLot:RECEIVE","MaterialLot",lot.getId(),null,view(lot),why,key);
  }
  receipt.setRecordStatus("APPROVED");receipt.setConfirmedBy(c.actorId());receipt.setConfirmedAt(now());db.receipt().update(receipt,expected,c.actorId(),List.of("recordStatus","confirmedBy","confirmedAt"));mutations.auditSnapshot(c,"Receipt:CONFIRM","MaterialReceipt",receipt.getId(),before,view(receipt),why,key);
  events.publishEvent(new ReceiptConfirmed(c.organizationId(),receipt.getId(),exemptionLots,directReceive,key));
 }
 private void storage(long org,Long warehouse,long location,Long container){var l=db.location().lock(org,location);active(l.getStatus());if(warehouse!=null&&!warehouse.equals(l.getWarehouseId()))throw new ComplianceException("LOCATION_WAREHOUSE_MISMATCH","Location belongs to a different warehouse");active(db.warehouse().lock(org,l.getWarehouseId()).getStatus());if(container!=null)active(db.container().lock(org,container).getStatus());}
 private BigDecimal convert(long org,long material,long from,long to,BigDecimal amount){var converted=units.convert(org,from,to,material,amount);return WmsRules.exact(amount,converted.factor(),converted.convertedValue());}
 private void append(CurrentPlatformContext c,MaterialLotEntity lot,long location,Long container,String type,BigDecimal qty,long unit,String source,String ref,String movementKey){var e=new LedgerEntity();e.setMaterialLotId(lot.getId());e.setLocationId(location);e.setContainerId(container);e.setEventType(type);e.setDeltaQty(qty);e.setUnitId(unit);e.setSourceType(source);e.setSourceRef(ref.length()>100?mutations.digest(ref):ref);e.setIdempotencyKey(mutations.digest(Map.of("org",c.organizationId(),"actor",c.actorId(),"lot",lot.getId(),"event",type,"movement",movementKey)));e.setOccurredAt(now());db.ledger().insert(e,c.organizationId(),c.actorId());mutations.auditSnapshot(c,"InventoryLedger:"+type,"InventoryLedger",e.getId(),null,views.view(e),source+":"+ref,movementKey.substring(0,Math.min(100,movementKey.length())));}

 private record StockKey(long lot,long location,Long container,long unit){}
 private Map<StockKey,BigDecimal> balances(List<LedgerEntity> rows){Map<StockKey,BigDecimal> result=new LinkedHashMap<>();for(var e:rows)result.merge(new StockKey(e.getMaterialLotId(),e.getLocationId(),e.getContainerId(),e.getUnitId()),e.getDeltaQty(),BigDecimal::add);return result;}
 private BigDecimal balance(long org,long lot,long location,Long container,long unit){var q=new QueryWrapper<LedgerEntity>().eq("org_id",org).eq("material_lot_id",lot).eq("location_id",location).eq("unit_id",unit);if(container==null)q.isNull("container_id");else q.eq("container_id",container);return db.ledgerMapper().selectList(q.last("FOR UPDATE")).stream().map(LedgerEntity::getDeltaQty).reduce(BigDecimal.ZERO,BigDecimal::add);}
 public ScopedStore.PageData<JsonNode> inventory(int page,int size,String keyword,Map<String,String> filters){
  var c=mutations.context("wms:inventory:view");if(page<0||page>1000000||size<1||size>100)throw new IllegalArgumentException("Invalid pagination");var q=new QueryWrapper<LedgerEntity>().eq("org_id",c.organizationId()).orderByAsc("id");for(String field:List.of("materialLotId","locationId"))if(filters.containsKey(field)&&!filters.get(field).isBlank())q.eq(field.equals("materialLotId")?"material_lot_id":"location_id",id(filters.get(field)));
  List<JsonNode> rows=new ArrayList<>();for(var entry:balances(db.ledgerMapper().selectList(q)).entrySet()){
   if(entry.getValue().signum()==0)continue;StockKey k=entry.getKey();var lot=db.materialLot().get(c.organizationId(),k.lot());var l=views.view(lot);JsonNode snapshot=l.path("materialSnapshot");String name=snapshot.path("materialName").asText();if(keyword!=null&&!keyword.isBlank()&&!lot.getLotNo().contains(keyword)&&!name.contains(keyword))continue;
   ObjectNode n=json.createObjectNode();n.put("materialLotId",Long.toString(k.lot()));n.put("lotNo",lot.getLotNo());n.put("materialId",lot.getMaterialId().toString());n.put("materialName",name);n.put("locationId",Long.toString(k.location()));if(k.container()!=null)n.put("containerId",k.container().toString());else n.putNull("containerId");n.put("unitId",Long.toString(k.unit()));n.put("quantity",entry.getValue().toPlainString());n.put("qualityStatus",lot.getQualityStatus());n.put("inventoryStatus",lot.getInventoryStatus());n.put("versionNo",lot.getVersionNo());if(lot.getExpiryDate()!=null)n.put("expiryDate",lot.getExpiryDate().toString());rows.add(n);
  }
  int from=(int)Math.min((long)page*size,rows.size());return new ScopedStore.PageData<>(rows.subList(from,Math.min(from+size,rows.size())),rows.size(),page,size);
 }
 @Transactional public JsonNode move(InventoryMove r,String header,String key){
  var c=mutations.context("wms:inventory:move");long expected=MasterMutation.version(header,r.versionNo());String why=reason(r.reason());return mutations.execute(c,"Inventory:MOVE",key,Map.of("version",expected,"body",r),()->{
   var lot=db.materialLot().lock(c.organizationId(),id(r.materialLotId()));WmsRules.version(lot.getVersionNo(),expected);WmsRules.movable(lot.getInventoryStatus(),lot.getExpiryDate(),lot.getRetestDate(),LocalDate.now(ZoneOffset.UTC));var before=view(lot);long from=id(r.fromLocationId()),to=id(r.toLocationId());Long fromContainer=nullableId(r.fromContainerId()),toContainer=nullableId(r.toContainerId());if(from==to&&Objects.equals(fromContainer,toContainer))throw new IllegalArgumentException("Move needs different source and destination");storage(c.organizationId(),null,from,fromContainer);storage(c.organizationId(),null,to,toContainer);long unit=before.path("materialSnapshot").path("baseUnitId").asLong();var qty=convert(c.organizationId(),lot.getMaterialId(),id(r.unitId()),unit,WmsRules.quantity(r.quantity(),false));WmsRules.balance(balance(c.organizationId(),lot.getId(),from,fromContainer,unit),qty.negate());
   append(c,lot,from,fromContainer,"MOVE_OUT",qty.negate(),unit,"InventoryMove",key,key+":OUT");append(c,lot,to,toContainer,"MOVE_IN",qty,unit,"InventoryMove",key,key+":IN");db.materialLot().update(lot,expected,c.actorId(),List.of());mutations.auditSnapshot(c,"MaterialLot:MOVE","MaterialLot",lot.getId(),before,view(lot),why,key);return lot;
  },this::view,200);
 }
 @Transactional public JsonNode adjust(InventoryAdjust r,String header,String key){
  var c=mutations.context("wms:inventory:adjust");long expected=MasterMutation.version(header,r.versionNo());String why=reason(r.reason());return mutations.execute(c,"Inventory:ADJUST",key,Map.of("version",expected,"body",r),()->{
   var lot=db.materialLot().lock(c.organizationId(),id(r.materialLotId()));WmsRules.version(lot.getVersionNo(),expected);WmsRules.movable(lot.getInventoryStatus(),lot.getExpiryDate(),lot.getRetestDate(),LocalDate.now(ZoneOffset.UTC));var before=view(lot);long location=id(r.locationId());Long container=nullableId(r.containerId());storage(c.organizationId(),null,location,container);long unit=before.path("materialSnapshot").path("baseUnitId").asLong();var delta=convert(c.organizationId(),lot.getMaterialId(),id(r.unitId()),unit,WmsRules.quantity(r.deltaQty(),true));WmsRules.balance(balance(c.organizationId(),lot.getId(),location,container,unit),delta);append(c,lot,location,container,"ADJUST",delta,unit,"InventoryAdjustment",key,key);db.materialLot().update(lot,expected,c.actorId(),List.of());mutations.auditSnapshot(c,"MaterialLot:ADJUST","MaterialLot",lot.getId(),before,view(lot),why,key);return lot;
  },this::view,200);
 }
 public List<JsonNode> reservations(String batch){var c=mutations.context("wms:reservation:view");return db.reservationMapper().selectList(new QueryWrapper<ReservationEntity>().eq("org_id",c.organizationId()).eq("main_batch_id",id(batch)).orderByAsc("id")).stream().map(x->(JsonNode)views.view(x)).toList();}
 public JsonNode unavailable(String permission,boolean receiptRequired){mutations.context(permission);throw new ComplianceException(receiptRequired?"RECEIPT_REQUIRED":"DEPENDENCY_NOT_READY",receiptRequired?"Use the controlled receipt workflow in this delivery phase":"Real MainBatch and MaterialEligibilityService dependencies are required");}
}
