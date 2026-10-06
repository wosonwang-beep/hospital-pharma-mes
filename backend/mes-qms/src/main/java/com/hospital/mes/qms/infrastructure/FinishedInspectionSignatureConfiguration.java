package com.hospital.mes.qms.infrastructure;
import com.hospital.mes.audit.signature.SignableObjectProvider;
import com.hospital.mes.masterdata.application.SignedRecordSupport;
import com.hospital.mes.qms.application.FinishedInspectionService;
import org.springframework.context.annotation.*;
@Configuration @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class FinishedInspectionSignatureConfiguration {
 @Bean SignableObjectProvider finishedSamplingSignature(org.springframework.beans.factory.ObjectProvider<FinishedInspectionService> services){return SignedRecordSupport.provider("FINISHED_SAMPLING",(org,id)->services.getObject().envelopes("FINISHED_SAMPLING",org,id));}
 @Bean SignableObjectProvider finishedReportSignature(org.springframework.beans.factory.ObjectProvider<FinishedInspectionService> services){return SignedRecordSupport.provider("FINISHED_INSPECTION_REPORT",(org,id)->services.getObject().envelopes("FINISHED_INSPECTION_REPORT",org,id));}
}
