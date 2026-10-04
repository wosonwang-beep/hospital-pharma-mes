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
 public SpecificationSnapshot requireSelectable(long orgId,long materialId,long specificationVersionId){var v=db.version(orgId,specificationVersionId,true);if(!"APPROVED".equals(v.getStatus()))throw QcSpecificationRules.conflict("QC_SPEC_NOT_SELECTABLE","QC specification version is not selectable");if(db.root(orgId,v.getSpecificationId()).getMaterialId()!=materialId)throw QcSpecificationRules.conflict("QC_SPEC_MATERIAL_MISMATCH","QC specification belongs to another material");definitions.requireValid(v);return definitions.snapshot(v);}
 @Transactional(readOnly=true)
 public SpecificationSnapshot getHistorical(long orgId,long specificationVersionId){var v=db.version(orgId,specificationVersionId,false);if(!java.util.Set.of("APPROVED","RETIRED").contains(v.getStatus()))throw QcSpecificationRules.conflict("QC_SPEC_NOT_SELECTABLE","QC specification version has no approved history");definitions.requireValid(v);return definitions.snapshot(v);}
}
