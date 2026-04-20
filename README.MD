# PassiveIntegration

`PassiveIntegration` is the **SkillTree-facing integration layer only** for features that should not live inside
`PassiveSkillTree`, `PassiveBurst`, or `GunCore`.

## Responsibility Split

- `GunCore`
  - owns gun-domain abstractions, bridge resolution, snapshots, and deep per-platform gun support
- `PassiveBurst`
  - owns the shared burst framework, runtime state, and control surface
- `PassiveIntegration`
  - owns SkillTree-facing carriers, bonuses, optional activation gates, mixin-backed gameplay hooks, and integration-specific config

## Current Code Layout

- `src/main/java/com/pickaid/passiveintegration/PassiveIntegration.java`
  - minimal mod entrypoint that delegates startup to `PassiveIntegrationBootstrap`
- `src/main/java/com/pickaid/passiveintegration/bootstrap/PassiveIntegrationBootstrap.java`
  - registers common config
  - creates the `CarrierBinders` + `CarrierRegistry`
  - binds active carrier catalogs (currently `compat/cgm/CgmCarrierCatalog`)
  - exposes the carrier debug bridge
- `src/main/java/com/pickaid/passiveintegration/compat/carrier/**`
  - integration-local carrier model, activation records, registry, and binder layer for SkillTree serializers/listeners/requirements/item bonuses/skill bonuses
  - binder implementations live under `src/main/java/com/pickaid/passiveintegration/compat/carrier/binding/**`
- `src/main/java/com/pickaid/passiveintegration/optional/CarrierGates.java`
  - optional dependency gates based on loaded mods and class presence
- `src/main/java/com/pickaid/passiveintegration/compat/cgm/**`
  - current concrete integration surface for CGM
  - carrier catalog entries for grenade-related bonuses
  - runtime grenade duration hooks and curve math
  - config-backed duration curves in `compat/cgm/CgmGrenadeDurationCurves` (values sourced from `config/PassiveIntegrationCommonConfig`)
  - bonus serializers for datapack-loaded SkillTree content
- `src/main/java/com/pickaid/passiveintegration/mixins/compat/cgm/ThrowableStunGrenadeEntityMixin.java`
  - redirects CGM stun grenade effect application into `CgmGrenadeEffectHooks`
- `src/test/resources/data/passiveintegration_test/**`
  - datapack fixtures used to test SkillTree-facing content shape
  - not a local SkillTreeJS ownership layer (SkillTreeJS is not owned in this repository)

## Current Gameplay Features

- `passiveintegration:cgm_stun_grenade_duration_taken_reduction`
  - reduces CGM blinded/deafened duration taken by the affected player
  - uses an exponential per-level reduction curve from `PassiveIntegrationCommonConfig.cgmStunReductionPerLevel()`
- `passiveintegration:cgm_flash_grenade_duration_applied_bonus`
  - increases duration applied by self-thrown CGM flash or stun grenades
  - uses an asymptotic growth curve with configurable cap and decay from `PassiveIntegrationCommonConfig`
- `CgmGrenadeEffectHooks`
  - only rewrites CGM blinded/deafened durations
  - preserves the original amplifier, ambient flag, and visibility flag

## Deliberate Non-Ownership

- no gun-domain bridge/helper/snapshot layer in this repository
- no client runtime, network sync layer, or generated-resource sanitizing layer
- no SkillTree editor widget ownership; integration bonuses throw if editor widgets are requested here
- no local SkillTreeJS support surface; datapack fixtures exist only for tests and manual integration validation

## Development Notes

- Build `../GunCore` first, or pass `-PgunCoreJar=/abs/path/to/guncore-forge-1.20.1-<version>.jar` (or set `GUN_CORE_JAR`).
- Local development can resolve `PassiveSkillTree-1.20.1-BETA-0.7.4-all.jar`; otherwise the build falls back to Curse Maven.
- Do not reintroduce gun-domain ownership here. `compat.gun`, `bridge.gun`, `helper.gun`, and gun-specific abstraction layers belong in `GunCore`, not `PassiveIntegration`.
