package com.hospital.mes.qc.application;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import com.hospital.mes.qc.domain.*;
import com.hospital.mes.qc.infrastructure.*;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;
import java.util.*;
import java.time.*;
import static com.hospital.mes.qc.domain.QcSpecificationRules.*;
/**
 * Maintains the QC quality standard basic library: create, read, update and delete only.
 * There is no approval, no electronic signature and no regulated version; every save appends a plain
 * maintenance revision so the change, its author and its reason stay traceable.
 */
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class QcSpecificationService {
 @org.springframework.beans.factory.annotation.Autowired private org.springframework.beans.factory.ObjectProvider<QcProductReferencePort> products;
 private final QcStore db; private final MasterMutation mutation; private final MaterialQueryService materials; private final ObjectMapper json;
 public QcSpecificationService(QcStore db,MasterMutation mutation,MaterialQueryService materials,ObjectMapper json){this.db=db;this.mutation=mutation;this.materials=materials;this.json=json;}
 @Transactional(readOnly=true)
 public QcViews.Page<QcViews.SpecificationSummary> list(int page,int size,String keyword,String basisType,String materialId,String productId,String status){
  var c=mutation.context("qms:specification:view");
  var result=db.list(c.organizationId(),page,size,keyword,basisType,materialId==null?null:id(materialId),productId==null?null:id(productId),status);
  return new QcViews.Page<>(result.items().stream().map(this::snapshotView).toList(),result.total(),page,size);
 }
 @Transactional(readOnly=true)
 public JsonNode get(String value){var c=mutation.context("qms:specification:view");return view(() -> detailView(db.root(c.organizationId(),id(value))));}
 @Transactional
 public JsonNode create(QcCommands.Create request,String key){
  var c=mutation.context("qms:specification:create");if(request==null)throw invalid("Request required");
  String code=text(request.specificationCode(),100),name=text(request.specificationName(),200);
  String basisType=basisType(request.basisType()),basisReference=text(request.basisReference(),300);
  String remark=optionalText(request.remark(),1000),reason=text(request.reason(),500);
  LocalDate effectiveDate=request.effectiveDate();
  var wantedScopes=scopes(request.materialIds(),request.productIds());
  if(wantedScopes.isEmpty())throw invalid("At least one applicable material or product required");
  if(request.items()==null||request.items().isEmpty())throw invalid("At least one item required");
  validateItems(request.items());
  verifyScopes(c.organizationId(),wantedScopes);
  try{return mutation.execute(c,"QC_SPEC_CREATE",key,canonicalise(code,name,basisType,basisReference,request,reason),()->{
   var r=new SpecificationEntity();r.setMaterialId(firstMaterial(wantedScopes));r.setSpecificationCode(code);r.setSpecificationName(name);r.setBasisType(basisType);r.setBasisReference(basisReference);r.setEffectiveDate(effectiveDate);r.setRemark(remark);r.setStatus(ACTIVE);db.insertRoot(r,c.organizationId(),c.actorId());
   replaceScopes(r,wantedScopes,c.actorId());replaceItems(r,request.items(),c.actorId());
   db.insertRevision(revision(r,c.actorId(),"CREATE",reason,null,view(() -> detailView(r))),c.organizationId(),c.actorId());
   mutation.auditSnapshot(c,"QC_SPEC_CREATED","QcSpecification",r.getId(),null,view(() -> detailView(r)),reason,key);return r;
  },row->view(() -> detailView((SpecificationEntity)row)),201);}catch(DuplicateKeyException e){throw conflict("QC_SPEC_CODE_EXISTS","QC specification code already exists");}
 }
 @Transactional
 public JsonNode edit(String specificationId,QcCommands.Edit request,String header,String key){
  var c=mutation.context("qms:specification:edit");long spec=id(specificationId);if(request==null)throw invalid("Request required");long expectedToken=expected(header,request.recordVersion());
  String code=text(request.specificationCode(),100),name=text(request.specificationName(),200);
  String basisType=basisType(request.basisType()),basisReference=text(request.basisReference(),300);
  String remark=optionalText(request.remark(),1000),status=request.status()==null?ACTIVE:status(request.status()),reason=text(request.reason(),500);
  LocalDate effectiveDate=request.effectiveDate();
  return mutation.execute(c,"QC_SPEC_EDIT:"+spec,key,Map.of("id",specificationId,"recordVersion",expectedToken,"specificationCode",code,"specificationName",name,"basisType",basisType,"basisReference",basisReference,"status",status,"reason",reason),()->{
   var r=locked(c,spec,expectedToken);var before=view(() -> detailView(r));
   r.setSpecificationCode(code);r.setSpecificationName(name);r.setBasisType(basisType);r.setBasisReference(basisReference);r.setEffectiveDate(effectiveDate);r.setRemark(remark);r.setStatus(status);
   db.updateRoot(r,expectedToken,c.actorId());
   var after=view(() -> detailView(r));db.insertRevision(revision(r,c.actorId(),"UPDATE",reason,before,after),c.organizationId(),c.actorId());
   mutation.auditSnapshot(c,"QC_SPEC_UPDATED","QcSpecification",r.getId(),before,after,reason,key);return r;
  },row->view(() -> detailView((SpecificationEntity)row)),200);
 }
 @Transactional
 public JsonNode replaceScope(String specificationId,QcCommands.ReplaceScope request,String header,String key){
  var c=mutation.context("qms:specification:edit");long spec=id(specificationId);if(request==null)throw invalid("Request required");long expectedToken=expected(header,request.recordVersion());
  String reason=text(request.reason(),500);var wanted=scopes(request.materialIds(),request.productIds());
  if(wanted.isEmpty())throw invalid("At least one applicable material or product required");
  verifyScopes(c.organizationId(),wanted);
  return mutation.execute(c,"QC_SPEC_SCOPE:"+spec,key,Map.of("id",specificationId,"recordVersion",expectedToken,"materialIds",request.materialIds()==null?List.of():request.materialIds(),"productIds",request.productIds()==null?List.of():request.productIds(),"reason",reason),()->{
   var r=locked(c,spec,expectedToken);var before=view(() -> detailView(r));
   replaceScopes(r,wanted,c.actorId());r.setMaterialId(firstMaterial(wanted));db.touchRoot(r,expectedToken,c.actorId());
   var after=view(() -> detailView(r));db.insertRevision(revision(r,c.actorId(),"UPDATE",reason,before,after),c.organizationId(),c.actorId());
   mutation.auditSnapshot(c,"QC_SPEC_SCOPE_CHANGED","QcSpecification",r.getId(),before,after,reason,key);return r;
  },row->view(() -> detailView((SpecificationEntity)row)),200);
 }
 @Transactional
 public JsonNode replaceItems(String specificationId,QcCommands.ReplaceItems request,String header,String key){
  var c=mutation.context("qms:specification:edit");long spec=id(specificationId);if(request==null)throw invalid("Request required");long expectedToken=expected(header,request.recordVersion());
  String reason=text(request.reason(),500);if(request.items()==null||request.items().isEmpty())throw invalid("At least one item required");validateItems(request.items());
  return mutation.execute(c,"QC_SPEC_ITEMS:"+spec,key,Map.of("id",specificationId,"recordVersion",expectedToken,"items",request.items(),"reason",reason),()->{
   var r=locked(c,spec,expectedToken);var before=view(() -> detailView(r));
   replaceItems(r,request.items(),c.actorId());db.touchRoot(r,expectedToken,c.actorId());
   var after=view(() -> detailView(r));db.insertRevision(revision(r,c.actorId(),"UPDATE",reason,before,after),c.organizationId(),c.actorId());
   mutation.auditSnapshot(c,"QC_SPEC_ITEMS_CHANGED","QcSpecification",r.getId(),before,after,reason,key);return r;
  },row->view(() -> detailView((SpecificationEntity)row)),200);
 }
 @Transactional
 public JsonNode delete(String specificationId,QcCommands.Remove request,String header,String key){
  var c=mutation.context("qms:specification:delete");long spec=id(specificationId);if(request==null)throw invalid("Request required");long expectedToken=expected(header,request.recordVersion());
  String reason=text(request.reason(),500);
  return mutation.execute(c,"QC_SPEC_DELETE:"+spec,key,Map.of("id",specificationId,"recordVersion",expectedToken,"reason",reason),()->{
   var r=locked(c,spec,expectedToken);var before=view(() -> detailView(r));
   var after=json.createObjectNode().put("id",r.getId().toString()).put("deleted",true);
   db.insertRevision(revision(r,c.actorId(),"DELETE",reason,before,null),c.organizationId(),c.actorId());
   for(var scope:db.scopes(r.getOrgId(),r.getId()))db.deleteScope(r.getOrgId(),scope.getId());
   for(var item:db.items(r.getOrgId(),r.getId()))db.deleteItem(r.getOrgId(),item.getId());
   db.deleteRoot(r.getOrgId(),r.getId());
   mutation.auditSnapshot(c,"QC_SPEC_DELETED","QcSpecification",r.getId(),before,after,reason,key);
    return r;
  },node->project(json.createObjectNode().put("id",specificationId).put("deleted",true)),200);
 }
 /** Locks the aggregate row, enforcing the optimistic token before any evidence is written. */
 private SpecificationEntity locked(CurrentPlatformContext c,long spec,long expectedToken){var r=db.lockRoot(c.organizationId(),spec);if(!java.util.Objects.equals(r.getVersionNo(),expectedToken))throw conflict("STALE_RECORD","Record was changed by another user");return r;}
 private static Map<String,Object> canonicalise(String code,String name,String basisType,String basisReference,QcCommands.Create request,String reason){var m=new LinkedHashMap<String,Object>();m.put("specificationCode",code);m.put("specificationName",name);m.put("basisType",basisType);m.put("basisReference",basisReference);m.put("effectiveDate",request.effectiveDate()==null?null:request.effectiveDate().toString());m.put("remark",request.remark());m.put("materialIds",request.materialIds()==null?List.of():request.materialIds());m.put("productIds",request.productIds()==null?List.of():request.productIds());m.put("items",request.items()==null?List.of():request.items());m.put("reason",reason);return m;}
 /** Applicability rows in canonical subject order, rejecting a duplicated or malformed subject. */
 private static List<String[]> scopes(List<String> materialIds,List<String> productIds){var out=new ArrayList<String[]>();for(Long m:ids(materialIds,"Materials",500))out.add(new String[]{"MATERIAL",m.toString()});for(Long p:ids(productIds,"Products",500))out.add(new String[]{"PRODUCT",p.toString()});return List.copyOf(out);}
 private static Long firstMaterial(List<String[]> wanted){for(var s:wanted)if("MATERIAL".equals(s[0]))return Long.valueOf(s[1]);return null;}
 private void verifyScopes(long org,List<String[]> wanted){for(var s:wanted){if("MATERIAL".equals(s[0])){try{materials.snapshot(org,Long.parseLong(s[1]));}catch(NoSuchElementException e){throw new QcNotFoundException();}}else{try{products.getObject().product(org,Long.parseLong(s[1]),false);}catch(NoSuchElementException e){throw new QcNotFoundException();}}}}
 private void replaceScopes(SpecificationEntity r,List<String[]> wanted,long actor){Map<String,SpecificationScopeEntity> existing=new HashMap<>();for(var e:db.scopes(r.getOrgId(),r.getId()))existing.put(e.getSubjectType()+":"+e.getSubjectId(),e);Set<String> retained=new HashSet<>();for(var s:wanted){String token=s[0]+":"+s[1];retained.add(token);if(existing.containsKey(token))continue;var e=new SpecificationScopeEntity();e.setSpecificationId(r.getId());e.setSubjectType(s[0]);e.setSubjectId(Long.valueOf(s[1]));db.insertScope(e,r.getOrgId(),actor);}for(var e:existing.values())if(!retained.contains(e.getSubjectType()+":"+e.getSubjectId()))db.deleteScope(r.getOrgId(),e.getId());}
 private void replaceItems(SpecificationEntity r,List<QcCommands.Item> wanted,long actor){var old=db.items(r.getOrgId(),r.getId());Map<String,SpecificationItemEntity> existing=new HashMap<>();for(var e:old)existing.put(e.getItemCode().toUpperCase(Locale.ROOT),e);Set<Long> retained=new HashSet<>();for(var line:wanted){String code=text(line.itemCode(),100);var e=existing.get(code.toUpperCase(Locale.ROOT));boolean fresh=e==null;if(fresh){e=new SpecificationItemEntity();e.setSpecificationId(r.getId());e.setItemCode(code);}e.setItemName(text(line.itemName(),200));e.setRequired(line.required());e.setResultType(line.resultType());e.setLowerLimit(decimal(line.lowerLimit()));e.setUpperLimit(decimal(line.upperLimit()));e.setUnitId(line.unitId()==null?null:id(line.unitId()));e.setTextAcceptanceCriteria(optionalText(line.textAcceptanceCriteria(),1000));e.setMethodCode(text(line.methodCode(),100));e.setMethodVersion(text(line.methodVersion(),50));if(fresh)db.insertItem(e,r.getOrgId(),actor);else db.updateItem(e,actor);retained.add(e.getId());}for(var e:old)if(!retained.contains(e.getId()))db.deleteItem(r.getOrgId(),e.getId());}
 private SpecificationRevisionEntity revision(SpecificationEntity r,long actor,String type,String reason,JsonNode before,JsonNode after){var e=new SpecificationRevisionEntity();e.setSpecificationId(r.getId());e.setRevisionNo(db.nextRevisionNo(r.getOrgId(),r.getId()));e.setRevisionType(type);e.setRevisionReason(reason);e.setBeforeJson(before==null?null:before.toString());e.setAfterJson(after==null?null:after.toString());return e;}
 private JsonNode project(JsonNode tree){var n=(ObjectNode)tree;for(String f:List.of("id","orgId","createdBy","updatedBy","specificationItemId","unitId","scopeId","subjectId"))if(n.hasNonNull(f))n.put(f,n.get(f).asText());return n;}
 private JsonNode view(java.util.function.Supplier<?> value){return project(json.valueToTree(value.get()));}
 private QcViews.SpecificationSummary snapshotView(SpecificationEntity r){return new QcViews.SpecificationSummary(r.getId().toString(),r.getOrgId().toString(),String.valueOf(r.getCreatedBy()),time(r.getCreatedAt()),String.valueOf(r.getUpdatedBy()),time(r.getUpdatedAt()),r.getVersionNo(),r.getSpecificationCode(),r.getSpecificationName(),r.getBasisType(),r.getBasisReference(),date(r.getEffectiveDate()),r.getRemark(),r.getStatus(),scopeViews(r));}
 private QcViews.SpecificationDetail detailView(SpecificationEntity r){var items=new ArrayList<QcViews.Item>();for(var i:db.items(r.getOrgId(),r.getId()))items.add(itemNode(i));var history=new ArrayList<QcViews.Revision>();for(var v:db.revisions(r.getOrgId(),r.getId(),20))history.add(new QcViews.Revision(String.valueOf(v.getRevisionNo()),v.getRevisionType(),v.getRevisionReason(),String.valueOf(v.getCreatedBy()),time(v.getCreatedAt())));return new QcViews.SpecificationDetail(r.getId().toString(),r.getOrgId().toString(),String.valueOf(r.getCreatedBy()),time(r.getCreatedAt()),String.valueOf(r.getUpdatedBy()),time(r.getUpdatedAt()),r.getVersionNo(),r.getSpecificationCode(),r.getSpecificationName(),r.getBasisType(),r.getBasisReference(),date(r.getEffectiveDate()),r.getRemark(),r.getStatus(),scopeViews(r),items,history);}
 private QcViews.Item itemNode(SpecificationItemEntity i){return new QcViews.Item(i.getId().toString(),i.getItemCode(),i.getItemName(),Boolean.TRUE.equals(i.getRequired()),i.getResultType(),plain(i.getLowerLimit()),plain(i.getUpperLimit()),i.getUnitId()==null?null:i.getUnitId().toString(),i.getTextAcceptanceCriteria(),i.getMethodCode(),i.getMethodVersion());}
 private List<QcViews.Scope> scopeViews(SpecificationEntity r){var out=new ArrayList<QcViews.Scope>();for(var s:db.scopes(r.getOrgId(),r.getId())){String code=null,name=null;if("MATERIAL".equals(s.getSubjectType())){try{var m=materials.snapshot(r.getOrgId(),s.getSubjectId());code=m.path("materialCode").asText();name=m.path("materialName").asText();}catch(NoSuchElementException e){throw new QcNotFoundException();}}else{var p=products.getObject().product(r.getOrgId(),s.getSubjectId(),false);code=p.path("productCode").asText();name=p.path("productName").asText();}out.add(new QcViews.Scope(s.getId().toString(),s.getSubjectType(),s.getSubjectId().toString(),code,name));}return List.copyOf(out);}
 private static String plain(java.math.BigDecimal v){return v==null?null:v.toPlainString();}
 private static String date(LocalDate d){return d==null?null:d.toString();}
 private static String time(LocalDateTime t){return t==null?null:t.toInstant(ZoneOffset.UTC).toString();}
}
