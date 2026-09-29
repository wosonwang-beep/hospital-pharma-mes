package com.hospital.mes.platform.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

class Mes001MigrationContractTest {
    private static final String V001_SHA = "0c9efa2411a7ed5e6c87a3f2f036b6a960b812ded1aac8f6df894438e90c0efd";
    private static final String V002_SHA = "8e2e2b68be8c9cdbfa7ebe08804509c62e43826845c16e5b7991ec719802c43a";

    @Test
    void preservesHistoricalMigrationsAndAllocatesOnlyV003AndV004() throws Exception {
        Path migrations = migrationDirectory();
        assertEquals(V001_SHA, sha256(migrations.resolve("V001__foundation_probe.sql")));
        assertEquals(V002_SHA, sha256(migrations.resolve("V002__iam_core.sql")));
        assertTrue(Files.exists(migrations.resolve("V003__mes_001_platform_base.sql")));
        assertTrue(Files.exists(migrations.resolve("V004__mes_001_platform_permissions.sql")));
        assertEquals(4, Files.list(migrations).filter(Files::isRegularFile).count());
    }

    @Test
    void platformMigrationContainsExactlyFiveTablesAndAuditGuards() throws Exception {
        String sql = Files.readString(migrationDirectory().resolve("V003__mes_001_platform_base.sql"));
        for (String table : new String[]{"gxp_audit_event", "gxp_signature", "integration_inbox",
            "integration_outbox", "platform_idempotency_record"}) {
            assertTrue(sql.contains("CREATE TABLE " + table), table);
        }
        assertEquals(5, count(sql, "CREATE TABLE "));
        assertTrue(sql.contains("BEFORE UPDATE ON gxp_audit_event"));
        assertTrue(sql.contains("BEFORE DELETE ON gxp_audit_event"));
        assertFalse(sql.toUpperCase().contains("ALTER TABLE SYS_"));
        assertFalse(sql.toUpperCase().contains("DROP TABLE"));
    }

    @Test
    void permissionMigrationSeedsOnlyFrozenCodes() throws Exception {
        String sql = Files.readString(migrationDirectory().resolve("V004__mes_001_platform_permissions.sql"));
        for (String permission : new String[]{"audit:view", "ebr:sign", "integration:view", "integration:retry"}) {
            assertTrue(sql.contains("'" + permission + "'"), permission);
        }
        assertFalse(sql.toUpperCase().contains("CREATE TABLE"));
        assertFalse(sql.toUpperCase().contains("ALTER TABLE"));
    }

    private static Path migrationDirectory() {
        return Path.of(System.getProperty("user.dir"), "src", "main", "resources", "db", "migration");
    }

    private static String sha256(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static int count(String text, String token) {
        int count = 0;
        for (int at = 0; (at = text.indexOf(token, at)) >= 0; at += token.length()) count++;
        return count;
    }
}
