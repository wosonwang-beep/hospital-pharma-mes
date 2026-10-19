package com.hospital.mes.qc.application;
import com.hospital.mes.qc.domain.*;
import com.hospital.mes.qc.infrastructure.QcStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class QcSpecificationQueryService {
 private final QcStore db; private final QcDefinitions definitions;
 public QcSpecificationQueryService(QcStore db,QcDefinitions definitions){this.db=db;this.definitions=definitions;}
 @Transactional(propagation=Propagation.MANDATORY)
 public SpecificationSnapshot requireSelectable(long orgId,long materialId,long specificationVersionId){var v=db.version(orgId,specificationVersionId,true);if(!"APPROVED".equals(v.getStatus()))throw QcSpecificationRules.conflict("QC_SPEC_NOT_SELECTABLE","QC specification version is not selectable");if(!"MATERIAL".equals(db.root(orgId,v.getSpecificationId()).getSubjectType())||!java.util.Objects.equals(db.root(orgId,v.getSpecificationId()).getMaterialId(),materialId))throw QcSpecificationRules.conflict("QC_SPEC_MATERIAL_MISMATCH","QC specification belongs to another material");definitions.requireValid(v);return definitions.snapshot(v);}
 @Transactional(propagation=Propagation.MANDATORY)
 public ProductSpecificationSnapshot requireProductSelectable(long org,long product,long version){var v=db.version(org,version,true);var root=db.root(org,v.getSpecificationId());if(!"APPROVED".equals(v.getStatus()))throw QcSpecificationRules.conflict("QC_SPEC_NOT_SELECTABLE","Approved standard required");if(!"PRODUCT".equals(root.getSubjectType())||!java.util.Objects.equals(root.getProductId(),product))throw QcSpecificationRules.conflict("QC_SPEC_PRODUCT_MISMATCH","Standard must belong to the batch product");definitions.requireValid(v);return definitions.productSnapshot(v);}
 @Transactional(readOnly=true)
 public SpecificationSnapshot getHistorical(long orgId,long specificationVersionId){var v=db.version(orgId,specificationVersionId,false);if(!java.util.Set.of("APPROVED","RETIRED").contains(v.getStatus()))throw QcSpecificationRules.conflict("QC_SPEC_NOT_SELECTABLE","QC specification version has no approved history");if(!"MATERIAL".equals(db.root(orgId,v.getSpecificationId()).getSubjectType())||v.getDefinitionSchemaVersion()!=1)throw QcSpecificationRules.conflict("QC_SPEC_SUBJECT_MISMATCH","Historical material reader cannot consume a product standard");definitions.requireValid(v);return definitions.snapshot(v);}
 @Transactional(readOnly=true)
 public ProductSpecificationSnapshot getProductHistorical(long orgId,long productId,long specificationVersionId){var v=db.version(orgId,specificationVersionId,false);var root=db.root(orgId,v.getSpecificationId());if(!java.util.Set.of("APPROVED","RETIRED").contains(v.getStatus()))throw QcSpecificationRules.conflict("QC_SPEC_NOT_SELECTABLE","QC specification version has no approved history");if(!"PRODUCT".equals(root.getSubjectType())||v.getDefinitionSchemaVersion()!=2||!java.util.Objects.equals(root.getProductId(),productId))throw QcSpecificationRules.conflict("QC_SPEC_PRODUCT_MISMATCH","Historical product reader requires the same product and schema 2");definitions.requireValid(v);return definitions.productSnapshot(v);}
}
