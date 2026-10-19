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
 // Read immutable historical authorization evidence; printing never writes new evidence.
 public Long artifactRevision(long org,long id){var e=artifacts.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PrintArtifactSourceEvidence>().eq("org_id",org).eq("artifact_id",id));return e==null?null:e.getSourceRevisionId();}
 public Long batchRevision(long org,long id){var e=batches.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PrintBatchSourceEvidence>().eq("org_id",org).eq("batch_id",id));return e==null?null:e.getSourceRevisionId();}
}
