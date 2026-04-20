package com.pickaid.passiveintegration.compat.carrier.binding;

import com.pickaid.passiveintegration.compat.carrier.CarrierActivation;
import com.pickaid.passiveintegration.compat.carrier.CarrierEntry;
import com.pickaid.passiveintegration.compat.carrier.CarrierKind;

public interface CarrierBinder {
    CarrierKind kind();

    CarrierActivation bind(CarrierEntry entry);
}
