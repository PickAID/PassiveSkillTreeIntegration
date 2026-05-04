package com.pickaid.passiveintegration.compat.cgm;

import com.pickaid.passiveintegration.config.PassiveIntegrationCommonConfig;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CgmGrenadeEffectHooksTest {
    @Test
    void ownerBonusAndTargetBonusBothApplyToCgmBlindAndDeafenEffects() {
        PassiveIntegrationCommonConfig.overrideForTests(0.15D, 0.5D, 0.85D);
        int modified = CgmGrenadeEffectHooks.calculateDuration(
                new ResourceLocation("cgm", "blinded"),
                200,
                2.0D,
                1.0D
        );

        assertEquals(194, modified);
    }

    @Test
    void nonCgmEffectsPassThroughUntouched() {
        PassiveIntegrationCommonConfig.overrideForTests(0.15D, 0.5D, 0.85D);
        assertEquals(200, CgmGrenadeEffectHooks.calculateDuration(
                new ResourceLocation("minecraft", "blindness"),
                200,
                3.0D,
                3.0D
        ));
    }
}
