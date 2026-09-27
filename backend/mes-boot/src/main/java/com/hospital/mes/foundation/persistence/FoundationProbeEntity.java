package com.hospital.mes.foundation.persistence;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
@TableName("sys_foundation_probe") public class FoundationProbeEntity {
    @TableId(type=IdType.AUTO) private Long id;
    private String probeKey; private LocalDateTime createdAt;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getProbeKey(){return probeKey;} public void setProbeKey(String value){this.probeKey=value;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime value){this.createdAt=value;}
}
