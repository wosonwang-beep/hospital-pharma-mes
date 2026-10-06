package com.hospital.mes.wms.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.common.exception.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.masterdata.domain.MasterRules;
import com.hospital.mes.wms.domain.*;
import com.hospital.mes.wms.infrastructure.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class MaterialRequestService {
 private final MaterialRequestMapper mapper; private final MaterialRequestItemMapper itemMapper;
 private final ScopedStore<MaterialRequestEntity> roots; private final ScopedStore<MaterialRequestItemEntity> lines;
 private final WmsStore db; private final IssueReturnMapper returns; private final WmsViews views;
 private final MasterMutation mutations; private final MasterQueryService units; private final ObjectMapper json;
 private final CurrentPlatformContextResolver actors; private final ObjectProvider<WmsProductionContextPort> production;
 public MaterialRequestService(MaterialRequestMapper mapper,MaterialRequestItemMapper itemMapper,WmsStore db,IssueReturnMapper returns,WmsViews views,MasterMutation mutations,MasterQueryService units,ObjectMapper json,CurrentPlatformContextResolver actors,ObjectProvider<WmsProductionContextPort> production){
  this.mapper=mapper;this.itemMapper=itemMapper;this.db=db;this.returns=returns;this.views=views;this.mutations=mutations;this.units=units;this.json=json;this.actors=actors;this.production=production;
  roots=new ScopedStore<>(mapper,List.of("request_no"),Map.of());lines=new ScopedStore<>(itemMapper,List.of(),Map.of());
 }
 public WmsProductionContextPort port(){var p=production.getIfAvailable();if(p==null)throw new ComplianceException("DEPENDENCY_NOT_READY","Production snapshot read unavailable");return p;}
 public JsonNode batchFact(long org,long batch){return port().batchFact(org,batch);}
 public MaterialRequestEntity getRoot(long org,long id){return roots.get(org,id);}
 @Transactional(propagation=Propagation.MANDATORY) public MaterialRequestEntity lock(long org,long id){return roots.lock(org,id);}
 public List<MaterialRequestItemEntity> items(long org,long request){return itemMapper.selectList(new QueryWrapper<MaterialRequestItemEntity>().eq("org_id",org).eq("request_id",request).orderByAsc("id").last(currentRead()));}
 @Transactional(readOnly=true) public JsonNode get(String id){var c=mutations.context("wms:request:view");return view(roots.get(c.organizationId(),id(id)));}
 @Transactional public JsonNode create(JsonNode body,String key){
  fields(body,"requestNo","mainBatchId","reason","items");var c=mutations.context("wms:request:create");String why=text(body,"reason",1000);
  return mutations.execute(c,"MaterialRequest:CREATE",key,body,()->{
   long batch=id(body,"mainBatchId");active(port().lockBatch(c.organizationId(),batch));var fact=batchFact(c.organizationId(),batch);
   var root=new MaterialRequestEntity();root.setMainBatchId(batch);root.setProcessSnapshotId(id(fact,"processSnapshotId"));root.setRequestNo(text(body,"requestNo",80));root.setStatus("DRAFT");roots.insert(root,c.organizationId(),c.actorId());
   writeItems(c,root,body,false);mutations.auditSnapshot(c,"MaterialRequest:CREATE","MaterialRequest",root.getId(),null,view(root),why,key);return root;
  },e->view((MaterialRequestEntity)e),201);
 }
 @Transactional public JsonNode edit(String target,JsonNode body,String header,String key){
  fields(body,"requestNo","versionNo","reason","items");var c=mutations.context("wms:request:update");long expected=version(body,header);String why=text(body,"reason",1000);
  return mutations.execute(c,"MaterialRequest:EDIT:"+target,key,Map.of("id",target,"body",body,"version",expected),()->{
   var known=roots.get(c.organizationId(),id(target));active(port().lockBatch(c.organizationId(),known.getMainBatchId()));var root=roots.lock(c.organizationId(),known.getId());WmsRules.version(root.getVersionNo(),expected);MaterialRequestRules.editable(root.getStatus());var before=view(root);
   root.setRequestNo(text(body,"requestNo",80));writeItems(c,root,body,true);roots.update(root,expected,c.actorId(),List.of("requestNo"));mutations.auditSnapshot(c,"MaterialRequest:EDIT","MaterialRequest",root.getId(),before,view(root),why,key);return root;
  },e->view((MaterialRequestEntity)e),200);
 }
 @Transactional public JsonNode submit(String target,JsonNode body,String header,String key){return action(target,body,header,key,false);}
 @Transactional public JsonNode cancel(String target,JsonNode body,String header,String key){return action(target,body,header,key,true);}
 private JsonNode action(String target,JsonNode body,String header,String key,boolean cancel){
  fields(body,"versionNo","reason");var c=mutations.context("wms:request:"+(cancel?"cancel":"submit"));long expected=version(body,header);String why=text(body,"reason",1000);
  return mutations.execute(c,"MaterialRequest:"+(cancel?"CANCEL:":"SUBMIT:")+target,key,Map.of("id",target,"body",body,"version",expected),()->{
   var known=roots.get(c.organizationId(),id(target));var batch=port().lockBatch(c.organizationId(),known.getMainBatchId());var root=roots.lock(c.organizationId(),known.getId());WmsRules.version(root.getVersionNo(),expected);var before=view(root);
   if(cancel){MaterialRequestRules.cancellable(root.getStatus(),hasIssues(c.organizationId(),root.getId()));root.setStatus("CANCELLED");root.setCancelledBy(c.actorId());root.setCancelledAt(now());root.setCancellationReason(why);roots.update(root,expected,c.actorId(),List.of("status","cancelledBy","cancelledAt","cancellationReason"));}
   else{active(batch);MaterialRequestRules.editable(root.getStatus());var fact=batchFact(c.organizationId(),root.getMainBatchId());gate(root.getProcessSnapshotId()==id(fact,"processSnapshotId"),"REQUEST_SNAPSHOT_MISMATCH");var rows=items(c.organizationId(),root.getId());if(rows.isEmpty())throw new IllegalArgumentException("Request lines required");for(var line:rows)validateLine(c.organizationId(),fact,line);root.setStatus("SUBMITTED");root.setSubmittedBy(c.actorId());root.setSubmittedAt(now());roots.update(root,expected,c.actorId(),List.of("status","submittedBy","submittedAt"));}
   mutations.auditSnapshot(c,"MaterialRequest:"+(cancel?"CANCEL":"SUBMIT"),"MaterialRequest",root.getId(),before,view(root),why,key);return root;
  },e->view((MaterialRequestEntity)e),200);
 }
 private void writeItems(CurrentPlatformContext c,MaterialRequestEntity root,JsonNode body,boolean edit){
  var fact=batchFact(c.organizationId(),root.getMainBatchId());gate(root.getProcessSnapshotId()==id(fact,"processSnapshotId"),"REQUEST_SNAPSHOT_MISMATCH");var existing=items(c.organizationId(),root.getId());Set<Long> retained=new HashSet<>(),formulas=new HashSet<>();
  var submitted=body.path("items");if(!submitted.isArray()||submitted.isEmpty()||submitted.size()>500)throw new IllegalArgumentException("1..500 request lines required");
  for(var input:submitted){fields(input,edit?new String[]{"id","formulaItemId","requestedQty","unitId"}:new String[]{"formulaItemId","requestedQty","unitId"});long formulaId=id(input,"formulaItemId");gate(formulas.add(formulaId),"DUPLICATE_REQUEST_FORMULA");var formula=formula(fact,formulaId);MaterialRequestItemEntity line;
   if(input.hasNonNull("id")){long lineId=id(input,"id");line=existing.stream().filter(x->x.getId()==lineId).findFirst().orElseThrow(()->new NoSuchElementException("Request item not in parent"));gate(retained.add(lineId)&&line.getFormulaItemId()==formulaId,"REQUEST_ITEM_IDENTITY_IMMUTABLE");}
   else{line=new MaterialRequestItemEntity();line.setRequestId(root.getId());line.setFormulaItemId(formulaId);line.setMaterialId(id(formula,"materialId"));}
   line.setRequestedQty(WmsRules.quantity(text(input,"requestedQty",50),false));line.setUnitId(id(input,"unitId"));validateLine(c.organizationId(),fact,line);
   if(line.getId()==null)lines.insert(line,c.organizationId(),c.actorId());else lines.update(line,line.getVersionNo(),c.actorId(),List.of("requestedQty","unitId"));
  }
  gate(retained.size()==existing.size(),"REQUEST_ITEM_REMOVAL_FORBIDDEN");
 }
 private void validateLine(long org,JsonNode fact,MaterialRequestItemEntity line){var formula=formula(fact,line.getFormulaItemId());gate(line.getMaterialId()==id(formula,"materialId"),"MATERIAL_NOT_IN_BOM");converted(org,line.getMaterialId(),line.getUnitId(),id(formula,"unitId"),line.getRequestedQty());}
 public static JsonNode formula(JsonNode fact,long id){for(var row:fact.path("processSnapshot").path("snapshot").path("process").path("formula").path("items"))if(row.path("formulaItemId").asText().equals(Long.toString(id)))return row;throw new ComplianceException("MATERIAL_NOT_IN_BOM","Formula item not in frozen batch");}
 public BigDecimal converted(long org,long material,long from,long to,BigDecimal amount){var c=units.convert(org,from,to,material,amount);return WmsRules.exact(amount,c.factor(),c.convertedValue());}
 public BigDecimal issued(MaterialRequestItemEntity line){
  var confirmed=db.materialIssueMapper().selectList(new QueryWrapper<MaterialIssueEntity>().eq("org_id",line.getOrgId()).eq("material_request_id",line.getRequestId()).in("status","CONFIRMED","CLOSED").last(currentRead()));if(confirmed.isEmpty())return BigDecimal.ZERO;
  BigDecimal total=BigDecimal.ZERO;for(var item:db.materialIssueItemMapper().selectList(new QueryWrapper<MaterialIssueItemEntity>().eq("org_id",line.getOrgId()).eq("material_request_item_id",line.getId()).in("issue_id",confirmed.stream().map(MaterialIssueEntity::getId).toList()).last(currentRead())))total=total.add(converted(line.getOrgId(),line.getMaterialId(),item.getUnitId(),line.getUnitId(),item.getIssuedQty()));return total;
 }
 @Transactional(propagation=Propagation.MANDATORY) public void afterConfirm(CurrentPlatformContext c,MaterialRequestEntity root,JsonNode before,String reason,String key){
  var lines=items(c.organizationId(),root.getId());root.setStatus(MaterialRequestRules.fulfillment(lines.stream().map(MaterialRequestItemEntity::getRequestedQty).toList(),lines.stream().map(this::issued).toList()));roots.update(root,root.getVersionNo(),c.actorId(),List.of("status"));mutations.auditSnapshot(c,"MaterialRequest:ISSUE_CONFIRMED","MaterialRequest",root.getId(),before,view(root),reason,key);
 }
 public void checkQuota(long org,MaterialRequestEntity root,List<MaterialIssueItemEntity> proposed){
  var rows=items(org,root.getId());var additional=new HashMap<Long,BigDecimal>();
  for(var item:proposed){var lot=db.materialLot().get(org,item.getMaterialLotId());validateIssueLine(org,root,item.getMaterialRequestItemId(),item.getFormulaItemId(),lot,item.getUnitId(),item.getIssuedQty());var target=rows.stream().filter(r->r.getId().equals(item.getMaterialRequestItemId())).findFirst().orElseThrow();additional.merge(target.getId(),converted(org,target.getMaterialId(),item.getUnitId(),target.getUnitId(),item.getIssuedQty()),BigDecimal::add);}
  MaterialRequestRules.fulfillment(rows.stream().map(MaterialRequestItemEntity::getRequestedQty).toList(),rows.stream().map(r->issued(r).add(additional.getOrDefault(r.getId(),BigDecimal.ZERO))).toList());
 }
 public void validateIssueLine(long org,MaterialRequestEntity request,Long requestItemId,long formulaId,MaterialLotEntity lot,long unit,BigDecimal quantity){
  gate(requestItemId!=null,"REQUEST_ITEM_REQUIRED");var line=items(org,request.getId()).stream().filter(x->x.getId().equals(requestItemId)).findFirst().orElseThrow(()->new ComplianceException("REQUEST_ITEM_MISMATCH","Request item not in linked parent"));gate(line.getFormulaItemId()==formulaId&&line.getMaterialId().equals(lot.getMaterialId()),"REQUEST_ITEM_MISMATCH");var amount=converted(org,line.getMaterialId(),unit,line.getUnitId(),quantity);gate(amount.compareTo(line.getRequestedQty())<=0,"REQUEST_QTY_EXCEEDED");
 }
 public ObjectNode view(MaterialRequestEntity root){
  var n=views.view(root);n.remove("orgId");var fact=batchFact(root.getOrgId(),root.getMainBatchId());context(n,fact);var a=n.putArray("items");
  for(var line:items(root.getOrgId(),root.getId())){var item=a.addObject();item.put("id",line.getId().toString());item.put("formulaItemId",line.getFormulaItemId().toString());item.put("materialId",line.getMaterialId().toString());var material=materialSnapshot(fact,line.getMaterialId());item.put("materialCode",material.path("materialCode").asText());item.put("materialName",material.path("materialName").asText());item.put("requestedQty",line.getRequestedQty().toPlainString());var issued=issued(line);item.put("issuedQty",issued.toPlainString());item.put("remainingQty",line.getRequestedQty().subtract(issued).toPlainString());item.put("unitId",line.getUnitId().toString());var u=units.unit(root.getOrgId(),line.getUnitId());item.put("unitCode",u.unitCode());item.put("unitName",units.unitName(root.getOrgId(),line.getUnitId()));}
  var issues=n.putArray("linkedIssues");for(var issue:linked(root.getOrgId(),root.getId()))issues.add(issueView(issue));var actions=n.putArray("allowedActions");var actor=actors.current();if("DRAFT".equals(root.getStatus())){if(actor.hasPermission("wms:request:update"))actions.add("UPDATE");if(actor.hasPermission("wms:request:submit"))actions.add("SUBMIT");}if(List.of("DRAFT","SUBMITTED").contains(root.getStatus())&&!hasIssues(root.getOrgId(),root.getId())&&actor.hasPermission("wms:request:cancel"))actions.add("CANCEL");return n;
 }
 public ObjectNode issueView(MaterialIssueEntity issue){var n=views.view(issue);context(n,batchFact(issue.getOrgId(),issue.getMainBatchId()));if(issue.getMaterialRequestId()==null){n.putNull("materialRequestId");n.putNull("materialRequestNo");}else n.put("materialRequestNo",roots.get(issue.getOrgId(),issue.getMaterialRequestId()).getRequestNo());var a=n.putArray("items");for(var item:db.materialIssueItemMapper().selectList(new QueryWrapper<MaterialIssueItemEntity>().eq("org_id",issue.getOrgId()).eq("issue_id",issue.getId()).orderByAsc("id")))a.add(views.view(item));var r=n.putArray("returns");for(var item:returns.selectList(new QueryWrapper<IssueReturnEntity>().eq("org_id",issue.getOrgId()).eq("issue_id",issue.getId()).orderByAsc("id"))){var v=(ObjectNode)json.valueToTree(item);v.remove("orgId");for(String k:List.of("id","issueId","issueItemId","unitId","returnedBy"))v.put(k,v.path(k).asText());v.put("quantity",item.getQuantity().toPlainString());v.put("returnedAt",item.getReturnedAt().toInstant(ZoneOffset.UTC).toString());r.add(v);}return n;}
 public void context(ObjectNode n,JsonNode fact){n.put("batchNo",fact.path("batchNo").asText());n.put("productId",fact.path("productId").asText());n.put("productName",fact.path("productName").asText());if(n.has("requestNo"))n.put("formulaVersionId",fact.path("processSnapshot").path("formulaVersionId").asText());}
 public static JsonNode materialSnapshot(JsonNode fact,long material){for(var m:fact.path("processSnapshot").path("snapshot").path("materials"))if(m.path("id").asText().equals(Long.toString(material)))return m;throw new ComplianceException("SNAPSHOT_MATERIAL_MISSING","Material missing from frozen snapshot");}
 private List<MaterialIssueEntity> linked(long org,long id){return db.materialIssueMapper().selectList(new QueryWrapper<MaterialIssueEntity>().eq("org_id",org).eq("material_request_id",id).orderByAsc("id").last(currentRead()));}
 private boolean hasIssues(long org,long id){return !db.materialIssueMapper().selectList(new QueryWrapper<MaterialIssueEntity>().select("id").eq("org_id",org).eq("material_request_id",id).last(currentRead())).isEmpty();}
 @Transactional(readOnly=true) public ScopedStore.PageData<JsonNode> list(int page,int size,Map<String,String> filters){var c=mutations.context("wms:request:view");validateFilters(filters,Set.of("keyword","requestNo","mainBatchId","productId","status","createdFrom","createdTo","page","size"));page(List.of(),page,size);if(nonblank(filters,"productId"))id(filters.get("productId"));var q=new QueryWrapper<MaterialRequestEntity>().eq("org_id",c.organizationId()).orderByAsc("id");for(String k:List.of("mainBatchId","requestNo","status"))if(nonblank(filters,k)){if(k.equals("status")&&!Set.of("DRAFT","SUBMITTED","PARTIALLY_ISSUED","FULFILLED","CANCELLED").contains(filters.get(k)))throw new IllegalArgumentException("Invalid status");q.eq(k.equals("mainBatchId")?"main_batch_id":k.equals("requestNo")?"request_no":"status",k.equals("mainBatchId")?id(filters.get(k)):filters.get(k));}dates(q,filters,"created_at","createdFrom","createdTo");var result=new ArrayList<JsonNode>();for(var row:mapper.selectList(q)){var v=view(row);if(nonblank(filters,"productId")&&!v.path("productId").asText().equals(Long.toString(id(filters.get("productId")))))continue;if(nonblank(filters,"keyword")&&!contains(v.path("requestNo").asText()+v.path("batchNo").asText()+v.path("productName").asText(),filters.get("keyword")))continue;result.add(v);}return page(result,page,size);}
 public static <T> ScopedStore.PageData<T> page(List<T> rows,int page,int size){if(page<0||page>1000000||size<1||size>100)throw new IllegalArgumentException("Invalid pagination");int from=(int)Math.min((long)page*size,rows.size());return new ScopedStore.PageData<>(rows.subList(from,Math.min(from+size,rows.size())),rows.size(),page,size);}
 public static boolean nonblank(Map<String,String> m,String key){return m.containsKey(key)&&!m.get(key).isBlank();}
 public static void validateFilters(Map<String,String> m,Set<String> allowed){for(String k:m.keySet())if(!allowed.contains(k))throw new IllegalArgumentException("Unknown filter: "+k);if(nonblank(m,"keyword")&&m.get("keyword").length()>200)throw new IllegalArgumentException("Keyword too long");}
 public static <E> void dates(QueryWrapper<E> q,Map<String,String> f,String col,String from,String to){LocalDateTime a=nonblank(f,from)?parseDate(f.get(from)):null,b=nonblank(f,to)?parseDate(f.get(to)):null;if(a!=null&&b!=null&&a.isAfter(b))throw new IllegalArgumentException("Date range reversed");if(a!=null)q.ge(col,a);if(b!=null)q.lt(col,b);}
 private static LocalDateTime parseDate(String value){try{return OffsetDateTime.parse(value).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();}catch(DateTimeException ex){throw new IllegalArgumentException("Date filter requires an ISO-8601 offset timestamp");}}
 public static boolean contains(String value,String key){return value.toLowerCase(Locale.ROOT).contains(key.strip().toLowerCase(Locale.ROOT));}
 private static void active(WmsProductionContextPort.BatchContext b){gate(Set.of("RELEASED","IN_PROGRESS").contains(b.status())&&b.snapshot().isObject(),"INVALID_BATCH_STATE");}
 public static void gate(boolean pass,String code){if(!pass)throw new ComplianceException(code,code);}
 public static long id(String value){return MasterMutation.id(value);}public static long id(JsonNode b,String f){return id(text(b,f,30));}
 public static String text(JsonNode b,String f,int max){if(!b.path(f).isTextual())throw new IllegalArgumentException(f+": string required");return MasterRules.text(b.path(f).asText(),max);}
 public static long version(JsonNode b,String header){if(!b.path("versionNo").isIntegralNumber()||!b.path("versionNo").canConvertToLong())throw new IllegalArgumentException("versionNo integer required");return MasterMutation.version(header,b.path("versionNo").longValue());}
 public static void fields(JsonNode b,String...allowed){if(b==null||!b.isObject())throw new IllegalArgumentException("Object required");var keys=Set.of(allowed);b.fieldNames().forEachRemaining(k->{if(!keys.contains(k))throw new IllegalArgumentException("Unknown field: "+k);});}
 private static String currentRead(){return org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()&&!org.springframework.transaction.support.TransactionSynchronizationManager.isCurrentTransactionReadOnly()?"LOCK IN SHARE MODE":"";}
 private static LocalDateTime now(){return LocalDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MILLIS);}
}
