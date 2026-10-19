package com.hospital.mes.configuration;
import org.springframework.context.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import com.hospital.mes.qc.application.QcProductReferencePort;
import com.hospital.mes.process.application.ProcessQueryService;
@Configuration @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class QcProductReferenceConfiguration {
 @Bean QcProductReferencePort qcProductReferences(ProcessQueryService products){return products::productReference;}
}
