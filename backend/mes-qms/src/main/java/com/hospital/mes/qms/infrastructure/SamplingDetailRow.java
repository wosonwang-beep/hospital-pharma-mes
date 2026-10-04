package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
@TableName("qms_sampling_detail")
public class SamplingDetailRow extends IncomingRow {
 private Long samplingTaskId;
 public Long getSamplingTaskId(){return samplingTaskId;} public void setSamplingTaskId(Long value){samplingTaskId=value;}
 private Integer detailNo;
 public Integer getDetailNo(){return detailNo;} public void setDetailNo(Integer value){detailNo=value;}
 private String containerNo;
 public String getContainerNo(){return containerNo;} public void setContainerNo(String value){containerNo=value;}
 private String samplingPoint;
 public String getSamplingPoint(){return samplingPoint;} public void setSamplingPoint(String value){samplingPoint=value;}
 private java.math.BigDecimal sampleQuantity;
 public java.math.BigDecimal getSampleQuantity(){return sampleQuantity;} public void setSampleQuantity(java.math.BigDecimal value){sampleQuantity=value;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long value){unitId=value;}
 private java.time.LocalDateTime sampledAt;
 public java.time.LocalDateTime getSampledAt(){return sampledAt;} public void setSampledAt(java.time.LocalDateTime value){sampledAt=value;}
 private Long sampledBy;
 public Long getSampledBy(){return sampledBy;} public void setSampledBy(Long value){sampledBy=value;}
 private Boolean packageResealed;
 public Boolean getPackageResealed(){return packageResealed;} public void setPackageResealed(Boolean value){packageResealed=value;}
}
