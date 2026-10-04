package com.hospital.mes.masterdata.application;
import com.fasterxml.jackson.databind.*;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.audit.domain.*;
import com.hospital.mes.audit.idempotency.*;
import com.hospital.mes.common.exception.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import org.erdtman.jcs.JsonCanonicalizer;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.time.*;
import java.util.function.Supplier;
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class MasterMutation {
    private final CurrentPlatformContextResolver contexts; private final PlatformIdempotencyService keys;
    private final AuditApplicationService audit; private final ObjectMapper json;
    public MasterMutation(CurrentPlatformContextResolver contexts,PlatformIdempotencyService keys,AuditApplicationService audit,ObjectMapper json){this.contexts=contexts;this.keys=keys;this.audit=audit;this.json=json;}
    public CurrentPlatformContext context(String permission){var c=contexts.current();if(!c.hasPermission(permission))throw new PermissionException("PERMISSION_DENIED","Permission required: "+permission);return c;}
    private String canonical(Object value){try{return new JsonCanonicalizer(json.writeValueAsBytes(value)).getEncodedString();}catch(java.io.IOException ex){throw new IllegalArgumentException("Invalid record",ex);}}
    @Transactional public JsonNode execute(CurrentPlatformContext c,String operation,String key,Object request,Supplier<ScopedEntity> work){
        return execute(c,operation,key,request,work,this::view,200);
    }
    @Transactional public JsonNode execute(CurrentPlatformContext c,String operation,String key,Object request,Supplier<ScopedEntity> work,java.util.function.Function<ScopedEntity,JsonNode> responseView,int httpStatus){
        var d=keys.begin(new IdempotencyCommand(c.organizationId(),c.actorId(),operation,key,canonical(request)));
        if(d.type()==IdempotencyDecisionType.CONFLICT)throw new ResourceConflictException("IDEMPOTENCY_KEY_REUSED","Key reused with different request");
        if(d.type()==IdempotencyDecisionType.IN_PROGRESS_CONFLICT)throw new ResourceConflictException("IDEMPOTENCY_IN_PROGRESS","Request is in progress");
        if(d.type()==IdempotencyDecisionType.REPLAY){try{return json.readTree(d.responseJson());}catch(java.io.IOException e){throw new IllegalStateException(e);}}
        ScopedEntity row;try{row=work.get();}catch(RuntimeException ex){throw ScopedStore.translateConcurrency(ex);}var result=responseView.apply(row);String response=canonical(result);keys.complete(d.handle(),httpStatus,response,row.getClass().getSimpleName(),row.getId().toString());try{return json.readTree(response);}catch(java.io.IOException ex){throw new IllegalStateException(ex);}
    }
    public void audit(CurrentPlatformContext c,String action,String type,ScopedEntity before,ScopedEntity after,String reason,String key){
        audit.append(new AuditCommand(c.organizationId(),c.actorId(),c.roleSnapshot(),action,type,after.getId().toString(),before==null?null:PlatformIdempotencyService.digest(canonical(view(before))),PlatformIdempotencyService.digest(canonical(view(after))),reason,null,Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS),UUID.randomUUID().toString(),c.requestId(),AuditSource.API,key));
    }
    public String digest(Object value){return PlatformIdempotencyService.digest(canonical(value));}
    public void auditSnapshot(CurrentPlatformContext c,String action,String type,Long id,JsonNode before,JsonNode after,String reason,String key){audit.append(new AuditCommand(c.organizationId(),c.actorId(),c.roleSnapshot(),action,type,id.toString(),before==null?null:digest(before),digest(after),reason,null,Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS),UUID.randomUUID().toString(),c.requestId(),AuditSource.API,key));}
    public JsonNode view(ScopedEntity e){var n=(com.fasterxml.jackson.databind.node.ObjectNode)json.valueToTree(e);for(String f:List.of("id","orgId","createdBy","updatedBy","parentId","fromUnitId","toUnitId","materialId","userId","baseUnitId","packUnitId","materialVersionId","supplierId","approvedBy")){if(n.hasNonNull(f))n.put(f,n.get(f).asText());}for(String f:List.of("factor","weighingPrecision","weighingTolerancePct","temperatureMin","temperatureMax","humidityMin","humidityMax")){if(n.hasNonNull(f))n.put(f,new java.math.BigDecimal(n.get(f).asText()).toPlainString());}for(String f:List.of("createdAt","updatedAt","effectiveFrom","effectiveTo","approvedAt")){if(n.hasNonNull(f))n.put(f,((java.time.LocalDateTime)new org.springframework.beans.BeanWrapperImpl(e).getPropertyValue(f)).format(java.time.format.DateTimeFormatter.ofPattern("uuuu-MM-dd\'T\'HH:mm:ss.SSS\'Z\'")));}var actions=n.putArray("allowedActions");e.allowedActions().forEach(actions::add);return n;}
    public static long id(String value){try{long id=Long.parseLong(value);if(id<=0)throw new NumberFormatException();return id;}catch(Exception e){throw new IllegalArgumentException("Invalid ID");}}
    public static long version(String header,Long body){Long v=null;if(header!=null){String raw=header.strip();if(raw.startsWith("\"")&&raw.endsWith("\""))raw=raw.substring(1,raw.length()-1);try{v=Long.valueOf(raw);}catch(NumberFormatException e){throw new IllegalArgumentException("Invalid If-Match");}}if(v!=null&&body!=null&&!v.equals(body))throw new IllegalArgumentException("Conflicting versions");if(v==null)v=body;if(v==null||v<0)throw new IllegalArgumentException("Version required");return v;}
}
