package com.pickaid.passiveintegration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PassiveIntegrationSourceLayoutTest {
    @Test
    void sourceSetKeepsDatapackFixturesButNoDirectSkilltreeCodeOwnership() throws IOException {
        Path projectRoot = Path.of("").toAbsolutePath();
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
        assertTrue(buildGradleText.contains("PassiveSkillTree-1.20.1-BETA-0.7.4-all.jar"));
    }
}
