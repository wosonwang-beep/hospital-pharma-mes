package com.hospital.mes.qms.application;

import java.math.BigDecimal;
import java.util.List;

/** QMS-owned contract; the Boot adapter invokes WMS without exposing WMS persistence to QMS. */
public interface SamplingOutboundInventoryPort {
 record Line(long samplingDetailId,long materialLotId,Long sourceLocationId,Long sourceContainerId,long unitId,BigDecimal quantity){}
 record Result(long samplingDetailId,long ledgerId,long sourceLocationId,Long sourceContainerId,long unitId,BigDecimal quantity){}
 List<Result> outbound(long organizationId,long actorId,List<Line> lines);
}
