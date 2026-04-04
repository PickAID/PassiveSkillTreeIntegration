package com.pickaid.passiveintegration.bootstrap;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrationBootstrapTest {
    @Test
    void buildsExpectedFeatureSetFromLoadedMods() {
        LoadedModSet loaded = modId -> Set.of("kubejs", "tacz", "pointblank").contains(modId);

        IntegrationBootstrap bootstrap = new IntegrationBootstrap(loaded);

        assertTrue(bootstrap.isFeatureEnabled("kubejs"));
        assertTrue(bootstrap.isFeatureEnabled("tacz"));
        assertTrue(bootstrap.isFeatureEnabled("pointblank"));
        assertEquals(false, bootstrap.isFeatureEnabled("cgm"));
    }
}
