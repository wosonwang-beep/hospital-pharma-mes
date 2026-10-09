package com.hospital.mes.configuration;
import com.hospital.mes.reporting.application.*;
import com.hospital.mes.qms.application.IncomingQualityService;
import com.hospital.mes.qms.infrastructure.*;
import com.hospital.mes.wms.infrastructure.*;
import com.hospital.mes.system.infrastructure.SysUserMapper;
import com.hospital.mes.masterdata.application.MasterMutation;
import com.hospital.mes.audit.signature.SignatureTransactionService;
import com.hospital.mes.common.exception.ComplianceException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
import static com.hospital.mes.qms.infrastructure.IncomingStore.*;
/** Report adapter: complete selected/original facts, organization scope and existing approval/signature gates. */
@Component @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class InspectionPrintProvider implements PrintDataProvider {
 private final IncomingQualityService quality;private final IncomingStore db;private final MaterialLotMapper lots;private final SysUserMapper users;private final MasterMutation mutations;private final SignatureTransactionService signatures;private final ObjectMapper json;private final com.hospital.mes.masterdata.infrastructure.UnitMapper units;
 public InspectionPrintProvider(IncomingQualityService quality,IncomingStore db,MaterialLotMapper lots,SysUserMapper users,MasterMutation mutations,SignatureTransactionService signatures,ObjectMapper json,com.hospital.mes.masterdata.infrastructure.UnitMapper units){this.quality=quality;this.db=db;this.lots=lots;this.users=users;this.mutations=mutations;this.signatures=signatures;this.json=json;this.units=units;}
 public String moduleLabel(){return "质量管理";}
 public String documentLabel(){return "检验报告";}
 public String businessType(){return "INSPECTION_REPORT";}
 public List<PrintField> fieldDefinitions(){return InspectionSampleTemplate.dictionary();}
 public Set<String> fields(){return InspectionSampleTemplate.fields();}
 public Set<String> itemFields(){return InspectionSampleTemplate.itemFields();}
 public Map<String,Object> example(){return InspectionSampleTemplate.example(8);}
 public void authorizeRead(String id){quality.get("qms_inspection_report","qms:report:view",id);}
 @Transactional public PrintSnapshot load(String id,boolean formal){
  var c=mutations.context("qms:report:view");
  var row=db.lock("qms_inspection_report",c.organizationId(),MasterMutation.id(id));
  var report=quality.view("qms_inspection_report",row);
  boolean approved="APPROVED".equals(text(row,"status"));
  if(formal&&(!approved||!"APPROVED".equals(text(row,"recordStatus"))||number(row,"approvalSignatureId")==null||!signatures.verify(c.organizationId(),number(row,"approvalSignatureId"))))throw new ComplianceException("PRINT_APPROVAL_REQUIRED","正式报告需有效审批及电子签名");
  if(formal)verifyApprovalBinding(row,id);
  var request=db.get("qms_inspection_request",c.organizationId(),number(row,"inspectionRequestId"));
  var lot=lots.selectOne(new QueryWrapper<MaterialLotEntity>().eq("org_id",c.organizationId()).eq("id",number(request,"materialLotId")));
  if(lot==null)throw new ComplianceException("PRINT_SOURCE_MISSING","报告物料批次来源缺失");
  String materialName;try{materialName=json.readTree(lot.getMaterialSnapshotJson()).path("materialName").asText("—");}catch(Exception e){throw new ComplianceException("PRINT_SOURCE_MISSING","物料快照不可读");}
  var items=new ArrayList<Map<String,String>>();var inspectors=new LinkedHashSet<String>();int sequence=0;
  for(var line:report.path("items")){
   var item=db.get("qms_inspection_item",c.organizationId(),Long.parseLong(line.path("inspectionItemId").asText()));
   JsonNode result=line.path("result"),original=line.path("originalResult");
   var execution=db.get("qms_test_execution",c.organizationId(),Long.parseLong(result.path("testExecutionId").asText()));
   String analyst=name(number(execution,"performedBy"));inspectors.add(analyst);
   var data=new InspectionItemData(""+(++sequence),text(item,"itemName"),criteria(item)+unit(c.organizationId(),number(item,"unitId")),value(result)+unit(c.organizationId(),result.hasNonNull("resultUnitId")?Long.parseLong(result.path("resultUnitId").asText()):null),result.path("resultConclusion").asText("—"),analyst,result.path("recordedAt").asText("—"),value(original)+unit(c.organizationId(),original.hasNonNull("resultUnitId")?Long.parseLong(original.path("resultUnitId").asText()):null)+" / "+original.path("resultConclusion").asText("—"));
   items.add(data.toMap());
  }
  var data=new InspectionReportData(text(row,"reportNo"),""+version(row),formal?"正式报告":"草稿 · 非正式报告",text(request,"requestNo"),materialName,lot.getLotNo(),text(row,"overallResult"),formal?name(number(row,"approvedBy")):"未作为正式审批签署",formal?String.valueOf(IncomingStore.value(row,"approvedAt")):"—",formal?String.valueOf(number(row,"approvalSignatureId")):"无（草稿）",String.join("、",inspectors),items);
  return new PrintSnapshot(data.businessVersion(),data.reportNo(),data.toMap());
 }
 private void verifyApprovalBinding(IncomingRow row,String id){try{
  var evidence=json.readValue(text(row,"approvalSignatureEvidenceJson"),com.hospital.mes.audit.signature.SignableObject.class);var canonical=evidence.canonicalRecord();
  if(!"INSPECTION_REPORT".equals(evidence.objectType())||!Long.toString(MasterMutation.id(id)).equals(evidence.objectId())||evidence.recordVersion()!=version(row)-1||!"APPROVE".equals(canonical.path("meaning").asText())||!text(row,"reportNo").equals(canonical.path("reportNo").asText())||!String.valueOf(number(row,"inspectionRequestId")).equals(canonical.path("inspectionRequestId").asText())||!text(row,"overallResult").equals(canonical.path("overallResult").asText())||!text(row,"evidenceDigest").equals(canonical.path("evidenceDigest").asText()))throw new IllegalArgumentException();
 }catch(Exception e){throw new ComplianceException("PRINT_APPROVAL_BINDING_INVALID","现有批准签名与报告身份、版本或证据不匹配");}}
 private String unit(long org,Long id){if(id==null)return "";var u=units.selectOne(new QueryWrapper<com.hospital.mes.masterdata.infrastructure.UnitEntity>().eq("org_id",org).eq("id",id));if(u==null)throw new ComplianceException("PRINT_SOURCE_MISSING","受控结果单位缺失");return " "+u.getUnitName();}
 private String name(Long id){if(id==null)return "—";var user=users.selectById(id);return user==null?"人员ID "+id:user.getDisplayName()+"（ID "+id+"）";}
 private static String value(JsonNode result){return result.hasNonNull("resultNumeric")?result.path("resultNumeric").asText():result.path("resultText").asText("—");}
 private static String criteria(IncomingRow row){String text=IncomingStore.text(row,"textAcceptanceCriteria");if(text!=null&&!text.isBlank())return text;Object low=IncomingStore.value(row,"lowerLimit"),high=IncomingStore.value(row,"upperLimit");return (low==null?"无下限":low.toString())+" ～ "+(high==null?"无上限":high.toString());}
 public record InspectionItemData(String sequence,String itemName,String criteria,String result,String conclusion,String recordedBy,String recordedAt,String originalResult){Map<String,String> toMap(){return Map.of("sequence",sequence,"itemName",itemName,"criteria",criteria,"result",result,"conclusion",conclusion,"recordedBy",recordedBy,"recordedAt",recordedAt,"originalResult",originalResult);}}
 public record InspectionReportData(String reportNo,String businessVersion,String modeLabel,String requestNo,String materialName,String lotNo,String overallResult,String approvedBy,String approvedAt,String signatureReference,String inspectors,List<Map<String,String>> items){Map<String,Object> toMap(){var m=new LinkedHashMap<String,Object>();m.put("reportNo",reportNo);m.put("businessVersion",businessVersion);m.put("modeLabel",modeLabel);m.put("requestNo",requestNo);m.put("materialName",materialName);m.put("lotNo",lotNo);m.put("overallResult",overallResult);m.put("approvedBy",approvedBy);m.put("approvedAt",approvedAt);m.put("signatureReference",signatureReference);m.put("inspectors",inspectors);m.put("items",List.copyOf(items));return m;}}
}
