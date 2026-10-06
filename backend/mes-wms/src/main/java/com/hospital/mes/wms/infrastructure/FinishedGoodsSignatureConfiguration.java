package com.hospital.mes.wms.infrastructure;
import com.hospital.mes.audit.signature.SignableObjectProvider;
import com.hospital.mes.masterdata.application.SignedRecordSupport;
import com.hospital.mes.wms.application.FinishedGoodsService;
import org.springframework.context.annotation.*;
@Configuration @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class FinishedGoodsSignatureConfiguration {
 @Bean SignableObjectProvider finishedReceiptSignature(org.springframework.beans.factory.ObjectProvider<FinishedGoodsService> services){return SignedRecordSupport.provider("FINISHED_WAREHOUSE_RECEIPT",(org,id)->services.getObject().envelopes("FINISHED_WAREHOUSE_RECEIPT",org,id));}
 @Bean SignableObjectProvider finishedShipmentSignature(org.springframework.beans.factory.ObjectProvider<FinishedGoodsService> services){return SignedRecordSupport.provider("FINISHED_SHIPMENT",(org,id)->services.getObject().envelopes("FINISHED_SHIPMENT",org,id));}
}
