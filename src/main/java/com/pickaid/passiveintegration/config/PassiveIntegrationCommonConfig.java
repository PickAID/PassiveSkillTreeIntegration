package com.pickaid.passiveintegration.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class PassiveIntegrationCommonConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.DoubleValue CGM_STUN_REDUCTION_PER_LEVEL;
    private static final ForgeConfigSpec.DoubleValue CGM_FLASH_DURATION_CAP;
    private static final ForgeConfigSpec.DoubleValue CGM_FLASH_DURATION_DECAY;

    private static double testReductionPerLevel = Double.NaN;
    private static double testFlashCap = Double.NaN;
    private static double testFlashDecay = Double.NaN;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("cgm");
        CGM_STUN_REDUCTION_PER_LEVEL =
                builder.comment("Per-level stun duration reduction before exponential compounding.")
                .defineInRange("stunReductionPerLevel", 0.15D, 0.0D, 0.95D);
        CGM_FLASH_DURATION_CAP =
                builder.comment("Upper bound for self-thrown flash duration amplification.")
                .defineInRange("flashDurationCap", 0.50D, 0.0D, 5.0D);
        CGM_FLASH_DURATION_DECAY =
                builder.comment("Decay factor used by the asymptotic flash-duration curve.")
                .defineInRange("flashDurationDecay", 0.85D, 0.0D, 0.999D);
        builder.pop();
        SPEC = builder.build();
    }

    private PassiveIntegrationCommonConfig() {
    }

    public static double cgmStunReductionPerLevel() {
        return Double.isNaN(testReductionPerLevel) ? CGM_STUN_REDUCTION_PER_LEVEL.get() : testReductionPerLevel;
    }

    public static double cgmFlashDurationCap() {
        return Double.isNaN(testFlashCap) ? CGM_FLASH_DURATION_CAP.get() : testFlashCap;
    }

    public static double cgmFlashDurationDecay() {
        return Double.isNaN(testFlashDecay) ? CGM_FLASH_DURATION_DECAY.get() : testFlashDecay;
    }

    public static void overrideForTests(double reductionPerLevel, double flashCap, double flashDecay) {
        testReductionPerLevel = reductionPerLevel;
        testFlashCap = flashCap;
        testFlashDecay = flashDecay;
    }
}
