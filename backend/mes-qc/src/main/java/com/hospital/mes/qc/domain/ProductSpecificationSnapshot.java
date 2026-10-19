package com.hospital.mes.qc.domain;
import java.time.Instant;
import java.util.List;
/** Separate V2 shape: never add nullable product fields to the signed legacy material shape. */
public record ProductSpecificationSnapshot(int definitionSchemaVersion,String subjectType,long productId,long specificationId,String specificationCode,String specificationName,long specificationVersionId,int versionNoBusiness,String contentHash,long approvalSignatureId,long approvedBy,Instant approvedAt,List<SpecificationItemSnapshot> items){public ProductSpecificationSnapshot{items=List.copyOf(items);}}
