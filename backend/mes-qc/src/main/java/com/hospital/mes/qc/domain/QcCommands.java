package com.hospital.mes.qc.domain;
import java.util.List;
public final class QcCommands {
 private QcCommands(){}
 public record Item(String itemCode,String itemName,Boolean required,String resultType,String lowerLimit,String upperLimit,String unitId,String textAcceptanceCriteria,String methodCode,String methodVersion){@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String field,Object value){throw QcSpecificationRules.invalid("Unknown field: "+field);}}
 public record CreateSpecification(String materialId,String specificationCode,String specificationName,String reason){@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String field,Object value){throw QcSpecificationRules.invalid("Unknown field: "+field);}}
 public record CreateVersion(Integer versionNoBusiness,List<Item> items,String reason){@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String field,Object value){throw QcSpecificationRules.invalid("Unknown field: "+field);}}
 public record EditVersion(Long versionNo,List<Item> items,String reason){@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String field,Object value){throw QcSpecificationRules.invalid("Unknown field: "+field);}}
 public record SignVersion(Long versionNo,String reason,@com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY) String reauthToken){@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String field,Object value){throw QcSpecificationRules.invalid("Unknown field: "+field);}@Override public String toString(){return "SignVersion[versionNo="+versionNo+",reauthToken=[REDACTED]]";}}
}
