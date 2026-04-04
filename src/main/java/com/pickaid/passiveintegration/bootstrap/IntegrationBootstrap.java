package com.pickaid.passiveintegration.bootstrap;

import com.pickaid.passiveintegration.integration.gun.GunPlatformRegistry;
import com.pickaid.passiveintegration.integration.gun.cgm.CgmGunPlatformAdapter;
import com.pickaid.passiveintegration.integration.gun.pointblank.PointBlankGunPlatformAdapter;
import com.pickaid.passiveintegration.integration.gun.tacz.TaczGunPlatformAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class IntegrationBootstrap {
    private static final List<String> MANAGED_FEATURES = List.of("kubejs", "cgm", "tacz", "pointblank");
    private final Set<String> enabledFeatures = new HashSet<>();
    private final GunPlatformRegistry gunPlatformRegistry = new GunPlatformRegistry();

    public IntegrationBootstrap(LoadedModSet loaded) {
        for (String feature : MANAGED_FEATURES) {
            if (loaded.isLoaded(feature)) {
                enabledFeatures.add(feature);
            }
        }
        if (loaded.isLoaded("cgm")) {
            gunPlatformRegistry.register(new CgmGunPlatformAdapter());
        }
        if (loaded.isLoaded("tacz")) {
            gunPlatformRegistry.register(new TaczGunPlatformAdapter());
        }
        if (loaded.isLoaded("pointblank")) {
            gunPlatformRegistry.register(new PointBlankGunPlatformAdapter());
        }
    }

    public boolean isFeatureEnabled(String id) {
        return enabledFeatures.contains(id);
    }

    public boolean managesFeature(String id) {
        return MANAGED_FEATURES.contains(id);
    }

    public GunPlatformRegistry gunPlatformRegistry() {
        return gunPlatformRegistry;
    }
}
