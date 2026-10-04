package com.hospital.mes.ebr.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.common.exception.ComplianceException;
import java.util.*;
import java.util.function.Function;
import static com.hospital.mes.ebr.domain.EbrCommands.*;

public final class EbrDefinitionRules {
    public static final Set<String> FIELD_TYPES=Set.of("NUMBER","TEXT","TEXTAREA","ENUM","MULTI_ENUM","BOOLEAN","DATE","TIME","DATETIME","BARCODE","MATERIAL_LOT","CONTAINER","EQUIPMENT","PERSON","ATTACHMENT","IMAGE","TIMER","CALCULATED","INSTRUMENT_VALUE","SIGNATURE_PLACEHOLDER");
    public static final Set<String> TRIGGERS=Set.of("ON_CHANGE","ON_SAVE","ON_SUBMIT","ON_OPERATION_COMPLETE","ON_BATCH_CLOSE");
    private static final Set<String> NULLABLE=Set.of("groupCode","unitId","precisionScale","defaultExpr","placeholder","helpText","validationJson","visibilityRuleCode","minOccurs","maxOccurs","operationDefId","formCode","fieldCode","severity","errorCode","messageTemplate");
    private EbrDefinitionRules(){}
    public static String text(String value,int maximum,String path){if(value==null||value.isBlank()||value.length()>maximum)throw new IllegalArgumentException(path+": required, maximum "+maximum);return value.strip();}
    public static void gate(boolean value,String message){if(!value)throw new ComplianceException("LINT_FAILED",message);}
    public static void draft(String state){gate("DRAFT".equals(state),"Only draft definitions are editable");}
    public static String transition(String state,String action){
        if(state.equals("DRAFT")&&action.equals("SUBMIT"))return "SUBMITTED";
        if(state.equals("SUBMITTED")&&action.equals("APPROVE"))return "APPROVED";
        if(state.equals("APPROVED")&&action.equals("PUBLISH"))return "EFFECTIVE";
        throw new ComplianceException("STATE_TRANSITION_NOT_ALLOWED","Definition transition not allowed");
    }
    public static List<Issue> lint(Definition definition,boolean publication){
        List<Issue> issues=new ArrayList<>();shape(definition,"definition",issues,0);if(!issues.isEmpty())return List.copyOf(issues);
        var sections=index(definition.sections(),Section::sectionCode,"sections",issues);
        var groups=index(definition.sections().stream().flatMap(s->s.groups().stream()).toList(),Group::groupCode,"groups",issues);
        var forms=index(definition.forms(),Form::formCode,"forms",issues);
        var fields=index(definition.forms().stream().flatMap(f->f.fields().stream()).toList(),Field::fieldCode,"forms.fields",issues);
        var rules=index(definition.rules(),Rule::ruleCode,"rules",issues);
        if(publication&&forms.isEmpty())issue(issues,"forms","REQUIRED","At least one form is required");
        for(var section:sections.values()){
            enumValue(section.repeatMode(),Set.of("NONE","LIST"),"sections."+section.sectionCode()+".repeatMode",issues);
            if(section.visibilityRuleCode()!=null){Rule rule=rules.get(section.visibilityRuleCode());if(rule==null||!rule.ruleType().equals("VISIBILITY")||!rule.activeFlag())issue(issues,"sections."+section.sectionCode()+".visibilityRuleCode","INVALID_REFERENCE","Active visibility rule required");}
        }
        for(var group:groups.values()){
            String path="groups."+group.groupCode();enumValue(group.repeatMode(),Set.of("NONE","LIST"),path+".repeatMode",issues);
            if(group.layoutColumns()<1||group.layoutColumns()>12)issue(issues,path+".layoutColumns","INVALID_RANGE","Columns must be 1..12");
            if(group.minOccurs()!=null&&group.maxOccurs()!=null&&group.minOccurs()>group.maxOccurs())issue(issues,path,"INVALID_RANGE","minOccurs exceeds maxOccurs");
        }
        Map<String,Set<String>> dependencies=new LinkedHashMap<>();
        for(var form:forms.values()){
            if(publication&&form.fields().isEmpty())issue(issues,"forms."+form.formCode()+".fields","REQUIRED","Form requires fields");
            for(var field:form.fields()){
                String path="forms."+form.formCode()+".fields."+field.fieldCode();
                enumValue(field.fieldType(),FIELD_TYPES,path+".fieldType",issues);enumValue(field.sourceType(),Set.of("MANUAL","BARCODE","INSTRUMENT","SYSTEM","DERIVED"),path+".sourceType",issues);
                if(field.groupCode()!=null&&!groups.containsKey(field.groupCode()))issue(issues,path+".groupCode","INVALID_REFERENCE","Group does not exist");
                if(field.precisionScale()!=null&&(field.precisionScale()<0||field.precisionScale()>12))issue(issues,path+".precisionScale","INVALID_RANGE","Precision must be 0..12");
                index(field.options(),Option::optionCode,path+".options",issues);
                if(publication&&Set.of("ENUM","MULTI_ENUM").contains(field.fieldType())&&field.options().stream().noneMatch(Option::activeFlag))issue(issues,path+".options","REQUIRED","Enumeration requires active options");
                if(field.fieldType().equals("INSTRUMENT_VALUE")&&!field.sourceType().equals("INSTRUMENT"))issue(issues,path+".sourceType","SOURCE_MISMATCH","Instrument value must use INSTRUMENT source");
                if(field.fieldType().equals("CALCULATED")&&!field.sourceType().equals("DERIVED"))issue(issues,path+".sourceType","SOURCE_MISMATCH","Calculated value must use DERIVED source");
                if(field.defaultExpr()!=null)dependencies.computeIfAbsent(field.fieldCode(),k->new LinkedHashSet<>()).addAll(expression(field.defaultExpr(),path+".defaultExpr",fields,issues));
                if(field.validationJson()!=null)try{var node=new ObjectMapper().readTree(field.validationJson());if(node==null||!node.isObject())throw new IllegalArgumentException();}catch(Exception e){issue(issues,path+".validationJson","INVALID_JSON","Validation must be a JSON object");}
            }
        }
        Set<String> calculationTargets=new HashSet<>();
        for(var rule:rules.values()){
            String path="rules."+rule.ruleCode();enumValue(rule.ruleType(),Set.of("VALIDATION","CALCULATION","VISIBILITY","BRANCH","COMPLETION","SIGNATURE","REVIEW","DEVIATION"),path+".ruleType",issues);enumValue(rule.triggerPoint(),TRIGGERS,path+".triggerPoint",issues);
            if(rule.severity()!=null)enumValue(rule.severity(),Set.of("BLOCK","WARN"),path+".severity",issues);
            if(rule.formCode()!=null&&!forms.containsKey(rule.formCode()))issue(issues,path+".formCode","INVALID_REFERENCE","Form does not exist");
            if(rule.fieldCode()!=null&&!fields.containsKey(rule.fieldCode()))issue(issues,path+".fieldCode","INVALID_REFERENCE","Field does not exist");
            if(rule.fieldCode()!=null&&rule.formCode()!=null&&forms.containsKey(rule.formCode())&&forms.get(rule.formCode()).fields().stream().noneMatch(f->f.fieldCode().equals(rule.fieldCode())))issue(issues,path+".fieldCode","INVALID_REFERENCE","Field is not in the specified form");
            var refs=expression(rule.expression(),path+".expression",fields,issues);
            if(rule.activeFlag()&&rule.fieldCode()!=null&&Set.of("CALCULATION","VISIBILITY","BRANCH").contains(rule.ruleType()))dependencies.computeIfAbsent(rule.fieldCode(),k->new LinkedHashSet<>()).addAll(refs);
            if(rule.ruleType().equals("CALCULATION")&&rule.fieldCode()==null)issue(issues,path+".fieldCode","REQUIRED","Calculation target field required");
            if(rule.ruleType().equals("CALCULATION")&&rule.activeFlag()&&!calculationTargets.add(rule.triggerPoint()+"|"+rule.fieldCode()))issue(issues,path+".fieldCode","DUPLICATE_CALCULATION_TARGET","Only one active calculation per trigger and field is permitted");
        }
        for(String key:dependencies.keySet())if(cycle(key,dependencies,new HashSet<>(),new HashSet<>())){issue(issues,"rules","CYCLIC_DEPENDENCY","Cyclic field dependency: "+key);break;}
        Set<String> policies=new HashSet<>();
        for(var rule:definition.signatureRules()){
            String path="signatureRules."+rule.objectCode();referenceScope(rule.objectScope(),rule.objectCode(),sections,groups,forms,fields,path,issues);
            enumValue(rule.meaning(),Set.of("VERIFY","APPROVE"),path+".meaning",issues);
            if(!policies.add("S|"+rule.objectScope()+"|"+rule.objectCode()+"|"+rule.meaning()+"|"+rule.sequenceNo()))issue(issues,path,"DUPLICATE_CODE","Duplicate signature policy");
        }
        for(var rule:definition.reviewRules()){
            String path="reviewRules."+rule.objectCode();referenceScope(rule.objectScope(),rule.objectCode(),sections,groups,forms,fields,path,issues);enumValue(rule.reviewType(),Set.of("VERIFY","APPROVE"),path+".reviewType",issues);
            if(!policies.add("R|"+rule.objectScope()+"|"+rule.objectCode()+"|"+rule.reviewType()+"|"+rule.sequenceNo()))issue(issues,path,"DUPLICATE_CODE","Duplicate review policy");
        }
        return List.copyOf(issues);
    }
    private static void referenceScope(String scope,String code,Map<String,Section> sections,Map<String,Group> groups,Map<String,Form> forms,Map<String,Field> fields,String path,List<Issue> issues){boolean found=switch(scope){case "SECTION"->sections.containsKey(code);case "GROUP"->groups.containsKey(code);case "FORM"->forms.containsKey(code);case "FIELD"->fields.containsKey(code);case "TEMPLATE"->true;default->false;};if(!found)issue(issues,path,"INVALID_REFERENCE","Policy target scope/code does not exist");}
    private static Set<String> expression(String source,String path,Map<String,Field> fields,List<Issue> issues){try{var expr=EbrDsl.parse(source);for(String ref:expr.references())if(!fields.containsKey(ref))issue(issues,path,"UNKNOWN_FIELD","Unknown field: "+ref);return expr.references();}catch(IllegalArgumentException ex){issue(issues,path,"UNSAFE_EXPRESSION",ex.getMessage());return Set.of();}}
    private static boolean cycle(String key,Map<String,Set<String>> graph,Set<String> visiting,Set<String> visited){if(visiting.contains(key))return true;if(!visited.add(key))return false;visiting.add(key);for(String next:graph.getOrDefault(key,Set.of()))if(cycle(next,graph,visiting,visited))return true;visiting.remove(key);return false;}
    private static <T> Map<String,T> index(List<T> values,Function<T,String> code,String path,List<Issue> issues){var result=new LinkedHashMap<String,T>();for(T value:values){String key=code.apply(value);if(result.putIfAbsent(key,value)!=null)issue(issues,path+"."+key,"DUPLICATE_CODE","Duplicate stable code: "+key);}return result;}
    private static void enumValue(String value,Set<String> allowed,String path,List<Issue> issues){if(!allowed.contains(value))issue(issues,path,"INVALID_ENUM","Unsupported value: "+value);}
    private static void issue(List<Issue> issues,String path,String code,String message){if(issues.size()<200)issues.add(new Issue(path,code,message));}
    private static void shape(Object value,String path,List<Issue> issues,int depth){
        if(depth>12){issue(issues,path,"TOO_DEEP","Definition nesting exceeded");return;}
        if(value==null){issue(issues,path,"REQUIRED","Definition or child required");return;}
        if(value instanceof List<?> list){if(list.size()>500){issue(issues,path,"TOO_LARGE","Maximum 500 elements");return;}for(int i=0;i<list.size();i++)shape(list.get(i),path+"["+i+"]",issues,depth+1);return;}
        if(!value.getClass().isRecord())return;
        for(var component:value.getClass().getRecordComponents())try{
            String name=component.getName();Object field=component.getAccessor().invoke(value);String p=path+"."+name;
            boolean nullable=NULLABLE.contains(name)&&!(name.equals("formCode")&&value instanceof Form)&&!(name.equals("fieldCode")&&value instanceof Field)||name.equals("title")&&value instanceof Group;
            if(field==null){if(!nullable)issue(issues,p,"REQUIRED","Field required");continue;}
            if(field instanceof String s){int max=switch(name){case "expression","defaultExpr"->4000;case "validationJson"->16000;case "helpText","messageTemplate"->1000;case "placeholder","optionValue"->500;case "title","label","formName","optionLabel"->200;case "schemaVersion"->20;case "dataType","objectScope"->30;case "meaning"->100;default->64;};if(s.isBlank()||s.length()>max)issue(issues,p,"INVALID_LENGTH","Nonblank field, maximum "+max);}
            if(field instanceof Integer n&&n<0)issue(issues,p,"INVALID_RANGE","Nonnegative value required");
            if(field instanceof List<?>||field.getClass().isRecord())shape(field,p,issues,depth+1);
        }catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
    }
}
