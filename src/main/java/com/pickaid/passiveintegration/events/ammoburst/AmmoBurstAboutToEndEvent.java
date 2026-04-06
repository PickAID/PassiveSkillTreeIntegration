package com.pickaid.passiveintegration.events.ammoburst;

import net.minecraft.server.level.ServerPlayer;

public class AmmoBurstAboutToEndEvent extends AmmoBurstEvent {
    private final AmmoBurstFinalReason reason;
    private final float drainThisStep;
    private AmmoBurstAboutToEndDecision decision = AmmoBurstAboutToEndDecision.END_NOW;
    private float refundEnergy;
    private int sustainStartDelayTicks;
    private int sustainIntervalTicks = 1;

    public AmmoBurstAboutToEndEvent(
            ServerPlayer player,
            AmmoBurstFinalReason reason,
            float currentEnergy,
            float maxEnergy,
            float regenPerSecond,
            float drainPerSecond,
            float activationCost,
            float drainThisStep
    ) {
        super(player, currentEnergy, maxEnergy, regenPerSecond, drainPerSecond, activationCost);
        this.reason = reason;
        this.drainThisStep = drainThisStep;
    }

    public AmmoBurstFinalReason getReason() {
        return reason;
    }

    public float getDrainThisStep() {
        return drainThisStep;
    }

    public AmmoBurstAboutToEndDecision getDecision() {
        return decision;
    }

    public float getRefundEnergy() {
        return refundEnergy;
    }

    public int getSustainStartDelayTicks() {
        return sustainStartDelayTicks;
    }

    public int getSustainIntervalTicks() {
        return sustainIntervalTicks;
    }

    public void endNow() {
        this.decision = AmmoBurstAboutToEndDecision.END_NOW;
        this.refundEnergy = 0.0F;
    }

    public void refundAndContinue(float amount) {
        this.decision = AmmoBurstAboutToEndDecision.REFUND_AND_CONTINUE;
        this.refundEnergy = Math.max(0.0F, amount);
    }

    public void enterZeroSustain(int startDelayTicks, int intervalTicks) {
        this.decision = AmmoBurstAboutToEndDecision.ENTER_ZERO_SUSTAIN;
        this.sustainStartDelayTicks = Math.max(0, startDelayTicks);
        this.sustainIntervalTicks = Math.max(1, intervalTicks);
    }

    public void setSustainIntervalTicks(int ticks) {
        this.sustainIntervalTicks = Math.max(1, ticks);
    }
}
