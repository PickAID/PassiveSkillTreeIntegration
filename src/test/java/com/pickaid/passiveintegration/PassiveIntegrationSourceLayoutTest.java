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
        Path removedMixin = projectRoot.resolve("src/main/java/org/crychicteam/passiveintegration/mixins/conditions/EnchantedConditionMixin.java");
        Path mixinConfig = projectRoot.resolve("src/main/resources/mixins.passiveintegration.json");

        String source = Files.readString(movedEntrypoint);
        String mixinConfigSource = Files.readString(mixinConfig);

        assertFalse(Files.exists(oldEntrypoint));
        assertFalse(Files.exists(removedMixin));
        assertFalse(source.contains("GunAbilityConfig"));
        assertFalse(source.contains("PassiveIntegrationSkillTreeSync"));
        assertFalse(source.contains("PassiveIntegrationBonuses"));
        assertFalse(source.contains("PassiveIntegrationDamageConditions"));
        assertFalse(source.contains("PassiveIntegrationNetwork"));
        assertFalse(source.contains("GunAbilityHandler"));
        assertFalse(mixinConfigSource.contains("conditions.EnchantedConditionMixin"));
    }
}
