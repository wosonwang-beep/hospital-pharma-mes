package com.hospital.mes.qms.domain;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class IpcRulesTest {
 private final ObjectMapper json=new ObjectMapper();
 @Test void frozenNumericBoundariesAndTextAreClassifiedWithoutClientConclusion() throws Exception {
  var numeric=json.readTree("{\"resultType\":\"NUMERIC\",\"lowerLimit\":\"1\",\"upperLimit\":\"3\"}");
  assertThat(IpcRules.conclusion(numeric,json.readTree("{\"resultNumeric\":\"1\"}"))).isEqualTo("PASS");
  assertThat(IpcRules.conclusion(numeric,json.readTree("{\"resultNumeric\":\"3\"}"))).isEqualTo("PASS");
  assertThat(IpcRules.conclusion(numeric,json.readTree("{\"resultNumeric\":\"3.00000001\"}"))).isEqualTo("FAIL");
  var text=json.readTree("{\"resultType\":\"TEXT\",\"expectedText\":\"clear\"}");
  assertThat(IpcRules.conclusion(text,json.readTree("{\"resultText\":\"clear\"}"))).isEqualTo("PASS");
  assertThat(IpcRules.conclusion(text,json.readTree("{\"resultText\":\"cloudy\"}"))).isEqualTo("FAIL");
 }
}
