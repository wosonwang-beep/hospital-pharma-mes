package com.hospital.mes.masterdata.application;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
public final class OrganizationCommands {
 private OrganizationCommands() {}
 @JsonIgnoreProperties(ignoreUnknown=false) public record Create(String parentId,String orgCode,String orgName,String orgType) { @com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String name,Object value){throw new IllegalArgumentException("Unknown field: "+name);} }
 @JsonIgnoreProperties(ignoreUnknown=false) public record Update(String action,String reason,Long versionNo,String parentId,String orgName) { @com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String name,Object value){throw new IllegalArgumentException("Unknown field: "+name);} }
}
