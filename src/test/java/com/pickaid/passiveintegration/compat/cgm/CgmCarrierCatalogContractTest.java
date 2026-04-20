package com.pickaid.passiveintegration.compat.cgm;

import com.pickaid.passiveintegration.compat.carrier.CarrierDomain;
import com.pickaid.passiveintegration.compat.carrier.CarrierEntry;
import com.pickaid.passiveintegration.compat.carrier.CarrierSemantic;
import com.pickaid.passiveintegration.compat.carrier.CarrierTarget;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CgmCarrierCatalogContractTest {
    @Test
    void catalogDeclaresTheTwoGrenadeControlEntries() {
        List<CarrierEntry> entries = new CgmCarrierCatalog().entries();

        assertEquals(2, entries.size());
        assertEquals(CarrierDomain.GRENADE, entries.get(0).domain());
        assertEquals(CarrierSemantic.CONTROL_DURATION_TAKEN, entries.get(0).semantic());
        assertEquals(CarrierTarget.SELF, entries.get(0).target());
        assertEquals(CarrierSemantic.CONTROL_DURATION_APPLIED, entries.get(1).semantic());
        assertEquals(CarrierTarget.VICTIM, entries.get(1).target());
    }
}
