package com.hospital.mes.ebr.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static com.hospital.mes.ebr.domain.EbrCommands.*;

class EbrDefinitionRulesTest {
    final ObjectMapper json=new ObjectMapper();
    Definition definition(String fields,String rules) throws Exception {
        return json.readValue("{\"sections\":[],\"forms\":[{\"formCode\":\"F\",\"formName\":\"Form\",\"schemaVersion\":\"1.0\",\"sequenceNo\":1,\"fields\":["+fields+"]}],\"rules\":["+rules+"],\"signatureRules\":[],\"reviewRules\":[]}",Definition.class);
    }
    String field(String code,String type) { return "{\"fieldCode\":\""+code+"\",\"label\":\"Weight\",\"fieldType\":\""+type+"\",\"sourceType\":\"MANUAL\",\"dataType\":\"DECIMAL\",\"requiredFlag\":true,\"readonlyFlag\":false,\"sequenceNo\":1,\"options\":[]}"; }
    @Test void tcEbr001DuplicateStableFieldIdHasConcretePath() throws Exception {
        var issues=EbrDefinitionRules.lint(definition(field("A","NUMBER")+","+field("A","NUMBER"),""),true);
        assertThat(issues).anySatisfy(i->{assertThat(i.code()).isEqualTo("DUPLICATE_CODE");assertThat(i.path()).contains("fields");});
    }
    @Test void missingReferenceAndInvalidExpressionAreReported() throws Exception {
        var d=definition(field("A","NUMBER"),"{\"ruleCode\":\"R\",\"ruleType\":\"VALIDATION\",\"triggerPoint\":\"ON_SUBMIT\",\"expression\":\"value('missing')>0\",\"severity\":\"BLOCK\",\"deviationTrigger\":false,\"activeFlag\":true}");
        assertThat(EbrDefinitionRules.lint(d,true)).anySatisfy(i->assertThat(i.code()).isEqualTo("UNKNOWN_FIELD"));
    }
    @Test void immutableLifecycleAndEmptyPublicationAreBlocked() {
        assertThat(EbrDefinitionRules.transition("DRAFT","SUBMIT")).isEqualTo("SUBMITTED");
        assertThat(EbrDefinitionRules.transition("SUBMITTED","APPROVE")).isEqualTo("APPROVED");
        assertThat(EbrDefinitionRules.transition("APPROVED","PUBLISH")).isEqualTo("EFFECTIVE");
        assertThatThrownBy(()->EbrDefinitionRules.transition("EFFECTIVE","SUBMIT")).isInstanceOf(RuntimeException.class);
        assertThat(EbrDefinitionRules.lint(new Definition(List.of(),List.of(),List.of(),List.of(),List.of()),true)).isNotEmpty();
    }
    @Test void fieldTypeCatalogAndUnknownPropertiesAreClosed() throws Exception {
        assertThat(EbrDefinitionRules.lint(definition(field("A","CUSTOM_JS"),""),true)).isNotEmpty();
        assertThatThrownBy(()->json.readValue("{\"processPackageId\":\"1\",\"templateCode\":\"T\",\"unexpectedField\":\"unfrozen\"}",Create.class)).hasMessageContaining("Unknown field");
    }
    @Test void duplicateCalculationTargetsCannotSilentlyOverwrite() throws Exception {
        var d=definition(field("A","NUMBER"),"");
        var one=new Rule("R1","F","A","CALCULATION","ON_SUBMIT","1","BLOCK",null,null,false,true);
        var two=new Rule("R2","F","A","CALCULATION","ON_SUBMIT","2","BLOCK",null,null,false,true);
        assertThat(EbrDefinitionRules.lint(new Definition(d.sections(),d.forms(),List.of(one,two),List.of(),List.of()),true)).anySatisfy(i->assertThat(i.code()).isEqualTo("DUPLICATE_CALCULATION_TARGET"));
    }
    @Test void cppConfigIsClosedAndBoundToCurrentForm() throws Exception {
        var d=definition(field("A","NUMBER"),"");
        String cfg="{\"kind\":\"CPP\",\"name\":\"关键参数\",\"description\":\"\",\"rootLogic\":\"AND\",\"groups\":[{\"id\":\"G1\",\"logic\":\"AND\",\"conditions\":[{\"id\":\"C1\",\"fieldCode\":\"A\",\"operator\":\">=\",\"value\":\"1\"}]}],\"actions\":[\"BLOCK_SUBMIT\"]}";
        var valid=new Rule("CPP1","F",null,"VALIDATION","ON_CHANGE","((value('A') >= 1))","BLOCK","CPP_RULE_FAILED","范围异常",false,true,cfg);
        assertThat(EbrDefinitionRules.lint(new Definition(d.sections(),d.forms(),List.of(valid),List.of(),List.of()),true)).noneMatch(i->i.code().startsWith("INVALID_CPP")||i.code().equals("CPP_CROSS_FORM_REFERENCE")||i.code().equals("CPP_EXPRESSION_MISMATCH"));
        var invalidAction=new Rule("CPP2","F",null,"VALIDATION","ON_CHANGE","((value('A') >= 1))","BLOCK","CPP_RULE_FAILED","范围异常",false,true,cfg.replace("BLOCK_SUBMIT","EXEC_SCRIPT"));
        assertThat(EbrDefinitionRules.lint(new Definition(d.sections(),d.forms(),List.of(invalidAction),List.of(),List.of()),true)).anySatisfy(i->assertThat(i.code()).isEqualTo("INVALID_CPP_ACTION"));
        var mismatch=new Rule("CPP3","F",null,"VALIDATION","ON_CHANGE","value('A') >= 2","BLOCK","CPP_RULE_FAILED","范围异常",false,true,cfg);
        assertThat(EbrDefinitionRules.lint(new Definition(d.sections(),d.forms(),List.of(mismatch),List.of(),List.of()),true)).anySatisfy(i->assertThat(i.code()).isEqualTo("CPP_EXPRESSION_MISMATCH"));
    }
}
