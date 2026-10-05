package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
@TableName("qms_capa")
public class CapaRow extends MutableIncomingRow {
    private String capaNo;
    public String getCapaNo() { return capaNo; }
    public void setCapaNo(String value) { capaNo = value; }
    private String sourceType;
    public String getSourceType() { return sourceType; }
    public void setSourceType(String value) { sourceType = value; }
    private Long sourceId;
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long value) { sourceId = value; }
    private String status;
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    private String actionText;
    public String getActionText() { return actionText; }
    public void setActionText(String value) { actionText = value; }
    private String completionText;
    public String getCompletionText() { return completionText; }
    public void setCompletionText(String value) { completionText = value; }
    private Long completedBy;
    public Long getCompletedBy() { return completedBy; }
    public void setCompletedBy(Long value) { completedBy = value; }
    private java.time.LocalDateTime completedAt;
    public java.time.LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(java.time.LocalDateTime value) { completedAt = value; }
}
