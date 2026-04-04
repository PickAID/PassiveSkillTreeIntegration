package com.pickaid.passiveintegration.integration.gun;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class GunPlatformRegistry {
    private final Map<String, GunPlatformAdapter> adapters = new LinkedHashMap<>();

    public void register(GunPlatformAdapter adapter) {
        adapters.put(adapter.id(), adapter);
    }

    public Collection<GunPlatformAdapter> adapters() {
        return adapters.values();
    }

    public Set<String> ids() {
        return adapters.keySet();
    }
}
