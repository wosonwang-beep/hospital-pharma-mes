package com.hospital.mes.masterdata.application;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
public final class UnitConversionCommands {
 private UnitConversionCommands() {}
 @JsonIgnoreProperties(ignoreUnknown=false) public record Create(String fromUnitId,String toUnitId,String factor,String materialId) { @com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String name,Object value){throw new IllegalArgumentException("Unknown field: "+name);} }
 @JsonIgnoreProperties(ignoreUnknown=false) public record Update(String action,String reason,Long versionNo,String fromUnitId,String toUnitId,String factor,String materialId) { @com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String name,Object value){throw new IllegalArgumentException("Unknown field: "+name);} }
}
