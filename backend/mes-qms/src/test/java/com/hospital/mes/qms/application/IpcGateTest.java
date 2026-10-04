package com.hospital.mes.qms.application;
import com.hospital.mes.qms.infrastructure.*;
import com.hospital.mes.execution.application.QualityOperationQueryService;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.common.exception.ComplianceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class IpcGateTest {
 @Test void optionalOriginalFailBlocksEvenWhenAllRequiredDefinitionsAreAbsent() throws Exception {
  var instances=mock(IpcInstanceMapper.class);var results=mock(IpcResultMapper.class);var reviews=mock(IpcReviewMapper.class);var signatures=mock(SignedRecordSupport.class);var json=new ObjectMapper();
  var instance=new IpcInstanceEntity();instance.setId(20L);instance.setCurrentResultRevisionId(21L);var original=new IpcResultEntity();original.setId(21L);original.setIpcInstanceId(20L);original.setResultConclusion("FAIL");
  when(instances.selectList(any())).thenReturn(java.util.List.of(instance));when(results.selectOne(any())).thenReturn(original);when(reviews.selectOne(any())).thenReturn(null);
  var service=new IpcService(instances,results,reviews,mock(QualityOperationQueryService.class),mock(MasterMutation.class),signatures,json);
  assertThatThrownBy(()->service.requireComplete(1,10,json.readTree("{\"ipcDefinitions\":[]}"))).isInstanceOfSatisfying(ComplianceException.class,ex->assertThat(ex.code()).isEqualTo("IPC_VALID_FAIL_BLOCKING"));
 }
 @Test void pendingRequiredInstanceCannotBeBypassedByAnEmptyCompletionRule() throws Exception {
  var instances=mock(IpcInstanceMapper.class);when(instances.selectList(any())).thenReturn(java.util.List.of());var json=new ObjectMapper();
  var service=new IpcService(instances,mock(IpcResultMapper.class),mock(IpcReviewMapper.class),mock(QualityOperationQueryService.class),mock(MasterMutation.class),mock(SignedRecordSupport.class),json);
  assertThatThrownBy(()->service.requireComplete(1,10,json.readTree("{\"ipcDefinitions\":[{\"ipcCode\":\"PH\",\"required\":true}]}"))).isInstanceOfSatisfying(ComplianceException.class,ex->assertThat(ex.code()).isEqualTo("IPC_REQUIRED_PENDING"));
 }
}
