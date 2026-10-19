package com.hospital.mes.ebr.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.common.exception.ComplianceException;
import java.util.*;
import java.util.function.Function;
import static com.hospital.mes.ebr.domain.EbrCommands.*;

public final class EbrDefinitionRules {
    public static final Set<String> FIELD_TYPES=Set.of("NUMBER","TEXT","TEXTAREA","ENUM","MULTI_ENUM","BOOLEAN","DATE","TIME","DATETIME","BARCODE","MATERIAL_LOT","CONTAINER","EQUIPMENT","PERSON","ATTACHMENT","IMAGE","TIMER","CALCULATED","INSTRUMENT_VALUE","SIGNATURE_PLACEHOLDER");
    public static final Set<String> TRIGGERS=Set.of("ON_CHANGE","ON_SAVE","ON_SUBMIT","ON_OPERATION_COMPLETE","ON_BATCH_CLOSE");
    private static final Set<String> NULLABLE=Set.of("groupCode","unitId","precisionScale","defaultExpr","placeholder","helpText","validationJson","visibilityRuleCode","minOccurs","maxOccurs","operationDefId","formCode","fieldCode","severity","errorCode","messageTemplate","canvasDocument","requiredRole","requiredRoleId","roleCodeSnapshot","roleNameSnapshot","schemaVersion","cppConfigJson");
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
        if(definition.schemaVersion()!=null&&!Set.of("1.0","2.0").contains(definition.schemaVersion()))issue(issues,"schemaVersion","INVALID_SCHEMA","Unsupported template definition schema");
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
            if(form.canvasDocument()!=null&&!form.canvasDocument().isNull()&&!form.canvasDocument().isMissingNode()){
                String canvasPath="forms."+form.formCode()+".canvasDocument";
                if(!form.canvasDocument().isObject())issue(issues,canvasPath,"INVALID_CANVAS_DOCUMENT","Canvas document must be a JSON object");
                else if(form.canvasDocument().toString().length()>2_000_000)issue(issues,canvasPath,"CANVAS_DOCUMENT_TOO_LARGE","Canvas document exceeds 2 MB");
                else if(!form.canvasDocument().path("data").path("main").isArray())issue(issues,canvasPath,"INVALID_CANVAS_DOCUMENT","Canvas editor data.main array required");
            }
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
            if(rule.cppConfigJson()!=null){
                var cpp=cppConfig(rule.cppConfigJson(),path+".cppConfigJson",issues);
                if(cpp!=null){
                    if(!"CPP".equals(cpp.path("kind").asText()))issue(issues,path+".cppConfigJson","INVALID_CPP_CONFIG","kind must be CPP");
                    if(rule.formCode()==null)issue(issues,path+".formCode","REQUIRED","CPP rule must belong to a form");
                    if(!"VALIDATION".equals(rule.ruleType()))issue(issues,path+".ruleType","INVALID_CPP_RULE_TYPE","CPP rule must use VALIDATION");
                    Form cppForm=rule.formCode()==null?null:forms.get(rule.formCode());
                    if(cppForm!=null)for(String ref:refs)if(cppForm.fields().stream().noneMatch(f->f.fieldCode().equals(ref)))issue(issues,path+".expression","CPP_CROSS_FORM_REFERENCE","CPP rule may only reference fields in its current form: "+ref);
                    String generated=cppExpression(cpp,cppForm,path+".cppConfigJson",issues);
                    if(generated!=null&&!generated.equals(rule.expression()))issue(issues,path+".expression","CPP_EXPRESSION_MISMATCH","CPP expression must be generated from cppConfigJson");
                    var actions=cpp.path("actions");
                    if(!actions.isArray())issue(issues,path+".cppConfigJson.actions","INVALID_CPP_ACTIONS","CPP actions array required");
                    else {
                        Set<String> seenActions=new HashSet<>();
                        for(var action:actions){
                            String value=action.isTextual()?action.asText():"";
                            if(!Set.of("MARK_EXCEPTION","BLOCK_SUBMIT","CREATE_DEVIATION","REQUIRE_QA_REVIEW").contains(value))issue(issues,path+".cppConfigJson.actions","INVALID_CPP_ACTION","Unsupported CPP action: "+value);
                            else if(!seenActions.add(value))issue(issues,path+".cppConfigJson.actions","DUPLICATE_CPP_ACTION","CPP action must be unique: "+value);
                        }
                    }
                    if(actions.isArray()&&java.util.stream.StreamSupport.stream(actions.spliterator(),false).anyMatch(a->"REQUIRE_QA_REVIEW".equals(a.asText()))&&rule.formCode()!=null&&definition.reviewRules().stream().noneMatch(r->"FORM".equals(r.objectScope())&&rule.formCode().equals(r.objectCode())))issue(issues,path+".cppConfigJson.actions","CPP_REVIEW_RULE_REQUIRED","QA review action requires an existing form review rule");
                }
            }
            if(rule.activeFlag()&&rule.fieldCode()!=null&&Set.of("CALCULATION","VISIBILITY","BRANCH").contains(rule.ruleType()))dependencies.computeIfAbsent(rule.fieldCode(),k->new LinkedHashSet<>()).addAll(refs);
            if(rule.ruleType().equals("CALCULATION")&&rule.fieldCode()==null)issue(issues,path+".fieldCode","REQUIRED","Calculation target field required");
            if(rule.ruleType().equals("CALCULATION")&&rule.activeFlag()&&!calculationTargets.add(rule.triggerPoint()+"|"+rule.fieldCode()))issue(issues,path+".fieldCode","DUPLICATE_CALCULATION_TARGET","Only one active calculation per trigger and field is permitted");
        }
        for(String key:dependencies.keySet())if(cycle(key,dependencies,new HashSet<>(),new HashSet<>())){issue(issues,"rules","CYCLIC_DEPENDENCY","Cyclic field dependency: "+key);break;}
        Set<String> policies=new HashSet<>();
        for(var rule:definition.signatureRules()){
            String path="signatureRules."+rule.objectCode();roleShape(definition.schemaVersion(),rule.requiredRoleId(),rule.requiredRole(),path,issues);referenceScope(rule.objectScope(),rule.objectCode(),sections,groups,forms,fields,path,issues);
            enumValue(rule.meaning(),Set.of("VERIFY","APPROVE"),path+".meaning",issues);
            if(!policies.add("S|"+rule.objectScope()+"|"+rule.objectCode()+"|"+rule.meaning()+"|"+rule.sequenceNo()))issue(issues,path,"DUPLICATE_CODE","Duplicate signature policy");
        }
        for(var rule:definition.reviewRules()){
            String path="reviewRules."+rule.objectCode();roleShape(definition.schemaVersion(),rule.requiredRoleId(),rule.requiredRole(),path,issues);referenceScope(rule.objectScope(),rule.objectCode(),sections,groups,forms,fields,path,issues);enumValue(rule.reviewType(),Set.of("VERIFY","APPROVE"),path+".reviewType",issues);
            if(!policies.add("R|"+rule.objectScope()+"|"+rule.objectCode()+"|"+rule.reviewType()+"|"+rule.sequenceNo()))issue(issues,path,"DUPLICATE_CODE","Duplicate review policy");
        }
        return List.copyOf(issues);
    }
    private static void roleShape(String schema,String id,String code,String path,List<Issue> issues){
        if("2.0".equals(schema)){try{if(id==null||!id.matches("[1-9][0-9]*")||Long.parseLong(id)<=0)throw new IllegalArgumentException();}catch(RuntimeException ex){issue(issues,path+".requiredRoleId","REQUIRED_ROLE_ID","schema2 requires a positive role ID; code fallback forbidden");}}
        else if(code==null||code.isBlank())issue(issues,path+".requiredRole","REQUIRED","Legacy role code required");
    }
    private static void referenceScope(String scope,String code,Map<String,Section> sections,Map<String,Group> groups,Map<String,Form> forms,Map<String,Field> fields,String path,List<Issue> issues){boolean found=switch(scope){case "SECTION"->sections.containsKey(code);case "GROUP"->groups.containsKey(code);case "FORM"->forms.containsKey(code);case "FIELD"->fields.containsKey(code);case "TEMPLATE"->true;default->false;};if(!found)issue(issues,path,"INVALID_REFERENCE","Policy target scope/code does not exist");}
    private static Set<String> expression(String source,String path,Map<String,Field> fields,List<Issue> issues){try{var expr=EbrDsl.parse(source);for(String ref:expr.references())if(!fields.containsKey(ref))issue(issues,path,"UNKNOWN_FIELD","Unknown field: "+ref);return expr.references();}catch(IllegalArgumentException ex){issue(issues,path,"UNSAFE_EXPRESSION",ex.getMessage());return Set.of();}}
    private static com.fasterxml.jackson.databind.JsonNode cppConfig(String source,String path,List<Issue> issues){try{var node=new ObjectMapper().readTree(source);if(node==null||!node.isObject())throw new IllegalArgumentException();return node;}catch(Exception ex){issue(issues,path,"INVALID_CPP_CONFIG","CPP config must be a JSON object");return null;}}
    private static String cppExpression(com.fasterxml.jackson.databind.JsonNode cpp,Form form,String path,List<Issue> issues){
        if(form==null)return null;
        closed(cpp,Set.of("kind","name","description","rootLogic","groups","actions"),path,issues);
        String name=cpp.path("name").asText("").strip();if(name.isEmpty()||name.length()>100)issue(issues,path+".name","INVALID_CPP_NAME","CPP rule name required, maximum 100");
        String description=cpp.path("description").asText("");if(description.length()>500)issue(issues,path+".description","INVALID_CPP_DESCRIPTION","CPP description maximum 500");
        String root=cpp.path("rootLogic").asText();if(!Set.of("AND","OR").contains(root))issue(issues,path+".rootLogic","INVALID_CPP_LOGIC","rootLogic must be AND or OR");
        var groups=cpp.path("groups");if(!groups.isArray()||groups.isEmpty()){issue(issues,path+".groups","INVALID_CPP_GROUPS","At least one CPP condition group required");return null;}
        if(groups.size()>20){issue(issues,path+".groups","TOO_LARGE","Maximum 20 CPP groups");return null;}
        Map<String,Field> available=new HashMap<>();form.fields().forEach(f->available.put(f.fieldCode(),f));
        List<String> renderedGroups=new ArrayList<>();
        for(int gi=0;gi<groups.size();gi++){
            var group=groups.get(gi);String gp=path+".groups["+gi+"]";if(!group.isObject()){issue(issues,gp,"INVALID_CPP_GROUP","CPP group must be an object");continue;}
            closed(group,Set.of("id","logic","conditions"),gp,issues);
            String logic=group.path("logic").asText();if(!Set.of("AND","OR").contains(logic))issue(issues,gp+".logic","INVALID_CPP_LOGIC","Group logic must be AND or OR");
            var conditions=group.path("conditions");if(!conditions.isArray()||conditions.isEmpty()){issue(issues,gp+".conditions","INVALID_CPP_CONDITIONS","At least one condition required");continue;}
            if(conditions.size()>20){issue(issues,gp+".conditions","TOO_LARGE","Maximum 20 CPP conditions per group");continue;}
            List<String> rendered=new ArrayList<>();
            for(int ci=0;ci<conditions.size();ci++){
                var condition=conditions.get(ci);String cp=gp+".conditions["+ci+"]";if(!condition.isObject()){issue(issues,cp,"INVALID_CPP_CONDITION","CPP condition must be an object");continue;}
                closed(condition,Set.of("id","fieldCode","operator","value"),cp,issues);
                String code=condition.path("fieldCode").asText(),operator=condition.path("operator").asText(),value=condition.path("value").asText("");
                Field field=available.get(code);if(field==null){issue(issues,cp+".fieldCode","CPP_CROSS_FORM_REFERENCE","CPP field must belong to current form: "+code);continue;}
                boolean numeric=Set.of("NUMBER","TIMER","CALCULATED","INSTRUMENT_VALUE").contains(field.fieldType());
                boolean bool="BOOLEAN".equals(field.fieldType());
                Set<String> allowed=numeric?Set.of(">=", "<=", ">", "<", "==", "!="):Set.of("==","!=");
                if(!allowed.contains(operator)){issue(issues,cp+".operator","INVALID_CPP_OPERATOR","Unsupported operator for field type");continue;}
                String literal;
                if(numeric){try{if(value.isBlank()||value.length()>40)throw new IllegalArgumentException();new java.math.BigDecimal(value);literal=value;}catch(Exception ex){issue(issues,cp+".value","INVALID_CPP_VALUE","Numeric CPP value required");continue;}}
                else if(bool){if(!Set.of("true","false").contains(value.toLowerCase(Locale.ROOT))){issue(issues,cp+".value","INVALID_CPP_VALUE","Boolean CPP value must be true or false");continue;}literal=value.toLowerCase(Locale.ROOT);}
                else {if(value.length()>1000){issue(issues,cp+".value","INVALID_CPP_VALUE","CPP text value maximum 1000");continue;}literal="'"+dslEscape(value)+"'";}
                rendered.add("(value('"+dslEscape(code)+"') "+operator+" "+literal+")");
            }
            if(!rendered.isEmpty())renderedGroups.add("("+String.join("AND".equals(logic)?" && ":" || ",rendered)+")");
        }
        if(renderedGroups.size()!=groups.size())return null;
        return String.join("AND".equals(root)?" && ":" || ",renderedGroups);
    }
    private static String dslEscape(String value){return value.replace("\\","\\\\").replace("'","\\'");}
    private static void closed(com.fasterxml.jackson.databind.JsonNode node,Set<String> allowed,String path,List<Issue> issues){node.fieldNames().forEachRemaining(name->{if(!allowed.contains(name))issue(issues,path+"."+name,"UNKNOWN_CPP_FIELD","Unsupported CPP config field: "+name);});}
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
            if(name.equals("schemaVersion")&&value instanceof Form)nullable=false;
            if(field==null){if(!nullable)issue(issues,p,"REQUIRED","Field required");continue;}
            if(field instanceof String s){int max=switch(name){case "expression","defaultExpr"->4000;case "validationJson"->16000;case "cppConfigJson"->32000;case "helpText","messageTemplate"->1000;case "placeholder","optionValue"->500;case "title","label","formName","optionLabel"->200;case "schemaVersion"->20;case "roleNameSnapshot"->128;case "dataType","objectScope"->30;case "meaning"->100;default->64;};if(s.isBlank()||s.length()>max)issue(issues,p,"INVALID_LENGTH","Nonblank field, maximum "+max);}
            if(field instanceof Integer n&&n<0)issue(issues,p,"INVALID_RANGE","Nonnegative value required");
            if(field instanceof List<?>||field.getClass().isRecord())shape(field,p,issues,depth+1);
        }catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
    }
}
