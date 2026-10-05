package com.hospital.mes.production;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/** Read-only physical allocation prerequisite. Does not create fixtures or change history. */
@SpringBootTest
@ActiveProfiles("ci")
class DevelopmentHistoryReadIT {
    @Autowired JdbcTemplate jdbc;
    @Test void reportsSuccessfulPhysicalVersionWithoutChangingHistory() {
        var summary=jdbc.queryForMap("SELECT MAX(CAST(version AS UNSIGNED)) AS highest, SUM(success=1) AS successful, SUM(success=0) AS failed FROM flyway_schema_history");
        assertThat(((Number)summary.get("failed")).longValue()).isZero();
        assertThat(((Number)summary.get("highest")).longValue()).isGreaterThanOrEqualTo(24);
        System.out.println("DEV_FLYWAY_READ_ONLY "+summary);
    }
}
