package com.pickaid.passiveintegration.compat.cgm;

import com.pickaid.passiveintegration.config.PassiveIntegrationCommonConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CgmGrenadeDurationCurvesTest {
    @Test
    void takenDurationUsesExponentialDecay() {
        PassiveIntegrationCommonConfig.overrideForTests(0.15D, 0.5D, 0.85D);

        assertEquals(0.85D, CgmGrenadeDurationCurves.takenMultiplier(1.0D), 0.0001D);
        assertEquals(0.7225D, CgmGrenadeDurationCurves.takenMultiplier(2.0D), 0.0001D);
        assertEquals(0.614125D, CgmGrenadeDurationCurves.takenMultiplier(3.0D), 0.0001D);
    }

    @Test
    void appliedDurationApproachesConfiguredCap() {
        PassiveIntegrationCommonConfig.overrideForTests(0.15D, 0.5D, 0.85D);

        assertEquals(1.075D, CgmGrenadeDurationCurves.appliedMultiplier(1.0D), 0.0001D);
        assertEquals(1.13875D, CgmGrenadeDurationCurves.appliedMultiplier(2.0D), 0.0001D);
        assertEquals(1.1929375D, CgmGrenadeDurationCurves.appliedMultiplier(3.0D), 0.0001D);
    }
}
