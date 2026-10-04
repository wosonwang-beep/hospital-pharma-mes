package com.hospital.mes.ebr.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("ebr_section_def") public class SectionEntity extends ScopedEntity {
private Long templateVersionId; public Long getTemplateVersionId(){return templateVersionId;} public void setTemplateVersionId(Long value){templateVersionId=value;}
private String sectionCode; public String getSectionCode(){return sectionCode;} public void setSectionCode(String value){sectionCode=value;}
private String title; public String getTitle(){return title;} public void setTitle(String value){title=value;}
private Integer sequenceNo; public Integer getSequenceNo(){return sequenceNo;} public void setSequenceNo(Integer value){sequenceNo=value;}
private String repeatMode; public String getRepeatMode(){return repeatMode;} public void setRepeatMode(String value){repeatMode=value;}
private Long visibilityRuleId; public Long getVisibilityRuleId(){return visibilityRuleId;} public void setVisibilityRuleId(Long value){visibilityRuleId=value;}
private Boolean pageBreakFlag; public Boolean getPageBreakFlag(){return pageBreakFlag;} public void setPageBreakFlag(Boolean value){pageBreakFlag=value;}
}
