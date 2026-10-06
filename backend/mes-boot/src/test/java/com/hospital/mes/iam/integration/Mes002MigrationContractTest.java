package com.hospital.mes.iam.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

class Mes002MigrationContractTest {
    private static final String V001_SHA = "0c9efa2411a7ed5e6c87a3f2f036b6a960b812ded1aac8f6df894438e90c0efd";
    private static final String V002_SHA = "8e2e2b68be8c9cdbfa7ebe08804509c62e43826845c16e5b7991ec719802c43a";

    @Test
    void preservesHistoryAndAllocatesV005Next() throws Exception {
        Path migrations = migrationDirectory();
        assertEquals(V001_SHA, sha256(migrations.resolve("V001__foundation_probe.sql")));
        assertEquals(V002_SHA, sha256(migrations.resolve("V002__iam_core.sql")));
        assertTrue(Files.exists(migrations.resolve("V005__mes_002_rbac_contract.sql")));
        // Approved cumulative baseline adds V006..V026; authorized registry repair adds V027.
        // Keep an exact count and contiguous identities; original hashes above remain frozen.
        try (var files = Files.list(migrations)) {
            var versions = files.filter(Files::isRegularFile)
                .map(path -> path.getFileName().toString())
                .peek(name -> assertTrue(name.matches("V[0-9]{3}__.+\\.sql"), name))
                .map(name -> Integer.parseInt(name.substring(1, 4))).sorted().toList();
            assertEquals(java.util.stream.IntStream.rangeClosed(1, 27).boxed().toList(), versions);
            assertEquals(5, versions.stream().filter(version -> version <= 5).count());
        }
    }

    @Test
    void v005IsAdditiveAndContainsOnlyMes002RbacContract() throws Exception {
        String sql = Files.readString(migrationDirectory().resolve("V005__mes_002_rbac_contract.sql"));
        assertTrue(sql.contains("CREATE TABLE sys_menu"));
        assertTrue(sql.contains("CREATE TABLE sys_role_menu"));
        assertTrue(sql.contains("ALTER TABLE sys_permission ADD COLUMN version"));
        for (String code : new String[]{
            "iam:user:view", "iam:user:create", "iam:user:update",
            "iam:role:view", "iam:role:create", "iam:role:update",
            "iam:permission:view", "iam:permission:create", "iam:permission:update",
            "iam:menu:view", "iam:menu:create", "iam:menu:update"}) {
            assertTrue(sql.contains("'" + code + "'"), code);
        }
        assertFalse(sql.toUpperCase().contains("DROP TABLE"));
        assertFalse(sql.toUpperCase().contains("TRUNCATE"));
        assertFalse(sql.toUpperCase().contains("DELETE FROM"));
        assertFalse(sql.contains("INSERT INTO sys_user"));
    }

    private static Path migrationDirectory() {
        return Path.of(System.getProperty("user.dir"), "src", "main", "resources", "db", "migration");
    }

    private static String sha256(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }
}
