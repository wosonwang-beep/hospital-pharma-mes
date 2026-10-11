package com.hospital.mes.reporting.application;
import com.hospital.mes.reporting.infrastructure.*;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class PrintSourceEvidenceService {
 private final PrintDataSourceService sources;private final PrintArtifactSourceEvidenceMapper artifacts;private final PrintBatchSourceEvidenceMapper batches;
 public PrintSourceEvidenceService(PrintDataSourceService sources,PrintArtifactSourceEvidenceMapper artifacts,PrintBatchSourceEvidenceMapper batches){this.sources=sources;this.artifacts=artifacts;this.batches=batches;}
 public void artifact(CurrentPlatformContext c,PrintTemplateVersion template,PrintArtifact artifact){if(!"CONFIGURED".equals(template.getSourceExecutionMode()))return;var fixed=sources.fixed(c.organizationId(),template.getBusinessType(),template.getSourceRevisionId());var e=new PrintArtifactSourceEvidence();e.setOrgId(c.organizationId());e.setArtifactId(artifact.getId());e.setSourceId(fixed.source().getId());e.setSourceRevisionId(fixed.revision().getId());e.setConfigHash(fixed.revision().getConfigHash());e.setTypeMetadataHash(fixed.typeMetadata().path("metadataHash").asText());e.setCatalogVersion(fixed.revision().getPublishedCatalogVersion());e.setCreatedBy(c.actorId());e.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));artifacts.insert(e);}
 public void batch(CurrentPlatformContext c,PrintTemplateVersion template,PrintBatch batch){if(!"CONFIGURED".equals(template.getSourceExecutionMode()))return;var fixed=sources.fixed(c.organizationId(),template.getBusinessType(),template.getSourceRevisionId());var e=new PrintBatchSourceEvidence();e.setOrgId(c.organizationId());e.setBatchId(batch.getId());e.setSourceId(fixed.source().getId());e.setSourceRevisionId(fixed.revision().getId());e.setConfigHash(fixed.revision().getConfigHash());e.setTypeMetadataHash(fixed.typeMetadata().path("metadataHash").asText());e.setCatalogVersion(fixed.revision().getPublishedCatalogVersion());e.setCreatedBy(c.actorId());e.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));batches.insert(e);}
}
