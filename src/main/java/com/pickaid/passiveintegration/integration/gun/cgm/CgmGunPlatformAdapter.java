package com.pickaid.passiveintegration.integration.gun.cgm;

import com.mrcrayfish.guns.entity.ProjectileEntity;
import com.mrcrayfish.guns.item.GunItem;
import com.pickaid.passiveintegration.integration.gun.GunPlatformAdapter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public final class CgmGunPlatformAdapter implements GunPlatformAdapter {
    @Override
    public String id() {
        return "cgm";
    }

    @Override
    public boolean matchesWeapon(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof GunItem;
    }

    @Override
    public boolean matchesProjectile(Entity entity) {
        return entity instanceof ProjectileEntity;
    }

    @Override
    public boolean matchesDamage(DamageSource source) {
        return source != null && matchesProjectile(source.getDirectEntity());
    }
}
