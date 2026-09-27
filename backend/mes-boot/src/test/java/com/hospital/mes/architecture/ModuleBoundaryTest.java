package com.hospital.mes.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;

class ModuleBoundaryTest {
    private static final List<String> MODULES = List.of("mes-boot", "mes-common", "mes-security", "mes-system", "mes-masterdata", "mes-product", "mes-process", "mes-form", "mes-ebr", "mes-wms", "mes-production", "mes-execution", "mes-equipment", "mes-qc", "mes-qms", "mes-release", "mes-workflow", "mes-traceability", "mes-integration", "mes-reporting");

    @Test
    void reactorHasExactModuleSetAndOnlyBootIsExecutable() throws Exception {
        Path root = Path.of(System.getProperty("user.dir")).getParent().getParent();
        var backend = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(root.resolve("backend/pom.xml").toFile());
        var nodes = backend.getElementsByTagName("module");
        assertEquals(20, nodes.getLength());
        for (String module : MODULES) assertTrue(Files.exists(root.resolve("backend").resolve(module).resolve("pom.xml")));
        for (String module : MODULES) {
            String pom = Files.readString(root.resolve("backend").resolve(module).resolve("pom.xml"));
            assertEquals(module.equals("mes-boot"), pom.contains("<goal>repackage</goal>"));
        }
    }

    @Test
    void cycleDetectorRejectsSyntheticCycle() {
        Map<String, Set<String>> graph = new HashMap<>();
        graph.put("a", Set.of("b")); graph.put("b", Set.of("a"));
        assertTrue(hasCycle(graph));
        graph.put("b", Set.of());
        assertFalse(hasCycle(graph));
    }

    private boolean hasCycle(Map<String, Set<String>> graph) {
        Set<String> visiting = new HashSet<>(), visited = new HashSet<>();
        for (String node : graph.keySet()) if (visit(node, graph, visiting, visited)) return true;
        return false;
    }

    private boolean visit(String node, Map<String, Set<String>> graph, Set<String> visiting, Set<String> visited) {
        if (visiting.contains(node)) return true;
        if (visited.contains(node)) return false;
        visiting.add(node);
        for (String next : graph.getOrDefault(node, Set.of())) if (visit(next, graph, visiting, visited)) return true;
        visiting.remove(node); visited.add(node); return false;
    }
}
