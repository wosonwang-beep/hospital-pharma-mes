package com.hospital.mes.configuration;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.masterdata.application.MasterMutation;
import com.hospital.mes.masterdata.infrastructure.UnitEntity;
import com.hospital.mes.masterdata.infrastructure.UnitMapper;
import com.hospital.mes.qms.application.IncomingMaterialPort;
import com.hospital.mes.qms.application.IncomingQualityService;
import com.hospital.mes.reporting.application.PrintDataProvider;
import com.hospital.mes.reporting.application.PrintField;
import com.hospital.mes.system.infrastructure.SysUserMapper;
import com.hospital.mes.wms.infrastructure.WmsStore;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Read-only projection of the existing incoming sampling task and its frozen WMS lineage. */
@Component
@ConditionalOnProperty(prefix="spring.datasource",name="url")
public class SamplingPrintProvider implements PrintDataProvider {
 private static final String PERMISSION="qms:sampling:view";
 private final IncomingQualityService quality;
 private final IncomingMaterialPort materials;
 private final WmsStore wms;
 private final MasterMutation mutations;
 private final UnitMapper units;
 private final SysUserMapper users;
 private final ObjectMapper json;

 public SamplingPrintProvider(IncomingQualityService quality,IncomingMaterialPort materials,WmsStore wms,MasterMutation mutations,UnitMapper units,SysUserMapper users,ObjectMapper json){this.quality=quality;this.materials=materials;this.wms=wms;this.mutations=mutations;this.units=units;this.users=users;this.json=json;}
 public String businessType(){return "SAMPLING_RECORD";}
 public String moduleLabel(){return "质量管理";}
 public String documentLabel(){return "取样单";}
 public Set<String> businessReadPermissions(){return Set.of(PERMISSION);}

 public List<PrintField> fieldDefinitions(){return List.of(
  field("reportNo","取样任务号","单据基本信息",false),field("modeLabel","打印标识","打印信息",false),field("samplingTaskNo","取样任务号","单据基本信息",false),field("requestNo","请验单号","单据基本信息",false),
  field("sampleName","样品名称","物料与批次",false),field("materialName","物料名称","物料与批次",false),field("lotNo","内部批号","物料与批次",false),field("manufacturerName","生产企业","物料与批次",false),field("manufacturerLotNo","生产批号","物料与批次",false),
  field("requestedDate","请验日期","计划信息",false),number("batchQuantity","批量","计划信息",false,"batchUnit"),field("batchUnit","批量单位","计划信息",false),number("plannedSamplingQuantity","计划取样量","计划信息",false,"plannedSamplingUnit"),field("plannedSamplingUnit","计划取样单位","计划信息",false),
  field("samplingStatus","取样状态","执行信息",false),field("sampledAt","取样日期","执行信息",false),field("sampledBy","取样人","执行信息",false),field("submittedAt","提交仓库时间","执行信息",false),field("warehouseConfirmedAt","仓库确认时间","执行信息",false),
  field("completionSignatureReference","取样签名证据编号","签名证据",false),field("warehouseSignatureReference","仓库确认签名证据编号","签名证据",false),
  field("sequence","序号","取样明细",true),field("containerNo","容器","取样明细",true),field("location","库位","取样明细",true),number("actualQuantity","实取量","取样明细",true,"unit"),field("unit","单位","取样明细",true),number("testSampleQuantity","检验样","取样明细",true,"unit"),number("retentionSampleQuantity","留样","取样明细",true,"unit"),field("packageResealed","复封状态","取样明细",true),field("detailSampledAt","取样时间","取样明细",true),field("detailSampledBy","取样人","取样明细",true)
 );}
 private PrintField field(String key,String label,String group,boolean repeated){return new PrintField(key,label,group,"来自现有取样、请验、物料批、样品分配或签名证据的只读事实",label+"示例",repeated);}
 private PrintField number(String key,String label,String group,boolean repeated,String unitField){return new PrintField(key,label,group,"来自现有业务记录的十进制数量","1.000000",repeated,"NUMBER",unitField);}
 public Set<String> fields(){var result=new HashSet<String>();fieldDefinitions().stream().filter(f->!f.repeated()).forEach(f->result.add(f.key()));result.add("items");return Set.copyOf(result);}
 public Set<String> itemFields(){var result=new HashSet<String>();fieldDefinitions().stream().filter(PrintField::repeated).forEach(f->result.add(f.key()));return Set.copyOf(result);}
 public Map<String,Object> example(){
  var data=new LinkedHashMap<String,Object>();data.put("reportNo","SM-261019-001");data.put("modeLabel","构造样张，非真实业务事实");data.put("samplingTaskNo","SM-261019-001");data.put("requestNo","IR-261019-001");data.put("sampleName","盐酸利多卡因");data.put("materialName","盐酸利多卡因");data.put("lotNo","LOT-26101901");data.put("manufacturerName","华东原料药有限公司");data.put("manufacturerLotNo","MFG-261001");data.put("requestedDate","2026-10-19");data.put("batchQuantity",new BigDecimal("100.000000"));data.put("batchUnit","千克");data.put("plannedSamplingQuantity",new BigDecimal("1.000000"));data.put("plannedSamplingUnit","千克");data.put("samplingStatus","未取样");data.put("sampledAt","未记录");data.put("sampledBy","未记录");data.put("submittedAt","未记录");data.put("warehouseConfirmedAt","未记录");data.put("completionSignatureReference","未记录");data.put("warehouseSignatureReference","未记录");
  var item=new LinkedHashMap<String,Object>();item.put("sequence",1);item.put("containerNo","原料桶 A-01");item.put("location","原辅料库 A 区一号位");item.put("actualQuantity",new BigDecimal("1.000000"));item.put("unit","千克");item.put("testSampleQuantity",new BigDecimal("0.800000"));item.put("retentionSampleQuantity",new BigDecimal("0.200000"));item.put("packageResealed","已复封");item.put("detailSampledAt","2026-10-19 09:28");item.put("detailSampledBy","李明");data.put("items",List.of(Map.copyOf(item)));return Map.copyOf(data);
 }

 public void authorizeRead(String id){quality.get("qms_sampling_task",PERMISSION,id);}

 @Transactional(readOnly=true)
 public PrintSnapshot load(String id,boolean formal){
  if(formal)throw new IllegalArgumentException("取样单按当前业务记录打印，不作为正式质量审批报告");
  var c=mutations.context(PERMISSION);
  JsonNode task=quality.get("qms_sampling_task",PERMISSION,id);
  JsonNode request=quality.get("qms_inspection_request",PERMISSION,text(task,"inspectionRequestId"));
  long lotId=Long.parseLong(text(request,"materialLotId"));
  var facts=materials.read(c.organizationId(),lotId);
  var lot=wms.materialLot().get(c.organizationId(),lotId);
  var receiptItem=wms.receiptItem().get(c.organizationId(),facts.receiptItemId());
  JsonNode material=parse(lot.getMaterialSnapshotJson());
  JsonNode source=parse(receiptItem.getSourceSnapshotJson());
  var details=new ArrayList<Map<String,Object>>();var samplers=new LinkedHashSet<String>();var sampledTimes=new ArrayList<String>();int sequence=0;
  for(var detail:task.path("details")){
   String sampler=name(id(detail,"sampledBy"));if(!"未记录".equals(sampler))samplers.add(sampler);String sampledAt=shown(detail,"sampledAt");if(!"未记录".equals(sampledAt))sampledTimes.add(sampledAt);
   var item=new LinkedHashMap<String,Object>();item.put("sequence",++sequence);item.put("containerNo",container(c.organizationId(),detail));item.put("location",location(c.organizationId(),detail));item.put("actualQuantity",decimal(detail,"sampleQuantity"));item.put("unit",unit(c.organizationId(),id(detail,"unitId")));item.put("testSampleQuantity",sampleTotal(detail,"TEST_SAMPLE"));item.put("retentionSampleQuantity",sampleTotal(detail,"RETENTION_SAMPLE"));item.put("packageResealed",detail.hasNonNull("packageResealed")?(detail.path("packageResealed").asBoolean()?"已复封":"未复封"):"未记录");item.put("detailSampledAt",sampledAt);item.put("detailSampledBy",sampler);details.add(Map.copyOf(item));
  }
  String status=text(task,"status");var data=new LinkedHashMap<String,Object>();String taskNo=text(task,"samplingTaskNo");data.put("reportNo",taskNo);data.put("samplingTaskNo",taskNo);data.put("modeLabel",details.isEmpty()?"执行前取样计划（未取样）":"COMPLETED".equals(status)?"已填写取样事实（业务记录）":"已填写取样事实（未完成）");data.put("requestNo",shown(request,"requestNo"));String materialName=value(material,"materialName");data.put("sampleName",blank(text(task,"otherSampleName"))?materialName:text(task,"otherSampleName"));data.put("materialName",materialName);data.put("lotNo",Objects.toString(lot.getLotNo(),"未记录"));data.put("manufacturerName",value(source,"manufacturerName"));data.put("manufacturerLotNo",Objects.toString(receiptItem.getManufacturerLotNo(),"未记录"));data.put("requestedDate",shown(request,"requestedDate"));data.put("batchQuantity",facts.quantity());data.put("batchUnit",unit(c.organizationId(),facts.unitId()));data.put("plannedSamplingQuantity",decimal(task,"plannedSamplingQuantity"));data.put("plannedSamplingUnit",unit(c.organizationId(),id(task,"plannedUnitId")));data.put("samplingStatus",status(status));data.put("sampledAt",sampledTimes.isEmpty()?"未记录":String.join("、",sampledTimes));data.put("sampledBy",samplers.isEmpty()?"未记录":String.join("、",samplers));data.put("submittedAt",shown(task,"submittedAt"));data.put("warehouseConfirmedAt",shown(task,"warehouseConfirmedAt"));data.put("completionSignatureReference",signature(task,"SAMPLING_TASK"));data.put("warehouseSignatureReference",signature(task,"SAMPLING_WAREHOUSE_OUTBOUND"));data.put("items",List.copyOf(details));return new PrintSnapshot(Long.toString(task.path("versionNo").asLong()),taskNo,Map.copyOf(data));
 }

 private JsonNode parse(String raw){if(raw==null||raw.isBlank())return json.createObjectNode();try{return json.readTree(raw);}catch(Exception e){throw new IllegalStateException("受控快照不可读",e);}}
 private String unit(long org,Long id){if(id==null)return "未记录";var row=units.selectOne(new QueryWrapper<UnitEntity>().eq("org_id",org).eq("id",id));return row==null?"未记录":Objects.toString(row.getUnitName(),"未记录");}
 private String name(Long id){if(id==null)return "未记录";var user=users.selectById(id);return user==null?"未记录":Objects.toString(user.getDisplayName(),"未记录");}
 private String location(long org,JsonNode detail){Long id=id(detail,"sourceLocationId");if(id==null)return "未记录";var row=wms.location().get(org,id);return join(row.getLocationCode(),row.getLocationName());}
 private String container(long org,JsonNode detail){String recorded=shown(detail,"containerNo");Long id=id(detail,"sourceContainerId");if(id==null)return recorded;var row=wms.container().get(org,id);return join("未记录".equals(recorded)?null:recorded,row.getContainerCode());}
 private static String join(String first,String second){if(blank(first))return blank(second)?"未记录":second;if(blank(second)||Objects.equals(first,second))return first;return first+" / "+second;}
 private static String signature(JsonNode task,String type){for(var evidence:task.path("signatureEvidence"))if(type.equals(evidence.path("objectType").asText())&&evidence.hasNonNull("signatureId"))return evidence.path("signatureId").asText();return "未记录";}
 private static BigDecimal sampleTotal(JsonNode detail,String type){BigDecimal total=BigDecimal.ZERO;for(var sample:detail.path("samples"))if(type.equals(sample.path("sampleType").asText())&&sample.hasNonNull("quantity"))total=total.add(decimal(sample,"quantity"));return total;}
 private static BigDecimal decimal(JsonNode node,String key){if(!node.hasNonNull(key))return BigDecimal.ZERO;var value=node.path(key);return value.isNumber()?value.decimalValue():new BigDecimal(value.asText());}
 private static Long id(JsonNode node,String key){return node.hasNonNull(key)&&!node.path(key).asText().isBlank()?Long.valueOf(node.path(key).asText()):null;}
 private static String text(JsonNode node,String key){return node.path(key).asText("");}
 private static String shown(JsonNode node,String key){String value=text(node,key);return blank(value)?"未记录":value;}
 private static String value(JsonNode node,String key){return shown(node,key);}
 private static boolean blank(String value){return value==null||value.isBlank();}
 private static String status(String value){return switch(value){case "PLANNED"->"未取样";case "IN_PROGRESS"->"取样中";case "PENDING_WAREHOUSE"->"待仓库确认";case "COMPLETED"->"已取样";default->blank(value)?"未记录":value;};}
}
