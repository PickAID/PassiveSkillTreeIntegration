package com.pickaid.passiveintegration.util;

import com.pickaid.passiveintegration.events.ammoburst.AmmoBurstFinalReason;
import com.pickaid.passiveintegration.events.ammoburst.AmmoBurstRuntimeState;

public record AmmoBurstStateData(
        AmmoBurstRuntimeState runtimeState,
        float energy,
        AmmoBurstFinalReason sourceReason,
        int sustainStartDelayTicks,
        int sustainIntervalTicks,
        long nextTriggerTick,
        int runIndex,
        int sustainAgeTicks
) {
    public static AmmoBurstStateData off(float energy) {
        return new AmmoBurstStateData(AmmoBurstRuntimeState.OFF, energy, null, 0, 0, 0L, 0, 0);
    }

    public static AmmoBurstStateData active(float energy) {
        return new AmmoBurstStateData(AmmoBurstRuntimeState.ACTIVE, energy, null, 0, 0, 0L, 0, 0);
    }

    public static AmmoBurstStateData zeroSustain(
            AmmoBurstFinalReason sourceReason,
            int sustainIntervalTicks,
            long nextTriggerTick,
            int runIndex,
            int sustainAgeTicks
    ) {
        return new AmmoBurstStateData(
                AmmoBurstRuntimeState.ZERO_SUSTAIN,
                0.0F,
                sourceReason,
                0,
                sustainIntervalTicks,
                nextTriggerTick,
                runIndex,
                sustainAgeTicks
        );
    }
}
