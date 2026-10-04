package com.hospital.mes.wms.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.audit.attachment.application.AttachmentService;
import com.hospital.mes.audit.domain.*;
import com.hospital.mes.audit.idempotency.*;
import com.hospital.mes.common.exception.*;
import com.hospital.mes.masterdata.domain.MasterRules;
import com.hospital.mes.wms.domain.WmsRules;
import com.hospital.mes.wms.infrastructure.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class WmsAttachmentService {
 public record Link(Long versionNo,String attachmentId,String purpose,String reason){
  @com.fasterxml.jackson.annotation.JsonAnySetter public void rejectUnknown(String name,Object value){throw new IllegalArgumentException("Unknown attachment field: "+name);}
 }
 public record View(String id,String receiptId,String purpose,String reason,String linkedBy,
  Instant linkedAt,long recordVersion,AttachmentService.Metadata attachment){}
 private final WmsStore store; private final ReceiptAttachmentMapper links; private final AttachmentService files;
 private final PlatformIdempotencyService keys; private final AuditApplicationService audit; private final ObjectMapper json;
 public WmsAttachmentService(WmsStore store,ReceiptAttachmentMapper links,AttachmentService files,
  PlatformIdempotencyService keys,AuditApplicationService audit,ObjectMapper json){
  this.store=store;this.links=links;this.files=files;this.keys=keys;this.audit=audit;this.json=json;
 }
 public List<View> list(String receiptId){var c=files.require("wms:receipt:view");long id=id(receiptId);
  return forReceipt(c.organizationId(),id);
 }
 /** Internal lineage query; caller enforces its complete trace read permission. */
 public List<View> forReceipt(long org,long receipt){store.receipt().get(org,receipt);return links.selectList(query(org,receipt).orderByAsc("id")).stream().map(this::view).toList();}
 public AttachmentService.Download download(String receiptId,String attachmentId){var c=files.require("wms:receipt:view");
  long receipt=id(receiptId),attachment=id(attachmentId);store.receipt().get(c.organizationId(),receipt);
  if(links.selectCount(query(c.organizationId(),receipt).eq("attachment_id",attachment))!=1)
   throw new NoSuchElementException("Receipt attachment not found");
  return files.readContent(c.organizationId(),attachment);
 }
 @Transactional public View link(String receiptId,Link body,String header,String key){
  var c=files.require("wms:receipt:update");files.require("attachment:upload");
  if(body==null||body.versionNo()==null||body.versionNo()<0||header==null||!header.matches("\"[0-9]+\"")||
   !header.equals("\""+body.versionNo()+"\""))throw new IllegalArgumentException("Matching quoted If-Match and versionNo required");
  long receipt=id(receiptId),attachment=id(body.attachmentId());
  if(!List.of("COA","DELIVERY_DOCUMENT","OTHER").contains(body.purpose()))throw new IllegalArgumentException("Invalid attachment purpose");
  String reason=MasterRules.text(body.reason(),1000);
  files.requireLinkable(c,attachment);
  String request=json.createObjectNode().put("receiptId",receiptId).set("body",json.valueToTree(body)).toString();
  var d=keys.begin(new IdempotencyCommand(c.organizationId(),c.actorId(),"RECEIPT_ATTACHMENT_LINK",key,request));
  if(d.type()==IdempotencyDecisionType.CONFLICT)throw new ResourceConflictException("IDEMPOTENCY_KEY_REUSED","Key reused with different request");
  if(d.type()==IdempotencyDecisionType.IN_PROGRESS_CONFLICT)throw new ResourceConflictException("IDEMPOTENCY_IN_PROGRESS","Link is in progress");
  if(d.type()==IdempotencyDecisionType.REPLAY){try{return json.readValue(d.responseJson(),View.class);}catch(Exception ex){throw new IllegalStateException(ex);}}
  var source=store.receipt().lock(c.organizationId(),receipt);WmsRules.version(source.getVersionNo(),body.versionNo());
  if(!List.of("DRAFT","APPROVED").contains(source.getRecordStatus()))throw new StateTransitionException("RECEIPT_ATTACHMENT_BLOCKED","Receipt cannot receive supplementary evidence");
  if(links.selectCount(query(c.organizationId(),receipt).eq("attachment_id",attachment))>0)
   throw new ResourceConflictException("ATTACHMENT_ALREADY_LINKED","File is already linked to this receipt");
  var row=new ReceiptAttachmentEntity();row.setOrgId(c.organizationId());row.setReceiptId(receipt);row.setAttachmentId(attachment);
  row.setPurpose(body.purpose());row.setReason(reason);row.setLinkedBy(c.actorId());
  row.setLinkedAt(LocalDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MILLIS));row.setRecordVersion(1L);links.insert(row);
  View result=view(row);String response=encode(result);
  audit.append(new AuditCommand(c.organizationId(),c.actorId(),c.roleSnapshot(),"RECEIPT_ATTACHMENT_LINKED",
   "ReceiptAttachment",result.id(),null,PlatformIdempotencyService.digest(response),reason,null,result.linkedAt(),
   UUID.randomUUID().toString(),c.requestId(),AuditSource.API,key));
  keys.complete(d.handle(),200,response,"ReceiptAttachment",result.id());return result;
 }
 private QueryWrapper<ReceiptAttachmentEntity> query(long org,long receipt){return new QueryWrapper<ReceiptAttachmentEntity>().eq("org_id",org).eq("receipt_id",receipt);}
 private View view(ReceiptAttachmentEntity row){return new View(row.getId().toString(),row.getReceiptId().toString(),row.getPurpose(),row.getReason(),row.getLinkedBy().toString(),row.getLinkedAt().toInstant(ZoneOffset.UTC),row.getRecordVersion(),files.readMetadata(row.getOrgId(),row.getAttachmentId()));}
 private String encode(Object value){try{return json.writeValueAsString(value);}catch(Exception ex){throw new IllegalStateException(ex);}}
 private static long id(String value){try{if(value!=null&&value.matches("[1-9][0-9]*"))return Long.parseLong(value);}catch(NumberFormatException ignored){}throw new IllegalArgumentException("Invalid ID");}
}
