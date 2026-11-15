package com.hospital.mes.execution.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.common.exception.ComplianceException;
import com.hospital.mes.execution.infrastructure.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.masterdata.application.SignedRecordSupport;
import com.hospital.mes.production.application.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.ObjectProvider;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductionQuantityServiceReadOnlyTest {
 private final QuantityEventMapper quantity=mock(QuantityEventMapper.class);
 private final ProductionQueryService batches=mock(ProductionQueryService.class);
 private final ExecutionOutputInventoryPort inventory=mock(ExecutionOutputInventoryPort.class);
 private final ObjectProvider<ExecutionOutputInventoryPort> provider=mock(ObjectProvider.class);
 private final ObjectMapper json=new ObjectMapper();
 private final ExecutionStore store=new ExecutionStore(mock(OperationMapper.class),mock(EquipmentUsageMapper.class),mock(EquipmentRunMapper.class),mock(ParameterValueMapper.class),mock(WeighingMapper.class),mock(ChargeMapper.class),quantity,mock(GenealogyMapper.class));
 private final ProductionQuantityService service=new ProductionQuantityService(store,batches,mock(ProductionService.class),mock(ExecutionQueryService.class),mock(MasterMutation.class),mock(MasterQueryService.class),mock(SignedRecordSupport.class),json,provider);
 private final ExecutionOutputInventoryPort.LogisticsQuantity fact=new ExecutionOutputInventoryPort.LogisticsQuantity(901,"ISSUE",777,new BigDecimal("10.000"),3,LocalDateTime.parse("2026-11-14T08:00:00"));
 @BeforeEach void setup(){when(provider.getObject()).thenReturn(inventory);when(inventory.confirmedLogistics(1,655)).thenReturn(List.of(fact));}
 @Test void missingProjectionIsReportedWithoutSynchronizingOrLocking(){
  when(batches.batch(1,655)).thenReturn(new ProductionQueryService.BatchContext(655,"IN_PROGRESS",json.createObjectNode(),7));
  when(quantity.selectOne(any(QueryWrapper.class))).thenReturn(null);
  assertThat(service.requireLogisticsSynchronized(1,655)).isFalse();
  verify(batches).batch(1,655);verify(batches,never()).lockBatch(anyLong(),anyLong());
  verify(quantity,never()).insert(any(QuantityEventEntity.class));
 }
 @Test void missingProjectionOnFinalBatchKeepsExistingMutationBlocker(){
  when(batches.batch(1,655)).thenReturn(new ProductionQueryService.BatchContext(655,"QA_RELEASED",json.createObjectNode(),7));
  when(quantity.selectOne(any(QueryWrapper.class))).thenReturn(null);
  assertThatThrownBy(()->service.requireLogisticsSynchronized(1,655)).isInstanceOf(ComplianceException.class).extracting(x->((ComplianceException)x).code()).isEqualTo("QUANTITY_BATCH_NOT_MUTABLE");
  verify(quantity,never()).insert(any(QuantityEventEntity.class));
 }
 @Test void exactProjectionIsAccepted(){
  when(batches.batch(1,655)).thenReturn(new ProductionQueryService.BatchContext(655,"IN_PROGRESS",json.createObjectNode(),7));
  when(quantity.selectOne(any(QueryWrapper.class))).thenReturn(existing(655,777,3,"10.000"));
  assertThat(service.requireLogisticsSynchronized(1,655)).isTrue();
  verify(quantity,never()).insert(any(QuantityEventEntity.class));
 }
 @Test void changedProjectionKeepsExistingSourceIntegrityBlocker(){
  when(batches.batch(1,655)).thenReturn(new ProductionQueryService.BatchContext(655,"IN_PROGRESS",json.createObjectNode(),7));
  when(quantity.selectOne(any(QueryWrapper.class))).thenReturn(existing(655,777,3,"9.000"));
  assertThatThrownBy(()->service.requireLogisticsSynchronized(1,655)).isInstanceOf(ComplianceException.class).extracting(x->((ComplianceException)x).code()).isEqualTo("QUANTITY_SOURCE_CHANGED");
  verify(quantity,never()).insert(any(QuantityEventEntity.class));
 }
 private QuantityEventEntity existing(long batch,long lot,long unit,String amount){
  var row=new QuantityEventEntity();row.setMainBatchId(batch);row.setMaterialLotId(lot);row.setUnitId(unit);row.setAmount(new BigDecimal(amount));return row;
 }
}
