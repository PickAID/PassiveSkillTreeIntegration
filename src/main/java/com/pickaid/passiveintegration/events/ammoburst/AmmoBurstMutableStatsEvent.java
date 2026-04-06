package com.pickaid.passiveintegration.events.ammoburst;

import net.minecraft.server.level.ServerPlayer;

public class AmmoBurstMutableStatsEvent extends AmmoBurstEvent {
    private float currentEnergy;
    private float maxEnergy;
    private float regenPerSecond;
    private float drainPerSecond;
    private float activationCost;

    public AmmoBurstMutableStatsEvent(
            ServerPlayer player,
            float currentEnergy,
            float maxEnergy,
            float regenPerSecond,
            float drainPerSecond,
            float activationCost
    ) {
        super(player, currentEnergy, maxEnergy, regenPerSecond, drainPerSecond, activationCost);
        this.currentEnergy = Math.max(0.0F, currentEnergy);
        this.maxEnergy = Math.max(0.0F, maxEnergy);
        this.regenPerSecond = Math.max(0.0F, regenPerSecond);
        this.drainPerSecond = Math.max(0.0F, drainPerSecond);
        this.activationCost = Math.max(0.0F, activationCost);
    }

    @Override
    public float getCurrentEnergy() {
        return currentEnergy;
    }

    public void setCurrentEnergy(float currentEnergy) {
        this.currentEnergy = Math.max(0.0F, currentEnergy);
    }

    @Override
    public float getMaxEnergy() {
        return maxEnergy;
    }

    public void setMaxEnergy(float maxEnergy) {
        this.maxEnergy = Math.max(0.0F, maxEnergy);
    }

    @Override
    public float getRegenPerSecond() {
        return regenPerSecond;
    }

    public void setRegenPerSecond(float regenPerSecond) {
        this.regenPerSecond = Math.max(0.0F, regenPerSecond);
    }

    @Override
    public float getDrainPerSecond() {
        return drainPerSecond;
    }

    public void setDrainPerSecond(float drainPerSecond) {
        this.drainPerSecond = Math.max(0.0F, drainPerSecond);
    }

    @Override
    public float getActivationCost() {
        return activationCost;
    }

    public void setActivationCost(float activationCost) {
        this.activationCost = Math.max(0.0F, activationCost);
    }
}
