package com.pickaid.passiveintegration.gun;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class GunHelperSourceLayoutTest {
    @Test
    void sourceSetDoesNotContainDuplicatedGunLayerPackages() {
        Path projectRoot = Path.of("").toAbsolutePath();

        for (String relativePath : List.of(
                "src/main/java/com/pickaid/passiveintegration/compat/gun",
                "src/main/java/com/pickaid/passiveintegration/bridge/gun",
                "src/main/java/com/pickaid/passiveintegration/helper/gun",
                "src/main/java/com/pickaid/passiveintegration/optional/gun",
                "src/test/java/com/pickaid/passiveintegration/bridge/gun",
                "src/test/java/com/pickaid/passiveintegration/compat/gun",
                "src/test/java/com/pickaid/passiveintegration/testsupport/gun")) {
            assertFalse(Files.exists(projectRoot.resolve(relativePath)), relativePath + " should be absent");
        }
    }

    @Test
    void sourceSetDoesNotRestoreLegacyGunLayouts() {
        Path projectRoot = Path.of("").toAbsolutePath();

        assertFalse(Files.exists(projectRoot.resolve(
                "src/main/java/com/pickaid/passiveintegration/integration/gun")));
        assertFalse(Files.exists(projectRoot.resolve(
                "src/main/java/com/pickaid/passiveintegration/util/GunCompatHelper.java")));
        assertFalse(Files.exists(projectRoot.resolve(
                "src/main/java/org/crychicteam/passiveintegration")));
    }
}
