package com.hospital.mes.qc.application;
import com.hospital.mes.audit.signature.*;
import java.util.Set;
@org.springframework.stereotype.Component @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class QcApprovalSignableProvider implements SignableObjectProvider {
 private final QcDefinitions definitions;
 public QcApprovalSignableProvider(QcDefinitions definitions){this.definitions=definitions;}
 public String objectType(){return QcDefinitions.type("APPROVE");}
 public Set<SignatureMeaning> allowedMeanings(){return Set.of(SignatureMeaning.APPROVE);}
 public SignableObject loadForSignature(long org,String id){return definitions.signable(org,id,"APPROVE");}
 public void validateSignable(SignatureValidationContext c,SignableObject o){definitions.validateSigning(c,o,"APPROVE");}
}
