package com.hospital.mes.foundation.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.hospital.mes.foundation.persistence.FoundationProbeEntity;
import com.hospital.mes.foundation.persistence.FoundationProbeMapper;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("ci")
class FoundationInfrastructureIT {

    private static final String REDIS_KEY_PREFIX = "mes:ci:";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FoundationProbeMapper foundationProbeMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String redisKey;

    @AfterEach
    void removeRedisTestKey() {
        if (redisKey != null) {
            redisTemplate.delete(redisKey);
        }
    }

    @Test
    void flywayMigratesFoundationTable() {
        Integer tableCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
              FROM information_schema.tables
             WHERE table_schema = DATABASE()
               AND table_name = 'sys_foundation_probe'
            """, Integer.class);

        assertThat(tableCount).isEqualTo(1);
    }

    @Test
    @Transactional
    void mybatisPlusWritesAndReadsProbe() {
        FoundationProbeEntity probe = new FoundationProbeEntity();
        probe.setProbeKey("ci-" + UUID.randomUUID());

        assertThat(foundationProbeMapper.insert(probe)).isEqualTo(1);
        assertThat(probe.getId()).isNotNull();
        assertThat(foundationProbeMapper.selectById(probe.getId()).getProbeKey())
            .isEqualTo(probe.getProbeKey());
    }

    @Test
    void redisWritesReadsAndCleansNamespacedValue() {
        redisKey = REDIS_KEY_PREFIX + UUID.randomUUID();

        redisTemplate.opsForValue().set(redisKey, "ready");

        assertThat(redisTemplate.opsForValue().get(redisKey)).isEqualTo("ready");
        assertThat(redisTemplate.delete(redisKey)).isTrue();
        assertThat(redisTemplate.opsForValue().get(redisKey)).isNull();
        redisKey = null;
    }
}
