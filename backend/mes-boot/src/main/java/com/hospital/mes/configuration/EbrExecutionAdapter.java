package com.hospital.mes.configuration;
import com.hospital.mes.ebr.application.EbrExecutionPort;
import com.hospital.mes.execution.application.ExecutionQueryService;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@Component @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class EbrExecutionAdapter implements EbrExecutionPort {
 private final ExecutionQueryService execution;
 public EbrExecutionAdapter(ExecutionQueryService execution){this.execution=execution;}
 public OperationContext operation(long org,long id){return map(execution.operation(org,id));}
 public OperationContext lockOperation(long org,long id){return map(execution.lockOperation(org,id));}
 public List<Long> operationIds(long org,long id){return execution.operationIds(org,id);}
 public List<Long> batchOperationIds(long org,long id){return execution.batchOperationIds(org,id);}
 private OperationContext map(ExecutionQueryService.OperationContext c){return new OperationContext(c.mainBatchId(),c.executionUnitId(),c.operationId(),c.operationDefId(),c.operationStatus(),c.batchStatus(),c.snapshot());}
}
