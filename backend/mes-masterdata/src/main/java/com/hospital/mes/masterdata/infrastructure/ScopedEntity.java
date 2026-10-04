package com.hospital.mes.masterdata.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
public abstract class ScopedEntity {
    public java.util.List<String> allowedActions(){return java.util.List.of("UPDATE");}
    @TableId(type=IdType.AUTO) private Long id;
    private Long orgId;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    @Version private Long versionNo;
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Long getOrgId(){return orgId;} public void setOrgId(Long v){orgId=v;}
    public Long getCreatedBy(){return createdBy;} public void setCreatedBy(Long v){createdBy=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public Long getUpdatedBy(){return updatedBy;} public void setUpdatedBy(Long v){updatedBy=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
    public Long getVersionNo(){return versionNo;} public void setVersionNo(Long v){versionNo=v;}
}
