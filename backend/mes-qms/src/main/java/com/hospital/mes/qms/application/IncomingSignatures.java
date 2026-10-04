package com.hospital.mes.qms.application;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.signature.*;
import com.hospital.mes.qms.infrastructure.*;
import com.hospital.mes.common.exception.ComplianceException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
import static com.hospital.mes.qms.infrastructure.IncomingStore.*;
@Component @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class IncomingSignatures {
 public record Slot(String table,String prefix){}
 public static final Map<String,Slot> SLOTS=Map.ofEntries(Map.entry("SAMPLING_PLAN",new Slot("qms_sampling_task","plan")),Map.entry("SAMPLING_TASK",new Slot("qms_sampling_task","completion")),Map.entry("SAMPLE_DISPOSAL",new Slot("qms_sample","disposal")),Map.entry("TEST_RESULT_REVISION",new Slot("qms_test_result_revision","")),Map.entry("INSPECTION_TASK",new Slot("qms_inspection_task","review")),Map.entry("DEVIATION_DECISION",new Slot("qms_deviation","decision")),Map.entry("DEVIATION_CLOSE",new Slot("qms_deviation","close")),Map.entry("INSPECTION_REPORT",new Slot("qms_inspection_report","approval")),Map.entry("MATERIAL_RELEASE_DECISION",new Slot("qms_release_decision","")));
 public record Intent(long org,long actor,SignatureMeaning meaning,SignableObject object){}
 public record Evidence(long signatureId,String envelope){}
 private static final ThreadLocal<Intent> INTENT=new ThreadLocal<>();
 private final IncomingStore db;private final ObjectMapper json;
 public IncomingSignatures(IncomingStore db,ObjectMapper json){this.db=db;this.json=json;}
 public static String idField(Slot s){return s.prefix().isEmpty()?"signatureId":s.prefix()+"SignatureId";}
 public static String evidenceField(Slot s){return s.prefix().isEmpty()?"signatureEvidenceJson":s.prefix()+"SignatureEvidenceJson";}
 public <T>T withIntent(CurrentPlatformContext c,SignatureMeaning meaning,SignableObject object,java.util.function.Supplier<T> work){if(INTENT.get()!=null)throw new IllegalStateException("Nested signing intent");INTENT.set(new Intent(c.organizationId(),c.actorId(),meaning,object));try{return work.get();}finally{INTENT.remove();}}
 public SignableObject load(String type,long org,String objectId){var active=INTENT.get();if(active!=null&&active.org()==org&&active.object().objectType().equals(type)&&active.object().objectId().equals(objectId))return active.object();Slot slot=SLOTS.get(type);List<IncomingRow> candidates;
  if(type.equals("TEST_RESULT_REVISION")){String[] p=objectId.split(":");if(p.length!=2)throw new NoSuchElementException("Signing target not found");candidates=db.rows(slot.table(),org,"testExecutionId",Long.valueOf(p[0]));}
  else if(type.equals("MATERIAL_RELEASE_DECISION")){String[] p=objectId.split(":");if(p.length!=2)throw new NoSuchElementException("Signing target not found");candidates=db.rows(slot.table(),org,"materialLotId",Long.valueOf(p[0]));}
  else candidates=List.of(db.get(slot.table(),org,Long.parseLong(objectId)));
  for(var row:candidates){String raw=text(row,evidenceField(slot));if(raw==null)continue;try{SignableObject saved=json.readValue(raw,SignableObject.class);if(saved.objectType().equals(type)&&saved.objectId().equals(objectId))return saved;}catch(java.io.IOException e){throw new IllegalStateException("Corrupt immutable signature envelope",e);}}
  throw new NoSuchElementException("Signing evidence not found");
 }
 public void validate(String type,SignatureValidationContext c,SignableObject object){Intent i=INTENT.get();if(i==null||i.org()!=c.platform().organizationId()||i.actor()!=c.platform().actorId()||i.meaning()!=c.meaning()||!i.object().equals(object)||!type.equals(object.objectType())||!c.platform().hasPermission("ebr:sign"))throw new ComplianceException("SIGNATURE_INTENT_REQUIRED","Sign through the authorized incoming command");}
 public String encode(SignableObject object){try{return json.writeValueAsString(object);}catch(Exception e){throw new IllegalStateException(e);}}
 public ObjectNode metadata(String type,IncomingRow row){Slot slot=SLOTS.get(type);Long id=number(row,idField(slot));if(id==null)return null;try{var saved=json.readValue(text(row,evidenceField(slot)),SignableObject.class);var n=json.createObjectNode();n.put("slot",slot.prefix());n.put("signatureId",id.toString());n.put("objectType",type);n.put("objectId",saved.objectId());n.put("recordVersion",saved.recordVersion());n.put("meaning",saved.canonicalRecord().path("meaning").asText());return n;}catch(Exception e){throw new IllegalStateException("Corrupt signature envelope",e);}}
}
