package com.hospital.mes.printing;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.configuration.SamplingPrintProvider;
import com.hospital.mes.masterdata.application.MasterMutation;
import com.hospital.mes.masterdata.infrastructure.UnitEntity;
import com.hospital.mes.masterdata.infrastructure.UnitMapper;
import com.hospital.mes.qms.application.IncomingMaterialPort;
import com.hospital.mes.qms.application.IncomingQualityService;
import com.hospital.mes.system.infrastructure.SysUserEntity;
import com.hospital.mes.system.infrastructure.SysUserMapper;
import com.hospital.mes.wms.infrastructure.ContainerEntity;
import com.hospital.mes.wms.infrastructure.LocationEntity;
import com.hospital.mes.wms.infrastructure.MaterialLotEntity;
import com.hospital.mes.wms.infrastructure.ReceiptItemEntity;
import com.hospital.mes.wms.infrastructure.WmsStore;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SamplingPrintProviderTest {
 private final ObjectMapper json=new ObjectMapper();
 private IncomingQualityService quality;
 private IncomingMaterialPort materials;
 private WmsStore wms;
 private MasterMutation mutations;
 private UnitMapper units;
 private SysUserMapper users;
 private SamplingPrintProvider provider;

 @BeforeEach void setup(){
  quality=mock(IncomingQualityService.class);materials=mock(IncomingMaterialPort.class);wms=mock(WmsStore.class,RETURNS_DEEP_STUBS);
  mutations=mock(MasterMutation.class);units=mock(UnitMapper.class);users=mock(SysUserMapper.class);
  when(mutations.context("qms:sampling:view")).thenReturn(new CurrentPlatformContext(1,20,Set.of("QC"),Set.of("qms:sampling:view"),"sampling-print","sampling-print"));
  provider=new SamplingPrintProvider(quality,materials,wms,mutations,units,users,json);
 }

 @Test void declaresCompleteControlledSamplingDictionaryAndNaturalNonFactExample(){
  assertThat(provider.businessType()).isEqualTo("SAMPLING_RECORD");
  assertThat(provider.businessReadPermissions()).containsExactly("qms:sampling:view");
  assertThat(provider.fields()).contains("reportNo","modeLabel","samplingTaskNo","requestNo","materialName","lotNo","manufacturerName","manufacturerLotNo","requestedDate","batchQuantity","batchUnit","plannedSamplingQuantity","plannedSamplingUnit","samplingStatus","sampledAt","sampledBy","completionSignatureReference","warehouseSignatureReference","items");
  assertThat(provider.itemFields()).contains("sequence","containerNo","location","actualQuantity","unit","testSampleQuantity","retentionSampleQuantity","packageResealed","detailSampledAt","detailSampledBy");
  assertThat(provider.example().get("modeLabel")).isEqualTo("构造样张，非真实业务事实");
  assertThat(provider.example().values()).allSatisfy(value->{if(value instanceof String text)assertThat(text).doesNotContainIgnoringCase("demo").doesNotContainIgnoringCase("test").doesNotContain("测试","模拟","演示");});
 }

 @Test void authorizeReadUsesSamplingViewPolicy(){
  provider.authorizeRead("41");
  verify(quality).get("qms_sampling_task","qms:sampling:view","41");
 }

 @Test void plannedTaskPrintsExplicitIncompleteStateAndNeverInventsSignatures(){
  ObjectNode task=task("PLANNED");
  when(quality.get("qms_sampling_task","qms:sampling:view","41")).thenReturn(task);
  when(quality.get("qms_inspection_request","qms:sampling:view","51")).thenReturn(request());
  facts();

  var snapshot=provider.load("41",false);

  assertThat(snapshot.reportNo()).isEqualTo("ST-261019-001");
  assertThat(snapshot.data()).containsEntry("modeLabel","执行前取样计划（未取样）").containsEntry("samplingStatus","未取样").containsEntry("sampledAt","未记录").containsEntry("sampledBy","未记录").containsEntry("completionSignatureReference","未记录").containsEntry("warehouseSignatureReference","未记录");
  assertThat(snapshot.data().get("items")).asList().isEmpty();
  assertThatThrownBy(()->provider.load("41",true)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("业务记录");
 }

 @Test void completedTaskProjectsOnlyStoredSamplingOutboundAndSignatureFacts(){
  ObjectNode task=task("COMPLETED");
  task.put("completedAt","2026-10-19T09:30:00").put("warehouseConfirmedAt","2026-10-19T09:35:00");
  task.putArray("signatureEvidence").addObject().put("objectType","SAMPLING_TASK").put("signatureId","701");
  task.withArray("signatureEvidence").addObject().put("objectType","SAMPLING_WAREHOUSE_OUTBOUND").put("signatureId","702");
  var detail=task.putArray("details").addObject().put("detailNo",1).put("containerNo","原料桶 A-01").put("sourceLocationId","81").put("sourceContainerId","91").put("sampleQuantity","1.000000").put("unitId","61").put("sampledAt","2026-10-19T09:28:00").put("sampledBy","21").put("packageResealed",true);
  detail.putArray("samples").addObject().put("sampleType","TEST_SAMPLE").put("quantity","0.800000");
  detail.withArray("samples").addObject().put("sampleType","RETENTION_SAMPLE").put("quantity","0.200000");
  when(quality.get("qms_sampling_task","qms:sampling:view","41")).thenReturn(task);
  when(quality.get("qms_inspection_request","qms:sampling:view","51")).thenReturn(request());
  facts();
  var location=new LocationEntity();location.setLocationCode("RAW-A-01");location.setLocationName("原辅料库 A 区一号位");when(wms.location().get(1,81)).thenReturn(location);
  var container=new ContainerEntity();container.setContainerCode("CNT-A-01");when(wms.container().get(1,91)).thenReturn(container);
  var sampler=new SysUserEntity();sampler.setDisplayName("李明");when(users.selectById(21L)).thenReturn(sampler);

  var snapshot=provider.load("41",false);

  assertThat(snapshot.data()).containsEntry("modeLabel","已填写取样事实（业务记录）").containsEntry("completionSignatureReference","701").containsEntry("warehouseSignatureReference","702").containsEntry("sampledBy","李明");
  assertThat(snapshot.data().get("items")).asList().singleElement().asInstanceOf(MAP)
   .containsEntry("location","RAW-A-01 / 原辅料库 A 区一号位").containsEntry("containerNo","原料桶 A-01 / CNT-A-01").containsEntry("actualQuantity",new BigDecimal("1.000000")).containsEntry("testSampleQuantity",new BigDecimal("0.800000")).containsEntry("retentionSampleQuantity",new BigDecimal("0.200000")).containsEntry("packageResealed","已复封");
 }

 private ObjectNode task(String status){var n=json.createObjectNode();n.put("id","41").put("versionNo",4).put("samplingTaskNo","ST-261019-001").put("inspectionRequestId","51").put("plannedSamplingQuantity","1.000000").put("plannedUnitId","61").put("status",status).put("assignedTo","21");n.putArray("details");n.putArray("signatureEvidence");return n;}
 private ObjectNode request(){return json.createObjectNode().put("id","51").put("requestNo","IR-261019-001").put("materialLotId","71").put("requestedDate","2026-10-19");}
 private void facts(){
  when(materials.read(1,71)).thenReturn(new IncomingMaterialPort.LotFacts(71,31,101,111,121,131,new BigDecimal("100.000000"),61,4,true,true,true,true,"QUARANTINE","BLOCKED",LocalDate.of(2027,10,19),null,2));
  var lot=new MaterialLotEntity();lot.setLotNo("LOT-26101901");lot.setMaterialSnapshotJson("{\"materialName\":\"盐酸利多卡因\"}");lot.setReceiptItemId(111L);when(wms.materialLot().get(1,71)).thenReturn(lot);
  var item=new ReceiptItemEntity();item.setManufacturerLotNo("MFG-261001");item.setSourceSnapshotJson("{\"manufacturerName\":\"华东原料药有限公司\"}");when(wms.receiptItem().get(1,111)).thenReturn(item);
  var unit=new UnitEntity();unit.setUnitName("千克");when(units.selectOne(any())).thenReturn(unit);
  var sampler=new SysUserEntity();sampler.setDisplayName("李明");when(users.selectById(21L)).thenReturn(sampler);
 }
}
