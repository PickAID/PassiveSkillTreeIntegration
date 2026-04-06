package com.pickaid.passiveintegration.events.ammoburst;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class AmmoBurstTryStartEvent extends AmmoBurstMutableStatsEvent {
    private boolean ignoreUnlockRequirement;
    private boolean ignoreSupportedGunRequirement;

    public AmmoBurstTryStartEvent(
            ServerPlayer player,
            float currentEnergy,
            float maxEnergy,
            float regenPerSecond,
            float drainPerSecond,
            float activationCost
    ) {
        super(player, currentEnergy, maxEnergy, regenPerSecond, drainPerSecond, activationCost);
    }

    public boolean isIgnoreUnlockRequirement() {
        return ignoreUnlockRequirement;
    }

    public void setIgnoreUnlockRequirement(boolean ignoreUnlockRequirement) {
        this.ignoreUnlockRequirement = ignoreUnlockRequirement;
    }

    public boolean isIgnoreSupportedGunRequirement() {
        return ignoreSupportedGunRequirement;
    }

    public void setIgnoreSupportedGunRequirement(boolean ignoreSupportedGunRequirement) {
        this.ignoreSupportedGunRequirement = ignoreSupportedGunRequirement;
    }
}
