package com.hospital.mes.masterdata.application;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
public final class QualificationCommands {
 private QualificationCommands() {}
 @JsonIgnoreProperties(ignoreUnknown=false) public record Create(String userId,String qualificationCode,java.time.LocalDate validFrom,java.time.LocalDate validTo) { @com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String name,Object value){throw new IllegalArgumentException("Unknown field: "+name);} }
 @JsonIgnoreProperties(ignoreUnknown=false) public record Update(String action,String reason,Long versionNo,java.time.LocalDate validFrom,java.time.LocalDate validTo) { @com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String name,Object value){throw new IllegalArgumentException("Unknown field: "+name);} }
}
