package com.hospital.mes.audit.attachment.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.audit.attachment.domain.AttachmentRules;
import com.hospital.mes.audit.attachment.infrastructure.*;
import com.hospital.mes.audit.domain.*;
import com.hospital.mes.audit.idempotency.*;
import com.hospital.mes.common.exception.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Service
@ConditionalOnProperty(prefix="spring.datasource",name="url")
public class AttachmentService {
    public record Metadata(String id,String fileName,String mediaType,long byteLength,String sha256,
        String uploadedBy,Instant uploadedAt,long recordVersion,String retentionStatus) { }
    public record Download(Metadata metadata,byte[] content) {
        public Download { content=content.clone(); }
        @Override public byte[] content(){return content.clone();}
    }
    private final AttachmentMapper mapper;
    private final CurrentPlatformContextResolver contexts;
    private final PlatformIdempotencyService keys;
    private final AuditApplicationService audit;
    private final ObjectMapper json;
    public AttachmentService(AttachmentMapper mapper,CurrentPlatformContextResolver contexts,
        PlatformIdempotencyService keys,AuditApplicationService audit,ObjectMapper json){
        this.mapper=mapper;this.contexts=contexts;this.keys=keys;this.audit=audit;this.json=json;
    }
    public CurrentPlatformContext require(String permission){
        var c=contexts.current();
        if(!c.hasPermission(permission))throw new PermissionException("PERMISSION_DENIED","Permission required: "+permission);
        return c;
    }
    @Transactional public Metadata upload(String submittedName,String submittedType,byte[] content,String key){
        var c=require("attachment:upload");Objects.requireNonNull(content,"file");
        AttachmentRules.validateSize(content.length);
        String name=AttachmentRules.fileName(submittedName),type=AttachmentRules.mediaType(submittedType),hash=AttachmentRules.digest(content);
        var canonical=json.createObjectNode().put("fileName",name).put("mediaType",type).put("byteLength",content.length).put("sha256",hash).toString();
        var decision=keys.begin(new IdempotencyCommand(c.organizationId(),c.actorId(),"ATTACHMENT_UPLOAD",key,canonical));
        if(decision.type()==IdempotencyDecisionType.CONFLICT)throw new ResourceConflictException("IDEMPOTENCY_KEY_REUSED","Key reused with different file");
        if(decision.type()==IdempotencyDecisionType.IN_PROGRESS_CONFLICT)throw new ResourceConflictException("IDEMPOTENCY_IN_PROGRESS","Upload is in progress");
        if(decision.type()==IdempotencyDecisionType.REPLAY){try{return json.readValue(decision.responseJson(),Metadata.class);}catch(Exception e){throw new IllegalStateException(e);}}
        var row=new AttachmentEntity();row.setOrgId(c.organizationId());row.setUploadedBy(c.actorId());
        row.setUploadedAt(LocalDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MILLIS));
        row.setFileName(name);row.setMediaType(type);row.setByteLength((long)content.length);row.setSha256(hash);
        row.setContent(content.clone());row.setRecordVersion(1L);row.setRetentionStatus("RETAINED");mapper.insert(row);
        Metadata result=metadata(row);String body=encode(result);
        audit.append(new AuditCommand(c.organizationId(),c.actorId(),c.roleSnapshot(),"ATTACHMENT_UPLOADED",
            "Attachment",result.id(),null,PlatformIdempotencyService.digest(body),null,null,result.uploadedAt(),
            UUID.randomUUID().toString(),c.requestId(),AuditSource.API,key));
        keys.complete(decision.handle(),200,body,"Attachment",result.id());return result;
    }
    public Metadata get(String id){var c=require("attachment:view");return readMetadata(c.organizationId(),id(id));}
    public Download download(String id){var c=require("attachment:view");return readContent(c.organizationId(),id(id));}
    /** Internal query contract: the owning consumer verifies its resource permission/association. */
    public Metadata readMetadata(long org,long id){return metadata(find(org,id));}
    public Metadata requireLinkable(CurrentPlatformContext c,long id){
        var row=find(c.organizationId(),id);
        if(row.getUploadedBy()!=c.actorId()&&!c.hasPermission("attachment:view"))
            throw new PermissionException("PERMISSION_DENIED","Access to the source attachment is required");
        return metadata(row);
    }
    public Download readContent(long org,long id){
        var row=find(org,id);
        var content=mapper.selectOne(new QueryWrapper<AttachmentEntity>().select("id","content")
            .eq("org_id",org).eq("id",id)).getContent();
        if(content.length!=row.getByteLength()||!AttachmentRules.digest(content).equals(row.getSha256()))
            throw new ComplianceException("ATTACHMENT_INTEGRITY_FAILURE","Stored attachment integrity check failed");
        return new Download(metadata(row),content);
    }
    private AttachmentEntity find(long org,long id){
        var row=mapper.selectOne(new QueryWrapper<AttachmentEntity>().eq("org_id",org).eq("id",id));
        if(row==null)throw new NoSuchElementException("Attachment not found");return row;
    }
    private Metadata metadata(AttachmentEntity e){return new Metadata(e.getId().toString(),e.getFileName(),e.getMediaType(),
        e.getByteLength(),e.getSha256(),e.getUploadedBy().toString(),e.getUploadedAt().toInstant(ZoneOffset.UTC),e.getRecordVersion(),e.getRetentionStatus());}
    private String encode(Object value){try{return json.writeValueAsString(value);}catch(Exception e){throw new IllegalStateException(e);}}
    private static long id(String value){try{long id=Long.parseLong(value);if(id>0)return id;}catch(NumberFormatException ignored){}throw new IllegalArgumentException("Invalid attachment ID");}
}
