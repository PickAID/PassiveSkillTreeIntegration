package com.pickaid.passiveintegration.bootstrap;

import org.junit.jupiter.api.Test;
import org.crychicteam.passiveintegration.util.GunCompatHelper;

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

    @Test
    void knowsWhichFeaturesItManages() {
        IntegrationBootstrap bootstrap = new IntegrationBootstrap(modId -> false);

        assertTrue(bootstrap.managesFeature("kubejs"));
        assertTrue(bootstrap.managesFeature("cgm"));
        assertTrue(bootstrap.managesFeature("tacz"));
        assertTrue(bootstrap.managesFeature("pointblank"));
        assertEquals(false, bootstrap.managesFeature("irons_spellbooks"));
    }

    @Test
    void registersOnlyAdaptersForLoadedMods() {
        LoadedModSet loaded = modId -> Set.of("tacz", "pointblank").contains(modId);

        IntegrationBootstrap bootstrap = new IntegrationBootstrap(loaded);

        assertEquals(Set.of("tacz", "pointblank"), bootstrap.gunPlatformRegistry().ids());
    }

    @Test
    void legacyGunCompatFacadeCanUseBootstrapRegistry() {
        LoadedModSet loaded = modId -> Set.of("tacz", "pointblank").contains(modId);
        IntegrationBootstrap bootstrap = new IntegrationBootstrap(loaded);

        GunCompatHelper.useRegistryForTesting(bootstrap.gunPlatformRegistry());
        try {
            assertEquals(Set.of("tacz", "pointblank"), GunCompatHelper.adapterIdsForTesting());
        } finally {
            GunCompatHelper.resetRegistryForTesting();
        }
    }
}
