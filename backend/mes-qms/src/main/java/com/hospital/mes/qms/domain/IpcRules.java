package com.hospital.mes.qms.domain;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.common.exception.ComplianceException;
import java.math.BigDecimal;
public final class IpcRules {
 private IpcRules(){}
 public static BigDecimal numeric(String raw){if(raw==null||!raw.matches("-?\\d+(\\.\\d{1,8})?"))throw new IllegalArgumentException("DECIMAL(24,8) string required");var n=new BigDecimal(raw);if(n.precision()-n.scale()>16)throw new IllegalArgumentException("IPC numeric precision exceeded");return n;}
 public static String conclusion(JsonNode definition,JsonNode body){
  if("NUMERIC".equals(definition.path("resultType").asText())){if(!body.path("resultNumeric").isTextual()||body.hasNonNull("resultText"))throw new IllegalArgumentException("Numeric IPC requires only resultNumeric");var value=numeric(body.get("resultNumeric").asText());var low=definition.hasNonNull("lowerLimit")?numeric(definition.get("lowerLimit").asText()):null;var high=definition.hasNonNull("upperLimit")?numeric(definition.get("upperLimit").asText()):null;return (low==null||value.compareTo(low)>=0)&&(high==null||value.compareTo(high)<=0)?"PASS":"FAIL";}
  if("TEXT".equals(definition.path("resultType").asText())){if(!body.path("resultText").isTextual()||body.path("resultText").asText().isBlank()||body.path("resultText").asText().length()>1000||body.hasNonNull("resultNumeric"))throw new IllegalArgumentException("Text IPC requires only resultText");return definition.path("expectedText").asText().equals(body.get("resultText").asText())?"PASS":"FAIL";}
  throw new ComplianceException("IPC_DEFINITION_INVALID","Unknown frozen IPC type");
 }
 public static void mutableOperation(String status){if(!java.util.Set.of("READY","IN_PROGRESS","PAUSED").contains(status))throw new ComplianceException("IPC_OPERATION_NOT_MUTABLE","Operation does not accept IPC changes");}
}
