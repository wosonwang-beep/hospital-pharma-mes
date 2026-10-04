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
}
