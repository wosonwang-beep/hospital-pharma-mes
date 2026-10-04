package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.time.LocalDateTime;
import java.math.BigDecimal;
@TableName(value="qms_ipc_instance") public class IpcInstanceEntity extends ScopedEntity {
 private Long operationExecutionId;
 public Long getOperationExecutionId(){return operationExecutionId;} public void setOperationExecutionId(Long value){operationExecutionId=value;}
 private String ipcCode;
 public String getIpcCode(){return ipcCode;} public void setIpcCode(String value){ipcCode=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private String result;
 public String getResult(){return result;} public void setResult(String value){result=value;}
 private LocalDateTime completedAt;
 public LocalDateTime getCompletedAt(){return completedAt;} public void setCompletedAt(LocalDateTime value){completedAt=value;}
 private String definitionSnapshotJson;
 public String getDefinitionSnapshotJson(){return definitionSnapshotJson;} public void setDefinitionSnapshotJson(String value){definitionSnapshotJson=value;}
 private Long currentResultRevisionId;
 public Long getCurrentResultRevisionId(){return currentResultRevisionId;} public void setCurrentResultRevisionId(Long value){currentResultRevisionId=value;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}
