package com.pickaid.passiveintegration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

class PassiveIntegrationSourceLayoutTest {
    @Test
    void movedEntrypointIsSelfContainedAndOldPackageClassIsGone() throws IOException {
        Path projectRoot = Path.of("").toAbsolutePath();
        Path movedEntrypoint = projectRoot.resolve("src/main/java/com/pickaid/passiveintegration/PassiveIntegration.java");
        Path oldEntrypoint = projectRoot.resolve("src/main/java/org/crychicteam/passiveintegration/PassiveIntegration.java");

        String source = Files.readString(movedEntrypoint);

        assertFalse(Files.exists(oldEntrypoint));
        assertFalse(source.contains("GunAbilityConfig"));
        assertFalse(source.contains("PassiveIntegrationSkillTreeSync"));
        assertFalse(source.contains("PassiveIntegrationBonuses"));
        assertFalse(source.contains("PassiveIntegrationDamageConditions"));
        assertFalse(source.contains("PassiveIntegrationNetwork"));
        assertFalse(source.contains("GunAbilityHandler"));
    }
}
