package com.pickaid.passiveintegration.bootstrap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class IntegrationBootstrap {
    private static final List<String> MANAGED_FEATURES = List.of("kubejs", "cgm", "tacz", "pointblank");
    private final Set<String> enabledFeatures = new HashSet<>();

    public IntegrationBootstrap(LoadedModSet loaded) {
        for (String feature : MANAGED_FEATURES) {
            if (loaded.isLoaded(feature)) {
                enabledFeatures.add(feature);
            }
        }
    }

    public boolean isFeatureEnabled(String id) {
        return enabledFeatures.contains(id);
    }

    public boolean managesFeature(String id) {
        return MANAGED_FEATURES.contains(id);
    }
}
