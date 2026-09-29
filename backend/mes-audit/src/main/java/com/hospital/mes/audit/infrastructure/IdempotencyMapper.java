package com.hospital.mes.audit.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface IdempotencyMapper extends BaseMapper<IdempotencyEntity> {
    @Update("""
        UPDATE platform_idempotency_record
           SET state='COMPLETED', http_status=#{status}, response_json=#{response},
               resource_type=#{resourceType}, resource_id=#{resourceId}, updated_at=CURRENT_TIMESTAMP(3),
               version_no=version_no+1
         WHERE id=#{id} AND version_no=#{version} AND state='IN_PROGRESS'
        """)
    int complete(@Param("id") long id, @Param("version") long version, @Param("status") int status,
                 @Param("response") String response, @Param("resourceType") String resourceType,
                 @Param("resourceId") String resourceId);
}
