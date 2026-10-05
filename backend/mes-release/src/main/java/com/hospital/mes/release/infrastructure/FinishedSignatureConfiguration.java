package com.hospital.mes.release.infrastructure;
import com.hospital.mes.audit.signature.*;
import com.hospital.mes.masterdata.application.SignedRecordSupport;
import com.hospital.mes.release.application.FinishedDecisionQuery;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.ObjectProvider;
import java.util.Set;
@Configuration @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class FinishedSignatureConfiguration {
 @Bean SignableObjectProvider finishedQaSignature(ObjectProvider<FinishedDecisionQuery> query){var delegate=SignedRecordSupport.provider("QA_RELEASE_DECISION",(org,id)->query.getObject().envelopes(org,id));return new SignableObjectProvider(){public String objectType(){return "QA_RELEASE_DECISION";}public Set<SignatureMeaning> allowedMeanings(){return Set.of(SignatureMeaning.RELEASE,SignatureMeaning.REJECT);}public SignableObject loadForSignature(long org,String id){return delegate.loadForSignature(org,id);}public void validateSignable(SignatureValidationContext c,SignableObject object){delegate.validateSignable(c,object);}};}
}
