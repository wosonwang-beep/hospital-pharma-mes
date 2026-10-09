package com.hospital.mes.equipment.application;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
public final class EquipmentCommands {
 private EquipmentCommands() {}
 @JsonIgnoreProperties(ignoreUnknown=false) public record Create(String equipmentCode,String equipmentName,String equipmentType,java.time.LocalDate calibrationDueDate,String location) { @com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String name,Object value){throw new IllegalArgumentException("Unknown field: "+name);} }
 @JsonIgnoreProperties(ignoreUnknown=false) public record Update(String action,String reason,@com.fasterxml.jackson.annotation.JsonIgnore Long versionNo,String equipmentName,String equipmentType,java.time.LocalDate calibrationDueDate,String location) { @com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String name,Object value){throw new IllegalArgumentException("Unknown field: "+name);} }
}
