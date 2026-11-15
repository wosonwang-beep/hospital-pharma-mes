package com.hospital.mes.qc.domain;

import java.util.List;

/**
 * Commands of the QC quality standard basic library (DCP-QC-SPEC-BASIC-LIBRARY-001).
 * A standard has no approval, no signature and no version; every field is maintained directly.
 * Unknown JSON fields are rejected so a client can never smuggle a silently ignored value.
 */
public final class QcCommands {
 private QcCommands(){}
 public record Item(String itemCode,String itemName,Boolean required,String resultType,String lowerLimit,String upperLimit,String unitId,String textAcceptanceCriteria,String methodCode,String methodVersion){@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String field,Object value){throw QcSpecificationRules.invalid("Unknown field: "+field);}}
 public record Create(String specificationCode,String specificationName,String basisType,String basisReference,java.time.LocalDate effectiveDate,String remark,List<String> materialIds,List<String> productIds,List<Item> items,String reason){@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String field,Object value){throw QcSpecificationRules.invalid("Unknown field: "+field);}}
 public record Edit(Long recordVersion,String specificationCode,String specificationName,String basisType,String basisReference,java.time.LocalDate effectiveDate,String remark,String status,String reason){@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String field,Object value){throw QcSpecificationRules.invalid("Unknown field: "+field);}}
 public record ReplaceScope(Long recordVersion,List<String> materialIds,List<String> productIds,String reason){@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String field,Object value){throw QcSpecificationRules.invalid("Unknown field: "+field);}}
 public record ReplaceItems(Long recordVersion,List<Item> items,String reason){@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String field,Object value){throw QcSpecificationRules.invalid("Unknown field: "+field);}}
 public record Remove(Long recordVersion,String reason){@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String field,Object value){throw QcSpecificationRules.invalid("Unknown field: "+field);}}
}
