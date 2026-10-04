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
        assertThatThrownBy(()->json.readValue("{\"packageVersionId\":\"1\",\"templateCode\":\"T\",\"templateName\":\"unfrozen\"}",Create.class)).hasMessageContaining("Unknown field");
    }
    @Test void duplicateCalculationTargetsCannotSilentlyOverwrite() throws Exception {
        var d=definition(field("A","NUMBER"),"");
        var one=new Rule("R1","F","A","CALCULATION","ON_SUBMIT","1","BLOCK",null,null,false,true);
        var two=new Rule("R2","F","A","CALCULATION","ON_SUBMIT","2","BLOCK",null,null,false,true);
        assertThat(EbrDefinitionRules.lint(new Definition(d.sections(),d.forms(),List.of(one,two),List.of(),List.of()),true)).anySatisfy(i->assertThat(i.code()).isEqualTo("DUPLICATE_CALCULATION_TARGET"));
    }
}
