package com.pickaid.passiveintegration.bootstrap;

import com.pickaid.passiveintegration.bridge.carrier.CarrierDebugBridge;
import com.pickaid.passiveintegration.compat.carrier.CarrierCatalog;
import com.pickaid.passiveintegration.compat.carrier.CarrierRegistry;
import com.pickaid.passiveintegration.compat.carrier.binding.CarrierBinders;
import com.pickaid.passiveintegration.compat.cgm.CgmCarrierCatalog;
import com.pickaid.passiveintegration.config.PassiveIntegrationCommonConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.List;
import java.util.Objects;

public final class PassiveIntegrationBootstrap {
    private static CarrierRegistry carrierRegistry;
    private static CarrierDebugBridge carrierDebugBridge;

    private PassiveIntegrationBootstrap() {
    }

    public static void init(IEventBus modBus) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PassiveIntegrationCommonConfig.SPEC);
        CarrierBinders binders = new CarrierBinders(Objects.requireNonNull(modBus, "modBus"));
        carrierRegistry = new CarrierRegistry(binders);
        carrierRegistry.bindAll(List.<CarrierCatalog>of(new CgmCarrierCatalog()));
        carrierDebugBridge = new CarrierDebugBridge(carrierRegistry);
    }

    static CarrierRegistry carrierRegistry() {
        return carrierRegistry;
    }

    static CarrierDebugBridge carrierDebugBridge() {
        return carrierDebugBridge;
    }
}
