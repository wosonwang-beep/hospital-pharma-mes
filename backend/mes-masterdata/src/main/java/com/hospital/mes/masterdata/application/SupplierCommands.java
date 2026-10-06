package com.hospital.mes.masterdata.application;
public final class SupplierCommands {
 private SupplierCommands(){}
 public record Create(String supplierCode,String supplierName,java.time.LocalDate validTo){@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String name,Object value){throw new IllegalArgumentException("Unknown field: "+name);}}
 public record Update(String action,Long versionNo,String reason,String supplierName,java.time.LocalDate validTo){@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String name,Object value){throw new IllegalArgumentException("Unknown field: "+name);}}
 public record Relationship(String supplierId,Boolean approved,Boolean preferred,java.time.LocalDate validTo,String manufacturerName){public Relationship(String supplierId,Boolean approved,Boolean preferred,java.time.LocalDate validTo){this(supplierId,approved,preferred,validTo,null);}@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String name,Object value){throw new IllegalArgumentException("Unknown field: "+name);}}
 public record Assign(Long versionNo,String reason,java.util.List<Relationship> suppliers){@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String name,Object value){throw new IllegalArgumentException("Unknown field: "+name);}}
}
