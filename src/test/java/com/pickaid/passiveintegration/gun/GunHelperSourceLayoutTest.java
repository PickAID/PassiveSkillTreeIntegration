package com.pickaid.passiveintegration.gun;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

class GunHelperSourceLayoutTest {
    @Test
    void sourceSetDoesNotContainDuplicatedGunLayerPackages() {
        Path projectRoot = projectRoot();

        for (String relativePath : List.of(
                "src/main/java/com/pickaid/passiveintegration/compat/gun",
                "src/main/java/com/pickaid/passiveintegration/bridge/gun",
                "src/main/java/com/pickaid/passiveintegration/helper/gun",
                "src/main/java/com/pickaid/passiveintegration/optional/gun",
                "src/test/java/com/pickaid/passiveintegration/bridge/gun",
                "src/test/java/com/pickaid/passiveintegration/compat/gun",
                "src/test/java/com/pickaid/passiveintegration/testsupport/gun",
                "src/test/java/com/pickaid/passiveintegration/gun/GunPlatformIdContractTest.java")) {
            assertFalse(Files.exists(projectRoot.resolve(relativePath)), relativePath + " should be absent");
        }
    }

    @Test
    void sourceSetDoesNotRestoreLegacyGunLayouts() {
        Path projectRoot = projectRoot();

        assertFalse(Files.exists(projectRoot.resolve(
                "src/main/java/com/pickaid/passiveintegration/integration/gun")),
                "src/main/java/com/pickaid/passiveintegration/integration/gun should be absent");
        assertFalse(Files.exists(projectRoot.resolve(
                "src/main/java/com/pickaid/passiveintegration/util/GunCompatHelper.java")),
                "src/main/java/com/pickaid/passiveintegration/util/GunCompatHelper.java should be absent");
        assertFalse(Files.exists(projectRoot.resolve(
                "src/main/java/org/crychicteam/passiveintegration")),
                "src/main/java/org/crychicteam/passiveintegration should be absent");
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
