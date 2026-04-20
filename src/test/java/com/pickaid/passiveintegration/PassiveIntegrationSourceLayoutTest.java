package com.pickaid.passiveintegration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class PassiveIntegrationSourceLayoutTest {
    @Test
    void sourceSetKeepsDatapackFixturesButNoDirectSkilltreeCodeOwnership() throws IOException {
        Path projectRoot = projectRoot();
        Path clientRoot = projectRoot.resolve("src/main/java/com/pickaid/passiveintegration/client");
        Path mainSkilltree = projectRoot.resolve("src/main/java/com/pickaid/passiveintegration/skilltree");
        Path testSkilltree = projectRoot.resolve("src/test/java/com/pickaid/passiveintegration/skilltree");
        Path datapackFixture = projectRoot.resolve(
                "src/test/resources/data/passiveintegration_test/skill_trees/cgm_grenade_control.json"
        );
        Path legacyNamespace = projectRoot.resolve("src/main/java/org/crychicteam/passiveintegration");
        Path generatedResourceSanitizer = projectRoot.resolve("src/main/java/com/pickaid/passiveintegration/util/GeneratedResourceSanitizer.java");
        Path buildGradle = projectRoot.resolve("build.gradle");
        String buildGradleText = Files.readString(buildGradle);

        assertFalse(Files.exists(clientRoot));
        assertFalse(Files.exists(mainSkilltree));
        assertFalse(Files.exists(testSkilltree));
        assertFalse(Files.exists(legacyNamespace));
        assertFalse(Files.exists(generatedResourceSanitizer));
        assertTrue(Files.exists(datapackFixture));
        assertTrue(buildGradleText.contains("resolvePassiveSkillTreeJar()"));
        assertTrue(buildGradleText.contains("passive-skill-tree-850298"));
    }

    private static Path projectRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();

        for (Path candidate = current; candidate != null; candidate = candidate.getParent()) {
            if (hasGradleProjectMarker(candidate)) {
                return candidate;
            }
        }

        fail("Could not locate project root from " + current + " using Gradle project markers");
        return current;
    }

    private static boolean hasGradleProjectMarker(Path candidate) {
        return Files.exists(candidate.resolve("build.gradle"))
                || Files.exists(candidate.resolve("build.gradle.kts"))
                || Files.exists(candidate.resolve("settings.gradle"))
                || Files.exists(candidate.resolve("settings.gradle.kts"));
    }
}
