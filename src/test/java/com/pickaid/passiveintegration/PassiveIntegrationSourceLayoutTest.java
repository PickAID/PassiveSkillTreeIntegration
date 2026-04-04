package com.pickaid.passiveintegration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertFalse(source.contains("fromNamespaceAndPath"));
        assertFalse(source.contains("handleCritBonuses"));
        assertFalse(source.contains("retrieveStuckAmmo"));
        assertFalse(source.contains("onGunFire"));
        assertFalse(source.contains("return ResourceLocation.fromNamespaceAndPath"));
        assertFalse(source.contains("TACZGunsEvents::handleCritBonuses"));
        assertFalse(source.contains("TACZGunsEvents::retrieveStuckAmmo"));
        assertFalse(source.contains("TACZGunsEvents::onGunFire"));
        assertFalse(source.contains("return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);"));
        assertFalse(source.contains("MinecraftForge.EVENT_BUS.addListener(TACZGunsEvents::handleCritBonuses);"));
        assertFalse(source.contains("MinecraftForge.EVENT_BUS.addListener(TACZGunsEvents::retrieveStuckAmmo);"));
        assertFalse(source.contains("MinecraftForge.EVENT_BUS.addListener(TACZGunsEvents::onGunFire);"));
        assertFalse(mixinConfigSource.contains("conditions.EnchantedConditionMixin"));
        assertFalse(source.contains("entityKilledByGunEvent()"));
        assertFalse(source.contains("new ResourceLocation(MOD_ID, path) == null"));
        assertTrue(source.contains("return new ResourceLocation(MOD_ID, path);"));
        assertTrue(source.contains("MinecraftForge.EVENT_BUS.addListener(TACZGunsEvents::entityKilledByGunEvent);"));
    }
}
