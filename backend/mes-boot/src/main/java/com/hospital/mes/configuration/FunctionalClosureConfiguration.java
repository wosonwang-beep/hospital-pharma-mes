package com.hospital.mes.configuration;
import com.hospital.mes.audit.signature.*;
import com.hospital.mes.masterdata.application.SignedRecordSupport;
import com.hospital.mes.wms.application.*;
import com.hospital.mes.wms.infrastructure.*;
import com.hospital.mes.qms.application.IncomingQualityQueryService;
import com.hospital.mes.qms.infrastructure.*;
import com.hospital.mes.execution.infrastructure.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.context.annotation.*;
@Configuration @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class FunctionalClosureConfiguration {
 @Bean InventoryUnfreezeGate inventoryUnfreeze(IncomingQualityQueryService quality){return (org,lot)->{var evidence=quality.evaluate(org,lot,"UNFREEZE",java.time.Instant.now());for(var reason:evidence.path("reasons"))if(!java.util.Set.of("INVENTORY_NOT_AVAILABLE","INVENTORY_FROZEN").contains(reason.asText()))throw new com.hospital.mes.common.exception.ComplianceException("UNFREEZE_GATE_FAILED",reason.asText());};}
 @Bean SignableObjectProvider inventoryDecisionSignature(InventoryDecisionMapper mapper){return SignedRecordSupport.provider("INVENTORY_DECISION",(org,id)->mapper.selectList(new QueryWrapper<InventoryDecisionEntity>().eq("org_id",org).eq("material_lot_id",Long.parseLong(id.split(":")[0]))).stream().map(InventoryDecisionEntity::getSignatureEvidenceJson).toList());}
 @Bean SignableObjectProvider ipcResultSignature(IpcResultMapper mapper){return SignedRecordSupport.provider("IPC_RESULT",(org,id)->mapper.selectList(new QueryWrapper<IpcResultEntity>().eq("org_id",org).eq("ipc_instance_id",Long.parseLong(id.split(":")[0]))).stream().map(IpcResultEntity::getSignatureEvidenceJson).toList());}
 @Bean SignableObjectProvider ipcReviewSignature(IpcReviewMapper mapper){return SignedRecordSupport.provider("IPC_REVIEW",(org,id)->mapper.selectList(new QueryWrapper<IpcReviewEntity>().eq("org_id",org).eq("ipc_result_revision_id",Long.parseLong(id.split(":")[0]))).stream().map(IpcReviewEntity::getSignatureEvidenceJson).toList());}
 @Bean SignableObjectProvider clearanceRecordSignature(ClearanceRecordMapper mapper){return SignedRecordSupport.provider("CLEARANCE_RECORD",(org,id)->mapper.selectList(new QueryWrapper<ClearanceRecordEntity>().eq("org_id",org).eq("operation_execution_id",Long.parseLong(id.split(":")[0]))).stream().map(ClearanceRecordEntity::getSignatureEvidenceJson).toList());}
 @Bean SignableObjectProvider clearanceReviewSignature(ClearanceRecordMapper records,ClearanceReviewMapper reviews){return SignedRecordSupport.provider("CLEARANCE_REVIEW",(org,id)->{var scopes=records.selectList(new QueryWrapper<ClearanceRecordEntity>().eq("org_id",org).eq("operation_execution_id",Long.parseLong(id.split(":")[0]))).stream().map(ClearanceRecordEntity::getId).toList();if(scopes.isEmpty())return java.util.List.of();return reviews.selectList(new QueryWrapper<ClearanceReviewEntity>().eq("org_id",org).in("clearance_record_id",scopes)).stream().map(ClearanceReviewEntity::getSignatureEvidenceJson).toList();});}
}
