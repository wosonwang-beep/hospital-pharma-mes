package com.hospital.mes.qc.domain;
import java.time.Instant;
import java.util.List;
public record SpecificationSnapshot(long specificationId,String specificationCode,String specificationName,long materialId,long specificationVersionId,int versionNoBusiness,String contentHash,long approvalSignatureId,long approvedBy,Instant approvedAt,List<SpecificationItemSnapshot> items){public SpecificationSnapshot{items=List.copyOf(items);}}
