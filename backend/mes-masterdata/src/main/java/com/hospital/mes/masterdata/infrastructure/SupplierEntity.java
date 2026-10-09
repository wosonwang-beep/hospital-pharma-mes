package com.hospital.mes.masterdata.infrastructure;
@com.baomidou.mybatisplus.annotation.TableName("md_supplier") public class SupplierEntity extends ScopedEntity {
 private String supplierCode;
 public String getSupplierCode(){return supplierCode;} public void setSupplierCode(String value){supplierCode=value;}
 private String supplierName;
 public String getSupplierName(){return supplierName;} public void setSupplierName(String value){supplierName=value;}
 private String qualificationStatus;
 public String getQualificationStatus(){return qualificationStatus;} public void setQualificationStatus(String value){qualificationStatus=value;}
 private java.time.LocalDate validTo;
 public java.time.LocalDate getValidTo(){return validTo;} public void setValidTo(java.time.LocalDate value){validTo=value;}
@Override public java.util.List<String> allowedActions(){return "INACTIVE".equals(qualificationStatus)?java.util.List.of():java.util.List.of("UPDATE","DISABLE");}
}
