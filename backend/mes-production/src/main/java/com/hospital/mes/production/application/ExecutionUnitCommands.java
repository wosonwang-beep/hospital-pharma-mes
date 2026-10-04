package com.hospital.mes.production.application;
import com.hospital.mes.production.infrastructure.*;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.masterdata.application.MasterMutation;
import org.springframework.transaction.annotation.*;
import java.util.List;
@org.springframework.stereotype.Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ExecutionUnitCommands {
 private final ProductionStore db;private final MasterMutation mutations;private final ProductionViews views;
 public ExecutionUnitCommands(ProductionStore db,MasterMutation mutations,ProductionViews views){this.db=db;this.mutations=mutations;this.views=views;}
 @Transactional(propagation=Propagation.MANDATORY) public void operationStarted(CurrentPlatformContext c,long execution,String reason,String key){var e=db.executions.lock(c.organizationId(),execution);if(!"IN_PROGRESS".equals(e.getStatus())){var before=views.view(e);e.setStatus("IN_PROGRESS");db.executions.update(e,e.getVersionNo(),c.actorId(),List.of("status"));mutations.auditSnapshot(c,"ExecutionUnit:START","ExecutionUnit",e.getId(),before,views.view(e),reason,key);}if(e.getSubBatchId()!=null){var sub=db.subs.lock(c.organizationId(),e.getSubBatchId());if(!"IN_PROGRESS".equals(sub.getStatus())){var before=views.view(sub);sub.setStatus("IN_PROGRESS");db.subs.update(sub,sub.getVersionNo(),c.actorId(),List.of("status"));mutations.auditSnapshot(c,"SubBatch:START","SubBatch",sub.getId(),before,views.view(sub),reason,key);}}}
 @Transactional(propagation=Propagation.MANDATORY) public void allOperationsCompleted(CurrentPlatformContext c,long execution,String reason,String key){var e=db.executions.lock(c.organizationId(),execution);var before=views.view(e);e.setStatus("COMPLETED");db.executions.update(e,e.getVersionNo(),c.actorId(),List.of("status"));mutations.auditSnapshot(c,"ExecutionUnit:COMPLETE","ExecutionUnit",e.getId(),before,views.view(e),reason,key);if(e.getSubBatchId()!=null){var sub=db.subs.lock(c.organizationId(),e.getSubBatchId());var subBefore=views.view(sub);sub.setStatus("COMPLETED");db.subs.update(sub,sub.getVersionNo(),c.actorId(),List.of("status"));mutations.auditSnapshot(c,"SubBatch:COMPLETE","SubBatch",sub.getId(),subBefore,views.view(sub),reason,key);}}
 @Transactional(propagation=Propagation.MANDATORY) public void runningOperationsPaused(CurrentPlatformContext c,long execution,String reason,String key){var e=db.executions.lock(c.organizationId(),execution);var before=views.view(e);e.setStatus("PAUSED");db.executions.update(e,e.getVersionNo(),c.actorId(),List.of("status"));mutations.auditSnapshot(c,"ExecutionUnit:PAUSE","ExecutionUnit",e.getId(),before,views.view(e),reason,key);}
}
