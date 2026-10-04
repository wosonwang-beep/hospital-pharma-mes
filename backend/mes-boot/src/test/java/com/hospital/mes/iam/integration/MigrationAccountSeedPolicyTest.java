package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MigrationAccountSeedPolicyTest {
    @Test
    void rejectsAccountCreationAcrossMariaDbDmlForms() {
        for (String sql : new String[]{
            "INSERT INTO sys_user (id) VALUES (1)",
            "insert ignore into `sys_user` select * from source",
            "INSERT LOW_PRIORITY IGNORE INTO db.sys_user VALUES (1)",
            "INSERT HIGH_PRIORITY `db`.`sys_user` SET id=1",
            "INSERT DELAYED sys_user VALUES (1)",
            "REPLACE INTO sys_user VALUES (1)",
            "REPLACE LOW_PRIORITY `db`.`sys_user` VALUES (1)",
            "LOAD DATA LOCAL INFILE 'users.csv' INTO TABLE sys_user;",
            "LOAD DATA INFILE 'users.csv' REPLACE INTO TABLE `db`.`sys_user`;"
        }) {
            assertThat(IamSchemaIT.ACCOUNT_SEED.matcher(sql).find()).as(sql).isTrue();
        }
    }

    @Test
    void permitsSchemaAndRoleAssignmentsWithoutSeedingAccounts() {
        for (String sql : new String[]{
            "CREATE TABLE sys_user (id BIGINT)",
            "INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 2)",
            "REPLACE INTO `sys_user_role` VALUES (1, 2)",
            "LOAD DATA INFILE 'roles.csv' INTO TABLE sys_user_role;",
            "SELECT * FROM sys_user"
        }) {
            assertThat(IamSchemaIT.ACCOUNT_SEED.matcher(sql).find()).as(sql).isFalse();
        }
    }
}
