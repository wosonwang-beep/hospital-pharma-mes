package com.hospital.mes.ebr.application;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.mes.ebr.infrastructure.*;
import com.hospital.mes.ebr.domain.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.process.application.ProcessQueryService;
import com.hospital.mes.common.exception.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static com.hospital.mes.ebr.domain.EbrCommands.*;
import static com.hospital.mes.ebr.domain.EbrDefinitionRules.*;

@org.springframework.stereotype.Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class EbrService {
    private final EbrStore db;private final EbrDefinitions definitions;private final EbrEngine engine;private final MasterMutation mutations;private final ProcessQueryService processes;private final MasterQueryService units;private final ObjectMapper json;private final org.springframework.context.ApplicationEventPublisher events;
    public EbrService(EbrStore db,EbrDefinitions definitions,EbrEngine engine,MasterMutation mutations,ProcessQueryService processes,MasterQueryService units,ObjectMapper json,org.springframework.context.ApplicationEventPublisher events){this.db=db;this.definitions=definitions;this.engine=engine;this.mutations=mutations;this.processes=processes;this.units=units;this.json=json;this.events=events;}
    public ScopedStore.PageData<Summary> list(int page,int size,String keyword,Map<String,String> filters){var c=mutations.context("ebr:template:view");var data=db.store(TemplateEntity.class).list(c.organizationId(),page,size,keyword,filters);return new ScopedStore.PageData<>(data.items().stream().map(this::summary).toList(),data.total(),page,size);}
    public JsonNode get(String id){var c=mutations.context("ebr:template:view");return detail(db.store(TemplateEntity.class).get(c.organizationId(),MasterMutation.id(id)));}
    @Transactional public JsonNode create(Create request,String key){
        var c=mutations.context("ebr:template:create");return mutations.execute(c,"EbrTemplate:CREATE",key,request,()->{
            String code=text(request.templateCode(),64,"templateCode");long process=MasterMutation.id(request.packageVersionId());processes.snapshot(c.organizationId(),process);
            if(!db.family(c.organizationId(),code).isEmpty())throw new ResourceConflictException("DUPLICATE_CODE","Template code already exists");
            var row=new TemplateEntity();row.setTemplateCode(code);row.setPackageVersionId(process);row.setBusinessVersion(1);row.setStatus("DRAFT");db.store(TemplateEntity.class).insert(row,c.organizationId(),c.actorId());
            mutations.auditSnapshot(c,"EbrTemplate:CREATE","EbrTemplateVersion",row.getId(),null,detail(row),null,key);return row;
        },r->detail((TemplateEntity)r),200);
    }
    @Transactional public JsonNode save(String id,Save request,String header,String key){
        var c=mutations.context("ebr:template:update");long version=MasterMutation.version(header,request.versionNo());String reason=text(request.reason(),1000,"reason");
        return mutations.execute(c,"EbrTemplate:SAVE:"+id,key,Map.of("id",id,"version",version,"body",request),()->{
            var row=db.lock(c.organizationId(),MasterMutation.id(id));expected(row,version);draft(row.getStatus());var before=detail(row);definitions.save(c,row,request.definition());db.store(TemplateEntity.class).update(row,version,c.actorId(),List.of());mutations.auditSnapshot(c,"EbrTemplate:SAVE","EbrTemplateVersion",row.getId(),before,detail(row),reason,key);return row;
        },r->detail((TemplateEntity)r),200);
    }
    @Transactional public JsonNode version(String id,VersionCreate request,String header,String key){
        var c=mutations.context("ebr:designer:edit");long version=MasterMutation.version(header,request.versionNo());String reason=text(request.reason(),1000,"reason");if(request.copyDefinition()==null)throw new IllegalArgumentException("copyDefinition required");
        return mutations.execute(c,"EbrTemplate:VERSION:"+id,key,Map.of("id",id,"version",version,"body",request),()->{
            var source=db.lock(c.organizationId(),MasterMutation.id(id));expected(source,version);var family=db.family(c.organizationId(),source.getTemplateCode());if(family.stream().anyMatch(t->t.getStatus().equals("DRAFT")))throw new ResourceConflictException("DRAFT_EXISTS","Maintain the existing draft first");
            var row=new TemplateEntity();row.setTemplateCode(source.getTemplateCode());row.setPackageVersionId(source.getPackageVersionId());row.setBusinessVersion(family.stream().mapToInt(TemplateEntity::getBusinessVersion).max().orElseThrow()+1);row.setStatus("DRAFT");db.store(TemplateEntity.class).insert(row,c.organizationId(),c.actorId());if(request.copyDefinition())definitions.save(c,row,definitions.load(source));mutations.auditSnapshot(c,"EbrTemplate:VERSION","EbrTemplateVersion",row.getId(),null,detail(row),reason,key);return row;
        },r->detail((TemplateEntity)r),200);
    }
    @Transactional public JsonNode command(String id,String action,Command request,String header,String key){
        String permission=action.equals("LINT")?"ebr:designer:edit":"ebr:template:"+action.toLowerCase(Locale.ROOT);var c=mutations.context(permission);long version=MasterMutation.version(header,request.versionNo());String reason=text(request.reason(),1000,"reason");var result=new AtomicReference<JsonNode>();
        return mutations.execute(c,"EbrTemplate:"+action+":"+id,key,Map.of("id",id,"version",version,"body",request),()->{
            var row=db.lock(c.organizationId(),MasterMutation.id(id));expected(row,version);var before=detail(row);var definition=definitions.load(row);
            if(action.equals("LINT")){var issues=definitions.lint(row,definition,true);result.set(json.valueToTree(new Lint(issues.isEmpty(),issues)));mutations.auditSnapshot(c,"EbrTemplate:LINT","EbrTemplateVersion",row.getId(),before,result.get(),reason,key);return row;}
            String next=transition(row.getStatus(),action);definitions.requireValid(row,definition,true);
            if(action.equals("APPROVE")){gate(row.getCreatedBy()!=c.actorId()&&row.getUpdatedBy()!=c.actorId(),"Independent approver cannot be creator/submitter");row.setApprovedBy(c.actorId());row.setApprovedAt(now());row.setContentHash(mutations.digest(definitions.canonical(row)));}
            if(action.equals("PUBLISH")){gate(Objects.equals(row.getContentHash(),mutations.digest(definitions.canonical(row))),"Approved definition hash changed");definitions.publish(c,row);row.setEffectiveFrom(now());}
            row.setStatus(next);db.store(TemplateEntity.class).update(row,version,c.actorId(),List.of("status","approvedBy","approvedAt","contentHash","effectiveFrom"));mutations.auditSnapshot(c,"EbrTemplate:"+action,"EbrTemplateVersion",row.getId(),before,detail(row),reason,key);events.publishEvent(new DefinitionTransitioned(c.organizationId(),row.getId(),next));return row;
        },r->action.equals("LINT")?result.get():detail((TemplateEntity)r),200);
    }
    public record DefinitionTransitioned(long organizationId,long templateVersionId,String state){}
    @Transactional public JsonNode simulate(String id,Simulation request,String header,String key){
        var c=mutations.context("ebr:designer:edit");long version=MasterMutation.version(header,request.versionNo());String reason=text(request.reason(),1000,"reason");var result=new AtomicReference<JsonNode>();
        return mutations.execute(c,"EbrTemplate:SIMULATE:"+id,key,Map.of("id",id,"version",version,"body",request),()->{
            var row=db.lock(c.organizationId(),MasterMutation.id(id));expected(row,version);var definition=definitions.load(row);definitions.requireValid(row,definition,false);Map<String,String> statuses=new HashMap<>();statuses.put("template",row.getStatus());definition.forms().forEach(f->statuses.put(f.formCode(),"DRAFT"));
            result.set(json.valueToTree(engine.evaluate(definition,request.inputs(),request.triggerPoint(),c.roleCodes(),statuses,Instant.now(),(value,from,to)->new java.math.BigDecimal(units.convert(c.organizationId(),MasterMutation.id(from),MasterMutation.id(to),null,value).convertedValue()),row.getBusinessVersion()+":"+mutations.digest(definition))));
            mutations.auditSnapshot(c,"EbrTemplate:SIMULATE","EbrTemplateVersion",row.getId(),definitions.canonical(row),result.get(),reason,key);return row;
        },r->result.get(),200);
    }
    public Comparison compare(String id,String other){var c=mutations.context("ebr:template:view");var a=db.store(TemplateEntity.class).get(c.organizationId(),MasterMutation.id(id));var b=db.store(TemplateEntity.class).get(c.organizationId(),MasterMutation.id(other));if(!a.getTemplateCode().equals(b.getTemplateCode()))throw new NoSuchElementException("Version not in template family");List<Difference> differences=new ArrayList<>();diff("definition",json.valueToTree(definitions.load(a)),json.valueToTree(definitions.load(b)),differences);return new Comparison(id,other,List.copyOf(differences));}
    public JsonNode published(long org,long id){var row=db.store(TemplateEntity.class).get(org,id);gate(row.getStatus().equals("EFFECTIVE"),"Template is not published");processes.requireUsable(org,row.getPackageVersionId());JsonNode canonical=definitions.canonical(row);String digest=mutations.digest(canonical);gate(digest.equals(row.getContentHash()),"Published definition hash mismatch");var payload=(ObjectNode)canonical.deepCopy();payload.put("definitionHash",digest);return payload;}
    private Summary summary(TemplateEntity row){return new Summary(row.getId().toString(),row.getPackageVersionId().toString(),row.getTemplateCode(),row.getBusinessVersion(),row.getStatus(),row.getContentHash(),time(row.getEffectiveFrom()),row.getApprovedBy()==null?null:row.getApprovedBy().toString(),time(row.getApprovedAt()),row.getVersionNo(),row.allowedActions());}
    private JsonNode detail(TemplateEntity row){var n=(ObjectNode)json.valueToTree(summary(row));n.set("definition",json.valueToTree(definitions.load(row)));n.set("versions",json.valueToTree(db.family(row.getOrgId(),row.getTemplateCode()).stream().sorted(Comparator.comparing(TemplateEntity::getBusinessVersion).reversed()).map(this::summary).toList()));n.set("operationChoices",json.valueToTree(processes.operations(row.getOrgId(),row.getPackageVersionId())));return n;}
    private void expected(TemplateEntity row,long version){if(row.getVersionNo()!=version)throw new ResourceConflictException("VERSION_CONFLICT","Reload before saving");}
    private static LocalDateTime now(){return LocalDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MILLIS);}
    private static String time(LocalDateTime value){return value==null?null:value.toInstant(ZoneOffset.UTC).toString();}
    private void diff(String path,JsonNode a,JsonNode b,List<Difference> result){
        if(Objects.equals(a,b))return;if(result.size()>=500)throw new IllegalArgumentException("Comparison exceeds 500 differences");
        if(a!=null&&b!=null&&a.isObject()&&b.isObject()){Set<String> keys=new TreeSet<>();a.fieldNames().forEachRemaining(keys::add);b.fieldNames().forEachRemaining(keys::add);for(String key:keys)diff(path+"."+key,a.get(key),b.get(key),result);}
        else if(a!=null&&b!=null&&a.isArray()&&b.isArray()){for(int i=0;i<Math.max(a.size(),b.size());i++)diff(path+"["+i+"]",a.get(i),b.get(i),result);}
        else result.add(new Difference(path,a==null?null:a.toString(),b==null?null:b.toString()));
    }
}
