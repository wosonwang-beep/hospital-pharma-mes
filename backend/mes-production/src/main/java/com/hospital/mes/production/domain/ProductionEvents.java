package com.hospital.mes.production.domain;
import java.util.List;
public final class ProductionEvents {
 private ProductionEvents(){}
 public record BatchReleased(long organizationId,long actorId,long mainBatchId,List<Long> executionUnitIds) {}
}
