package com.pickaid.passiveintegration.compat.cgm;

import com.pickaid.passiveintegration.PassiveIntegration;
import com.pickaid.passiveintegration.compat.carrier.CarrierCatalog;
import com.pickaid.passiveintegration.compat.carrier.CarrierDomain;
import com.pickaid.passiveintegration.compat.carrier.CarrierEntry;
import com.pickaid.passiveintegration.compat.carrier.CarrierSemantic;
import com.pickaid.passiveintegration.compat.carrier.CarrierTarget;
import com.pickaid.passiveintegration.compat.carrier.binding.SkillBonusCarrierBinding;
import com.pickaid.passiveintegration.compat.cgm.bonus.CgmFlashGrenadeDurationAppliedBonus;
import com.pickaid.passiveintegration.compat.cgm.bonus.CgmStunGrenadeDurationTakenBonus;
import com.pickaid.passiveintegration.optional.CarrierGates;

import java.util.List;

public final class CgmCarrierCatalog implements CarrierCatalog {
    @Override
    public List<CarrierEntry> entries() {
        return List.of(
                CarrierEntry.skillBonus(
                        PassiveIntegration.id("cgm_stun_grenade_duration_taken_reduction"),
                        "cgm",
                        CarrierGates.all(
                                CarrierGates.modLoaded("cgm"),
                                CarrierGates.classPresent("com.mrcrayfish.guns.entity.ThrowableStunGrenadeEntity")
                        ),
                        CarrierDomain.GRENADE,
                        CarrierSemantic.CONTROL_DURATION_TAKEN,
                        CarrierTarget.SELF,
                        "Reduce CGM stun-grenade duration taken",
                        true,
                        new SkillBonusCarrierBinding(
                                CgmStunGrenadeDurationTakenBonus.Serializer::new,
                                CgmStunGrenadeDurationTakenBonus::bindSerializer
                        )
                ),
                CarrierEntry.skillBonus(
                        PassiveIntegration.id("cgm_flash_grenade_duration_applied_bonus"),
                        "cgm",
                        CarrierGates.all(
                                CarrierGates.modLoaded("cgm"),
                                CarrierGates.classPresent("com.mrcrayfish.guns.entity.ThrowableStunGrenadeEntity")
                        ),
                        CarrierDomain.GRENADE,
                        CarrierSemantic.CONTROL_DURATION_APPLIED,
                        CarrierTarget.VICTIM,
                        "Increase duration applied by self-thrown CGM flash and stun grenades",
                        true,
                        new SkillBonusCarrierBinding(
                                CgmFlashGrenadeDurationAppliedBonus.Serializer::new,
                                CgmFlashGrenadeDurationAppliedBonus::bindSerializer
                        )
                )
        );
    }
}
