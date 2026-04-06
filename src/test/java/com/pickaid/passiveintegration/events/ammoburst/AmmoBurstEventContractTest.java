package com.pickaid.passiveintegration.events.ammoburst;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AmmoBurstEventContractTest {
    @Test
    void aboutToEndDefaultsToImmediateEnd() {
        AmmoBurstAboutToEndEvent event = new AmmoBurstAboutToEndEvent(
                null,
                AmmoBurstFinalReason.ENERGY_DEPLETED,
                30.0F,
                100.0F,
                2.0F,
                10.0F,
                15.0F,
                10.0F
        );

        assertEquals(AmmoBurstAboutToEndDecision.END_NOW, event.getDecision());
        event.setSustainIntervalTicks(0);
        assertEquals(1, event.getSustainIntervalTicks());
    }

    @Test
    void sustainDefaultsToContinueUntilExplicitlyChanged() {
        AmmoBurstSustainEvent event = new AmmoBurstSustainEvent(
                null,
                AmmoBurstFinalReason.ENERGY_DEPLETED,
                15.0F,
                10.0F,
                10.0F,
                0,
                0
        );

        assertEquals(AmmoBurstSustainDecision.CONTINUE, event.getDecision());
        event.setRefundEnergy(8.0F);
        assertEquals(8.0F, event.getRefundEnergy());
    }
}
