package com.hospital.mes.masterdata.infrastructure;
@com.baomidou.mybatisplus.annotation.TableName("md_material") public class MaterialEntity extends ScopedEntity {
 private String materialCode;
 public String getMaterialCode(){return materialCode;} public void setMaterialCode(String value){materialCode=value;}
 private String materialName;
 public String getMaterialName(){return materialName;} public void setMaterialName(String value){materialName=value;}
 private String genericName;
 public String getGenericName(){return genericName;} public void setGenericName(String value){genericName=value;}
 private String englishName;
 public String getEnglishName(){return englishName;} public void setEnglishName(String value){englishName=value;}
 private String aliasName;
 public String getAliasName(){return aliasName;} public void setAliasName(String value){aliasName=value;}
 private String materialType;
 public String getMaterialType(){return materialType;} public void setMaterialType(String value){materialType=value;}
 private String specification;
 public String getSpecification(){return specification;} public void setSpecification(String value){specification=value;}
 private String gradePurity;
 public String getGradePurity(){return gradePurity;} public void setGradePurity(String value){gradePurity=value;}
 private String appearance;
 public String getAppearance(){return appearance;} public void setAppearance(String value){appearance=value;}
 private Long baseUnitId;
 public Long getBaseUnitId(){return baseUnitId;} public void setBaseUnitId(Long value){baseUnitId=value;}
 private String packSpec;
 public String getPackSpec(){return packSpec;} public void setPackSpec(String value){packSpec=value;}
 private Long packUnitId;
 public Long getPackUnitId(){return packUnitId;} public void setPackUnitId(Long value){packUnitId=value;}
 private String manufacturerName;
 public String getManufacturerName(){return manufacturerName;} public void setManufacturerName(String value){manufacturerName=value;}
 private String qualityStandardCode;
 public String getQualityStandardCode(){return qualityStandardCode;} public void setQualityStandardCode(String value){qualityStandardCode=value;}
 private String storageCondition;
 public String getStorageCondition(){return storageCondition;} public void setStorageCondition(String value){storageCondition=value;}
 private Integer shelfLifeDays;
 public Integer getShelfLifeDays(){return shelfLifeDays;} public void setShelfLifeDays(Integer value){shelfLifeDays=value;}
 private Integer retestPeriodDays;
 public Integer getRetestPeriodDays(){return retestPeriodDays;} public void setRetestPeriodDays(Integer value){retestPeriodDays=value;}
 private Boolean lotControlled;
 public Boolean getLotControlled(){return lotControlled;} public void setLotControlled(Boolean value){lotControlled=value;}
 private Boolean samplingRequired;
 public Boolean getSamplingRequired(){return samplingRequired;} public void setSamplingRequired(Boolean value){samplingRequired=value;}
 private Boolean inspectionRequired;
 public Boolean getInspectionRequired(){return inspectionRequired;} public void setInspectionRequired(Boolean value){inspectionRequired=value;}
 private Boolean releaseRequired;
 public Boolean getReleaseRequired(){return releaseRequired;} public void setReleaseRequired(Boolean value){releaseRequired=value;}
 private Boolean weighingRequired;
 public Boolean getWeighingRequired(){return weighingRequired;} public void setWeighingRequired(Boolean value){weighingRequired=value;}
 private Boolean criticalMaterial;
 public Boolean getCriticalMaterial(){return criticalMaterial;} public void setCriticalMaterial(Boolean value){criticalMaterial=value;}
 private java.math.BigDecimal weighingPrecision;
 public java.math.BigDecimal getWeighingPrecision(){return weighingPrecision;} public void setWeighingPrecision(java.math.BigDecimal value){weighingPrecision=value;}
 private java.math.BigDecimal weighingTolerancePct;
 public java.math.BigDecimal getWeighingTolerancePct(){return weighingTolerancePct;} public void setWeighingTolerancePct(java.math.BigDecimal value){weighingTolerancePct=value;}
 private String specialControlType;
 public String getSpecialControlType(){return specialControlType;} public void setSpecialControlType(String value){specialControlType=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private java.time.LocalDateTime effectiveFrom;
 public java.time.LocalDateTime getEffectiveFrom(){return effectiveFrom;} public void setEffectiveFrom(java.time.LocalDateTime value){effectiveFrom=value;}
 private java.time.LocalDateTime effectiveTo;
 public java.time.LocalDateTime getEffectiveTo(){return effectiveTo;} public void setEffectiveTo(java.time.LocalDateTime value){effectiveTo=value;}
 private String remark;
 public String getRemark(){return remark;} public void setRemark(String value){remark=value;}
private Boolean requiresIncomingInspection;
 public Boolean getRequiresIncomingInspection(){return requiresIncomingInspection;} public void setRequiresIncomingInspection(Boolean value){requiresIncomingInspection=value;}
 @Override public java.util.List<String> allowedActions(){return com.hospital.mes.masterdata.domain.MaterialRules.enabled(status)?java.util.List.of("UPDATE","DISABLE"):java.util.List.of("UPDATE");}
}
