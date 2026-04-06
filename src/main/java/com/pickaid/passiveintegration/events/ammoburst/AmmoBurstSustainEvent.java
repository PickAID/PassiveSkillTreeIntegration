package com.pickaid.passiveintegration.events.ammoburst;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

public class AmmoBurstSustainEvent extends Event {
    private final ServerPlayer player;
    private final AmmoBurstFinalReason sourceEndReason;
    private final float activationCost;
    private final float drainPerSecond;
    private final float drainThisStep;
    private final int runIndex;
    private final int sustainAgeTicks;
    private AmmoBurstSustainDecision decision = AmmoBurstSustainDecision.CONTINUE;
    private float refundEnergy;

    public AmmoBurstSustainEvent(
            ServerPlayer player,
            AmmoBurstFinalReason sourceEndReason,
            float activationCost,
            float drainPerSecond,
            float drainThisStep,
            int runIndex,
            int sustainAgeTicks
    ) {
        this.player = player;
        this.sourceEndReason = sourceEndReason;
        this.activationCost = activationCost;
        this.drainPerSecond = drainPerSecond;
        this.drainThisStep = drainThisStep;
        this.runIndex = runIndex;
        this.sustainAgeTicks = sustainAgeTicks;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public AmmoBurstFinalReason getSourceEndReason() {
        return sourceEndReason;
    }

    public float getActivationCost() {
        return activationCost;
    }

    public float getDrainPerSecond() {
        return drainPerSecond;
    }

    public float getDrainThisStep() {
        return drainThisStep;
    }

    public int getRunIndex() {
        return runIndex;
    }

    public int getSustainAgeTicks() {
        return sustainAgeTicks;
    }

    public AmmoBurstSustainDecision getDecision() {
        return decision;
    }

    public float getRefundEnergy() {
        return refundEnergy;
    }

    public void continueSustain() {
        this.decision = AmmoBurstSustainDecision.CONTINUE;
        this.refundEnergy = 0.0F;
    }

    public void terminate() {
        this.decision = AmmoBurstSustainDecision.TERMINATE;
        this.refundEnergy = 0.0F;
    }

    public void exitWithRefund(float amount) {
        this.decision = AmmoBurstSustainDecision.EXIT_WITH_REFUND;
        this.refundEnergy = Math.max(0.0F, amount);
    }

    public void setRefundEnergy(float refundEnergy) {
        this.refundEnergy = Math.max(0.0F, refundEnergy);
    }
}
