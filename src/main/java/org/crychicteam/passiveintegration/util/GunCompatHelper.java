package org.crychicteam.passiveintegration.util;

import com.pickaid.passiveintegration.PassiveIntegration;
import com.pickaid.passiveintegration.integration.gun.GunPlatformRegistry;
import com.pickaid.passiveintegration.integration.gun.cgm.CgmGunPlatformAdapter;
import com.pickaid.passiveintegration.integration.gun.pointblank.PointBlankGunPlatformAdapter;
import com.pickaid.passiveintegration.integration.gun.tacz.TaczGunPlatformAdapter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Set;

public final class GunCompatHelper {
    private static volatile GunPlatformRegistry testingRegistry;

    private GunCompatHelper() {
    }

    public static boolean isGunDamage(DamageSource source) {
        return helper().isGunDamage(source);
    }

    public static boolean isSupportedGun(ItemStack stack) {
        return helper().isSupportedGun(stack);
    }

    public static boolean isGunProjectile(Entity entity) {
        return helper().isGunProjectile(entity);
    }

    public static void useRegistryForTesting(GunPlatformRegistry registry) {
        testingRegistry = Objects.requireNonNull(registry);
    }

    public static Set<String> adapterIdsForTesting() {
        return Set.copyOf(activeRegistry().ids());
    }

    public static void resetRegistryForTesting() {
        testingRegistry = null;
    }

    private static com.pickaid.passiveintegration.util.GunCompatHelper helper() {
        return new com.pickaid.passiveintegration.util.GunCompatHelper(activeRegistry());
    }

    private static GunPlatformRegistry activeRegistry() {
        GunPlatformRegistry registry = testingRegistry;
        return registry != null ? registry : runtimeRegistry();
    }

    private static GunPlatformRegistry runtimeRegistry() {
        GunPlatformRegistry registry = new GunPlatformRegistry();
        if (PassiveIntegration.isLoaded("cgm")) {
            registry.register(new CgmGunPlatformAdapter());
        }
        if (PassiveIntegration.isLoaded("tacz")) {
            registry.register(new TaczGunPlatformAdapter());
        }
        if (PassiveIntegration.isLoaded("pointblank")) {
            registry.register(new PointBlankGunPlatformAdapter());
        }
        return registry;
    }
}
