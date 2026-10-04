package com.hospital.mes.ebr.application;
import com.hospital.mes.audit.signature.*;
import com.hospital.mes.common.exception.*;
import com.hospital.mes.ebr.infrastructure.*;
import java.util.*;
@org.springframework.stereotype.Component @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class EbrReviewSignableProvider implements SignableObjectProvider {
 private final EbrReviewIntent intents;private final EbrRuntimeStore db;private final EbrRuntimeModel model;
 public EbrReviewSignableProvider(EbrReviewIntent intents,EbrRuntimeStore db,EbrRuntimeModel model){this.intents=intents;this.db=db;this.model=model;}
 public String objectType(){return "EBR_REVIEW_RECORD";}public Set<SignatureMeaning> allowedMeanings(){return Set.of(SignatureMeaning.VERIFY,SignatureMeaning.APPROVE);}
 public SignableObject loadForSignature(long org,String id){var intent=intents.current();if(intent!=null&&intent.org()==org&&intent.object().objectId().equals(id))return intent.object();String[] p=id.split(":");if(p.length!=3)throw new IllegalArgumentException("Review identity required");long form=com.hospital.mes.masterdata.application.MasterMutation.id(p[0]);for(var row:db.rows(RuntimeReviewEntity.class,org,"object_id",form)){var e=model.parse(row.getSignatureEvidenceJson());if(e.path("objectId").asText().equals(id)){List<String> evidence=new ArrayList<>();e.path("evidenceIds").forEach(x->evidence.add(x.asText()));return new SignableObject(objectType(),id,row.getFormRevision(),e.path("canonicalRecord"),evidence);}}throw new NoSuchElementException("Review evidence not found");}
 public void validateSignable(SignatureValidationContext c,SignableObject object){var i=intents.current();if(i==null||i.org()!=c.platform().organizationId()||i.actor()!=c.platform().actorId()||i.meaning()!=c.meaning()||!i.object().equals(object))throw new PermissionException("PERMISSION_DENIED","Use authorized eBR review command");}
}
