package com.pickaid.passiveintegration.events.ammoburst;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

public abstract class AmmoBurstEvent extends Event {
    private final ServerPlayer player;
    private final float currentEnergy;
    private final float maxEnergy;
    private final float regenPerSecond;
    private final float drainPerSecond;
    private final float activationCost;

    protected AmmoBurstEvent(
            ServerPlayer player,
            float currentEnergy,
            float maxEnergy,
            float regenPerSecond,
            float drainPerSecond,
            float activationCost
    ) {
        this.player = player;
        this.currentEnergy = currentEnergy;
        this.maxEnergy = maxEnergy;
        this.regenPerSecond = regenPerSecond;
        this.drainPerSecond = drainPerSecond;
        this.activationCost = activationCost;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public float getCurrentEnergy() {
        return currentEnergy;
    }

    public float getMaxEnergy() {
        return maxEnergy;
    }

    public float getRegenPerSecond() {
        return regenPerSecond;
    }

    public float getDrainPerSecond() {
        return drainPerSecond;
    }

    public float getActivationCost() {
        return activationCost;
    }
}
