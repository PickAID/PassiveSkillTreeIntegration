package com.pickaid.passiveintegration.events.ammoburst;

import net.minecraft.server.level.ServerPlayer;

public class AmmoBurstFailEvent extends AmmoBurstEvent {
    private final AmmoBurstFailReason reason;

    public AmmoBurstFailEvent(
            ServerPlayer player,
            AmmoBurstFailReason reason,
            float currentEnergy,
            float maxEnergy,
            float regenPerSecond,
            float drainPerSecond,
            float activationCost
    ) {
        super(player, currentEnergy, maxEnergy, regenPerSecond, drainPerSecond, activationCost);
        this.reason = reason;
    }

    public AmmoBurstFailReason getReason() {
        return reason;
    }
}
