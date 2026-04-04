package com.pickaid.passiveintegration.util;

import com.pickaid.passiveintegration.integration.gun.GunPlatformRegistry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class GunCompatHelper {
    private final GunPlatformRegistry registry;

    public GunCompatHelper(GunPlatformRegistry registry) {
        this.registry = Objects.requireNonNull(registry);
    }

    public boolean isSupportedGun(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && registry.adapters().stream().anyMatch(adapter -> adapter.matchesWeapon(stack));
    }

    public boolean isGunProjectile(Entity entity) {
        return entity != null
                && registry.adapters().stream().anyMatch(adapter -> adapter.matchesProjectile(entity));
    }

    public boolean isGunDamage(DamageSource source) {
        return source != null
                && registry.adapters().stream().anyMatch(adapter -> adapter.matchesDamage(source));
    }
}
