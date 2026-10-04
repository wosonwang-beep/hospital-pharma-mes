package com.hospital.mes.ebr.application;

import com.fasterxml.jackson.databind.*;
import com.hospital.mes.ebr.domain.*;
import java.util.*;
import java.time.Instant;
import static com.hospital.mes.ebr.domain.EbrCommands.*;

/** Pure simulation/consumer engine. Production evidence persistence belongs to LG-007B. */
@org.springframework.stereotype.Component
public class EbrEngine {
    private final ObjectMapper json;
    public EbrEngine(ObjectMapper json){this.json=json;}
    public SimulationResult evaluate(Definition definition,List<Input> inputs,String trigger,Set<String> roles,
            Map<String,String> statuses,Instant at,EbrDsl.Converter converter,String version) {
        return evaluateWithValues(definition,inputs,trigger,roles,statuses,at,converter,version).result();
    }
    public record RuntimeEvaluation(SimulationResult result,List<Input> effectiveInputs){}
    public RuntimeEvaluation evaluateWithValues(Definition definition,List<Input> inputs,String trigger,Set<String> roles,Map<String,String> statuses,Instant at,EbrDsl.Converter converter,String version){
        if(trigger==null||!EbrDefinitionRules.TRIGGERS.contains(trigger)||inputs==null||inputs.size()>500)throw new IllegalArgumentException("Invalid simulation input");
        Map<String,Field> fields=new LinkedHashMap<>();definition.forms().forEach(f->f.fields().forEach(x->fields.put(x.fieldCode(),x)));
        Map<String,List<Object>> values=new LinkedHashMap<>();
        for(var input:inputs){
            if(input==null||!fields.containsKey(input.fieldCode())||values.containsKey(input.fieldCode())||input.values()==null||input.values().size()>500)throw new IllegalArgumentException("Invalid or duplicate input field");
            List<Object> list=new ArrayList<>();
            for(var value:input.values()){
                if(value==null||value.isNull()||value.isContainerNode()||value.toString().length()>4000)throw new IllegalArgumentException("Primitive input required");
                String type=fields.get(input.fieldCode()).fieldType();
                if(Set.of("NUMBER","TIMER","CALCULATED","INSTRUMENT_VALUE").contains(type)&&!value.isNumber())throw new IllegalArgumentException("Numeric input required");
                if(type.equals("BOOLEAN")&&!value.isBoolean())throw new IllegalArgumentException("Boolean input required");
                if(Set.of("ENUM","MULTI_ENUM").contains(type)&&fields.get(input.fieldCode()).options().stream().noneMatch(o->o.activeFlag()&&o.optionValue().equals(value.asText())))throw new IllegalArgumentException("Active option required");
                list.add(value.isNumber()?value.decimalValue():value.isBoolean()?value.booleanValue():value.asText());
            }
            values.put(input.fieldCode(),list);
        }
        var context=new EbrDsl.Context(values,Set.copyOf(roles),Map.copyOf(statuses),at,converter);
        Map<String,Rule> calculations=new LinkedHashMap<>();definition.rules().stream().filter(r->r.activeFlag()&&r.triggerPoint().equals(trigger)&&r.ruleType().equals("CALCULATION")).forEach(r->calculations.put(r.fieldCode(),r));
        List<RuleResult> results=new ArrayList<>();Set<String> computed=new HashSet<>(),visiting=new HashSet<>();
        for(String field:fields.keySet())compute(field,fields,calculations,context,computed,visiting,results,version);
        for(var rule:definition.rules())if(rule.activeFlag()&&rule.triggerPoint().equals(trigger)&&!rule.ruleType().equals("CALCULATION"))results.add(execute(rule,context,version));
        for(var field:fields.values())if(field.requiredFlag()&&(!values.containsKey(field.fieldCode())||values.get(field.fieldCode()).stream().noneMatch(EbrRuntimeRules::hasValue)))results.add(new RuleResult("REQUIRED:"+field.fieldCode(),false,"BLOCK","Required field has no value",snapshot(values),json.nullNode(),at.toString(),EbrDsl.ENGINE_VERSION,version));
        return new RuntimeEvaluation(new SimulationResult(results.stream().noneMatch(r->!r.passed()&&"BLOCK".equals(r.severity())),List.copyOf(results)),snapshot(values));
    }
    private void compute(String code,Map<String,Field> fields,Map<String,Rule> rules,EbrDsl.Context context,Set<String> completed,Set<String> visiting,List<RuleResult> results,String version){
        if(completed.contains(code))return;if(!visiting.add(code))throw new IllegalArgumentException("Cyclic calculation");
        var field=fields.get(code);var rule=rules.get(code);String expression=rule==null?field.defaultExpr():rule.expression();
        if(expression!=null&&(rule!=null||!context.values().containsKey(code))){
            for(String dependency:EbrDsl.parse(expression).references())compute(dependency,fields,rules,context,completed,visiting,results,version);
            if(rule!=null){var result=execute(rule,context,version);results.add(result);if(result.passed())context.values().put(code,List.of(primitive(result.outputValue())));}
            else try{context.values().put(code,List.of(EbrDsl.parse(expression).evaluate(context)));}catch(IllegalArgumentException ex){results.add(new RuleResult("DEFAULT:"+code,false,"BLOCK",ex.getMessage(),snapshot(context.values()),json.nullNode(),context.time().toString(),EbrDsl.ENGINE_VERSION,version));}
        }
        visiting.remove(code);completed.add(code);
    }
    private Object primitive(JsonNode value){return value.isNumber()?value.decimalValue():value.isBoolean()?value.booleanValue():value.asText();}
    private RuleResult execute(Rule rule,EbrDsl.Context context,String version){
        var input=snapshot(context.values());String severity=rule.severity()==null?"BLOCK":rule.severity();
        try{
            Object output=EbrDsl.parse(rule.expression()).evaluate(context);
            boolean passed=Set.of("CALCULATION","VISIBILITY","BRANCH","DEVIATION").contains(rule.ruleType())||EbrDsl.truth(output);
            if(Set.of("VISIBILITY","BRANCH","DEVIATION").contains(rule.ruleType()))EbrDsl.truth(output);
            return new RuleResult(rule.ruleCode(),passed,severity,passed?null:rule.messageTemplate(),input,json.valueToTree(output),context.time().toString(),EbrDsl.ENGINE_VERSION,version);
        }catch(IllegalArgumentException ex){return new RuleResult(rule.ruleCode(),false,severity,ex.getMessage(),input,json.nullNode(),context.time().toString(),EbrDsl.ENGINE_VERSION,version);}
    }
    private List<Input> snapshot(Map<String,List<Object>> values){return values.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e->new Input(e.getKey(),e.getValue().stream().map(v->(JsonNode)json.valueToTree(v)).toList())).toList();}
}
