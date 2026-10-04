package com.hospital.mes.ebr.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("ebr_group_def") public class GroupEntity extends ScopedEntity {
private Long sectionDefId; public Long getSectionDefId(){return sectionDefId;} public void setSectionDefId(Long value){sectionDefId=value;}
private String groupCode; public String getGroupCode(){return groupCode;} public void setGroupCode(String value){groupCode=value;}
private String title; public String getTitle(){return title;} public void setTitle(String value){title=value;}
private Integer sequenceNo; public Integer getSequenceNo(){return sequenceNo;} public void setSequenceNo(Integer value){sequenceNo=value;}
private Integer layoutColumns; public Integer getLayoutColumns(){return layoutColumns;} public void setLayoutColumns(Integer value){layoutColumns=value;}
private String repeatMode; public String getRepeatMode(){return repeatMode;} public void setRepeatMode(String value){repeatMode=value;}
private Integer minOccurs; public Integer getMinOccurs(){return minOccurs;} public void setMinOccurs(Integer value){minOccurs=value;}
private Integer maxOccurs; public Integer getMaxOccurs(){return maxOccurs;} public void setMaxOccurs(Integer value){maxOccurs=value;}
}
