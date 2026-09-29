package com.hospital.mes.audit.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditEventMapper extends BaseMapper<AuditEventEntity> { }
