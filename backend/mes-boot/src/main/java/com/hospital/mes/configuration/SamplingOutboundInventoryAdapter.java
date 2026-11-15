package com.hospital.mes.configuration;

import com.hospital.mes.qms.application.SamplingOutboundInventoryPort;
import com.hospital.mes.wms.application.WmsSamplingOutboundService;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class SamplingOutboundInventoryAdapter implements SamplingOutboundInventoryPort {
 private final WmsSamplingOutboundService service;
 public SamplingOutboundInventoryAdapter(WmsSamplingOutboundService service){this.service=service;}
 public List<Result> outbound(long org,long actor,List<Line> lines){return service.outbound(org,actor,lines.stream().map(x->new WmsSamplingOutboundService.Line(x.samplingDetailId(),x.materialLotId(),x.sourceLocationId(),x.sourceContainerId(),x.unitId(),x.quantity())).toList()).stream().map(x->new Result(x.detailId(),x.ledgerId(),x.locationId(),x.containerId(),x.unitId(),x.quantity())).toList();}
}
