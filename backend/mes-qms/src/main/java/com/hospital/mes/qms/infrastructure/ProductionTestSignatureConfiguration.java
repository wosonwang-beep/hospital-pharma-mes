package com.hospital.mes.qms.infrastructure;
import com.hospital.mes.audit.signature.SignableObjectProvider;
import com.hospital.mes.masterdata.application.SignedRecordSupport;
import com.hospital.mes.qms.application.ProductionQualityService;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@Configuration @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ProductionTestSignatureConfiguration {
 @Bean SignableObjectProvider productionTestResultSignature(ObjectProvider<ProductionQualityService> service){return SignedRecordSupport.provider("PRODUCTION_TEST_RESULT",(org,id)->service.getObject().envelopes("PRODUCTION_TEST_RESULT",org,id));}
 @Bean SignableObjectProvider productionTestReviewSignature(ObjectProvider<ProductionQualityService> service){return SignedRecordSupport.provider("PRODUCTION_TEST_REVIEW",(org,id)->service.getObject().envelopes("PRODUCTION_TEST_REVIEW",org,id));}
 @Bean SignableObjectProvider productionDeviationSignature(ObjectProvider<ProductionQualityService> service){return SignedRecordSupport.provider("PRODUCTION_DEVIATION",(org,id)->service.getObject().envelopes("PRODUCTION_DEVIATION",org,id));}
 @Bean SignableObjectProvider productionCapaSignature(ObjectProvider<ProductionQualityService> service){return SignedRecordSupport.provider("PRODUCTION_CAPA",(org,id)->service.getObject().envelopes("PRODUCTION_CAPA",org,id));}
}
