package com.hospital.mes.masterdata.domain;
public final class MaterialEvents {
 private MaterialEvents(){}
 public record Created(long organizationId,long materialId){}
 public record Updated(long organizationId,long materialId){}
 public record Disabled(long organizationId,long materialId){}
}
