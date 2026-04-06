package com.pickaid.passiveintegration.util;

import com.pickaid.passiveintegration.events.ammoburst.AmmoBurstAboutToEndDecision;
import com.pickaid.passiveintegration.events.ammoburst.AmmoBurstFinalReason;
import com.pickaid.passiveintegration.events.ammoburst.AmmoBurstRuntimeState;
import com.pickaid.passiveintegration.events.ammoburst.AmmoBurstSustainDecision;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GunAbilityHandlerTest {
    @Test
    void alreadyActiveWinsOverOtherFailures() {
        assertEquals(
                GunAbilityHandler.ActivationResult.ALREADY_ACTIVE,
                GunAbilityHandler.evaluateActivationPreconditions(true, true, false, false, true, false, false, 100.0F, 25.0F)
        );
    }

    @Test
    void missingEnergyBlocksActivation() {
        assertEquals(
                GunAbilityHandler.ActivationResult.MISSING_ENERGY,
                GunAbilityHandler.evaluateActivationPreconditions(false, true, false, false, false, false, false, 10.0F, 25.0F)
        );
    }

    @Test
    void missingUnlockBlocksWhenNotIgnored() {
        assertEquals(
                GunAbilityHandler.ActivationResult.MISSING_UNLOCK,
                GunAbilityHandler.evaluateActivationPreconditions(false, true, false, false, false, false, false, 100.0F, 25.0F)
        );
    }

    @Test
    void missingUnlockCanBeIgnoredByScript() {
        assertNull(GunAbilityHandler.evaluateActivationPreconditions(false, true, false, true, false, false, false, 100.0F, 25.0F));
    }

    @Test
    void missingGunBlocksWhenNotIgnored() {
        assertEquals(
                GunAbilityHandler.ActivationResult.MISSING_GUN,
                GunAbilityHandler.evaluateActivationPreconditions(false, false, false, false, true, false, false, 100.0F, 25.0F)
        );
    }

    @Test
    void missingGunCanBeIgnoredByScript() {
        assertNull(GunAbilityHandler.evaluateActivationPreconditions(false, false, false, false, true, false, true, 100.0F, 25.0F));
    }

    @Test
    void inactiveStateRegeneratesEnergyUpToMax() {
        assertEquals(65.0F, GunAbilityHandler.advanceEnergy(50.0F, 100.0F, 15.0F, 25.0F, false));
        assertEquals(100.0F, GunAbilityHandler.advanceEnergy(95.0F, 100.0F, 15.0F, 25.0F, false));
    }

    @Test
    void activeStateConsumesEnergyDownToZero() {
        assertEquals(25.0F, GunAbilityHandler.advanceEnergy(50.0F, 100.0F, 15.0F, 25.0F, true));
        assertEquals(0.0F, GunAbilityHandler.advanceEnergy(10.0F, 100.0F, 15.0F, 25.0F, true));
    }

    @Test
    void configuredStatsUseDirectAttributeMultipliers() {
        assertEquals(22.5F, GunAbilityHandler.applyMultiplier(22.5F, 1.0F));
        assertEquals(11.25F, GunAbilityHandler.applyMultiplier(22.5F, 0.5F));
        assertEquals(0.0F, GunAbilityHandler.applyMultiplier(22.5F, -2.0F));
    }

    @Test
    void activationCostIsClampedIntoCurrentEnergyPool() {
        assertEquals(22.5F, GunAbilityHandler.clampActivationCost(22.5F, 30.0F));
        assertEquals(30.0F, GunAbilityHandler.clampActivationCost(120.0F, 30.0F));
        assertEquals(0.0F, GunAbilityHandler.clampActivationCost(-5.0F, 30.0F));
    }

    @Test
    void refundDecisionReturnsToActiveState() {
        AmmoBurstStateData state = AmmoBurstStateData.active(0.0F);

        AmmoBurstStateData updated = GunAbilityHandler.applyAboutToEndDecision(
                state,
                AmmoBurstAboutToEndDecision.REFUND_AND_CONTINUE,
                12.5F,
                0,
                0,
                200L,
                AmmoBurstFinalReason.ENERGY_DEPLETED
        );

        assertEquals(AmmoBurstRuntimeState.ACTIVE, updated.runtimeState());
        assertEquals(12.5F, updated.energy());
    }

    @Test
    void zeroSustainDecisionStoresSchedule() {
        AmmoBurstStateData state = AmmoBurstStateData.active(0.0F);

        AmmoBurstStateData updated = GunAbilityHandler.applyAboutToEndDecision(
                state,
                AmmoBurstAboutToEndDecision.ENTER_ZERO_SUSTAIN,
                0.0F,
                10,
                20,
                200L,
                AmmoBurstFinalReason.ENERGY_DEPLETED
        );

        assertEquals(AmmoBurstRuntimeState.ZERO_SUSTAIN, updated.runtimeState());
        assertEquals(210L, updated.nextTriggerTick());
        assertEquals(20, updated.sustainIntervalTicks());
    }

    @Test
    void sustainRefundReturnsFromZeroSustainToActiveState() {
        AmmoBurstStateData state = AmmoBurstStateData.zeroSustain(
                AmmoBurstFinalReason.ENERGY_DEPLETED,
                20,
                220L,
                3,
                60
        );

        AmmoBurstStateData updated = GunAbilityHandler.applySustainDecision(
                state,
                AmmoBurstSustainDecision.EXIT_WITH_REFUND,
                18.0F,
                240L
        );

        assertEquals(AmmoBurstRuntimeState.ACTIVE, updated.runtimeState());
        assertEquals(18.0F, updated.energy());
    }
}
