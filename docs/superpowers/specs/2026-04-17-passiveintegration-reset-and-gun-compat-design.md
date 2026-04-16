# PassiveIntegration Reset And Gun Compatibility Design

**Date:** 2026-04-17

**Status:** Approved in design review on April 17, 2026.

**Goal:** Reset `PassiveIntegration` back to a clean integration-focused mod by removing direct `SkillTree` content ownership, fully retiring the legacy `crychicteam` structure, and defining one unified gun compatibility architecture with explicit support depth, helper surfaces, and compatibility rules.

## Scope

This design covers four connected decisions:

- `PassiveIntegration` responsibility boundaries after the split with `PassiveBurst` and `PassiveSTJS`
- the `Passive Skill Tree` capability map that future integration work must reference
- repository and package cleanup, including full removal of `org.crychicteam`
- the unified gun compatibility matrix and support depth for current and planned gun mods

This design intentionally does not define implementation steps. It defines the target architecture and boundaries that the implementation plan must follow.

## Responsibility Boundary

### PassiveIntegration

`PassiveIntegration` becomes a runtime integration layer only.

It owns:

- gun platform detection
- gun runtime normalization
- gun hit and secondary-effect compatibility
- bottom-layer compat shims for supported gun mods
- stable Java helper and bridge APIs for other mods
- gun-only bonuses and gun-only conditions that are genuinely integration-specific

It does not own:

- `SkillTree` content generation
- direct registration of exported tree JSON into `Passive Skill Tree` runtime maps
- client editor sync for managed tree content
- direct loading of exported tree files
- KubeJS content registration for `SkillTree` authoring
- Burst runtime, Burst HUD, Burst network, or Burst KJS logic

### PassiveBurst

`PassiveBurst` owns Burst runtime concerns and may consume stable Java APIs exposed by `PassiveIntegration`.

It may consume:

- normalized gun data
- normalized gun hit context
- per-platform gun helpers

It must not reimplement:

- gun platform extraction
- gun compatibility logic
- platform-specific deep data rules

### PassiveSTJS / SkillTreeJS Workflow

`PassiveIntegration` no longer acts as the tree-content authoring or testing center.

When tree behavior needs to be tested:

- use `PassiveSTJS` / `SkillTreeJS` as the temporary authoring and testing workflow
- export final tree content to datapack JSON
- load final tree content through the normal datapack path

This means `PassiveIntegration` no longer contains a parallel content system just to make tree testing easier.

## Passive Skill Tree Capability Map

Future integration work must be designed against the real `Passive Skill Tree` system surface instead of guessing what exists.

The current verified system surfaces are:

- `36` skill bonuses
- `2` item bonus container types
- `10` event listeners
- `8` damage conditions
- `7` item conditions
- `12` living conditions
- `2` living multipliers
- `9` float functions
- `4` skill requirements

### Skill Bonus Surface

The built-in `SkillBonus` registry already covers the major gameplay families:

- direct damage, crit chance, crit damage, damage taken, damage avoidance, damage conversion
- effect duration, inflict effect, inflict ignite, inflict damage
- projectile speed, projectile duplication, arrow retrieval
- item usage speed, item use movement speed, item durability loss avoidance
- attributes, all attributes, healing, incoming healing, health reservation
- loot, experience, recipes, granted items, repair efficiency, and related utility bonuses

`PassiveIntegration` should prefer adapting these existing systems to gun behavior instead of creating a second general-purpose bonus ecosystem.

### Item Bonus Surface

`Passive Skill Tree` already provides item-level bonus containers:

- `skill_bonus`
- `item_bonus_list`

That means gun integration should treat item bonuses as part of the compatibility matrix, not as a separate custom system.

### Listener Surface

The verified built-in listener families are:

- `attack`
- `block`
- `evasion`
- `item_used`
- `damage_taken`
- `on_kill`
- `skill_learned`
- `skill_removed`
- `ticking`
- `critical_hit`

Listener handling must be included in the gun compatibility design. Gun support is not only about damage bonuses.

### Requirement Surface

The verified built-in requirement families are:

- `stat_value`
- `numeric_value`
- `advancement`
- `learned_skill`

These remain part of the datapack and tree-content side of the ecosystem. `PassiveIntegration` should not invent a parallel gun-specific requirement framework.

### Condition Surface

Verified built-in condition families are:

- damage conditions: `none`, `projectile`, `melee`, `magic`, `fall`, `fire`, `poison`, `thorns`
- item conditions: `none`, `potions`, `food`, `item_id`, `enchanted`, `tag`, `equipment_type`
- living conditions: `none`, `has_item_equipped`, `has_effect`, `burning`, `fishing`, `underwater`, `dual_wielding`, `has_item_in_hand`, `crouching`, `unarmed`, `numeric_value`, `all_armor`

The design must account for how gun compatibility interacts with these conditions, especially:

- ranged-weapon checks
- enchanted-item checks
- target-state checks such as burning, poisoned, or effect-bearing targets
- the difference between generic projectile logic and explicit gun logic

### Value And Multiplier Surface

Verified built-in float functions:

- `attribute_value`
- `effect_amount`
- `food_level`
- `health_level`
- `equipment_durability`
- `enchantment_amount`
- `enchantment_levels`
- `distance_to_target`
- `learned_skills_amount`

Verified built-in multipliers:

- `none`
- `numeric_value`

These matter directly for the gun compatibility matrix because many approved gun-damage interactions are really combinations of existing conditions plus existing value functions.

## Extension Boundary

After this reset, `PassiveIntegration` may extend `Passive Skill Tree` only where an integration gap genuinely exists.

Allowed extension categories:

- gun-specific bonuses
- gun-specific damage conditions
- stable gun helper and bridge classes
- platform-specific compat shims and normalized runtime models

Disallowed or strongly discouraged extension categories:

- general-purpose listener families
- general-purpose requirement families
- general-purpose value/function families
- general-purpose multiplier families
- a second `SkillTree` content authoring system inside `PassiveIntegration`

The rule is simple: adapt `Passive Skill Tree`; do not try to grow a second `Passive Skill Tree` inside `PassiveIntegration`.

## SkillTree Content Removal

The current direct-content path inside `PassiveIntegration` is no longer part of the target architecture.

This includes removing or externalizing the current runtime content and sync chain, including classes equivalent to:

- runtime tree and skill injection helpers
- managed content bookkeeping
- datapack sync snapshots used only to mirror generated content into runtime
- client editor data stores for managed content
- custom content-sync packets that only exist to support this parallel content model

Final tree loading behavior should be:

1. author or test content outside `PassiveIntegration`
2. export final JSON
3. load through datapacks

No direct exported-file loading path should remain in `PassiveIntegration`.

## Repository Cleanup And Package Structure

### Package Root

The final source root must be:

- `com.pickaid.passiveintegration`

The legacy package root must be fully removed:

- `org.crychicteam.passiveintegration`

This is a full migration, not a long-term dual-tree arrangement.

### Target Structure

The repository should settle around these responsibilities:

- `com.pickaid.passiveintegration.bootstrap`
  - mod startup and loaded-mod registration
- `com.pickaid.passiveintegration.config`
  - runtime config that still belongs to `PassiveIntegration`
- `com.pickaid.passiveintegration.compat.gun`
  - normalized contracts and shared compat services
- `com.pickaid.passiveintegration.compat.gun.platform.<modid>`
  - platform-specific gun compat implementations
- `com.pickaid.passiveintegration.compat.gun.helper`
  - stable public Java helper API and read-only platform views
- `com.pickaid.passiveintegration.optional.gun`
  - optional behavior layers that consume normalized gun contexts
- `com.pickaid.passiveintegration.mixin.compat.gun.<modid>`
  - mixin hook shells only
- `com.pickaid.passiveintegration.util`
  - truly generic utilities only

### Layer Rules

The new layers have different jobs:

- `compat`
  - third-party gun recognition, hook points, normalization, and low-level extraction
- `helper`
  - stable public Java entrypoints and stable read-only views
- `optional`
  - PST-aware behavior that consumes normalized contexts to apply integration rules

This separation replaces the old scattered structure where behavior, hook logic, and platform checks were mixed together inside legacy `optional` and helper classes.

### KubeJS Boundary

`PassiveIntegration` should not continue owning KubeJS event and binding registration for these systems.

The target boundary is:

- stable Java APIs live in `PassiveIntegration`
- `PassiveBurst` and `PassiveSTJS` may consume those APIs where appropriate
- `PassiveIntegration` itself should not remain a KubeJS content-authoring surface

## Gun Compatibility Architecture

Gun compatibility is defined as four separate behavior pipelines, not one giant "gun damage" bucket.

### A. Direct Gun Hit Pipeline

This is the mainline path for bullet-like direct gun hits.

It owns:

- normalized `GunHitContext`
- normalized gun damage recognition
- direct-hit bonus application
- crit handling
- kill and damage-taken semantics related to gun hits
- target, attacker, distance, effect, hunger, and item-state condition evaluation for gun hits

This pipeline is a distinct semantic category.

It is:

- not melee
- not generic projectile by default
- not a synonym for vanilla attack-damage logic

It must use an explicit gun-compat matrix instead of piggybacking on broad projectile assumptions.

### B. Condition And Listener Pipeline

Gun compatibility must explicitly define how existing `Passive Skill Tree` conditions and listeners behave for guns.

Required behavior:

- ranged-weapon checks must support all supported gun mods
- enchanted-item checks must support guns when the stack itself is enchanted
- `GunDamageCondition` must exist as the dedicated gun-damage semantic
- `ProjectileDamageCondition` must keep its projectile meaning and must not be treated as the main gun-damage semantic

Listener policy:

- `attack` supports direct gun hits
- `critical_hit` supports gun crit and headshot semantics
- `on_kill` supports gun kills
- `damage_taken` supports receiving gun damage
- `item_used` is not automatically treated as a shoot event unless a platform exposes a stable action semantic that the implementation explicitly chooses to support
- `block`, `ticking`, `skill_learned`, and `skill_removed` do not need special gun-specific semantics beyond existing PST behavior

### C. Runtime Action Pipeline

This covers action-state behavior that is not direct damage:

- reload speed
- fire cadence
- ammo-free or burst-like runtime hooks that remain inside `PassiveIntegration`
- normalized weapon runtime flags
- ammo and attachment state
- helper-readable deep weapon data

This pipeline belongs in normalized gun compat services and helper APIs, not in scattered mod-specific action classes.

### D. Secondary Effect Pipeline

This covers non-direct-hit weapon behaviors such as:

- grenade effects
- rockets
- splash and explosive payloads
- stun, blind, and deafen effects
- future elemental or payload-style behaviors

This pipeline must stay separate from direct-hit gun damage.

Every special effect must opt in explicitly instead of being silently pulled into the direct gun-hit matrix.

`CGM` stun grenade support belongs here.

## Gun Compatibility Matrix

The approved matrix is derived from the verified behavior list in `img.png` and should become the canonical standard for all supported gun mods.

### Effects That Should Apply To Direct Gun Hits

The direct-hit matrix should support:

- damage and crit bonuses that are already semantically about actual hit damage
- target-state conditional damage and crit bonuses
- distance-based, hunger-based, effect-based, and similar value-driven damage logic
- eligible item bonuses that wrap those same damage and crit systems
- ranged-weapon and enchanted-item gating conditions where those conditions are only acting as eligibility checks

### Effects That Should Not Automatically Apply To Direct Gun Hits

The direct-hit matrix should reject automatic inheritance from:

- generic projectile semantics as the main gun path
- melee-only or attack-damage-only semantics
- vanilla `Strength` style logic unless a specific platform rule explicitly says otherwise
- broad "weapon in hand" assumptions that were built for melee or standard held-item logic
- external mod active abilities that have not been explicitly bridged into gun compat

This is the key normalization rule:

- direct gun hit damage is its own semantic category
- not every projectile rule should affect it
- not every held-weapon or attack-damage rule should affect it

## Existing Legacy Compat To Absorb

Current scattered compat logic such as:

- custom gun damage conditions
- helper methods like `isGunDamage` and `isSupportedGun`
- ranged-weapon predicate extensions
- projectile-condition patches that were only added to make some guns behave closer to PST expectations

must be absorbed into the unified architecture instead of extended ad hoc.

The new design should leave one official gun-compat path, not many partial ones.

## Support Depth

Gun mod support is defined in four depth levels.

- `S1` Basic Recognition
  - gun stack recognition
  - gun damage recognition
  - ranged and enchanted predicate support
- `S2` Unified Combat
  - direct-hit compatibility matrix
  - crit, kill, and damage-taken semantics
  - normalized hit context
- `S3` Deep Data
  - normalized snapshot
  - stable helper APIs
  - per-platform views
  - weapon identity, ammo state, action state, and runtime flags
- `S4` Special Behaviors
  - grenades
  - rockets
  - stun, blind, deafen, and other secondary effects

### Target Depth By Platform

- `TacZ`: target `S1-S3`
- `CGM`: target `S1-S4`
- `Vanilla Animated Guns`: target `S1-S3`
- `Scorched Guns 2`: initial release target `S1-S3`; `S4` is deferred until after the reset lands cleanly
- `Just Enough Guns`: initial release target `S1-S3`; `S4` is deferred until after the reset lands cleanly
- `PointBlank`: initial release target `S1-S2`

Initial release boundary:

- `PointBlank` is not a release gate for the first delivery of the reset
- `CGM` stun grenade support is a release target because it falls under approved `S4` behavior

## Documentation Deliverables

This reset must leave behind two durable reference documents:

- a `Passive Skill Tree` capability map for future development
- a gun compatibility matrix and support-depth reference for supported gun mods

These documents are part of the architecture, not optional notes. They are required so future work does not drift back into guesswork or platform-specific inconsistency.

## Non-Goals

This design does not require:

- keeping a second tree-content runtime inside `PassiveIntegration`
- keeping any `crychicteam` package alive for compatibility comfort
- expanding `PassiveIntegration` into a new general-purpose PST content framework
- treating every projectile-support feature as a gun-support feature
- forcing every gun mod to reach `S4` in the first delivery

## Acceptance Criteria

The design target is met when all of the following are true:

- `PassiveIntegration` no longer owns direct `SkillTree` content generation, runtime injection, or editor sync
- final tree loading happens through datapacks
- `PassiveIntegration` uses only the `com.pickaid.passiveintegration` package root
- the repository structure reflects the `compat / helper / optional` separation
- the verified `Passive Skill Tree` capability map exists as a maintained reference
- gun compatibility uses one explicit matrix instead of scattered special cases
- all supported gun mods are assigned an explicit target support depth
- `CGM` stun grenade support is treated as secondary-effect support, not direct-hit damage support

## Recommendation

Do the reset before further gun expansion.

The current repository already shows the cost of mixing three concerns inside one mod:

- integration runtime
- tree-content authoring
- platform-specific gun behavior

If gun support continues to grow before these boundaries are cleaned up, every new mod will multiply the inconsistency. Resetting the ownership model first gives the gun work one stable foundation instead of six partial ones.
