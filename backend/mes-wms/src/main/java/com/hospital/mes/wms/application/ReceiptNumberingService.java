package com.hospital.mes.wms.application;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.common.exception.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.masterdata.domain.MasterRules;
import com.hospital.mes.wms.infrastructure.*;
import com.hospital.mes.wms.domain.ReceiptNumberingPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.time.*;
import java.util.*;
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ReceiptNumberingService {
 private final ReceiptNumberRuleMapper rules; private final ReceiptNumberReservationMapper reservations;
 private final MasterMutation mutations; private final MaterialQueryService materials; private final MaterialLotMapper lots; private final ReceiptItemMapper receiptItems;
 private final ScopedStore<ReceiptNumberRule> ruleStore;private final ScopedStore<ReceiptNumberReservation> reservationStore;
 public ReceiptNumberingService(ReceiptNumberRuleMapper rules,ReceiptNumberReservationMapper reservations,MasterMutation mutations,MaterialQueryService materials,MaterialLotMapper lots,ReceiptItemMapper receiptItems){
  this.rules=rules;this.reservations=reservations;this.mutations=mutations;this.materials=materials;this.lots=lots;this.receiptItems=receiptItems;
  ruleStore=new ScopedStore<>(rules,List.of(),Map.of());reservationStore=new ScopedStore<>(reservations,List.of(),Map.of());
 }
 public record Reserve(String materialId,String clientLineId){@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String name,Object v){throw new IllegalArgumentException("Unknown field: "+name);}}
 @Transactional public JsonNode reserve(Reserve input,String key){
  var c=mutations.context("wms:receipt:create");long material=MasterMutation.id(input.materialId());String line=MasterRules.text(input.clientLineId(),100);
  materials.requireUsable(c.organizationId(),material,Instant.now());
  return mutations.execute(c,"ReceiptNumber:RESERVE",key,input,()->{
   // Serialize by organization rule. No browser counters and no implicit approval.
   var rule=rules.selectOne(new QueryWrapper<ReceiptNumberRule>().eq("org_id",c.organizationId()).last("FOR UPDATE"));
   if(rule==null||!Boolean.TRUE.equals(rule.getEnabled())||rule.getApprovalReference()==null||rule.getApprovalReference().isBlank())throw new ComplianceException("RECEIPT_NUMBER_RULE_NOT_APPROVED","尚未配置获批准的内部物料批号规则，不能生成正式批号");
   var old=reservations.selectOne(new QueryWrapper<ReceiptNumberReservation>().eq("org_id",c.organizationId()).eq("client_line_id",line));
   if(old!=null){if(!old.getMaterialId().equals(material)||!old.getCreatedBy().equals(c.actorId()))throw new ResourceConflictException("RECEIPT_NUMBER_INTENT_CONFLICT","编号申请与原物料或操作者不一致");return old;}
   Instant at=Instant.now();ZoneId zone=ZoneId.of(rule.getTimezoneId());
   boolean monthly=ReceiptNumberingPolicy.monthly(rule.getPrefix(),rule.getDatePattern(),rule.getSequenceWidth());
   String period=ReceiptNumberingPolicy.period(zone,at);
   long seq=monthly&&!period.equals(rule.getSequencePeriod())?observedNext(c.organizationId(),period):rule.getNextValue();
   if(monthly&&seq>9999)throw new ComplianceException("RECEIPT_NUMBER_MONTH_EXHAUSTED","本月内部物料批号已达9999，不能重复或增加流水位数");
   String lot=ReceiptNumberingPolicy.format(rule.getPrefix(),rule.getDatePattern(),rule.getSequenceWidth(),seq,zone,at);
   if(lots.selectCount(new QueryWrapper<MaterialLotEntity>().eq("org_id",c.organizationId()).eq("lot_no",lot))>0)throw new ResourceConflictException("RECEIPT_NUMBER_COLLISION","编号与历史物料批号冲突，需检查批准规则");
   if(reservations.selectCount(new QueryWrapper<ReceiptNumberReservation>().eq("org_id",c.organizationId()).eq("lot_no",lot))>0||receiptItems.selectCount(new QueryWrapper<ReceiptItemEntity>().eq("org_id",c.organizationId()).eq("lot_no",lot))>0)throw new ResourceConflictException("RECEIPT_NUMBER_COLLISION","编号已存在，未跳号或覆盖历史，请复核规则");
   rule.setNextValue(Math.addExact(seq,1));if(monthly)rule.setSequencePeriod(period);
   ruleStore.update(rule,rule.getVersionNo(),c.actorId(),monthly?List.of("nextValue","sequencePeriod"):List.of("nextValue"));
   var r=new ReceiptNumberReservation();r.setRuleId(rule.getId());r.setMaterialId(material);r.setClientLineId(line);r.setLotNo(lot);r.setApprovalReference(rule.getApprovalReference());reservationStore.insert(r,c.organizationId(),c.actorId());
   mutations.auditSnapshot(c,"ReceiptNumber:RESERVE","ReceiptNumberReservation",r.getId(),null,mutations.view(r),"依据批准规则 "+rule.getApprovalReference(),key);return r;
  });
 }
 public JsonNode configuration(){
  var c=mutations.context("iam:dict:view");var r=rules.selectOne(new QueryWrapper<ReceiptNumberRule>().eq("org_id",c.organizationId()));
  return r==null?com.fasterxml.jackson.databind.node.NullNode.instance:mutations.view(r);
 }
 public record ConfigureMonthly(String approvalReference,Long versionNo,String reason){@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String name,Object value){throw new IllegalArgumentException("Unknown field: "+name);}}
 @Transactional public JsonNode configureMonthly(ConfigureMonthly input,String key){
  var c=mutations.context("iam:dict:update");String reference=MasterRules.text(input.approvalReference(),200),why=MasterRules.text(input.reason(),1000);
  return mutations.execute(c,"ReceiptNumberRule:MONTHLY4",key,input,()->{
   var r=rules.selectOne(new QueryWrapper<ReceiptNumberRule>().eq("org_id",c.organizationId()).last("FOR UPDATE"));
   JsonNode before=r==null?null:mutations.view(r);boolean create=r==null;
   if(!create&&(input.versionNo()==null||!input.versionNo().equals(r.getVersionNo())))throw new ResourceConflictException("VERSION_CONFLICT","编号规则已变化，请重新读取");
   if(create&&input.versionNo()!=null)throw new ResourceConflictException("VERSION_CONFLICT","编号规则不存在，请重新读取");
   if(create)r=new ReceiptNumberRule();
   String period=ReceiptNumberingPolicy.period(ZoneId.of("Asia/Shanghai"),Instant.now());
   long floor=observedNext(c.organizationId(),period);
   if(!create&&ReceiptNumberingPolicy.monthly(r.getPrefix(),r.getDatePattern(),r.getSequenceWidth())&&period.equals(r.getSequencePeriod()))floor=Math.max(floor,r.getNextValue());
   r.setPrefix("");r.setDatePattern("yyyyMM");r.setSequenceWidth(4);r.setTimezoneId("Asia/Shanghai");r.setNextValue(floor);r.setSequencePeriod(period);r.setApprovalReference(reference);r.setEnabled(true);
   if(create)ruleStore.insert(r,c.organizationId(),c.actorId());else ruleStore.update(r,r.getVersionNo(),c.actorId(),List.of("prefix","datePattern","sequenceWidth","timezoneId","nextValue","sequencePeriod","approvalReference","enabled"));
   mutations.auditSnapshot(c,"ReceiptNumberRule:CONFIGURE","ReceiptNumberRule",r.getId(),before,mutations.view(r),why,key);return r;
  });
 }
 private long observedNext(long org,String period){
  var values=new ArrayList<String>();
  reservations.selectList(new QueryWrapper<ReceiptNumberReservation>().select("lot_no").eq("org_id",org).likeRight("lot_no",period)).forEach(r->values.add(r.getLotNo()));
  lots.selectList(new QueryWrapper<MaterialLotEntity>().select("lot_no").eq("org_id",org).likeRight("lot_no",period)).forEach(r->values.add(r.getLotNo()));
  receiptItems.selectList(new QueryWrapper<ReceiptItemEntity>().select("lot_no").eq("org_id",org).likeRight("lot_no",period)).forEach(r->values.add(r.getLotNo()));
  return values.stream().filter(Objects::nonNull).filter(v->v.matches(period+"[0-9]{4}")).mapToLong(v->Long.parseLong(v.substring(6))).max().orElse(0L)+1;
 }
 // Called in the same transaction as receipt item persistence; another receipt may never steal a number.
 public void bind(long org,long actor,ReceiptItemEntity item){
  var r=reservations.selectOne(new QueryWrapper<ReceiptNumberReservation>().eq("org_id",org).eq("lot_no",item.getLotNo()).last("FOR UPDATE"));
  if(r==null||!r.getMaterialId().equals(item.getMaterialId()))throw new ComplianceException("RECEIPT_NUMBER_NOT_RESERVED","内部批号未由当前组织为该物料分配");
  if(r.getBoundReceiptItemId()!=null){if(!r.getBoundReceiptItemId().equals(item.getId()))throw new ResourceConflictException("RECEIPT_NUMBER_ALREADY_USED","内部批号已绑定其他收货明细");return;}
  if(!r.getCreatedBy().equals(actor))throw new PermissionException("RECEIPT_NUMBER_OWNER_MISMATCH","不能使用其他操作者未绑定的编号申请");
  r.setBoundReceiptItemId(item.getId());reservationStore.update(r,r.getVersionNo(),actor,List.of("boundReceiptItemId"));
 }
}
