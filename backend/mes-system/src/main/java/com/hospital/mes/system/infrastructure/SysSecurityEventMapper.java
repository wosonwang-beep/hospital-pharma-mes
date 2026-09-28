package com.hospital.mes.system.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysSecurityEventMapper extends BaseMapper<SysSecurityEventEntity> {
    @Select("SELECT COUNT(*) FROM sys_security_event WHERE event_type = 'BOOTSTRAP_ADMIN' AND outcome = 'SUCCESS'")
    int successfulBootstrapCount();
}
