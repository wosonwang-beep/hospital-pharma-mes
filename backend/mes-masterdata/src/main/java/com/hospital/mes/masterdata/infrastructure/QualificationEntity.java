package com.hospital.mes.masterdata.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("md_qualification") public class QualificationEntity extends ScopedEntity {
    private Long userId;
    private String qualificationCode;
    private java.time.LocalDate validFrom;
    private java.time.LocalDate validTo;
    private String status;
    public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;}
    public String getQualificationCode(){return qualificationCode;} public void setQualificationCode(String v){qualificationCode=v;}
    public java.time.LocalDate getValidFrom(){return validFrom;} public void setValidFrom(java.time.LocalDate v){validFrom=v;}
    public java.time.LocalDate getValidTo(){return validTo;} public void setValidTo(java.time.LocalDate v){validTo=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of("UPDATE", "INACTIVE".equals(status)?"ENABLE":"DISABLE");}
}
