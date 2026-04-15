# PassiveIntegration Gun Helper Bridge Design

**Date:** 2026-04-15

**Status:** Approved in design review on April 15, 2026.

**Goal:** Keep gun compatibility in `PassiveIntegration`, expose a stable Java gun API through a unified bridge plus per-platform helpers, and let `PassiveBurst` consume those classes from KJS through dynamic imports without moving gun logic into `PassiveSTJS`.

## Scope

This design defines the public gun-facing Java API for the split architecture that now uses:

- `PassiveIntegration` for bottom-layer gun compatibility and gun data access
- `PassiveBurst` for Burst runtime and KJS bindings
- no gun feature ownership in `PassiveSTJS`

This design applies to the current supported gun platforms:

- TacZ
- Vanilla Animated Guns
- Scorched Guns 2
- Just Enough Guns

The same structure must remain usable for CGM and PointBlank if they are restored into the new tracked layout.

## Goals

- Keep all gun compatibility ownership inside `PassiveIntegration`.
- Provide one stable Java bridge for generic consumers.
- Provide one stable Java helper per gun platform for platform-specific consumers.
- Keep `GunWeaponSnapshot` as the normalized cross-platform contract.
- Make platform-specific data available without exposing unstable third-party mod classes.
- Let `PassiveBurst` dynamically import the bridge and individual helpers from KJS if those classes exist.
- Avoid a giant bridge class that absorbs every platform-specific method.

## Non-Goals

- Do not move gun logic into `PassiveSTJS`.
- Do not add a new standalone `GunJS` module.
- Do not expose raw third-party gun classes such as TacZ `GunData`, `IGun`, or upstream `Gun` objects as public API.
- Do not add new gun-specific KJS events.
- Do not merge Burst behavior into the gun bridge layer.

## Module Boundaries

### PassiveIntegration

Owns:

- gun platform detection
- gun adapter logic
- gun data extraction
- gun hit context
- the normalized snapshot contract
- platform-specific helper classes

Must expose:

- a unified Java bridge
- one Java helper per platform
- stable read-only view objects for platform-specific data

Must not own:

- Burst runtime
- Burst HUD
- Burst network
- gun-specific KJS binding code

### PassiveBurst

Owns:

- Burst runtime
- Burst KJS bindings
- dynamic import of gun bridge and gun helpers

May consume:

- `GunDataBridge`
- per-platform helpers
- `GunWeaponSnapshot`

Must not own:

- gun compatibility logic
- platform-specific extraction rules
- direct copies of gun helper logic

### PassiveSTJS

Owns:

- its own Passive Skill Tree scripting surface only

Must not own:

- gun compatibility logic
- gun helper bindings
- Burst-to-gun compatibility glue

## Public Java API

### Unified Bridge

`com.pickaid.passiveintegration.service.gun.GunDataBridge`

This is the generic entrypoint for code that only needs to know whether a stack is a supported gun and, if so, what the normalized data looks like.

Required methods:

- `loadedPlatforms()`
- `supports(ItemStack, @Nullable LivingEntity)`
- `platformId(ItemStack, @Nullable LivingEntity)`
- `snapshot(ItemStack, @Nullable LivingEntity)`
- `hitContext(@Nullable LivingEntity, @Nullable LivingEntity, DamageSource, @Nullable Entity, float)`

Rules:

- The bridge may route to platform helpers internally.
- The bridge must not expose platform-specific methods directly.
- The bridge must stay small enough to remain a stable generic API.

### Per-Platform Helpers

Required helper classes:

- `com.pickaid.passiveintegration.integration.gun.helper.TaczGunHelper`
- `com.pickaid.passiveintegration.integration.gun.helper.VanillaAnimatedGunsHelper`
- `com.pickaid.passiveintegration.integration.gun.helper.ScorchedGuns2Helper`
- `com.pickaid.passiveintegration.integration.gun.helper.JustEnoughGunsHelper`

Required common helper shape:

- `loaded()`
- `supports(ItemStack, @Nullable LivingEntity)`
- `snapshot(ItemStack, @Nullable LivingEntity)`
- `view(ItemStack, @Nullable LivingEntity)`

Rules:

- Helpers are public Java API.
- Helpers may expose platform-specific methods and platform-specific view accessors.
- Helpers must return stable value objects, not upstream mod runtime classes.

## Platform View Objects

Each helper returns a read-only platform view object. These views exist to expose platform-specific data without leaking unstable third-party types into the public API.

Required view classes:

- `TaczGunView`
- `VanillaAnimatedGunsView`
- `ScorchedGuns2View`
- `JustEnoughGunsView`

Minimum rules for every view:

- must expose `snapshot()`
- must expose platform id and core normalized identifiers
- must expose platform-specific getters for stable values
- may expose `JsonObject data()` or `JsonObject raw()` as a fallback
- must only expose stable Java values such as `String`, `int`, `double`, `boolean`, `List<String>`, and `JsonObject`

Must not expose:

- upstream mod gun objects
- mutable implementation internals
- live runtime handles that would break when the target mod changes its internals

## Normalized Snapshot Contract

`GunWeaponSnapshot` remains the shared contract between platforms.

It continues to own:

- normalized weapon identity
- normalized fire and reload state
- normalized ammo and projectile state
- shared runtime booleans
- `data().raw` and `data().runtime` for deep helper data

Rules:

- The bridge always returns normalized snapshots.
- Each platform helper also returns the same normalized snapshot for shared use.
- Platform views may add richer access, but must not replace the normalized snapshot.

## Data Flow

### Generic Consumer Path

For generic Java consumers:

1. Call `GunDataBridge.supports(...)` or `GunDataBridge.snapshot(...)`.
2. Bridge resolves the current platform.
3. Bridge delegates to the corresponding platform helper.
4. Consumer receives a normalized `GunWeaponSnapshot` or hit context.

### Platform-Specific Consumer Path

For Java consumers that care about platform details:

1. Call the platform helper directly.
2. Validate `loaded()` or `supports(...)`.
3. Read the platform `view(...)`.
4. Use `snapshot()` for shared fields and view getters for platform-specific fields.

### PassiveBurst KJS Path

For `PassiveBurst` KJS bindings:

1. KJS bridge code attempts to load `GunDataBridge`.
2. KJS bridge code attempts to load each platform helper class individually.
3. If a class exists, it is exposed.
4. If a class does not exist, the binding returns `null` or omits that helper.

This keeps gun KJS access optional and dependency-safe without moving the logic into `PassiveBurst`.

## KJS Binding Rules

`PassiveBurst` may expose gun helpers through its own KJS binding layer, but only as imported Java classes.

Required rules:

- `PassiveIntegration` does not own gun KJS registration.
- `PassiveBurst` does not reimplement gun extraction logic.
- `PassiveBurst` only imports and forwards existing Java APIs.
- Missing helper classes must fail softly.

Recommended KJS shape:

- one gun root binding in `PassiveBurst`
- `bridge()` for the unified bridge
- one accessor per helper such as `tacz()`, `vag()`, `scorchedGuns2()`, `justEnoughGuns()`

If a helper class is absent, the accessor returns `null`.

## Error Handling

The bridge and helpers must degrade safely.

Rules:

- If a platform mod is absent, `loaded()` returns `false`.
- If a stack is unsupported, `supports(...)` returns `false`.
- If no snapshot can be built, `snapshot(...)` returns empty instead of throwing.
- If platform-specific deep data is missing, view getters return stable empty values or `null`.
- Dynamic KJS imports in `PassiveBurst` must treat missing classes as an expected condition, not a crash.

## Testing Requirements

### PassiveIntegration

Must cover:

- bridge routing to the correct platform helper
- helper `loaded()` and `supports(...)` behavior
- normalized snapshot continuity across bridge and helpers
- platform view exposure for TacZ, VAG, Scorched Guns 2, and JEG
- safe behavior when optional mods are absent

### PassiveBurst

Must cover:

- dynamic import success when bridge or helper classes are present
- dynamic import fallback when classes are absent
- no embedded gun extraction logic inside KJS binding code

### Boundary Tests

Current source layout tests still reflect an older split where gun packages were expected to disappear from `PassiveIntegration`.

Those tests must be updated to enforce the new boundary instead:

- old legacy compat packages remain forbidden
- old util-based gun helpers remain forbidden
- new `integration.gun` and `service.gun` API packages are allowed
- gun KJS code still stays out of `PassiveIntegration`

## Migration Constraints

The repository is currently in an in-between state with deleted tracked gun packages and new untracked gun sources.

Implementation must stabilize this by:

- restoring the new gun API as tracked source
- deleting only obsolete legacy paths
- updating source layout tests to match the new architecture
- avoiding a second parallel helper structure under `util`

## Recommendation

Implement the bridge-plus-helper model now.

It matches the split that already moved Burst ownership into `PassiveBurst`, keeps gun compatibility local to `PassiveIntegration`, and gives KJS a stable class-based import path without making `PassiveSTJS` responsible for any gun feature again.
