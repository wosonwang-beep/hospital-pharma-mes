package com.hospital.mes.integration.infrastructure;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;import org.apache.ibatis.annotations.*;
@Mapper public interface InboxMapper extends BaseMapper<InboxEntity>{
 @Update("UPDATE integration_inbox SET status='PROCESSING',updated_at=#{now},version_no=version_no+1 WHERE id=#{id} AND org_id=#{org} AND version_no=#{version} AND (status='RECEIVED' OR (status='RETRY_WAIT' AND next_retry_at<=#{now}))")int claim(@Param("org")long org,@Param("id")long id,@Param("version")long version,@Param("now")java.time.LocalDateTime now);
 @Update("UPDATE integration_inbox SET status=#{e.status},processed_at=#{e.processedAt},retry_count=#{e.retryCount},next_retry_at=#{e.nextRetryAt},last_error_code=#{e.lastErrorCode},last_error_message=#{e.lastErrorMessage},updated_by=#{e.updatedBy},updated_at=#{e.updatedAt},version_no=#{e.versionNo} WHERE id=#{e.id} AND org_id=#{e.orgId} AND version_no=#{previous}")int save(@Param("e")InboxEntity e,@Param("previous")long previous);
}
