package com.hospital.mes.qc.application;

import com.hospital.mes.qc.infrastructure.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class QcSubjectHistoryTest {
 @Test void productHistoryCannotEnterMaterialReader() {
  var db=mock(QcStore.class);var definitions=mock(QcDefinitions.class);
  var root=new SpecificationEntity();root.setSubjectType("PRODUCT");root.setProductId(42L);
  var version=version("RETIRED",2);when(db.version(1,9,false)).thenReturn(version);when(db.root(1,3)).thenReturn(root);
  assertThatThrownBy(()->new QcSpecificationQueryService(db,definitions).getHistorical(1,9)).hasMessageContaining("material reader");
  verifyNoInteractions(definitions);
 }
 @Test void retiredProductUsesDedicatedHistoryProjection() {
  var db=mock(QcStore.class);var definitions=mock(QcDefinitions.class);
  var root=new SpecificationEntity();root.setSubjectType("PRODUCT");root.setProductId(42L);
  var version=version("RETIRED",2);when(db.version(1,9,false)).thenReturn(version);when(db.root(1,3)).thenReturn(root);
  new QcSpecificationQueryService(db,definitions).getProductHistorical(1,42,9);
  verify(definitions).requireValid(version);verify(definitions).productSnapshot(version);verify(definitions,never()).snapshot(any());
 }
 @Test void historicalProductAndSchemaMustMatch() {
  var db=mock(QcStore.class);var definitions=mock(QcDefinitions.class);
  var root=new SpecificationEntity();root.setSubjectType("PRODUCT");root.setProductId(42L);
  var version=version("APPROVED",2);when(db.version(1,9,false)).thenReturn(version);when(db.root(1,3)).thenReturn(root);
  var service=new QcSpecificationQueryService(db,definitions);
  assertThatThrownBy(()->service.getProductHistorical(1,43,9)).hasMessageContaining("same product");
  version.setDefinitionSchemaVersion(1);
  assertThatThrownBy(()->service.getProductHistorical(1,42,9)).hasMessageContaining("schema 2");
  verifyNoInteractions(definitions);
 }
 private SpecificationVersionEntity version(String status,int schema){var v=new SpecificationVersionEntity();v.setSpecificationId(3L);v.setStatus(status);v.setDefinitionSchemaVersion(schema);return v;}
}
