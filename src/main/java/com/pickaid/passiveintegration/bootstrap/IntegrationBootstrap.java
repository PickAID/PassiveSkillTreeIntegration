package com.pickaid.passiveintegration.bootstrap;

import java.util.HashSet;
import java.util.Set;

public final class IntegrationBootstrap {
    private final Set<String> enabledFeatures = new HashSet<>();

    public IntegrationBootstrap(LoadedModSet loaded) {
        if (loaded.isLoaded("kubejs")) {
            enabledFeatures.add("kubejs");
        }
        if (loaded.isLoaded("cgm")) {
            enabledFeatures.add("cgm");
        }
        if (loaded.isLoaded("tacz")) {
            enabledFeatures.add("tacz");
        }
        if (loaded.isLoaded("pointblank")) {
            enabledFeatures.add("pointblank");
        }
    }

    public boolean isFeatureEnabled(String id) {
        return enabledFeatures.contains(id);
    }
}
