package com.hospital.mes.qc.application;
import java.util.List;
/** Read projections of the QC quality standard basic library (DCP-QC-SPEC-BASIC-LIBRARY-001). */
public final class QcViews {
 private QcViews(){}
 /** One inspection item of a standard; the unit id stays a string in every QC projection. */
 public record Item(String specificationItemId,String itemCode,String itemName,boolean required,String resultType,String lowerLimit,String upperLimit,String unitId,String textAcceptanceCriteria,String methodCode,String methodVersion){}
 /** One applicability row: a standard applies to this material or this product. */
 public record Scope(String scopeId,String subjectType,String subjectId,String subjectCode,String subjectName){}
 /** One maintained revision of the basic record, used only to show who changed what and why. */
 public record Revision(String revisionNo,String revisionType,String revisionReason,String revisedBy,String revisedAt){}
 public record SpecificationSummary(String id,String orgId,String createdBy,String createdAt,String updatedBy,String updatedAt,long versionNo,String specificationCode,String specificationName,String basisType,String basisReference,String effectiveDate,String remark,String status,List<Scope> scopes){public SpecificationSummary{scopes=List.copyOf(scopes);}}
 public record SpecificationDetail(String id,String orgId,String createdBy,String createdAt,String updatedBy,String updatedAt,long versionNo,String specificationCode,String specificationName,String basisType,String basisReference,String effectiveDate,String remark,String status,List<Scope> scopes,List<Item> items,List<Revision> revisions){public SpecificationDetail{scopes=List.copyOf(scopes);items=List.copyOf(items);revisions=List.copyOf(revisions);}}
 public record Page<T>(List<T> items,long total,int page,int size){public Page{items=List.copyOf(items);}}
}
