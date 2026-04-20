package com.pickaid.passiveintegration.compat.cgm;

import com.pickaid.passiveintegration.config.PassiveIntegrationCommonConfig;

public final class CgmGrenadeDurationCurves {
    private CgmGrenadeDurationCurves() {
    }

    public static double takenMultiplier(double levels) {
        return Math.pow(1.0D - PassiveIntegrationCommonConfig.cgmStunReductionPerLevel(), levels);
    }

    public static double appliedMultiplier(double levels) {
        double cap = PassiveIntegrationCommonConfig.cgmFlashDurationCap();
        double decay = PassiveIntegrationCommonConfig.cgmFlashDurationDecay();
        return 1.0D + cap * (1.0D - Math.pow(decay, levels));
    }
}
