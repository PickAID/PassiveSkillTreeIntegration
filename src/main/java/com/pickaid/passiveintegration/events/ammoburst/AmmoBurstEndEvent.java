package com.pickaid.passiveintegration.events.ammoburst;

import net.minecraft.server.level.ServerPlayer;

public class AmmoBurstEndEvent extends AmmoBurstEvent {
    private final AmmoBurstFinalReason finalReason;
    private final AmmoBurstFinalReason sourceReason;
    private final boolean passedThroughZeroSustain;

    public AmmoBurstEndEvent(
            ServerPlayer player,
            AmmoBurstFinalReason finalReason,
            AmmoBurstFinalReason sourceReason,
            float currentEnergy,
            float maxEnergy,
            float regenPerSecond,
            float drainPerSecond,
            float activationCost,
            boolean passedThroughZeroSustain
    ) {
        super(player, currentEnergy, maxEnergy, regenPerSecond, drainPerSecond, activationCost);
        this.finalReason = finalReason;
        this.sourceReason = sourceReason;
        this.passedThroughZeroSustain = passedThroughZeroSustain;
    }

    public AmmoBurstFinalReason getFinalReason() {
        return finalReason;
    }

    public AmmoBurstFinalReason getSourceReason() {
        return sourceReason;
    }

    public boolean isPassedThroughZeroSustain() {
        return passedThroughZeroSustain;
    }
}
