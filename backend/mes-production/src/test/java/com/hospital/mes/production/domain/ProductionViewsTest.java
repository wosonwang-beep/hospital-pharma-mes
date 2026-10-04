package com.hospital.mes.production.domain;
import static org.assertj.core.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.production.application.ProductionViews;
import com.hospital.mes.production.infrastructure.*;
import org.junit.jupiter.api.Test;

class ProductionViewsTest {
 @Test void apiEvidenceProjectionRetainsInternalAndSignedCanonicalIdentity(){var json=new ObjectMapper();var source=json.createObjectNode();source.putArray("gateEvidence").addObject().put("action","charge-create").put("materialId","25").put("formulaItemId","18");source.putArray("signatureEvidence").addObject().set("canonicalRecord",source.deepCopy());var result=new ProductionViews(json).read(source);assertThat(result.path("gateEvidence").get(0).has("materialId")).isFalse();assertThat(source.path("gateEvidence").get(0).path("materialId").asText()).isEqualTo("25");assertThat(result.path("signatureEvidence").get(0).path("canonicalRecord").path("gateEvidence").get(0).path("materialId").asText()).isEqualTo("25");}
 @Test void closedReadProjectionExcludesPersistenceScopeAndIncludesActions(){var row=new ExecutionUnitEntity();row.setId(8L);row.setOrgId(1L);row.setCreatedBy(2L);row.setUpdatedBy(3L);row.setMainBatchId(4L);row.setVersionNo(0L);var out=new ProductionViews(new ObjectMapper()).view(row);assertThat(out.path("id").asText()).isEqualTo("8");assertThat(out.path("mainBatchId").isTextual()).isTrue();assertThat(out.has("orgId")||out.has("createdBy")||out.has("updatedBy")).isFalse();assertThat(out.path("allowedActions").isArray()).isTrue();}
 @Test void processSnapshotHasOnlyFrozenReadProperties(){var row=new SnapshotEntity();row.setId(1L);row.setSnapshotJson("{\"materials\":[]}");var out=new ProductionViews(new ObjectMapper()).view(row);var names=new java.util.HashSet<String>();out.fieldNames().forEachRemaining(names::add);assertThat(names).containsExactlyInAnyOrder("id","packageVersionId","formulaVersionId","routeVersionId","ebrTemplateVersionId","snapshotHash","snapshot");}
}
