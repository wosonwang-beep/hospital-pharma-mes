package com.hospital.mes.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Existing business-payload naming gate; persistent seed entrypoints only.
 * This is not an acceptance-test skip or an authorization/qualification bypass.
 */
final class BusinessSeedPayloadGuard {
    private BusinessSeedPayloadGuard() { }
    static void check(Object businessValues) {
        Path payload = null;
        try {
            Path root = Path.of(System.getProperty("user.dir")).toAbsolutePath();
            while (root != null && !Files.isRegularFile(root.resolve("tools/check_business_seed_payload.py"))) root = root.getParent();
            if (root == null) throw new IllegalStateException("Required business naming validator is unavailable; no seed writes permitted");
            payload = Files.createTempFile("mes-business-seed-payload-", ".json");
            new ObjectMapper().writeValue(payload.toFile(), businessValues);
            Process process = new ProcessBuilder("python", root.resolve("tools/check_business_seed_payload.py").toString(), payload.toString())
                .directory(root.toFile()).redirectErrorStream(true).start();
            String result = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exit = process.waitFor();
            if (exit != 0) throw new IllegalStateException("Business seed naming preflight rejected before data writes: " + result);
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt(); throw new IllegalStateException("Business naming preflight interrupted; no seed writes permitted", failure);
        } catch (java.io.IOException failure) {
            throw new IllegalStateException("Business naming preflight unavailable; no seed writes permitted", failure);
        } finally {
            if (payload != null) try { Files.deleteIfExists(payload); } catch (java.io.IOException ignored) { /* own temporary validator payload only */ }
        }
    }
}
