package com.pickaid.passiveintegration.bootstrap;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PassiveIntegrationBootstrapBoundaryTest {
    @Test
    void passiveIntegrationOwnsOnlyMinimalBootstrap() throws IOException {
        Path projectRoot = Path.of("").toAbsolutePath();
        Path passiveIntegration = projectRoot.resolve("src/main/java/com/pickaid/passiveintegration/PassiveIntegration.java");
        Path passiveIntegrationClientDir = projectRoot.resolve("src/main/java/com/pickaid/passiveintegration/client");
        Path passiveIntegrationNetworkDir = projectRoot.resolve("src/main/java/com/pickaid/passiveintegration/network");
        Path passiveIntegrationNetwork = projectRoot.resolve("src/main/java/com/pickaid/passiveintegration/network/PassiveIntegrationNetwork.java");
        Path syncManagedContentMessage = projectRoot.resolve("src/main/java/com/pickaid/passiveintegration/network/s2c/SyncManagedContentMessage.java");
        Path generatedResourceSanitizer = projectRoot.resolve("src/main/java/com/pickaid/passiveintegration/util/GeneratedResourceSanitizer.java");
        Path buildGradle = projectRoot.resolve("build.gradle");

        String passiveIntegrationText = Files.readString(passiveIntegration);
        String buildGradleText = Files.readString(buildGradle);

        assertFalse(passiveIntegrationText.contains("GeneratedResourceSanitizer"));
        assertFalse(passiveIntegrationText.contains("registerClientRuntime"));
        assertFalse(passiveIntegrationText.contains("PassiveIntegrationClient"));
        assertFalse(passiveIntegrationText.contains("FMLEnvironment"));
        assertFalse(passiveIntegrationText.contains("Dist.CLIENT"));
        assertFalse(Files.exists(passiveIntegrationClientDir));
        assertFalse(passiveIntegrationText.contains("PassiveIntegrationNetwork.init"));
        assertFalse(passiveIntegrationText.contains("ManagedContentSync"));
        assertFalse(passiveIntegrationText.contains("LatePstServerSync"));
        assertFalse(Files.exists(passiveIntegrationNetworkDir));
        assertFalse(Files.exists(passiveIntegrationNetwork));
        assertFalse(Files.exists(syncManagedContentMessage));
        assertFalse(Files.exists(generatedResourceSanitizer));
        assertTrue(buildGradleText.contains("PassiveSkillTree-1.20.1-BETA-0.7.4-all.jar"));
    }
}
