package com.hospital.mes.ebr.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.ebr.application.EbrEngine;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.time.Instant;
import static org.assertj.core.api.Assertions.*;
import static com.hospital.mes.ebr.domain.EbrCommands.*;

class EbrEngineTest {
    final ObjectMapper json=new ObjectMapper();final EbrEngine engine=new EbrEngine(json);
    Field field(String code,String source,String expression){return new Field(code,null,code,source.equals("DERIVED")?"CALCULATED":"NUMBER",source,"DECIMAL",null,2,true,source.equals("DERIVED"),expression,null,null,1,null,List.of());}
    Rule rule(String code,String type,String target,String expression,String severity){return new Rule(code,"F",target,type,"ON_SUBMIT",expression,severity,"LIMIT","Outside limit",false,true);}
    Definition definition(List<Field> fields,List<Rule> rules){return new Definition(List.of(),List.of(new Form("F","Form",null,"1.0",1,fields)),rules,List.of(),List.of());}
    SimulationResult run(Definition d,int value){return engine.evaluate(d,List.of(new Input("A",List.of(json.valueToTree(value)))),"ON_SUBMIT",Set.of(),Map.of("F","DRAFT"),Instant.parse("2026-10-03T00:00:00Z"),(n,a,b)->n,"1");}
    @Test void blockAndWarnProduceDifferentAllowedActionsWithEvidence(){
        var fields=List.of(field("A","MANUAL",null));
        var block=run(definition(fields,List.of(rule("LIMIT","VALIDATION",null,"value('A') < 5","BLOCK"))),10);
        assertThat(block.allowed()).isFalse();assertThat(block.results().getFirst().inputSnapshot()).hasSize(1);
        assertThat(run(definition(fields,List.of(rule("LIMIT","VALIDATION",null,"value('A') < 5","WARN"))),10).allowed()).isTrue();
    }
    @Test void calculationDependencyOrderAndConditionalVisibilityRemainDeterministic(){
        var d=definition(List.of(field("C","DERIVED",null),field("B","DERIVED",null),field("A","MANUAL",null)),List.of(rule("C","CALCULATION","C","value('B')*2",null),rule("B","CALCULATION","B","value('A')+1",null),rule("HIDE","VISIBILITY",null,"value('C') < 1",null)));
        var result=run(d,2);assertThat(result.allowed()).isTrue();assertThat(result.results().stream().filter(r->r.ruleCode().equals("C")).findFirst().orElseThrow().outputValue().decimalValue()).isEqualByComparingTo("6");assertThat(result.results().stream().filter(r->r.ruleCode().equals("HIDE")).findFirst().orElseThrow().outputValue().asBoolean()).isFalse();
        assertThat(run(d,2)).isEqualTo(result);
    }
    @Test void requiredFieldsAndMalformedInputFailClosed(){
        var d=definition(List.of(field("A","MANUAL",null)),List.of());
        assertThat(engine.evaluate(d,List.of(),"ON_SUBMIT",Set.of(),Map.of(),Instant.EPOCH,(n,a,b)->n,"1").allowed()).isFalse();
        assertThatThrownBy(()->engine.evaluate(d,List.of(new Input("A",List.of(json.valueToTree("text")))),"ON_SUBMIT",Set.of(),Map.of(),Instant.EPOCH,(n,a,b)->n,"1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->engine.evaluate(d,List.of(),null,Set.of(),Map.of(),Instant.EPOCH,(n,a,b)->n,"1")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void cppRuleEvaluatesDuringEntryAndBlocksOnlyOnSubmit(){
        var fields=List.of(field("A","MANUAL",null),field("B","MANUAL",null));
        var config="{\"kind\":\"CPP\",\"name\":\"灭菌参数\",\"description\":\"\",\"rootLogic\":\"AND\",\"groups\":[{\"id\":\"G1\",\"logic\":\"AND\",\"conditions\":[{\"id\":\"C1\",\"fieldCode\":\"A\",\"operator\":\">=\",\"value\":\"121\"},{\"id\":\"C2\",\"fieldCode\":\"A\",\"operator\":\"<=\",\"value\":\"125\"},{\"id\":\"C3\",\"fieldCode\":\"B\",\"operator\":\">=\",\"value\":\"20\"}]}],\"actions\":[\"MARK_EXCEPTION\",\"BLOCK_SUBMIT\"]}";
        var cpp=new Rule("CPP1","F",null,"VALIDATION","ON_CHANGE","((value('A') >= 121) && (value('A') <= 125) && (value('B') >= 20))","BLOCK","CPP_RULE_FAILED","超出工艺范围",false,true,config);
        var d=definition(fields,List.of(cpp));
        var bad=List.of(new Input("A",List.of(json.valueToTree(128))),new Input("B",List.of(json.valueToTree(25))));
        var onChange=engine.evaluate(d,bad,"ON_CHANGE",Set.of(),Map.of(),Instant.EPOCH,(n,a,b)->n,"1");
        assertThat(onChange.allowed()).isTrue();
        assertThat(onChange.results()).anySatisfy(r->{assertThat(r.ruleCode()).isEqualTo("CPP1");assertThat(r.passed()).isFalse();assertThat(r.severity()).isEqualTo("WARN");});
        var onSubmit=engine.evaluate(d,bad,"ON_SUBMIT",Set.of(),Map.of(),Instant.EPOCH,(n,a,b)->n,"1");
        assertThat(onSubmit.allowed()).isFalse();
        assertThat(onSubmit.results()).anySatisfy(r->{assertThat(r.ruleCode()).isEqualTo("CPP1");assertThat(r.severity()).isEqualTo("BLOCK");});
        var good=List.of(new Input("A",List.of(json.valueToTree(123))),new Input("B",List.of(json.valueToTree(25))));
        assertThat(engine.evaluate(d,good,"ON_SUBMIT",Set.of(),Map.of(),Instant.EPOCH,(n,a,b)->n,"1").allowed()).isTrue();
    }
}
