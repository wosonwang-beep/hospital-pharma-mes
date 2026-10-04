package com.hospital.mes.ebr.domain;
import static org.assertj.core.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import java.util.*;
import org.junit.jupiter.api.Test;
class EbrRuntimeRulesTest {
 private final ObjectMapper json=new ObjectMapper();
 @Test void exactNumericInputAndPrecision(){var f=field("NUMBER","MANUAL",false,3);assertThat(EbrRuntimeRules.normalize(f,TextNode.valueOf("123456789012.123"),"7",false).asText()).isEqualTo("123456789012.123");assertThatThrownBy(()->EbrRuntimeRules.normalize(f,TextNode.valueOf("1.2345"),"7",false)).hasMessageContaining("precision");assertThatThrownBy(()->EbrRuntimeRules.normalize(f,TextNode.valueOf("1"),"8",false)).hasMessageContaining("unit");}
 @Test void sourceAndReadonlyFieldsCannotBeSpoofed(){assertThatThrownBy(()->EbrRuntimeRules.normalize(field("INSTRUMENT_VALUE","INSTRUMENT",true,3),TextNode.valueOf("1"),"7",false)).hasMessageContaining("source");assertThatThrownBy(()->EbrRuntimeRules.normalize(field("CALCULATED","DERIVED",true,3),TextNode.valueOf("1"),"7",false)).hasMessageContaining("source");}
 @Test void noSilentRuntimeBindingOrJumpedState(){var form=new EbrCommands.Form("F","Form",null,"1",1,List.of(field("TEXT","MANUAL",false,null)));var d=new EbrCommands.Definition(List.of(),List.of(form),List.of(),List.of(),List.of());assertThatThrownBy(()->EbrRuntimeRules.requireBindings(d)).hasMessageContaining("F");assertThat(EbrRuntimeRules.transition("DRAFT","SUBMIT")).isEqualTo("SUBMITTED");assertThatThrownBy(()->EbrRuntimeRules.transition("DRAFT","APPROVE")).hasMessageContaining("state");}
 @Test void explicitMissingValuesAndDatesAreValidated(){assertThat(EbrRuntimeRules.normalize(field("TEXT","MANUAL",false,null),NullNode.instance,null,false).isNull()).isTrue();assertThatThrownBy(()->EbrRuntimeRules.normalize(field("DATE","MANUAL",false,null),TextNode.valueOf("2026-02-30"),null,false)).hasMessageContaining("date");}
 @Test void requiredPresenceRejectsEmptySelectionsButKeepsFalseAndZero(){assertThat(EbrRuntimeRules.hasValue(json.createArrayNode())).isFalse();assertThat(EbrRuntimeRules.hasValue(TextNode.valueOf("  "))).isFalse();assertThat(EbrRuntimeRules.hasValue(json.createArrayNode().add("YES"))).isTrue();assertThat(EbrRuntimeRules.hasValue(BooleanNode.FALSE)).isTrue();assertThat(EbrRuntimeRules.hasValue(IntNode.valueOf(0))).isTrue();}
 private EbrCommands.Field field(String type,String source,boolean readOnly,Integer scale){return new EbrCommands.Field("A",null,"Field",type,source,"STRING",Set.of("NUMBER","INSTRUMENT_VALUE","CALCULATED").contains(type)?"7":null,scale,true,readOnly,null,null,null,1,null,List.of());}
}
