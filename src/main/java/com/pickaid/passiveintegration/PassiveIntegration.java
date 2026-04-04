package com.pickaid.passiveintegration;

import com.pickaid.passiveintegration.bootstrap.IntegrationBootstrap;
import com.pickaid.passiveintegration.bootstrap.LoadedModSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.crychicteam.passiveintegration.config.CgmConfig;
import org.crychicteam.passiveintegration.events.tacz.TACZGunsEvents;

import java.util.logging.Logger;

@Mod(PassiveIntegration.MOD_ID)
public class PassiveIntegration
{
	public static final String MOD_ID = "passiveintegration";
	public static final Logger LOGGER = Logger.getLogger(MOD_ID);
    private final IntegrationBootstrap bootstrap;

	public static ResourceLocation id(String path)
	{
		return new ResourceLocation(MOD_ID, path);
	}

	public PassiveIntegration() {
        LoadedModSet loaded = modId -> ModList.get().isLoaded(modId);
        this.bootstrap = new IntegrationBootstrap(loaded);
		ModLoadingContext cxt = ModLoadingContext.get();
		registerConfig(cxt, "cgm", CgmConfig.SPEC);
		registerEvents();
	}

	private void registerEvents() {
		if (isFeatureEnabled("tacz")) {
			MinecraftForge.EVENT_BUS.addListener(TACZGunsEvents::handleRetrievalBonus);
			MinecraftForge.EVENT_BUS.addListener(TACZGunsEvents::entityKilledByGunEvent);
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
        if (bootstrap.managesFeature(modId)) {
            return bootstrap.isFeatureEnabled(modId);
        }
        return isLoaded(modId);
    }

	public static Boolean isLoaded(String mod) {
        return ModList.get().isLoaded(mod);
	}
}
