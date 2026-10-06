package com.hospital.mes.release.application;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.qms.infrastructure.*;
import java.util.*;
@org.springframework.stereotype.Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class FinishedDecisionQuery {
 public static final String TABLE="qms_release_decision";
 private final IncomingStore db;
 public FinishedDecisionQuery(IncomingStore db){this.db=db;}
 public List<ReleaseDecisionRow> rows(long org,long batch){return db.rows(TABLE,org,"mainBatchId",batch).stream().map(x->(ReleaseDecisionRow)x).filter(x->"FINISHED_PRODUCT".equals(x.getReleaseScope())).toList();}
 public List<JsonNode> decisions(long org,long batch){return rows(org,batch).stream().map(this::view).toList();}
 public List<JsonNode> sourceDecisionEvidence(long org,long batch){return rows(org,batch).stream().map(x->(JsonNode)db.view(x)).toList();}
 @Transactional(propagation=Propagation.MANDATORY)
 public ReleaseDecisionRow effectiveCurrent(long org,long batch){return selectEffective(db.lockedRows(TABLE,org,"mainBatchId",batch).stream().map(x->(ReleaseDecisionRow)x).filter(x->"FINISHED_PRODUCT".equals(x.getReleaseScope())).toList());}
 private ReleaseDecisionRow selectEffective(java.util.List<ReleaseDecisionRow> rows){Set<Long> replaced=new HashSet<>();rows.forEach(x->{if(x.getSupersedesDecisionId()!=null)replaced.add(x.getSupersedesDecisionId());});var current=rows.stream().filter(x->!replaced.contains(x.getId())).toList();if(current.size()>1)throw new IllegalStateException("Competing finished release chains");return current.isEmpty()?null:current.getFirst();}
 public ReleaseDecisionRow effective(long org,long batch){var rows=rows(org,batch);Set<Long> superseded=new HashSet<>();rows.forEach(x->{if(x.getSupersedesDecisionId()!=null)superseded.add(x.getSupersedesDecisionId());});var effective=rows.stream().filter(x->!superseded.contains(x.getId())).toList();if(effective.size()>1)throw new IllegalStateException("Competing finished release chains");return effective.isEmpty()?null:effective.getFirst();}
 public JsonNode get(long org,long id){var row=(ReleaseDecisionRow)db.get(TABLE,org,id);if(!"FINISHED_PRODUCT".equals(row.getReleaseScope()))throw new NoSuchElementException("Finished decision not found");return view(row);}
 public JsonNode view(ReleaseDecisionRow row){var n=db.view(row);n.retain("id","orgId","createdBy","createdAt","mainBatchId","finishedLotId","finishedInspectionReportId","releaseScope","decision","releaseBasis","decisionBy","decisionAt","reason","signatureId","supersedesDecisionId");return n;}
 public List<String> envelopes(long org,String target){long batch=Long.parseLong(target.split(":")[0]);return rows(org,batch).stream().map(ReleaseDecisionRow::getSignatureEvidenceJson).filter(Objects::nonNull).toList();}
}
