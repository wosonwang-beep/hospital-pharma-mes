package com.hospital.mes.wms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("wms_receipt_number_rule")
public class ReceiptNumberRule extends ScopedEntity {
 private String prefix;
 public String getPrefix(){return prefix;} public void setPrefix(String v){prefix=v;}
 private String datePattern;
 public String getDatePattern(){return datePattern;} public void setDatePattern(String v){datePattern=v;}
 private Integer sequenceWidth;
 public Integer getSequenceWidth(){return sequenceWidth;} public void setSequenceWidth(Integer v){sequenceWidth=v;}
 private Long nextValue;
 public Long getNextValue(){return nextValue;} public void setNextValue(Long v){nextValue=v;}
 private String sequencePeriod;
 public String getSequencePeriod(){return sequencePeriod;} public void setSequencePeriod(String v){sequencePeriod=v;}
 private String timezoneId;
 public String getTimezoneId(){return timezoneId;} public void setTimezoneId(String v){timezoneId=v;}
 private String approvalReference;
 public String getApprovalReference(){return approvalReference;} public void setApprovalReference(String v){approvalReference=v;}
 private Boolean enabled;
 public Boolean getEnabled(){return enabled;} public void setEnabled(Boolean v){enabled=v;}
}
