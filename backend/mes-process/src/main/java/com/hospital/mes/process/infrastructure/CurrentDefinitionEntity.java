package com.hospital.mes.process.infrastructure;

import com.baomidou.mybatisplus.annotation.TableName;

/** Single mutable definition. Inherited legacy accessors are adapters, never revision state. */
@TableName(value="proc_current_definition", excludeProperty={"businessVersion","activationMode","contentHash","effectiveFrom","versionNo"})
public class CurrentDefinitionEntity extends VersionEntity {
    @Override public Integer getBusinessVersion(){return 0;}
    @Override public Long getVersionNo(){return 0L;}
    @Override public java.util.List<String> allowedActions(){return java.util.List.of("EDIT");}
}
