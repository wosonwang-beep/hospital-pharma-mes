package com.hospital.mes.configuration;
import com.hospital.mes.wms.domain.ReceiptConfirmed;
import com.hospital.mes.qms.application.IncomingQualityService;
import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.audit.idempotency.PlatformIdempotencyService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@Component @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class IncomingReceiptListener {
 private final IncomingQualityService quality;private final CurrentPlatformContextResolver contexts;
 public IncomingReceiptListener(IncomingQualityService quality,CurrentPlatformContextResolver contexts){this.quality=quality;this.contexts=contexts;}
 @EventListener public void confirmed(ReceiptConfirmed event){
  if(contexts.current().organizationId()!=event.organizationId())throw new IllegalStateException("Receipt event organization mismatch");
  for(long lot:event.exemptionLotIds())quality.evaluateExemption(lot,PlatformIdempotencyService.digest(event.idempotencyKey()+":exemption:"+lot),event.directReceive());
 }
}
