package com.hospital.mes.system.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUserEntity> {
    @Update("""
        UPDATE sys_user SET
          locked_until = CASE WHEN failed_login_count + 1 >= 5 THEN #{lockUntil} ELSE locked_until END,
          failed_login_count = failed_login_count + 1,
          version = version + 1, updated_at = CURRENT_TIMESTAMP(6)
        WHERE id = #{id} AND version = #{version}
        """)
    int recordFailure(@Param("id") long id, @Param("version") long version,
                      @Param("lockUntil") LocalDateTime lockUntil);

    @Update("""
        UPDATE sys_user SET failed_login_count = 0, locked_until = NULL,
          version = version + 1, updated_at = CURRENT_TIMESTAMP(6)
        WHERE id = #{id} AND version = #{version}
        """)
    int recordSuccess(@Param("id") long id, @Param("version") long version);

    @Update("""
        UPDATE sys_user SET failed_login_count = 0, locked_until = NULL,
          version = version + 1, updated_at = CURRENT_TIMESTAMP(6)
        WHERE id = #{id} AND version = #{version} AND locked_until <= #{now}
        """)
    int resetExpiredLock(@Param("id") long id, @Param("version") long version,
                         @Param("now") LocalDateTime now);
}
