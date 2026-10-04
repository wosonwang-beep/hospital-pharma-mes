package com.hospital.mes.ebr.application;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.mes.ebr.infrastructure.*;
import com.hospital.mes.ebr.domain.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import com.hospital.mes.process.application.ProcessQueryService;
import com.hospital.mes.system.application.SystemReferenceQuery;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.common.exception.*;
import org.springframework.beans.BeanWrapperImpl;
import java.util.*;
import java.util.function.*;
import static com.hospital.mes.ebr.domain.EbrCommands.*;

/** Maps the normalized, retained definition rows to the portable typed tree. */
@org.springframework.stereotype.Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class EbrDefinitions {
    private final EbrStore db;private final ObjectMapper json;private final ProcessQueryService processes;private final MasterQueryService units;private final SystemReferenceQuery identities;
    public EbrDefinitions(EbrStore db,ObjectMapper json,ProcessQueryService processes,MasterQueryService units,SystemReferenceQuery identities){this.db=db;this.json=json;this.processes=processes;this.units=units;this.identities=identities;}
    public Definition load(TemplateEntity template){
        long org=template.getOrgId(),id=template.getId();
        var sections=db.rows(SectionEntity.class,org,"template_version_id",id);var forms=db.rows(FormEntity.class,org,"template_version_id",id);var rules=db.rows(RuleEntity.class,org,"template_version_id",id);
        Map<Long,String> groupCodes=new HashMap<>(),formCodes=new HashMap<>(),fieldCodes=new HashMap<>(),ruleCodes=new HashMap<>();
        rules.forEach(r->ruleCodes.put(r.getId(),r.getRuleCode()));forms.forEach(f->formCodes.put(f.getId(),f.getFormCode()));
        var sectionDtos=sections.stream().map(s->{var groups=db.rows(GroupEntity.class,org,"section_def_id",s.getId());groups.forEach(g->groupCodes.put(g.getId(),g.getGroupCode()));return dto(s,Section.class,extras("visibilityRuleCode",ruleCodes.get(s.getVisibilityRuleId()),"groups",groups.stream().map(g->dto(g,Group.class,Map.of())).sorted(Comparator.comparing(Group::sequenceNo).thenComparing(Group::groupCode)).toList()));}).sorted(Comparator.comparing(Section::sequenceNo).thenComparing(Section::sectionCode)).toList();
        var formDtos=forms.stream().map(f->{var fields=db.rows(FieldEntity.class,org,"form_def_id",f.getId());fields.forEach(x->fieldCodes.put(x.getId(),x.getFieldCode()));return dto(f,Form.class,Map.of("fields",fields.stream().map(x->dto(x,Field.class,extras("groupCode",groupCodes.get(x.getGroupDefId()),"options",db.rows(OptionEntity.class,org,"field_def_id",x.getId()).stream().map(o->dto(o,Option.class,Map.of())).sorted(Comparator.comparing(Option::sequenceNo).thenComparing(Option::optionCode)).toList()))).sorted(Comparator.comparing(Field::sequenceNo).thenComparing(Field::fieldCode)).toList()));}).sorted(Comparator.comparing(Form::sequenceNo).thenComparing(Form::formCode)).toList();
        var ruleDtos=rules.stream().map(r->dto(r,Rule.class,extras("formCode",formCodes.get(r.getFormDefId()),"fieldCode",fieldCodes.get(r.getFieldDefId())))).sorted(Comparator.comparing(Rule::ruleCode)).toList();
        var signatures=db.rows(SignatureRuleEntity.class,org,"template_version_id",id).stream().map(r->dto(r,SignatureRule.class,Map.of())).sorted(Comparator.comparing(SignatureRule::sequenceNo).thenComparing(SignatureRule::objectCode)).toList();
        var reviews=db.rows(ReviewRuleEntity.class,org,"template_version_id",id).stream().map(r->dto(r,ReviewRule.class,Map.of())).sorted(Comparator.comparing(ReviewRule::sequenceNo).thenComparing(ReviewRule::objectCode)).toList();
        return new Definition(sectionDtos,formDtos,ruleDtos,signatures,reviews);
    }
    public List<Issue> lint(TemplateEntity template,Definition definition,boolean publication){
        List<Issue> issues=new ArrayList<>(EbrDefinitionRules.lint(definition,publication));if(!issues.isEmpty())return List.copyOf(issues);
        long org=template.getOrgId();
        check(issues,"packageVersionId",()->{if(publication)processes.requireUsable(org,template.getPackageVersionId());else processes.snapshot(org,template.getPackageVersionId());});
        for(var form:definition.forms()){
            if(form.operationDefId()!=null)check(issues,"forms."+form.formCode()+".operationDefId",()->processes.requireOperation(org,template.getPackageVersionId(),MasterMutation.id(form.operationDefId())));
            for(var field:form.fields()){
                if(field.unitId()!=null)check(issues,"fields."+field.fieldCode()+".unitId",()->units.unit(org,MasterMutation.id(field.unitId())));
                if(field.defaultExpr()!=null)references(org,field.defaultExpr(),"fields."+field.fieldCode()+".defaultExpr",issues);
            }
        }
        for(var rule:definition.rules())references(org,rule.expression(),"rules."+rule.ruleCode()+".expression",issues);
        for(var rule:definition.signatureRules())check(issues,"signatureRules."+rule.objectCode()+".requiredRole",()->identities.requireRole(rule.requiredRole()));
        for(var rule:definition.reviewRules())check(issues,"reviewRules."+rule.objectCode()+".requiredRole",()->identities.requireRole(rule.requiredRole()));
        return List.copyOf(issues);
    }
    private void references(long org,String source,String path,List<Issue> issues){var expression=EbrDsl.parse(source);for(String role:expression.roles())check(issues,path,()->identities.requireRole(role));for(var pair:expression.conversions())check(issues,path,()->units.convert(org,MasterMutation.id(pair.get(0)),MasterMutation.id(pair.get(1)),null,java.math.BigDecimal.ONE));}
    private void check(List<Issue> issues,String path,Runnable check){try{check.run();}catch(NoSuchElementException|IllegalArgumentException|ComplianceException ex){issues.add(new Issue(path,"INVALID_REFERENCE",ex.getMessage()));}}
    public void requireValid(TemplateEntity template,Definition definition,boolean publication){var issues=lint(template,definition,publication);if(!issues.isEmpty())throw new ComplianceException("LINT_FAILED",issues.getFirst().path()+": "+issues.getFirst().message());}
    public void save(CurrentPlatformContext context,TemplateEntity template,Definition definition){
        requireValid(template,definition,false);long org=context.organizationId(),id=template.getId();
        var oldSections=db.rows(SectionEntity.class,org,"template_version_id",id);retained(oldSections,SectionEntity::getSectionCode,definition.sections(),Section::sectionCode);
        var oldForms=db.rows(FormEntity.class,org,"template_version_id",id);retained(oldForms,FormEntity::getFormCode,definition.forms(),Form::formCode);
        Map<String,Long> groupIds=new HashMap<>(),formIds=new HashMap<>(),fieldIds=new HashMap<>(),ruleIds=new HashMap<>();
        List<SectionEntity> savedSections=new ArrayList<>();
        for(var s:definition.sections()){
            var row=find(oldSections,SectionEntity::getSectionCode,s.sectionCode(),SectionEntity::new);row.setTemplateVersionId(id);apply(s,row);write(context,row);savedSections.add(row);
            var oldGroups=db.rows(GroupEntity.class,org,"section_def_id",row.getId());retained(oldGroups,GroupEntity::getGroupCode,s.groups(),Group::groupCode);
            for(var g:s.groups()){var child=find(oldGroups,GroupEntity::getGroupCode,g.groupCode(),GroupEntity::new);child.setSectionDefId(row.getId());apply(g,child);write(context,child);groupIds.put(g.groupCode(),child.getId());}
        }
        for(var f:definition.forms()){
            var row=find(oldForms,FormEntity::getFormCode,f.formCode(),FormEntity::new);row.setTemplateVersionId(id);row.setStatus("DRAFT");apply(f,row);row.setSchemaJson(encode(f));write(context,row);formIds.put(f.formCode(),row.getId());
            var oldFields=db.rows(FieldEntity.class,org,"form_def_id",row.getId());retained(oldFields,FieldEntity::getFieldCode,f.fields(),Field::fieldCode);
            for(var field:f.fields()){
                var child=find(oldFields,FieldEntity::getFieldCode,field.fieldCode(),FieldEntity::new);ownership(child.getId(),child.getGroupDefId(),groupIds.get(field.groupCode()));child.setFormDefId(row.getId());child.setGroupDefId(groupIds.get(field.groupCode()));apply(field,child);write(context,child);fieldIds.put(field.fieldCode(),child.getId());
                var oldOptions=db.rows(OptionEntity.class,org,"field_def_id",child.getId());retained(oldOptions,OptionEntity::getOptionCode,field.options(),Option::optionCode);
                for(var option:field.options()){var op=find(oldOptions,OptionEntity::getOptionCode,option.optionCode(),OptionEntity::new);op.setFieldDefId(child.getId());apply(option,op);write(context,op);}
            }
        }
        var oldRules=db.rows(RuleEntity.class,org,"template_version_id",id);retained(oldRules,RuleEntity::getRuleCode,definition.rules(),Rule::ruleCode);
        for(var r:definition.rules()){var row=find(oldRules,RuleEntity::getRuleCode,r.ruleCode(),RuleEntity::new);ownership(row.getId(),row.getFormDefId(),formIds.get(r.formCode()));ownership(row.getId(),row.getFieldDefId(),fieldIds.get(r.fieldCode()));row.setTemplateVersionId(id);row.setFormDefId(formIds.get(r.formCode()));row.setFieldDefId(fieldIds.get(r.fieldCode()));apply(r,row);write(context,row);ruleIds.put(r.ruleCode(),row.getId());}
        for(int i=0;i<definition.sections().size();i++){var row=savedSections.get(i);Long visibility=ruleIds.get(definition.sections().get(i).visibilityRuleCode());if(!Objects.equals(row.getVisibilityRuleId(),visibility)){row.setVisibilityRuleId(visibility);db.store(SectionEntity.class).update(row,row.getVersionNo(),context.actorId(),List.of("visibilityRuleId"));}}
        var oldSignatures=db.rows(SignatureRuleEntity.class,org,"template_version_id",id);retained(oldSignatures,this::signatureKey,definition.signatureRules(),this::signatureKey);
        for(var rule:definition.signatureRules()){var row=find(oldSignatures,this::signatureKey,signatureKey(rule),SignatureRuleEntity::new);row.setTemplateVersionId(id);apply(rule,row);write(context,row);}
        var oldReviews=db.rows(ReviewRuleEntity.class,org,"template_version_id",id);retained(oldReviews,this::reviewKey,definition.reviewRules(),this::reviewKey);
        for(var rule:definition.reviewRules()){var row=find(oldReviews,this::reviewKey,reviewKey(rule),ReviewRuleEntity::new);row.setTemplateVersionId(id);apply(rule,row);write(context,row);}
    }
    public void publish(CurrentPlatformContext context,TemplateEntity template){
        Definition definition=load(template);
        for(var form:db.rows(FormEntity.class,context.organizationId(),"template_version_id",template.getId())){
            var portable=json.createObjectNode();portable.put("schemaVersion",form.getSchemaVersion());portable.set("form",json.valueToTree(definition.forms().stream().filter(f->f.formCode().equals(form.getFormCode())).findFirst().orElseThrow()));portable.set("rules",json.valueToTree(definition.rules().stream().filter(r->r.formCode()==null||r.formCode().equals(form.getFormCode())).toList()));form.setSchemaJson(encode(portable));form.setStatus("EFFECTIVE");db.store(FormEntity.class).update(form,form.getVersionNo(),context.actorId(),List.of("schemaJson","status"));
        }
    }
    public JsonNode canonical(TemplateEntity template){var n=json.createObjectNode();n.put("schemaVersion","1.0");n.put("templateVersionId",template.getId().toString());n.put("packageVersionId",template.getPackageVersionId().toString());n.put("templateCode",template.getTemplateCode());n.put("version",template.getBusinessVersion());n.set("definition",json.valueToTree(load(template)));return n;}
    private void ownership(Long id,Long before,Long after){if(id!=null&&!Objects.equals(before,after))throw new ResourceConflictException("RETAINED_DEFINITION_REQUIRED","Saved definition ownership must be retained");}
    private String signatureKey(SignatureRule r){return r.objectScope()+"|"+r.objectCode()+"|"+r.meaning()+"|"+r.sequenceNo();}
    private String signatureKey(SignatureRuleEntity r){return r.getObjectScope()+"|"+r.getObjectCode()+"|"+r.getMeaning()+"|"+r.getSequenceNo();}
    private String reviewKey(ReviewRule r){return r.objectScope()+"|"+r.objectCode()+"|"+r.reviewType()+"|"+r.sequenceNo();}
    private String reviewKey(ReviewRuleEntity r){return r.getObjectScope()+"|"+r.getObjectCode()+"|"+r.getReviewType()+"|"+r.getSequenceNo();}
    private <A,B>void retained(List<A> existing,Function<A,String> oldKey,List<B> desired,Function<B,String> newKey){Set<String> wanted=new HashSet<>();desired.forEach(x->wanted.add(newKey.apply(x)));for(var row:existing)if(!wanted.contains(oldKey.apply(row)))throw new ResourceConflictException("RETAINED_DEFINITION_REQUIRED","Saved definition rows must be retained; use an empty new draft version for replacement");}
    private <T>T find(List<T> values,Function<T,String> key,String wanted,Supplier<T> create){return values.stream().filter(x->key.apply(x).equals(wanted)).findFirst().orElseGet(create);}
    private void apply(Record record,ScopedEntity row){var bean=new BeanWrapperImpl(row);for(var field:record.getClass().getRecordComponents())try{if(bean.isWritableProperty(field.getName())){Object value=field.getAccessor().invoke(record);if(value instanceof String s&&bean.getPropertyType(field.getName())==Long.class)value=MasterMutation.id(s);bean.setPropertyValue(field.getName(),value);}}catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}}
    @SuppressWarnings("unchecked") private <T extends ScopedEntity>void write(CurrentPlatformContext c,T row){Class<T> type=(Class<T>)row.getClass();if(row.getId()==null)db.store(type).insert(row,c.organizationId(),c.actorId());else db.store(type).update(row,row.getVersionNo(),c.actorId(),Arrays.stream(type.getDeclaredFields()).map(java.lang.reflect.Field::getName).toList());}
    private <T>T dto(ScopedEntity row,Class<T> type,Map<String,Object> extra){var bean=new BeanWrapperImpl(row);ObjectNode node=json.createObjectNode();for(var field:type.getRecordComponents()){String key=field.getName();Object value=extra.containsKey(key)?extra.get(key):bean.isReadableProperty(key)?bean.getPropertyValue(key):null;if(value instanceof Long)value=value.toString();node.set(key,json.valueToTree(value));}return json.convertValue(node,type);}
    private Map<String,Object> extras(String a,Object av,String b,Object bv){Map<String,Object> map=new HashMap<>();map.put(a,av);map.put(b,bv);return map;}
    private String encode(Object value){try{return json.writeValueAsString(value);}catch(Exception ex){throw new IllegalArgumentException("Invalid definition",ex);}}
}
