package com.hospital.mes.audit.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper public interface SignatureMapper extends BaseMapper<SignatureEntity>{
    @Update("""
        UPDATE gxp_signature SET status='INVALIDATED',invalidated_at=#{at},invalidation_reason=#{reason},
          updated_by=#{actor},updated_at=#{at},version_no=version_no+1
        WHERE id=#{id} AND org_id=#{org} AND version_no=#{version} AND status='VALID'
        """)
    int invalidate(@Param("org")long org,@Param("id")long id,@Param("version")long version,
                   @Param("actor")long actor,@Param("at")java.time.LocalDateTime at,@Param("reason")String reason);
}
