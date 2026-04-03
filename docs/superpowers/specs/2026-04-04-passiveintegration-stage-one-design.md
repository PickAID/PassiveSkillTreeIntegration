# PassiveIntegration Stage One Design

## Status

Approved in design review on April 4, 2026.

## Scope

Stage One standardizes the mod around a service-based core for KubeJS content, custom bonuses, custom conditions, strict integration isolation, and the Ammo Burst gun feature. The goal is to make future integrations faster to add, easier to document, and safer to maintain.

This design replaces ad hoc growth with a small standard:

- setup is declarative
- runtime use is separate from setup
- integration modules are isolated behind adapters
- editor, ProbeJS, docs, and runtime behavior all use the same metadata

## Goals

- Standardize the mod around reusable services instead of feature-by-feature hardcoding.
- Add a real custom bonus registry and custom condition registry for KubeJS.
- Keep setup and runtime logic separate.
- Make requirements compose from conditions instead of becoming another callback system.
- Rework Ammo Burst into a normalized percent-based feature with slow recharge and meaningful tree investment.
- Keep gun integrations isolated by adapter modules instead of spreading `isLoaded()` checks across the codebase.
- Clean up the KubeJS folder structure and document the phase rules in a PMMOJS-style guide.
- Fix current user-facing breakage in the editor, preview, HUD, and translations.

## Non-Goals

- Do not rename the runtime mod id or resource namespace. `passiveintegration` stays stable.
- Do not ship a large generic runtime framework with rich callback contexts in Stage One.
- Do not add built-in feature-specific KubeJS helpers such as `supportedGun()` to the standard API.
- Do not depend on direct `ForgeEvents.onEvent(...)` usage for server runtime logic in `server_scripts`.

## Identity

- Rename Java package-side identity from the current project-specific naming to `pickaid`.
- Keep the runtime mod id, resource ids, asset namespace, config prefixes, and existing public ids on `passiveintegration`.
- Treat rename work as package/group/docs cleanup, not a compatibility-breaking content namespace migration.

## Core Architecture

Stage One introduces a small set of standard services.

### Standard Services

- `BonusTypeRegistry`
  - Registers built-in and custom KubeJS bonus types.
  - Owns merge rules, default values, editor metadata, display names, descriptions, units, and ProbeJS metadata.
- `ConditionTypeRegistry`
  - Registers built-in and custom KubeJS condition types.
  - Owns parameter metadata and evaluation dispatch.
- `RequirementService`
  - Evaluates requirement trees through registered conditions plus vanilla requirement checks.
  - Supports logical composition instead of custom requirement callbacks.
- `BonusQueryService`
  - Computes effective bonus values for a player or runtime context by bonus id.
  - Exposes number, boolean, and object queries.
- `ContentSyncService`
  - Owns managed KubeJS tree and skill sync to the client editor cache.
  - Keeps tree preview and `/skilltree editor` aligned with generated content.
- `IntegrationBootstrap`
  - Performs all `ModList.get().isLoaded(...)` checks once.
  - Activates feature and platform modules.

### Integration Isolation

Only the bootstrap layer is allowed to inspect mod presence directly. After bootstrap:

- feature code uses service interfaces
- platform-specific behavior uses adapter interfaces
- no scattered `PassiveIntegration.isLoaded("...")` checks remain in feature logic

This replaces the current pattern in core and helpers with a single source of truth.

### Module Structure

- Core services live under a service-oriented package layout.
- Feature modules depend on services, not on direct mod checks.
- Platform modules depend on target mods and expose adapter implementations.

Initial Stage One modules:

- `AmmoBurstFeatureModule`
- `CgmGunAdapterModule`
- `TaczGunAdapterModule`
- `PointBlankGunAdapterModule`
- placeholder adapter modules for Vanilla Animated Guns, Scorched Guns 2, and Just Enough Guns

## KubeJS Standard

The KubeJS standard follows Forge 1.20.1 phase reality and the PMMOJS documentation style.

### Script Phases

`startup_scripts/passiveintegration/`

- register custom bonus types
- register custom condition types
- register startup-only hook metadata
- do not place live gameplay logic here

`server_scripts/passiveintegration/content/`

- own `PassiveIntegrationEvents.skillTreeContent(...)`
- create trees, skills, icons, requirements, bonus payloads, and disable or edit existing trees
- remain reload-friendly

`server_scripts/passiveintegration/runtime/`

- use server-side `PassiveIntegration` runtime events and generic query APIs
- perform gameplay logic through query and evaluation services
- do not rely on direct startup-only Forge hooks

`client_scripts/passiveintegration/`

- client-only HUD, tooltip, and preview helpers
- no content registration

`server_scripts/passiveintegration/docs/`

- `ProbeJSEvents.generateDoc(...)`
- dev snippets
- example runtime queries

### File Organization

Stage One standardizes the KubeJS folder layout:

```text
startup_scripts/passiveintegration/00_bonus_types.js
startup_scripts/passiveintegration/01_condition_types.js
server_scripts/passiveintegration/content/10_trees_core.js
server_scripts/passiveintegration/content/20_trees_guns.js
server_scripts/passiveintegration/runtime/10_ammo_burst.js
server_scripts/passiveintegration/runtime/20_gun_modules.js
server_scripts/passiveintegration/docs/90_probe_docs.js
client_scripts/passiveintegration/10_hud.js
```

The mod should ship examples and docs that follow this layout so modpack authors do not end up with scattered files.

### Standard KubeJS API Surface

Stage One adds or standardizes these entrypoints:

- `PassiveIntegrationEvents.bonusRegistry(...)`
- `PassiveIntegrationEvents.conditionRegistry(...)`
- `PassiveIntegrationEvents.skillTreeContent(...)`
- `PassiveIntegration.getBonusNumber(...)`
- `PassiveIntegration.getBonusBoolean(...)`
- `PassiveIntegration.getBonusObject(...)`
- `PassiveIntegration.testCondition(...)`

The standard API must stay generic. It must not include feature-specific helpers such as `supportedGun()`.

Legacy one-off event surfaces that do not fit the standard, including `PassiveIntegrationEvents.gunHit`, are removed instead of carried forward.

## Bonus Model

Custom bonuses are registered once, then used as data in tree content and queried at runtime.

### Bonus Type Rules

Each bonus type declares:

- id
- value type: `number`, `boolean`, or `object`
- default value
- merge rule
- editor metadata
- ProbeJS metadata

### Bonus Use Rules

- Tree content stores serialized bonus payloads only.
- Runtime behavior does not live inside tree data.
- Runtime logic queries effective bonus values by id.

This keeps content previewable, syncable, and documentable.

### Merge Rules

Stage One needs built-in merge support for at least:

- additive number
- multiplicative number
- max number
- boolean any-true
- object merge with per-field metadata

## Condition Model

Conditions are reusable pure predicates.

### Condition Rules

- no side effects
- serializable arguments
- usable by bonuses, requirements, listeners, and runtime scripts
- documented through metadata

### Condition Examples

Expected use cases:

- item or tag checks
- effect checks
- numeric comparisons through value providers
- runtime bonus-derived checks
- integration-defined semantic checks exposed by adapter modules

Gun semantics are not built into the core API. If a pack needs a gun-aware condition, that condition must be registered by a gun adapter module or by scripts.

## Requirement Model

Requirements are gates for learning or unlocking content. They are not a second callback framework.

### Built-in Requirement Forms

- learned skill
- advancement
- stat value
- numeric value
- condition reference
- logical composition

### Required Composition Helpers

- `allOf(...)`
- `anyOf(...)`
- `not(...)`
- `condition(...)`

This keeps requirements readable in tree content and prevents duplication between condition logic and requirement logic.

## Gun Integration Model

Gun support must stay isolated by adapter.

### Gun Adapter Contract

Each adapter may answer generic questions such as:

- does this stack belong to this adapter’s supported weapon family
- does this projectile belong to this adapter
- can this shot bypass ammo for the feature
- what bridge events can the adapter emit for runtime logic

Core Ammo Burst code depends on the adapter contract, not on specific mod classes.

### Adapter Activation

Adapters are activated once by bootstrap. After that:

- the core feature only sees active adapters
- inactive adapters do nothing
- unsupported mod setups do not produce ghost HUD, keybind, or logic behavior

## Ammo Burst Design

Ammo Burst becomes a normalized percent-based feature.

### Runtime Rules

- server authoritative
- synced to the client through packets
- toggleable by config
- unavailable unless unlocked
- gun-scoped through active gun adapters
- no action bar messages in normal use

### Base Values

- base max energy: `100%`
- base refill time from `0%` to `100%`: `300s`
- base regen: `0.333% / sec`
- base activation cost: `50%`
- base active drain: `5.0% / sec`
- default active time with no bonuses: `10s`

### Bonus Axes

Stage One standardizes Ammo Burst bonus axes as:

- unlock
- max energy increase
- active drain reduction
- regen increase

### Formulas

Let:

- `total_regen_bonus` be additive and expressed as a decimal multiplier, for example `0.25` for `+25%`
- `total_drain_reduction` be additive and expressed as a decimal multiplier

Then:

- `effective_regen_per_sec = base_regen_per_sec * (1 + total_regen_bonus)`
- `effective_activation_cost_percent = clamp(50 - (total_regen_bonus * 12), 30, 50)`
- `effective_active_drain_per_sec = clamp(5.0 * (1 - total_drain_reduction), 2.0, 5.0)`

This means:

- regen bonuses improve both recharge speed and activation efficiency
- drain reduction extends active uptime
- activation cost has a hard floor of `30%`
- active drain has a hard floor of `2.0% / sec`

### Transition Rules

Activation, ticking, and forced shutdown must use the same requirement path. This fixes the current mismatch where a scripted activation can succeed and the next tick can immediately shut the feature down for a different reason.

### HUD Rules

- The HUD is code-rendered and packet-driven.
- The HUD must not appear, even for one frame, when Ammo Burst is disabled, not unlocked, unsupported by active adapters, or otherwise unavailable.
- Pressing the key while the feature is unavailable must not create a transient overlay flash.
- The HUD renders normalized percentages directly.

### Client Registration

Client registration must move away from `@Mod.EventBusSubscriber` annotation-driven setup for overlay and key mapping registration. Client bootstrap should register listeners explicitly from the mod entrypoint or a dedicated client initializer.

## Editor and Preview

### Custom Metadata

Registered custom bonus and condition types become first-class editor metadata, not opaque JSON.

Each custom bonus type can declare:

- display name
- description
- value type
- merge mode
- optional unit
- optional icon hint

### Preview Rules

- Tree preview must show effective custom bonus summaries.
- Added custom bonuses must be visible in preview.
- Existing trees must remain editable.
- Default trees must be removable or disableable.
- Full icon, background, border, title, and title-color configuration must remain supported.

### Bug Fix Expectations

Stage One includes fixes for:

- empty generated tree content in the editor
- duplicated tooltip description lines in generated skill data
- hidden custom bonus effects in preview

## ProbeJS

The default documentation path is script-side ProbeJS generation, not a ProbeJS-only Java event system.

### ProbeJS Output

Registry metadata should generate:

- bonus ids
- condition ids
- requirement composition helpers
- runtime query helper docs
- Ammo Burst state stages and reasons

Optional advanced ProbeJS plugin support may be added later, but only where script-side docs are not enough.

## Documentation

Stage One documentation is required work, not follow-up work.

### Minimum Doc Set

- `docs/kubejs/phases.md`
- `docs/kubejs/bonus-registry.md`
- `docs/kubejs/condition-registry.md`
- `docs/kubejs/requirements.md`
- `docs/kubejs/skilltree-content.md`
- `docs/kubejs/runtime-queries.md`
- `docs/kubejs/probejs.md`
- `docs/kubejs/ammo-burst.md`

### Documentation Style

The docs should follow PMMOJS-style guidance:

- short explanation of phase rules
- good examples
- wrong-phase examples
- explicit guidance on setup vs runtime use

## Translations

All Stage One user-facing strings must be translated at least in `en_us`.

This includes:

- HUD labels
- keybind strings
- category names
- custom bonus and condition labels
- config text where surfaced to users
- sample tree content

New Stage One content must not ship with raw translation keys shown to the player.

## Verification

Stage One is not complete until runtime, editor, docs, and ProbeJS all agree.

### Required Verification

- KubeJS-generated tree content appears correctly in `/skilltree editor`
- managed trees and skills sync correctly to the client editor cache
- generated and edited tree bonuses show in preview
- default trees can be disabled or removed
- Ammo Burst no longer flickers when unavailable
- pressing the Ammo Burst key while unavailable does not produce a one-frame HUD flash
- normalized Ammo Burst pacing matches the approved formulas
- ProbeJS docs generate correctly
- KubeJS folder layout matches the documented standard
- Stage One strings are translated in `en_us`

### Verification Types

- unit tests for registry, merge, and query behavior
- integration tests for tree generation and sync
- manual dev scripts in `run/.../kubejs/...`
- a written test checklist in docs

## Stage One Deliverables

- service-based core standard for bonuses, conditions, requirements, and queries
- strict integration bootstrap and adapter isolation
- cleaned KubeJS layout and PMMOJS-style docs
- ProbeJS metadata generation from registry metadata
- normalized Ammo Burst feature with fixed pacing and fixed HUD behavior
- editor and preview fixes for generated content
- package-side rename to `pickaid` without changing the runtime namespace
