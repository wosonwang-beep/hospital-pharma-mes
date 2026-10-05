package com.hospital.mes.qms.infrastructure;
import com.hospital.mes.audit.signature.SignableObjectProvider;
import com.hospital.mes.masterdata.application.SignedRecordSupport;
import com.hospital.mes.qms.application.ProductionQualityPlanService;
import org.springframework.context.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@Configuration @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ProductionQualitySignatureConfiguration {
 @Bean SignableObjectProvider balanceInvestigationSignature(org.springframework.beans.factory.ObjectProvider<com.hospital.mes.qms.application.MaterialBalanceService> balances){return SignedRecordSupport.provider("BALANCE_INVESTIGATION",(org,id)->balances.getObject().envelopes(org,id));}
 @Bean SignableObjectProvider productionQualityPlanSignature(org.springframework.beans.factory.ObjectProvider<ProductionQualityPlanService> plans){return SignedRecordSupport.provider("PRODUCTION_QUALITY_PLAN",(org,id)->plans.getObject().envelopes(org,id));}
}
