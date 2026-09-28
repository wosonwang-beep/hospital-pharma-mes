package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.dao.DuplicateKeyException;
import org.testcontainers.containers.MariaDBContainer;

class IamUpgradeIT {
    @Test
    void upgradeFromV002PreservesOnlyActivePairs() {
        try (MariaDBContainer<?> database = new MariaDBContainer<>("mariadb:11.8.9-noble")
            .withDatabaseName("iam_upgrade").withUsername("mes").withPassword("test_password")) {
            database.start();
            var dataSource = new DriverManagerDataSource(database.getJdbcUrl(),
                database.getUsername(), database.getPassword());
            Flyway.configure().dataSource(dataSource).locations("classpath:db/migration")
                .target("002").load().migrate();
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            jdbc.update("INSERT INTO sys_user (login_name, login_name_normalized, display_name, password_hash) VALUES ('upgrade', 'upgrade', 'Upgrade', 'hash')");
            jdbc.update("INSERT INTO sys_role (role_code, display_name) VALUES ('UPGRADE_ROLE', 'Upgrade Role')");
            jdbc.update("INSERT INTO sys_permission (permission_code, permission_type, display_name) VALUES ('action:upgrade', 'ACTION', 'Upgrade')");
            Long user = jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name = 'upgrade'", Long.class);
            Long role = jdbc.queryForObject("SELECT id FROM sys_role WHERE role_code = 'UPGRADE_ROLE'", Long.class);
            Long permission = jdbc.queryForObject("SELECT id FROM sys_permission WHERE permission_code = 'action:upgrade'", Long.class);
            jdbc.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)", user, role);
            jdbc.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)", user, role);
            jdbc.update("INSERT INTO sys_user_role (user_id, role_id, revoked_at) VALUES (?, ?, CURRENT_TIMESTAMP(6))", user, role);
            jdbc.update("INSERT INTO sys_role_permission (role_id, permission_id) VALUES (?, ?)", role, permission);
            jdbc.update("INSERT INTO sys_role_permission (role_id, permission_id) VALUES (?, ?)", role, permission);
            jdbc.update("INSERT INTO sys_role_permission (role_id, permission_id, revoked_at) VALUES (?, ?, CURRENT_TIMESTAMP(6))", role, permission);

            Flyway.configure().dataSource(dataSource).locations("classpath:db/migration")
                .load().migrate();

            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_user_role WHERE user_id = ? AND role_id = ?", Integer.class, user, role)).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_role_permission WHERE role_id = ? AND permission_id = ?", Integer.class, role, permission)).isEqualTo(1);
            assertThat(jdbc.queryForList("SELECT column_name FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'sys_user_role'", String.class))
                .doesNotContain("revoked_at", "granted_at");
            assertThatThrownBy(() -> jdbc.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)", user, role))
                .isInstanceOf(DuplicateKeyException.class);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_permission WHERE permission_code IN ('menu:iam:users', 'menu:iam:roles')", Integer.class))
                .isEqualTo(2);
        }
    }
}
