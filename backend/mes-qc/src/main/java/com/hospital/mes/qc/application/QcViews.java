package com.hospital.mes.qc.application;
import java.util.List;
public final class QcViews {
 private QcViews(){}
 public record Item(String specificationItemId,String itemCode,String itemName,boolean required,String resultType,String lowerLimit,String upperLimit,String unitId,String textAcceptanceCriteria,String methodCode,String methodVersion){}
 public record SpecificationSummary(String id,String orgId,String createdBy,String createdAt,String updatedBy,String updatedAt,long versionNo,String materialId,String materialCode,String materialName,String specificationCode,String specificationName){}
 public record SpecificationDetail(String id,String orgId,String createdBy,String createdAt,String updatedBy,String updatedAt,long versionNo,String materialId,String materialCode,String materialName,String specificationCode,String specificationName,List<SpecificationVersionSummary> versions){public SpecificationDetail{versions=List.copyOf(versions);}}
 public record SpecificationVersionSummary(String id,String orgId,String createdBy,String createdAt,String updatedBy,String updatedAt,long versionNo,String specificationId,int versionNoBusiness,String status,String contentHash,String approvedBy,String approvedAt,String approvalSignatureId,String approvalReason,String retiredBy,String retiredAt,String retirementSignatureId,String retirementReason){}
 public record SpecificationVersionDetail(String id,String orgId,String createdBy,String createdAt,String updatedBy,String updatedAt,long versionNo,String specificationId,int versionNoBusiness,String status,String contentHash,String approvedBy,String approvedAt,String approvalSignatureId,String approvalReason,String retiredBy,String retiredAt,String retirementSignatureId,String retirementReason,String specificationCode,String specificationName,String materialId,String materialCode,String materialName,List<Item> items){public SpecificationVersionDetail{items=List.copyOf(items);}}
 public record Page<T>(List<T> items,long total,int page,int size){public Page{items=List.copyOf(items);}}
}
