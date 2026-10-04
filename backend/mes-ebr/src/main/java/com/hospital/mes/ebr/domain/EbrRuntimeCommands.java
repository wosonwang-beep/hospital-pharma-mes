package com.hospital.mes.ebr.domain;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
public final class EbrRuntimeCommands {
 private EbrRuntimeCommands(){}
 public record Value(String fieldCode,String occurrencePath,JsonNode value,String unitId,String sourceRef){@com.fasterxml.jackson.annotation.JsonAnySetter public void reject(String field,Object v){throw new IllegalArgumentException("Unknown field: "+field);}}
 public record Save(Long formRevision,String reason,List<Value> values){@com.fasterxml.jackson.annotation.JsonAnySetter public void reject(String field,Object v){throw new IllegalArgumentException("Unknown field: "+field);}}
 public record Command(Long formRevision,String reason){@com.fasterxml.jackson.annotation.JsonAnySetter public void reject(String field,Object v){throw new IllegalArgumentException("Unknown field: "+field);}}
 public record Correction(Long formRevision,String reason,JsonNode newValue){@com.fasterxml.jackson.annotation.JsonAnySetter public void reject(String field,Object v){throw new IllegalArgumentException("Unknown field: "+field);}}
 public record Review(Long formRevision,String reviewRuleId,String decision,String comment,String reason,@com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY) String reauthToken){@com.fasterxml.jackson.annotation.JsonAnySetter public void reject(String field,Object v){throw new IllegalArgumentException("Unknown field: "+field);}@Override public String toString(){return "Review[formRevision="+formRevision+",reauthToken=[REDACTED]]";}}
}
