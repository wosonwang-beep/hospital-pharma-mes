package com.hospital.mes.qc.application;
import com.hospital.mes.qc.domain.*;
import com.hospital.mes.qc.infrastructure.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
/**
 * Read contracts other MES modules use to freeze a QC quality standard into their own document
 * (DCP-QC-SPEC-BASIC-LIBRARY-001). A caller freezes the returned snapshot; it never re-reads the live
 * basic library to prove a historical document, so later maintenance cannot rewrite frozen evidence.
 */
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class QcSpecificationQueryService {
 public static final int SNAPSHOT_SCHEMA_VERSION=3;
 private final QcStore db;
 public QcSpecificationQueryService(QcStore db){this.db=db;}
 /** Selectable standard for an incoming material: ACTIVE and explicitly applicable to that material. */
 @Transactional(propagation=Propagation.MANDATORY)
 public QcStandardSnapshot requireSelectable(long orgId,long materialId,long specificationId){var r=db.root(orgId,specificationId);requireActive(r);requireScope(orgId,r,"MATERIAL",materialId,"QC_SPEC_MATERIAL_MISMATCH","QC specification does not apply to this material");return snapshot(r);}
 /** Selectable standard for a production batch product: ACTIVE and explicitly applicable to that product. */
 @Transactional(propagation=Propagation.MANDATORY)
 public QcStandardSnapshot requireProductSelectable(long orgId,long productId,long specificationId){var r=db.root(orgId,specificationId);requireActive(r);requireScope(orgId,r,"PRODUCT",productId,"QC_SPEC_PRODUCT_MISMATCH","Standard must apply to the batch product");return snapshot(r);}
 /**
  * Current state of a standard already frozen by a downstream document. It deliberately does not require
  * ACTIVE: a document keeps working after the basic record was edited or inactivated, because it holds
  * its own snapshot; only the frozen snapshot proves what the document was built on.
  */
 @Transactional(readOnly=true)
 public QcStandardSnapshot read(long orgId,long specificationId){return snapshot(db.root(orgId,specificationId));}
 /** Live inspection items of a standard, used when a test instance itself must freeze its own copy. */
 @Transactional(readOnly=true)
 public List<SpecificationItemSnapshot> items(long orgId,long specificationId){db.root(orgId,specificationId);return itemSnapshots(orgId,specificationId);}
 private void requireActive(SpecificationEntity r){if(!QcSpecificationRules.ACTIVE.equals(r.getStatus()))throw QcSpecificationRules.conflict("QC_SPEC_NOT_SELECTABLE","QC standard is inactive");}
 private void requireScope(long org,SpecificationEntity r,String type,long subject,String code,String message){for(var s:db.scopes(org,r.getId()))if(type.equals(s.getSubjectType())&&s.getSubjectId()==subject)return;throw QcSpecificationRules.conflict(code,message);}
 private QcStandardSnapshot snapshot(SpecificationEntity r){var materials=new ArrayList<String>();var products=new ArrayList<String>();for(var s:db.scopes(r.getOrgId(),r.getId())){if("MATERIAL".equals(s.getSubjectType()))materials.add(s.getSubjectId().toString());else products.add(s.getSubjectId().toString());}return new QcStandardSnapshot(SNAPSHOT_SCHEMA_VERSION,r.getId(),r.getSpecificationCode(),r.getSpecificationName(),r.getBasisType(),r.getBasisReference(),r.getEffectiveDate(),materials,products,itemSnapshots(r.getOrgId(),r.getId()));}
 private List<SpecificationItemSnapshot> itemSnapshots(long org,long specification){var out=new ArrayList<SpecificationItemSnapshot>();for(var i:db.items(org,specification))out.add(new SpecificationItemSnapshot(i.getId(),i.getItemCode(),i.getItemName(),Boolean.TRUE.equals(i.getRequired()),i.getResultType(),i.getLowerLimit(),i.getUpperLimit(),i.getUnitId(),i.getTextAcceptanceCriteria(),i.getMethodCode(),i.getMethodVersion()));return List.copyOf(out);}
}
