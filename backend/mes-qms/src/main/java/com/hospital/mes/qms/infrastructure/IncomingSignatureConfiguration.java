package com.hospital.mes.qms.infrastructure;
import com.hospital.mes.qms.application.IncomingSignatures;
import com.hospital.mes.audit.signature.*;
import org.springframework.context.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.Set;
@Configuration @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class IncomingSignatureConfiguration {
 private SignableObjectProvider provider(IncomingSignatures service,String type){return new SignableObjectProvider(){public String objectType(){return type;}public Set<SignatureMeaning> allowedMeanings(){return type.equals("MATERIAL_RELEASE_DECISION")?Set.of(SignatureMeaning.RELEASE,SignatureMeaning.REJECT,SignatureMeaning.APPROVE):Set.of(type.equals("SAMPLING_TASK")||type.equals("TEST_RESULT_REVISION")||type.equals("INSPECTION_TASK")?SignatureMeaning.VERIFY:SignatureMeaning.APPROVE);}public SignableObject loadForSignature(long org,String id){return service.load(type,org,id);}public void validateSignable(SignatureValidationContext c,SignableObject o){service.validate(type,c,o);}};}
 @Bean public SignableObjectProvider incoming0(IncomingSignatures s){return provider(s,"SAMPLING_PLAN");}
 @Bean public SignableObjectProvider incoming1(IncomingSignatures s){return provider(s,"SAMPLING_TASK");}
 @Bean public SignableObjectProvider incoming2(IncomingSignatures s){return provider(s,"SAMPLE_DISPOSAL");}
 @Bean public SignableObjectProvider incoming3(IncomingSignatures s){return provider(s,"TEST_RESULT_REVISION");}
 @Bean public SignableObjectProvider incoming4(IncomingSignatures s){return provider(s,"INSPECTION_TASK");}
 @Bean public SignableObjectProvider incoming5(IncomingSignatures s){return provider(s,"DEVIATION_DECISION");}
 @Bean public SignableObjectProvider incoming6(IncomingSignatures s){return provider(s,"DEVIATION_CLOSE");}
 @Bean public SignableObjectProvider incoming7(IncomingSignatures s){return provider(s,"INSPECTION_REPORT");}
 @Bean public SignableObjectProvider incoming8(IncomingSignatures s){return provider(s,"MATERIAL_RELEASE_DECISION");}
}
