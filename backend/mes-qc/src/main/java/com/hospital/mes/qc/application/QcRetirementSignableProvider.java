package com.hospital.mes.qc.application;
import com.hospital.mes.audit.signature.*;
import java.util.Set;
@org.springframework.stereotype.Component @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class QcRetirementSignableProvider implements SignableObjectProvider {
 private final QcDefinitions definitions;
 public QcRetirementSignableProvider(QcDefinitions definitions){this.definitions=definitions;}
 public String objectType(){return QcDefinitions.type("RETIRE");}
 public Set<SignatureMeaning> allowedMeanings(){return Set.of(SignatureMeaning.APPROVE);}
 public SignableObject loadForSignature(long org,String id){return definitions.signable(org,id,"RETIRE");}
 public void validateSignable(SignatureValidationContext c,SignableObject o){definitions.validateSigning(c,o,"RETIRE");}
}
