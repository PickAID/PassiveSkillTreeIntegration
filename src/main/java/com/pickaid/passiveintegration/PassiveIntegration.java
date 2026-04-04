package com.pickaid.passiveintegration;

import com.pickaid.passiveintegration.bootstrap.IntegrationBootstrap;
import com.pickaid.passiveintegration.bootstrap.LoadedModSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.crychicteam.passiveintegration.config.CgmConfig;
import org.crychicteam.passiveintegration.config.GunAbilityConfig;
import org.crychicteam.passiveintegration.events.PassiveIntegrationSkillTreeSync;
import org.crychicteam.passiveintegration.events.tacz.TACZGunsEvents;
import org.crychicteam.passiveintegration.init.PassiveIntegrationBonuses;
import org.crychicteam.passiveintegration.init.PassiveIntegrationDamageConditions;
import org.crychicteam.passiveintegration.network.PassiveIntegrationNetwork;
import org.crychicteam.passiveintegration.util.GunAbilityHandler;

import java.util.logging.Logger;

@Mod(PassiveIntegration.MOD_ID)
public class PassiveIntegration
{
	public static final String MOD_ID = "passiveintegration";
	public static final Logger LOGGER = Logger.getLogger(MOD_ID);
    private final IntegrationBootstrap bootstrap;

	public static ResourceLocation id(String path)
	{
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	public PassiveIntegration() {
        LoadedModSet loaded = modId -> ModList.get().isLoaded(modId);
        this.bootstrap = new IntegrationBootstrap(loaded);
		ModLoadingContext cxt = ModLoadingContext.get();
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		PassiveIntegrationBonuses.REGISTRY.register(modEventBus);
		PassiveIntegrationDamageConditions.REGISTRY.register(modEventBus);
		PassiveIntegrationNetwork.init();
		cxt.registerConfig(ModConfig.Type.COMMON, GunAbilityConfig.SPEC, "passiveintegration-gun-ability.toml");
		registerConfig(cxt, "cgm", CgmConfig.SPEC);
		registerEvents();
	}

	private void registerEvents() {
		MinecraftForge.EVENT_BUS.addListener(GunAbilityHandler::handlePlayerTick);
		MinecraftForge.EVENT_BUS.addListener(PassiveIntegrationSkillTreeSync::handleDatapackSync);
		if (isFeatureEnabled("tacz")) {
			MinecraftForge.EVENT_BUS.addListener(TACZGunsEvents::handleCritBonuses);
			MinecraftForge.EVENT_BUS.addListener(TACZGunsEvents::handleRetrievalBonus);
			MinecraftForge.EVENT_BUS.addListener(TACZGunsEvents::retrieveStuckAmmo);
		}
		if (isLoaded("irons_spellbooks")) {

		}
		if (isFeatureEnabled("pointblank")) {

		}
	}

	private void registerConfig(ModLoadingContext cxt, String mod, ForgeConfigSpec config) {
		if (isFeatureEnabled(mod)) {
			cxt.registerConfig(ModConfig.Type.COMMON, config, "passive" + mod + "-integration.toml");
		}
	}

    private boolean isFeatureEnabled(String modId) {
        return switch (modId) {
            case "kubejs", "cgm", "tacz", "pointblank" -> bootstrap.isFeatureEnabled(modId);
            default -> isLoaded(modId);
        };
    }

	public static Boolean isLoaded(String mod) {
        return ModList.get().isLoaded(mod);
	}
}
