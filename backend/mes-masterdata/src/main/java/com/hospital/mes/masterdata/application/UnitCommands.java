package com.hospital.mes.masterdata.application;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
public final class UnitCommands {
 private UnitCommands() {}
 @JsonIgnoreProperties(ignoreUnknown=false) public record Create(String unitCode,String unitName,String dimension,Integer scale) { @com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String name,Object value){throw new IllegalArgumentException("Unknown field: "+name);} }
 @JsonIgnoreProperties(ignoreUnknown=false) public record Update(String action,String reason,@com.fasterxml.jackson.annotation.JsonIgnore Long versionNo,String unitName,String dimension,Integer scale) { @com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String name,Object value){throw new IllegalArgumentException("Unknown field: "+name);} }
}
