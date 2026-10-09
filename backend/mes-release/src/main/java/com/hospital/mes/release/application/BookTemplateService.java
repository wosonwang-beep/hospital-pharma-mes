package com.hospital.mes.release.application;
import com.fasterxml.jackson.databind.*;import com.fasterxml.jackson.databind.node.*;
import com.hospital.mes.masterdata.application.*;import com.hospital.mes.process.application.*;import com.hospital.mes.ebr.application.*;import com.hospital.mes.production.application.*;import com.hospital.mes.reporting.application.*;import com.hospital.mes.common.exception.*;
import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.jdbc.support.*;import org.springframework.transaction.annotation.Transactional;
import java.util.*;import org.springframework.transaction.annotation.Propagation;

@org.springframework.stereotype.Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class BookTemplateService implements ProductionBookPort {
 private final JdbcTemplate db;private final ObjectMapper json;private final MasterMutation mutations;private final ProcessQueryService process;private final EbrQueryService ebr;private final PrintService print;
 public BookTemplateService(JdbcTemplate db,ObjectMapper json,MasterMutation mutations,ProcessQueryService process,EbrQueryService ebr,PrintService print){this.db=db;this.json=json;this.mutations=mutations;this.process=process;this.ebr=ebr;this.print=print;}

 public JsonNode list(){var c=mutations.context("ebr:template:view");var out=json.createArrayNode();for(var row:db.queryForList("SELECT * FROM ebr_book_template WHERE org_id=? ORDER BY id DESC LIMIT 200",c.organizationId()))out.add(view(row));return out;}
 public JsonNode get(String id){var c=mutations.context("ebr:template:view");return view(row(c.organizationId(),MasterMutation.id(id),false));}

 @Transactional public JsonNode create(String code,JsonNode definition,String reason){
  var c=mutations.context("ebr:template:create");
  if(code==null||!code.matches("[A-Za-z][A-Za-z0-9_-]{0,63}"))throw new IllegalArgumentException("Invalid template code");
  reason(reason);var d=BookDefinition.parse(definition,json);requireCurrentBinding(d);
  db.queryForList("SELECT id FROM ebr_book_template WHERE org_id=? AND template_code=? FOR UPDATE",c.organizationId(),code);
  Integer revision=db.queryForObject("SELECT COALESCE(MAX(revision_no),0)+1 FROM ebr_book_template WHERE org_id=? AND template_code=?",Integer.class,c.organizationId(),code);
  var key=new GeneratedKeyHolder();
  db.update(conn->{var s=conn.prepareStatement("INSERT INTO ebr_book_template(org_id,template_code,revision_no,status,definition_json,definition_hash,created_by) VALUES(?,?,?,'DRAFT',?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);s.setLong(1,c.organizationId());s.setString(2,code);s.setInt(3,revision);s.setString(4,definition.toString());s.setString(5,mutations.digest(definition));s.setLong(6,c.actorId());return s;},key);
  long id=Objects.requireNonNull(key.getKey()).longValue();var result=view(row(c.organizationId(),id,false));mutations.auditSnapshot(c,"EBR_BOOK_TEMPLATE_CREATE","EbrBookTemplate",id,null,result,reason,null);return result;
 }

 @Transactional public JsonNode transition(String id,String action,long expected,String reason){
  var c=mutations.context("ebr:template:publish");reason(reason);long target=MasterMutation.id(id);var r=row(c.organizationId(),target,true);
  if(((Number)r.get("version_no")).longValue()!=expected)throw new ResourceConflictException("VERSION_CONFLICT","Electronic batch record template changed");
  var before=view(r);String next;
  if(action.equals("PUBLISH")&&r.get("status").equals("DRAFT")){
   var d=BookDefinition.parse(parse(r.get("definition_json")),json);requireCurrentBinding(d);validate(c.organizationId(),d);
   for(var other:db.queryForList("SELECT * FROM ebr_book_template WHERE org_id=? AND status='PUBLISHED' AND id<>? FOR UPDATE",c.organizationId(),target)){
    var od=BookDefinition.parse(parse(other.get("definition_json")),json);
    if(overlaps(d,od))throw new ComplianceException("BOOK_MAPPING_CONFLICT","Deactivate the previous electronic batch record template before publishing an overlapping process binding");
   }
   next="PUBLISHED";
  }else if(action.equals("DEACTIVATE")&&r.get("status").equals("PUBLISHED"))next="INACTIVE";
  else throw new ComplianceException("BOOK_TRANSITION_INVALID","Invalid electronic batch record template transition");
  db.update("UPDATE ebr_book_template SET status=?,published_by=COALESCE(published_by,?),published_at=COALESCE(published_at,CURRENT_TIMESTAMP(3)),version_no=version_no+1 WHERE org_id=? AND id=? AND version_no=?",next,c.actorId(),c.organizationId(),target,expected);
  var result=view(row(c.organizationId(),target,false));mutations.auditSnapshot(c,"EBR_BOOK_TEMPLATE_"+action,"EbrBookTemplate",target,before,result,reason,null);return result;
 }

 private void requireCurrentBinding(BookDefinition d){
  if(d.modern()){
   if(d.processPackageId()==null||d.ebrTemplateVersionId()==null)throw new ComplianceException("BOOK_CURRENT_PROCESS_REQUIRED","New electronic batch record templates must bind a production process and its effective process-form template");
   return;
  }
  if(d.mappings().stream().anyMatch(m->m.processPackageId()==null))throw new ComplianceException("BOOK_CURRENT_PROCESS_REQUIRED","Legacy draft mappings must bind the current production process before publication");
 }
 private boolean overlaps(BookDefinition a,BookDefinition b){
  if(a.modern()&&b.modern())return a.processPackageId().equals(b.processPackageId())&&a.ebrTemplateVersionId().equals(b.ebrTemplateVersionId());
  if(a.modern())return b.mappings().stream().anyMatch(m->a.processPackageId().equals(m.processPackageId())&&a.ebrTemplateVersionId().equals(m.ebrTemplateVersionId()));
  if(b.modern())return a.mappings().stream().anyMatch(m->b.processPackageId().equals(m.processPackageId())&&b.ebrTemplateVersionId().equals(m.ebrTemplateVersionId()));
  return a.mappings().stream().anyMatch(m->b.mappings().stream().anyMatch(o->m.productId().equals(o.productId())&&m.processKey().equals(o.processKey())&&m.ebrTemplateVersionId().equals(o.ebrTemplateVersionId())));
 }

 public void validate(long org,BookDefinition d){
  if(d.modern()){
   var p=process.requireCurrentIdentified(org,MasterMutation.id(d.processPackageId()));
   var e=ebr.requirePublished(org,MasterMutation.id(d.ebrTemplateVersionId()));
   if(!e.path("processPackageId").asText().equals(d.processPackageId()))throw new ComplianceException("BOOK_MAPPING_INVALID","Production process and process-form template mismatch");
   validateEntries(org,d,p,e,new HashMap<>());
   return;
  }
  Map<String,JsonNode> portable=new HashMap<>();
  for(var m:d.mappings()){
   var p=m.processPackageId()!=null?process.requireCurrentIdentified(org,MasterMutation.id(m.processPackageId())):process.requireUsableIdentified(org,MasterMutation.id(m.packageVersionId()));
   var e=ebr.requirePublished(org,MasterMutation.id(m.ebrTemplateVersionId()));
   if(!p.path("productId").asText().equals(m.productId())||!e.path(m.processPackageId()!=null?"processPackageId":"packageVersionId").asText().equals(m.processPackageId()!=null?m.processPackageId():m.packageVersionId()))throw new ComplianceException("BOOK_MAPPING_INVALID","Product/process/eBR mapping mismatch");
   validateEntries(org,d,p,e,portable);
  }
 }
 private void validateEntries(long org,BookDefinition d,JsonNode processSnapshot,JsonNode ebrSnapshot,Map<String,JsonNode> portable){
  for(var entry:d.entries())if(entry.kind().equals("FORM")){
   JsonNode f=null;for(var candidate:ebrSnapshot.path("definition").path("forms"))if(candidate.path("formCode").asText().equals(entry.formCode()))f=candidate;
   if(f==null)throw new ComplianceException("BOOK_FORM_MISSING","Mapped process form missing: "+entry.formCode());
   JsonNode op=processSnapshot.hasNonNull("processPackageId")?json.valueToTree(process.requireCurrentOperation(org,MasterMutation.id(processSnapshot.path("processPackageId").asText()),f.path("operationDefId").asLong())):json.valueToTree(process.requireOperation(org,MasterMutation.id(processSnapshot.path("packageVersionId").asText()),f.path("operationDefId").asLong()));
   if(!op.path("operationCode").asText().equals(entry.operationCode()))throw new ComplianceException("BOOK_OPERATION_MISMATCH","Stable operation binding mismatch");
   if(entry.modern()&&(!"PROCESS".equals(entry.flowType())||!entry.flowCode().equals(entry.operationCode())))throw new ComplianceException("BOOK_FLOW_OPERATION_MISMATCH","Process flow entry must use its stable operation code");
   for(String field:entry.fields()){boolean found=false;for(var fd:f.path("fields"))found|=field.equals(fd.path("fieldCode").asText());if(!found)throw new ComplianceException("BOOK_FIELD_MISSING","Unknown stable field "+field);}
   var shape=(ObjectNode)f.deepCopy();shape.remove("operationDefId");var prior=portable.putIfAbsent(entry.formCode(),shape);if(prior!=null&&!prior.equals(shape))throw new ComplianceException("BOOK_SPECIFICATION_SCHEMA_MISMATCH","Shared process form schemas differ");
   if(entry.printTemplateVersionId()!=null)print.requirePublishedTemplate(entry.printTemplateVersionId(),"EBR_PROCESS_FORM");
  }
 }

 @Override @Transactional(propagation=Propagation.MANDATORY)
 public JsonNode freeze(long org,long productId,JsonNode processDefinition,JsonNode ebrDefinition){
  var candidates=new ArrayList<ObjectNode>();
  for(var r:db.queryForList("SELECT * FROM ebr_book_template WHERE org_id=? AND status='PUBLISHED' FOR UPDATE",org)){
   var raw=parse(r.get("definition_json"));if(!mutations.digest(raw).equals(r.get("definition_hash")))throw new ComplianceException("BOOK_TEMPLATE_CORRUPT","Electronic batch record template digest mismatch");
   var d=BookDefinition.parse(raw,json);boolean match=false;JsonNode binding=json.nullNode();
   if(d.modern()){
    match=d.processPackageId().equals(processDefinition.path("processPackageId").asText())&&d.ebrTemplateVersionId().equals(ebrDefinition.path("templateVersionId").asText());
    if(match){var b=json.createObjectNode();b.put("processPackageId",d.processPackageId()).put("ebrTemplateVersionId",d.ebrTemplateVersionId());binding=b;}
   }else{
    for(var m:d.mappings())if(m.productId().equals(Long.toString(productId))&&(m.processPackageId()!=null?m.processPackageId().equals(processDefinition.path("processPackageId").asText()):m.packageVersionId().equals(processDefinition.path("packageVersionId").asText()))&&m.ebrTemplateVersionId().equals(ebrDefinition.path("templateVersionId").asText())){match=true;binding=json.valueToTree(m);break;}
   }
   if(match){var n=json.createObjectNode();n.put("templateVersionId",r.get("id").toString()).put("templateRevision",((Number)r.get("revision_no")).intValue()).put("definitionHash",r.get("definition_hash").toString());n.set("definition",raw);if(d.modern())n.set("binding",binding);else n.set("mapping",binding);n.set("batchFormCodes",json.valueToTree(d.batchFormCodes().stream().sorted().toList()));candidates.add(n);}
  }
  if(candidates.size()>1)throw new ComplianceException("BOOK_MAPPING_CONFLICT","Ambiguous published electronic batch record template binding");
  return candidates.isEmpty()?json.nullNode():candidates.getFirst();
 }

 private Map<String,Object> row(long org,long id,boolean lock){var rows=db.queryForList("SELECT * FROM ebr_book_template WHERE org_id=? AND id=?"+(lock?" FOR UPDATE":""),org,id);if(rows.isEmpty())throw new NoSuchElementException("Electronic batch record template not found");return rows.getFirst();}
 private ObjectNode view(Map<String,Object> r){var n=json.createObjectNode();n.put("id",r.get("id").toString()).put("templateCode",r.get("template_code").toString()).put("revision",((Number)r.get("revision_no")).intValue()).put("status",r.get("status").toString()).put("versionNo",((Number)r.get("version_no")).longValue()).put("definitionHash",r.get("definition_hash").toString()).put("createdBy",r.get("created_by").toString());n.set("definition",parse(r.get("definition_json")));return n;}
 private JsonNode parse(Object s){try{return json.readTree(s.toString());}catch(Exception e){throw new IllegalStateException("Invalid retained electronic batch record template JSON",e);}}
 private static void reason(String s){if(s==null||s.isBlank()||s.length()>1000)throw new IllegalArgumentException("Reason required");}
}
