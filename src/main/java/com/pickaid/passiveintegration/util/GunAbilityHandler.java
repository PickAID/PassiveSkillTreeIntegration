package com.pickaid.passiveintegration.util;

import com.pickaid.passiveintegration.config.GunAbilityConfig;
import com.pickaid.passiveintegration.events.ammoburst.AmmoBurstAboutToEndDecision;
import com.pickaid.passiveintegration.events.ammoburst.AmmoBurstAboutToEndEvent;
import com.pickaid.passiveintegration.events.ammoburst.AmmoBurstEndEvent;
import com.pickaid.passiveintegration.events.ammoburst.AmmoBurstFailEvent;
import com.pickaid.passiveintegration.events.ammoburst.AmmoBurstFailReason;
import com.pickaid.passiveintegration.events.ammoburst.AmmoBurstFinalReason;
import com.pickaid.passiveintegration.events.ammoburst.AmmoBurstRuntimeState;
import com.pickaid.passiveintegration.events.ammoburst.AmmoBurstStartEvent;
import com.pickaid.passiveintegration.events.ammoburst.AmmoBurstSustainDecision;
import com.pickaid.passiveintegration.events.ammoburst.AmmoBurstSustainEvent;
import com.pickaid.passiveintegration.events.ammoburst.AmmoBurstTryStartEvent;
import com.pickaid.passiveintegration.init.PassiveIntegrationAttributes;
import com.pickaid.passiveintegration.network.PassiveIntegrationNetwork;
import com.pickaid.passiveintegration.network.s2c.SyncAmmoBurstStateMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.RegistryObject;

import java.util.Locale;

public final class GunAbilityHandler {
    private static final String ROOT_TAG = "PassiveIntegrationGunAbility";
    private static final String ACTIVE_TAG = "AmmoBurstActive";
    private static final String ENERGY_TAG = "AmmoBurstEnergy";
    private static final String RUNTIME_STATE_TAG = "AmmoBurstRuntimeState";
    private static final String SOURCE_REASON_TAG = "AmmoBurstSourceReason";
    private static final String DISABLED_REDIRECT_PENDING_TAG = "AmmoBurstDisabledRedirectPending";
    private static final String SUSTAIN_START_DELAY_TAG = "AmmoBurstSustainStartDelayTicks";
    private static final String SUSTAIN_INTERVAL_TAG = "AmmoBurstSustainIntervalTicks";
    private static final String NEXT_TRIGGER_TICK_TAG = "AmmoBurstNextTriggerTick";
    private static final String RUN_INDEX_TAG = "AmmoBurstRunIndex";
    private static final String SUSTAIN_AGE_TICKS_TAG = "AmmoBurstSustainAgeTicks";
    private static final String LEGACY_ACTIVE_UNTIL_TAG = "AmmoBurstActiveUntil";
    private static final String LEGACY_COOLDOWN_UNTIL_TAG = "AmmoBurstCooldownUntil";
    private static final float ENERGY_EPSILON = 0.0001F;

    private GunAbilityHandler() {
    }

    public static void handlePlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        tick(player);
    }

    public static void handleActivationRequest(ServerPlayer player) {
        ActivationResult result = tryActivate(player);
        if (GunAbilityConfig.SEND_STATUS_MESSAGES.get()) {
            sendStatusMessage(player, result);
        }
        syncState(player);
    }

    public static boolean isAmmoBurstActive(Player player) {
        if (!GunAbilityConfig.ENABLE_AMMO_BURST.get()) {
            return false;
        }
        CompoundTag data = getExistingAbilityData(player);
        if (data == null) {
            return false;
        }
        return isStateActiveForReporting(getStateData(data, Math.max(0.0F, data.getFloat(ENERGY_TAG))).runtimeState());
    }

    public static ActivationResult tryActivate(ServerPlayer player) {
        if (!GunAbilityConfig.ENABLE_AMMO_BURST.get()) {
            clearRuntimeState(player, false);
            postFailEvent(player, AmmoBurstFailReason.DISABLED, 0.0F, AmmoBurstStats.disabled());
            return ActivationResult.DISABLED;
        }

        AmmoBurstStats stats = getStats(player);
        CompoundTag data = getAbilityData(player, stats.maxEnergy);
        clearDisabledRedirectPending(data);
        AmmoBurstStateData state = getStateData(data, stats.maxEnergy);
        float currentEnergy = state.energy();

        if (isToggleStopState(state.runtimeState())) {
            long currentTick = player.level().getGameTime();
            AboutToEndTransition transition = handleAboutToEnd(
                    player,
                    state,
                    stats,
                    currentTick,
                    AmmoBurstFinalReason.MANUAL
            );
            AmmoBurstStateData updated = transition.updatedState();
            storeStateData(data, updated, stats.maxEnergy);
            postPendingEndEvent(player, updated.energy(), stats, transition.endEventDispatch());
            return mapStopStateToResult(updated.runtimeState());
        }

        boolean hasUnlockBonus = hasUnlockBonus(player);
        boolean hasSupportedGun = GunCompatHelper.isSupportedGun(player.getMainHandItem());
        boolean ignoreUnlockRequirement = false;
        boolean ignoreSupportedGunRequirement = false;

        AmmoBurstTryStartEvent tryStartEvent = new AmmoBurstTryStartEvent(
                player,
                currentEnergy,
                stats.maxEnergy,
                stats.regenPerSecond,
                stats.drainPerSecond,
                stats.activationCost
        );
        if (MinecraftForge.EVENT_BUS.post(tryStartEvent)) {
            postFailEvent(player, AmmoBurstFailReason.CANCELED, currentEnergy, stats);
            return ActivationResult.BLOCKED_BY_SCRIPT;
        }

        stats = sanitizeStats(
                tryStartEvent.getMaxEnergy(),
                tryStartEvent.getRegenPerSecond(),
                tryStartEvent.getDrainPerSecond(),
                tryStartEvent.getActivationCost()
        );
        currentEnergy = clampEnergy(tryStartEvent.getCurrentEnergy(), stats.maxEnergy());
        ignoreUnlockRequirement = tryStartEvent.isIgnoreUnlockRequirement();
        ignoreSupportedGunRequirement = tryStartEvent.isIgnoreSupportedGunRequirement();

        ActivationResult preconditionResult = evaluateActivationPreconditions(
                false,
                GunAbilityConfig.REQUIRE_UNLOCK_BONUS.get(),
                hasUnlockBonus,
                ignoreUnlockRequirement,
                GunAbilityConfig.REQUIRE_SUPPORTED_GUN.get(),
                hasSupportedGun,
                ignoreSupportedGunRequirement,
                currentEnergy,
                stats.activationCost
        );
        if (preconditionResult != null) {
            AmmoBurstFailReason failReason = mapActivationFailure(preconditionResult);
            if (failReason != null) {
                postFailEvent(player, failReason, currentEnergy, stats);
            }
            return preconditionResult;
        }

        float remainingEnergy = clampEnergy(currentEnergy - stats.activationCost, stats.maxEnergy);
        AmmoBurstStateData activatedState = AmmoBurstStateData.active(remainingEnergy);
        storeStateData(data, activatedState, stats.maxEnergy);
        postStartEvent(player, activatedState.energy(), stats);
        return ActivationResult.ACTIVATED;
    }

    private static void tick(ServerPlayer player) {
        if (!GunAbilityConfig.ENABLE_AMMO_BURST.get()) {
            clearRuntimeState(player, true);
            return;
        }

        AmmoBurstStats stats = getStats(player);
        CompoundTag data = getAbilityData(player, stats.maxEnergy);
        clearDisabledRedirectPending(data);
        AmmoBurstStateData state = getStateData(data, stats.maxEnergy);
        AmmoBurstStateData updated = state;
        EndEventDispatch endEventDispatch = null;
        long currentTick = player.level().getGameTime();

        switch (state.runtimeState()) {
            case ACTIVE -> {
                if (GunAbilityConfig.REQUIRE_UNLOCK_BONUS.get() && !hasUnlockBonus(player)) {
                    AboutToEndTransition transition = handleAboutToEnd(
                            player,
                            state,
                            stats,
                            currentTick,
                            AmmoBurstFinalReason.UNLOCK_LOST
                    );
                    updated = transition.updatedState();
                    endEventDispatch = transition.endEventDispatch();
                } else {
                    float nextEnergy = advanceEnergy(
                            state.energy(),
                            stats.maxEnergy,
                            stats.regenPerSecond / 20.0F,
                            stats.drainPerSecond / 20.0F,
                            true
                    );
                    if (nextEnergy <= ENERGY_EPSILON) {
                        AboutToEndTransition transition = handleAboutToEnd(
                                player,
                                AmmoBurstStateData.active(0.0F),
                                stats,
                                currentTick,
                                AmmoBurstFinalReason.ENERGY_DEPLETED
                        );
                        updated = transition.updatedState();
                        endEventDispatch = transition.endEventDispatch();
                    } else {
                        updated = AmmoBurstStateData.active(nextEnergy);
                    }
                }
            }
            case OFF -> {
                float nextEnergy = advanceEnergy(
                        state.energy(),
                        stats.maxEnergy,
                        stats.regenPerSecond / 20.0F,
                        stats.drainPerSecond / 20.0F,
                        false
                );
                updated = AmmoBurstStateData.off(nextEnergy);
            }
            case ZERO_SUSTAIN -> {
                if (GunAbilityConfig.REQUIRE_UNLOCK_BONUS.get() && !hasUnlockBonus(player)) {
                    AboutToEndTransition transition = handleAboutToEnd(
                            player,
                            state,
                            stats,
                            currentTick,
                            AmmoBurstFinalReason.UNLOCK_LOST
                    );
                    updated = transition.updatedState();
                    endEventDispatch = transition.endEventDispatch();
                } else if (currentTick >= state.nextTriggerTick()) {
                    AmmoBurstSustainEvent sustainEvent = new AmmoBurstSustainEvent(
                            player,
                            state.sourceReason(),
                            stats.activationCost,
                            stats.drainPerSecond,
                            stats.drainPerSecond / 20.0F,
                            state.runIndex(),
                            state.sustainAgeTicks()
                    );
                    MinecraftForge.EVENT_BUS.post(sustainEvent);

                    updated = applySustainDecision(
                            state,
                            sustainEvent.getDecision(),
                            sustainEvent.getRefundEnergy(),
                            currentTick
                    );
                    if (updated.runtimeState() == AmmoBurstRuntimeState.OFF) {
                        endEventDispatch = new EndEventDispatch(
                                AmmoBurstFinalReason.SUSTAIN_TERMINATED,
                                resolveEndEventSourceReason(state, AmmoBurstFinalReason.SUSTAIN_TERMINATED),
                                didPassThroughZeroSustain(state)
                        );
                    }
                }
            }
        }

        boolean changed = !updated.equals(state);
        storeStateData(data, updated, stats.maxEnergy);
        postPendingEndEvent(player, updated.energy(), stats, endEventDispatch);

        if (changed || player.tickCount % 20 == 0) {
            syncState(player);
        }
    }

    static AmmoBurstStateData applyAboutToEndDecision(
            AmmoBurstStateData state,
            AmmoBurstAboutToEndDecision decision,
            float refundEnergy,
            int sustainStartDelayTicks,
            int sustainIntervalTicks,
            long currentTick,
            AmmoBurstFinalReason sourceReason
    ) {
        AmmoBurstFinalReason zeroSustainSourceReason = state.runtimeState() == AmmoBurstRuntimeState.ZERO_SUSTAIN
                ? resolveEndEventSourceReason(state, sourceReason)
                : sourceReason;
        return switch (decision) {
            case END_NOW -> AmmoBurstStateData.off(Math.max(0.0F, state.energy()));
            case REFUND_AND_CONTINUE -> AmmoBurstStateData.active(Math.max(0.0F, refundEnergy));
            case ENTER_ZERO_SUSTAIN -> new AmmoBurstStateData(
                    AmmoBurstRuntimeState.ZERO_SUSTAIN,
                    0.0F,
                    zeroSustainSourceReason,
                    Math.max(0, sustainStartDelayTicks),
                    Math.max(1, sustainIntervalTicks),
                    currentTick + Math.max(0, sustainStartDelayTicks),
                    0,
                    Math.max(0, sustainStartDelayTicks)
            );
        };
    }

    static boolean isToggleStopState(AmmoBurstRuntimeState runtimeState) {
        return runtimeState == AmmoBurstRuntimeState.ACTIVE || runtimeState == AmmoBurstRuntimeState.ZERO_SUSTAIN;
    }

    static boolean isStateActiveForReporting(AmmoBurstRuntimeState runtimeState) {
        return runtimeState == AmmoBurstRuntimeState.ACTIVE || runtimeState == AmmoBurstRuntimeState.ZERO_SUSTAIN;
    }

    static AmmoBurstFinalReason resolveEndEventSourceReason(AmmoBurstStateData state, AmmoBurstFinalReason finalReason) {
        if (state.runtimeState() == AmmoBurstRuntimeState.ZERO_SUSTAIN) {
            return state.sourceReason() != null ? state.sourceReason() : finalReason;
        }
        return finalReason;
    }

    static boolean didPassThroughZeroSustain(AmmoBurstStateData state) {
        return state.runtimeState() == AmmoBurstRuntimeState.ZERO_SUSTAIN;
    }

    static ActivationResult mapStopStateToResult(AmmoBurstRuntimeState runtimeState) {
        return runtimeState == AmmoBurstRuntimeState.OFF
                ? ActivationResult.DEACTIVATED
                : ActivationResult.STOP_REDIRECTED;
    }

    static AmmoBurstStateData applySustainDecision(
            AmmoBurstStateData state,
            AmmoBurstSustainDecision decision,
            float refundEnergy,
            long currentTick
    ) {
        return switch (decision) {
            case CONTINUE -> new AmmoBurstStateData(
                    AmmoBurstRuntimeState.ZERO_SUSTAIN,
                    0.0F,
                    state.sourceReason(),
                    state.sustainStartDelayTicks(),
                    state.sustainIntervalTicks(),
                    currentTick + state.sustainIntervalTicks(),
                    state.runIndex() + 1,
                    state.sustainAgeTicks() + state.sustainIntervalTicks()
            );
            case TERMINATE -> AmmoBurstStateData.off(0.0F);
            case EXIT_WITH_REFUND -> AmmoBurstStateData.active(Math.max(0.0F, refundEnergy));
        };
    }

    public static boolean hasUnlockBonus(Player player) {
        return getAttributeValue(player, PassiveIntegrationAttributes.AMMO_BURST_UNLOCKED, 0.0F) > ENERGY_EPSILON;
    }

    public static float getCurrentEnergy(Player player) {
        if (!GunAbilityConfig.ENABLE_AMMO_BURST.get()) {
            return 0.0F;
        }
        AmmoBurstStats stats = getStats(player);
        CompoundTag data = getAbilityData(player, stats.maxEnergy);
        AmmoBurstStateData state = getStateData(data, stats.maxEnergy);
        storeStateData(data, state, stats.maxEnergy);
        return state.energy();
    }

    public static float getMaxEnergy(Player player) {
        return getStats(player).maxEnergy;
    }

    public static float getEnergyRegenPerSecond(Player player) {
        return getStats(player).regenPerSecond;
    }

    public static float getEnergyDrainPerSecond(Player player) {
        return getStats(player).drainPerSecond;
    }

    public static float getActivationCost(Player player) {
        return getStats(player).activationCost;
    }

    static ActivationResult evaluateActivationPreconditions(
            boolean alreadyActive,
            boolean requireUnlockBonus,
            boolean hasUnlockBonus,
            boolean ignoreUnlockRequirement,
            boolean requireSupportedGun,
            boolean hasSupportedGun,
            boolean ignoreSupportedGunRequirement,
            float currentEnergy,
            float activationCost
    ) {
        if (alreadyActive) {
            return ActivationResult.ALREADY_ACTIVE;
        }
        if (currentEnergy < activationCost) {
            return ActivationResult.MISSING_ENERGY;
        }
        if (requireUnlockBonus && !ignoreUnlockRequirement && !hasUnlockBonus) {
            return ActivationResult.MISSING_UNLOCK;
        }
        if (requireSupportedGun && !ignoreSupportedGunRequirement && !hasSupportedGun) {
            return ActivationResult.MISSING_GUN;
        }
        return null;
    }

    static float advanceEnergy(
            float currentEnergy,
            float maxEnergy,
            float regenPerStep,
            float drainPerStep,
            boolean active
    ) {
        float clampedMaxEnergy = Math.max(0.0F, maxEnergy);
        if (active) {
            return Mth.clamp(currentEnergy - Math.max(0.0F, drainPerStep), 0.0F, clampedMaxEnergy);
        }
        return Mth.clamp(currentEnergy + Math.max(0.0F, regenPerStep), 0.0F, clampedMaxEnergy);
    }

    public static int getRemainingCooldownTicks(Player player) {
        return estimateRechargeTicks(getCurrentEnergy(player), getActivationCost(player), getEnergyRegenPerSecond(player));
    }

    public static int getRemainingActiveTicks(Player player) {
        if (!isAmmoBurstActive(player)) {
            return 0;
        }
        return estimateActiveTicks(getCurrentEnergy(player), getEnergyDrainPerSecond(player));
    }

    public static int estimateActiveTicks(float energy, float drainPerSecond) {
        if (energy <= ENERGY_EPSILON || drainPerSecond <= ENERGY_EPSILON) {
            return 0;
        }
        return Math.max(1, Mth.ceil((energy / drainPerSecond) * 20.0F));
    }

    public static int estimateRechargeTicks(float currentEnergy, float targetEnergy, float regenPerSecond) {
        if (currentEnergy >= targetEnergy - ENERGY_EPSILON) {
            return 0;
        }
        if (regenPerSecond <= ENERGY_EPSILON) {
            return Integer.MAX_VALUE;
        }
        return Math.max(1, Mth.ceil(((targetEnergy - currentEnergy) / regenPerSecond) * 20.0F));
    }

    public static void syncState(ServerPlayer player) {
        if (!GunAbilityConfig.ENABLE_AMMO_BURST.get()) {
            PassiveIntegrationNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncAmmoBurstStateMessage(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, false, false, false));
            return;
        }

        AmmoBurstStats stats = getStats(player);
        CompoundTag data = getAbilityData(player, stats.maxEnergy);
        AmmoBurstStateData state = getStateData(data, stats.maxEnergy);
        storeStateData(data, state, stats.maxEnergy);
        PassiveIntegrationNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncAmmoBurstStateMessage(
                        state.energy(),
                        stats.maxEnergy,
                        stats.regenPerSecond,
                        stats.drainPerSecond,
                        stats.activationCost,
                        isStateActiveForReporting(state.runtimeState()),
                        hasUnlockBonus(player),
                        GunCompatHelper.isSupportedGun(player.getMainHandItem())
                ));
    }

    private static void clearRuntimeState(ServerPlayer player, boolean sendSync) {
        CompoundTag data = getExistingAbilityData(player);
        if (data != null) {
            AmmoBurstStateData state = getStateData(data, Math.max(0.0F, data.getFloat(ENERGY_TAG)));
            if (state.runtimeState() != AmmoBurstRuntimeState.OFF) {
                if (isDisabledRedirectPending(data)) {
                    if (sendSync) {
                        syncState(player);
                    }
                    return;
                }

                AboutToEndTransition transition = handleAboutToEnd(
                        player,
                        state,
                        AmmoBurstStats.disabled(),
                        player.level().getGameTime(),
                        AmmoBurstFinalReason.DISABLED
                );
                AmmoBurstStateData updated = transition.updatedState();
                if (shouldRetainStateAfterDisabledRedirect(updated.runtimeState())) {
                    // Disabled teardown has no live stat envelope; preserve redirected state until the feature returns.
                    storeStateData(data, updated, Float.MAX_VALUE);
                    markDisabledRedirectPending(data);
                } else {
                    player.getPersistentData().remove(ROOT_TAG);
                }
                postPendingEndEvent(player, updated.energy(), AmmoBurstStats.disabled(), transition.endEventDispatch());
                if (sendSync) {
                    syncState(player);
                }
                return;
            }
        }
        player.getPersistentData().remove(ROOT_TAG);
        if (sendSync) {
            syncState(player);
        }
    }

    private static AmmoBurstStats getStats(Player player) {
        float maxEnergyMultiplier = getAttributeValue(
                player,
                PassiveIntegrationAttributes.AMMO_BURST_MAX_ENERGY_MULTIPLIER,
                1.0F
        );
        float regenMultiplier = getAttributeValue(
                player,
                PassiveIntegrationAttributes.AMMO_BURST_ENERGY_REGEN_MULTIPLIER,
                1.0F
        );
        float drainMultiplier = getAttributeValue(
                player,
                PassiveIntegrationAttributes.AMMO_BURST_DRAIN_MULTIPLIER,
                1.0F
        );
        float activationCostMultiplier = getAttributeValue(
                player,
                PassiveIntegrationAttributes.AMMO_BURST_ACTIVATION_COST_MULTIPLIER,
                1.0F
        );

        return sanitizeStats(
                applyMultiplier(GunAbilityConfig.BASE_MAX_ENERGY.get().floatValue(), maxEnergyMultiplier),
                applyMultiplier(
                        GunAbilityConfig.BASE_ENERGY_REGEN_PER_SECOND.get().floatValue(),
                        regenMultiplier
                ),
                applyMultiplier(
                        GunAbilityConfig.BASE_ENERGY_DRAIN_PER_SECOND.get().floatValue(),
                        drainMultiplier
                ),
                applyMultiplier(GunAbilityConfig.ACTIVATION_COST.get().floatValue(), activationCostMultiplier)
        );
    }

    static AmmoBurstStats sanitizeStats(
            float maxEnergy,
            float regenPerSecond,
            float drainPerSecond,
            float activationCost
    ) {
        float sanitizedMaxEnergy = Math.max(1.0F, maxEnergy);
        return new AmmoBurstStats(
                sanitizedMaxEnergy,
                Math.max(0.0F, regenPerSecond),
                Math.max(0.0F, drainPerSecond),
                clampActivationCost(activationCost, sanitizedMaxEnergy)
        );
    }

    static float applyMultiplier(float baseValue, float multiplier) {
        return Math.max(0.0F, baseValue * Math.max(0.0F, multiplier));
    }

    static float clampActivationCost(float activationCost, float maxEnergy) {
        return Mth.clamp(Math.max(0.0F, activationCost), 0.0F, Math.max(0.0F, maxEnergy));
    }

    private static CompoundTag getAbilityData(Player player, float initialEnergy) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            persistentData.put(ROOT_TAG, new CompoundTag());
        }
        CompoundTag data = persistentData.getCompound(ROOT_TAG);
        normalizeLegacyData(data, initialEnergy);
        return data;
    }

    private static CompoundTag getExistingAbilityData(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }
        return data.getCompound(ROOT_TAG);
    }

    private static void normalizeLegacyData(CompoundTag data, float initialEnergy) {
        if (data.contains(LEGACY_ACTIVE_UNTIL_TAG)) {
            data.remove(LEGACY_ACTIVE_UNTIL_TAG);
        }
        if (data.contains(LEGACY_COOLDOWN_UNTIL_TAG)) {
            data.remove(LEGACY_COOLDOWN_UNTIL_TAG);
        }
        if (!data.contains(ENERGY_TAG, Tag.TAG_FLOAT)) {
            data.putFloat(ENERGY_TAG, Math.max(0.0F, initialEnergy));
        }
        if (!data.contains(ACTIVE_TAG, Tag.TAG_BYTE)) {
            data.putBoolean(ACTIVE_TAG, false);
        }
        if (!data.contains(RUNTIME_STATE_TAG, Tag.TAG_STRING)) {
            data.putString(
                    RUNTIME_STATE_TAG,
                    data.getBoolean(ACTIVE_TAG) ? AmmoBurstRuntimeState.ACTIVE.name() : AmmoBurstRuntimeState.OFF.name()
            );
        }
        if (data.contains(DISABLED_REDIRECT_PENDING_TAG, Tag.TAG_BYTE) && !data.getBoolean(DISABLED_REDIRECT_PENDING_TAG)) {
            data.remove(DISABLED_REDIRECT_PENDING_TAG);
        }
        if (!data.contains(SUSTAIN_START_DELAY_TAG, Tag.TAG_INT)) {
            data.putInt(SUSTAIN_START_DELAY_TAG, 0);
        }
        if (!data.contains(SUSTAIN_INTERVAL_TAG, Tag.TAG_INT)) {
            data.putInt(SUSTAIN_INTERVAL_TAG, 1);
        }
        if (!data.contains(NEXT_TRIGGER_TICK_TAG, Tag.TAG_LONG)) {
            data.putLong(NEXT_TRIGGER_TICK_TAG, 0L);
        }
        if (!data.contains(RUN_INDEX_TAG, Tag.TAG_INT)) {
            data.putInt(RUN_INDEX_TAG, 0);
        }
        if (!data.contains(SUSTAIN_AGE_TICKS_TAG, Tag.TAG_INT)) {
            data.putInt(SUSTAIN_AGE_TICKS_TAG, 0);
        }
    }

    private static AmmoBurstStateData getStateData(CompoundTag data, float maxEnergy) {
        AmmoBurstRuntimeState runtimeState = parseRuntimeState(
                data.getString(RUNTIME_STATE_TAG),
                data.getBoolean(ACTIVE_TAG)
        );
        float energy = clampEnergy(data.getFloat(ENERGY_TAG), maxEnergy);
        AmmoBurstFinalReason sourceReason = parseFinalReason(data.getString(SOURCE_REASON_TAG));
        int sustainStartDelayTicks = Math.max(0, data.getInt(SUSTAIN_START_DELAY_TAG));
        int sustainIntervalTicks = Math.max(1, data.getInt(SUSTAIN_INTERVAL_TAG));
        long nextTriggerTick = data.getLong(NEXT_TRIGGER_TICK_TAG);
        int runIndex = Math.max(0, data.getInt(RUN_INDEX_TAG));
        int sustainAgeTicks = Math.max(0, data.getInt(SUSTAIN_AGE_TICKS_TAG));

        return switch (runtimeState) {
            case OFF -> AmmoBurstStateData.off(energy);
            case ACTIVE -> AmmoBurstStateData.active(energy);
            case ZERO_SUSTAIN -> new AmmoBurstStateData(
                    AmmoBurstRuntimeState.ZERO_SUSTAIN,
                    0.0F,
                    sourceReason != null ? sourceReason : AmmoBurstFinalReason.ENERGY_DEPLETED,
                    sustainStartDelayTicks,
                    sustainIntervalTicks,
                    nextTriggerTick,
                    runIndex,
                    sustainAgeTicks
            );
        };
    }

    private static void storeStateData(CompoundTag data, AmmoBurstStateData rawState, float maxEnergy) {
        AmmoBurstStateData state = sanitizeState(rawState, maxEnergy);
        data.putString(RUNTIME_STATE_TAG, state.runtimeState().name());
        data.putFloat(ENERGY_TAG, state.energy());
        data.putBoolean(ACTIVE_TAG, isStateActiveForReporting(state.runtimeState()));
        if (state.sourceReason() != null) {
            data.putString(SOURCE_REASON_TAG, state.sourceReason().name());
        } else {
            data.remove(SOURCE_REASON_TAG);
        }
        data.putInt(SUSTAIN_START_DELAY_TAG, state.sustainStartDelayTicks());
        data.putInt(SUSTAIN_INTERVAL_TAG, state.sustainIntervalTicks());
        data.putLong(NEXT_TRIGGER_TICK_TAG, state.nextTriggerTick());
        data.putInt(RUN_INDEX_TAG, state.runIndex());
        data.putInt(SUSTAIN_AGE_TICKS_TAG, state.sustainAgeTicks());
    }

    private static AmmoBurstStateData sanitizeState(AmmoBurstStateData state, float maxEnergy) {
        AmmoBurstRuntimeState runtimeState = state.runtimeState() == null ? AmmoBurstRuntimeState.OFF : state.runtimeState();
        return switch (runtimeState) {
            case OFF -> AmmoBurstStateData.off(clampEnergy(state.energy(), maxEnergy));
            case ACTIVE -> AmmoBurstStateData.active(clampEnergy(state.energy(), maxEnergy));
            case ZERO_SUSTAIN -> new AmmoBurstStateData(
                    AmmoBurstRuntimeState.ZERO_SUSTAIN,
                    0.0F,
                    state.sourceReason() != null ? state.sourceReason() : AmmoBurstFinalReason.ENERGY_DEPLETED,
                    Math.max(0, state.sustainStartDelayTicks()),
                    Math.max(1, state.sustainIntervalTicks()),
                    state.nextTriggerTick(),
                    Math.max(0, state.runIndex()),
                    Math.max(0, state.sustainAgeTicks())
            );
        };
    }

    private static AboutToEndTransition handleAboutToEnd(
            ServerPlayer player,
            AmmoBurstStateData state,
            AmmoBurstStats stats,
            long currentTick,
            AmmoBurstFinalReason sourceReason
    ) {
        AmmoBurstAboutToEndEvent aboutToEndEvent = new AmmoBurstAboutToEndEvent(
                player,
                sourceReason,
                state.energy(),
                stats.maxEnergy,
                stats.regenPerSecond,
                stats.drainPerSecond,
                stats.activationCost,
                stats.drainPerSecond / 20.0F
        );
        MinecraftForge.EVENT_BUS.post(aboutToEndEvent);

        AmmoBurstStateData updated = applyAboutToEndDecision(
                state,
                aboutToEndEvent.getDecision(),
                aboutToEndEvent.getRefundEnergy(),
                aboutToEndEvent.getSustainStartDelayTicks(),
                aboutToEndEvent.getSustainIntervalTicks(),
                currentTick,
                sourceReason
        );
        EndEventDispatch endEventDispatch = updated.runtimeState() == AmmoBurstRuntimeState.OFF
                ? new EndEventDispatch(
                    sourceReason,
                    resolveEndEventSourceReason(state, sourceReason),
                    didPassThroughZeroSustain(state)
                )
                : null;
        return new AboutToEndTransition(updated, endEventDispatch);
    }

    static boolean shouldRetainStateAfterDisabledRedirect(AmmoBurstRuntimeState runtimeState) {
        return runtimeState != AmmoBurstRuntimeState.OFF;
    }

    static boolean isDisabledRedirectPending(CompoundTag data) {
        return data.getBoolean(DISABLED_REDIRECT_PENDING_TAG);
    }

    static void markDisabledRedirectPending(CompoundTag data) {
        data.putBoolean(DISABLED_REDIRECT_PENDING_TAG, true);
    }

    static void clearDisabledRedirectPending(CompoundTag data) {
        data.remove(DISABLED_REDIRECT_PENDING_TAG);
    }

    private static AmmoBurstRuntimeState parseRuntimeState(String value, boolean activeFlag) {
        if (value == null || value.isBlank()) {
            return activeFlag ? AmmoBurstRuntimeState.ACTIVE : AmmoBurstRuntimeState.OFF;
        }
        try {
            return AmmoBurstRuntimeState.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return activeFlag ? AmmoBurstRuntimeState.ACTIVE : AmmoBurstRuntimeState.OFF;
        }
    }

    private static AmmoBurstFinalReason parseFinalReason(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return AmmoBurstFinalReason.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static AmmoBurstFailReason mapActivationFailure(ActivationResult result) {
        return switch (result) {
            case MISSING_ENERGY -> AmmoBurstFailReason.MISSING_ENERGY;
            case MISSING_UNLOCK -> AmmoBurstFailReason.MISSING_UNLOCK;
            case MISSING_GUN -> AmmoBurstFailReason.MISSING_GUN;
            default -> null;
        };
    }

    private static void postFailEvent(
            ServerPlayer player,
            AmmoBurstFailReason reason,
            float currentEnergy,
            AmmoBurstStats stats
    ) {
        MinecraftForge.EVENT_BUS.post(new AmmoBurstFailEvent(
                player,
                reason,
                currentEnergy,
                stats.maxEnergy,
                stats.regenPerSecond,
                stats.drainPerSecond,
                stats.activationCost
        ));
    }

    private static void postStartEvent(ServerPlayer player, float currentEnergy, AmmoBurstStats stats) {
        MinecraftForge.EVENT_BUS.post(new AmmoBurstStartEvent(
                player,
                currentEnergy,
                stats.maxEnergy,
                stats.regenPerSecond,
                stats.drainPerSecond,
                stats.activationCost
        ));
    }

    private static void postEndEvent(
            ServerPlayer player,
            float currentEnergy,
            AmmoBurstStats stats,
            AmmoBurstFinalReason finalReason,
            AmmoBurstFinalReason sourceReason,
            boolean passedThroughZeroSustain
    ) {
        MinecraftForge.EVENT_BUS.post(new AmmoBurstEndEvent(
                player,
                finalReason,
                sourceReason,
                currentEnergy,
                stats.maxEnergy,
                stats.regenPerSecond,
                stats.drainPerSecond,
                stats.activationCost,
                passedThroughZeroSustain
        ));
    }

    private static void postPendingEndEvent(
            ServerPlayer player,
            float currentEnergy,
            AmmoBurstStats stats,
            EndEventDispatch dispatch
    ) {
        if (dispatch == null) {
            return;
        }
        postEndEvent(
                player,
                currentEnergy,
                stats,
                dispatch.finalReason(),
                dispatch.sourceReason(),
                dispatch.passedThroughZeroSustain()
        );
    }

    static float clampEnergy(float energy, float maxEnergy) {
        return Mth.clamp(energy, 0.0F, Math.max(0.0F, maxEnergy));
    }

    private static float getAttributeValue(
            Player player,
            RegistryObject<Attribute> attribute,
            float fallback
    ) {
        var instance = player.getAttribute(attribute.get());
        return instance != null ? (float) instance.getValue() : fallback;
    }

    private static void sendStatusMessage(ServerPlayer player, ActivationResult result) {
        switch (result) {
            case ACTIVATED ->
                    player.displayClientMessage(
                            Component.translatable(
                                    "message.passiveintegration.ammo_burst_activated",
                                    formatTicks(getRemainingActiveTicks(player))),
                            true);
            case DEACTIVATED -> player.displayClientMessage(
                    Component.translatable("message.passiveintegration.ammo_burst_deactivated"),
                    true);
            case STOP_REDIRECTED -> {
            }
            case MISSING_ENERGY -> player.displayClientMessage(
                    Component.translatable("message.passiveintegration.ammo_burst_missing_energy"),
                    true);
            case MISSING_UNLOCK -> player.displayClientMessage(
                    Component.translatable("message.passiveintegration.ammo_burst_missing_unlock"),
                    true);
            case MISSING_GUN -> player.displayClientMessage(
                    Component.translatable("message.passiveintegration.ammo_burst_requires_gun"),
                    true);
            case ALREADY_ACTIVE -> player.displayClientMessage(
                    Component.translatable("message.passiveintegration.ammo_burst_already_active"),
                    true);
            case DISABLED -> player.displayClientMessage(
                    Component.translatable("message.passiveintegration.ammo_burst_disabled"),
                    true);
            case BLOCKED_BY_SCRIPT -> player.displayClientMessage(
                    Component.translatable("message.passiveintegration.ammo_burst_blocked"),
                    true);
        }
    }

    private static String formatTicks(int ticks) {
        return String.format(Locale.ROOT, "%.1f", ticks / 20.0F);
    }

    public enum ActivationResult {
        ACTIVATED,
        DEACTIVATED,
        STOP_REDIRECTED,
        DISABLED,
        MISSING_ENERGY,
        MISSING_UNLOCK,
        MISSING_GUN,
        ALREADY_ACTIVE,
        BLOCKED_BY_SCRIPT
    }

    static record AmmoBurstStats(
            float maxEnergy,
            float regenPerSecond,
            float drainPerSecond,
            float activationCost
    ) {
        private static AmmoBurstStats disabled() {
            return new AmmoBurstStats(0.0F, 0.0F, 0.0F, 0.0F);
        }
    }

    private record EndEventDispatch(
            AmmoBurstFinalReason finalReason,
            AmmoBurstFinalReason sourceReason,
            boolean passedThroughZeroSustain
    ) {
    }

    private record AboutToEndTransition(
            AmmoBurstStateData updatedState,
            EndEventDispatch endEventDispatch
    ) {
    }
}
