package com.pickaid.passiveintegration.compat.carrier;

import java.util.List;

@FunctionalInterface
public interface CarrierCatalog {
    List<CarrierEntry> entries();
}
