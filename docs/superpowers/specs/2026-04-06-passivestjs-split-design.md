# PassiveSTJS Split Design

## Status

Approved in design review on April 6, 2026.

## Scope

This design splits the current mixed KubeJS support out of `PassiveIntegration` and into a new independent Forge mod named `PassiveSTJS`.

`PassiveIntegration` keeps the core `ammoBurst` feature and exposes stable Forge events.

`PassiveSTJS` becomes the only KubeJS-facing integration layer for:

- Skill Tree scripting support
- future Skill Tree editor-side script support
- optional `ammoBurst` KubeJS support when `PassiveIntegration` is installed

This design is about boundaries and event contracts. It does not implement the split yet.

## Goals

- Make `PassiveIntegration` independent from KubeJS.
- Move all current KubeJS support into a separate mod.
- Keep `PassiveIntegration` responsible for `ammoBurst` core logic only.
- Expose `ammoBurst` through a stable Forge event layer instead of direct internal hooks.
- Make `PassiveSTJS` depend on `skilltree` and `kubejs`, with `passiveintegration` optional.
- Use one new KubeJS event group, `PassiveSTJSEvents`, instead of preserving old script names.
- Support advanced `ammoBurst` continuation logic, including refund-based continuation and zero-energy sustain, without making the final end event ambiguous.

## Non-Goals

- Do not preserve `PassiveIntegrationEvents` for backward compatibility.
- Do not keep any KubeJS package or KubeJS event registration inside `PassiveIntegration`.
- Do not make `PassiveSTJS` read `PassiveIntegration` internal classes directly.
- Do not turn `PassiveIntegration` into a general scheduler or script runtime.
- Do not implement a generic callback object or raw `Consumer` transport across Forge events.

## Naming

### New Mod

- display name: `PassiveSTJS`
- recommended mod id: `passivestjs`
- recommended base package: `com.pickaid.passivestjs`

### Existing Mod

- `PassiveIntegration` keeps its current mod id and runtime identity.
- `PassiveIntegration` no longer owns any KubeJS event surface.

### Script Event Group

- all new script events live under `PassiveSTJSEvents`
- old `PassiveIntegrationEvents` names are removed rather than forwarded

## Module Boundaries

### PassiveIntegration

`PassiveIntegration` owns:

- `ammoBurst` state machine
- `ammoBurst` attributes
- key handling and server-authoritative activation
- network sync for client state
- supported gun detection and gun-mod integration
- public Forge events for `ammoBurst`

`PassiveIntegration` does not own:

- KubeJS event registration
- JS wrapper event classes
- skill tree content script APIs

### PassiveSTJS

`PassiveSTJS` owns:

- all Skill Tree KubeJS-facing APIs
- all current KubeJS support being moved out of `PassiveIntegration`
- KubeJS wrapper classes for `ammoBurst`
- optional bridge logic that listens to `PassiveIntegration` Forge events and republishes them to KubeJS

### Dependency Rules

`PassiveSTJS` dependencies:

- required: `skilltree`
- required: `kubejs`
- optional: `passiveintegration`

`PassiveIntegration` dependencies:

- no dependency on `kubejs`
- no dependency on `PassiveSTJS`

Dependency flow is one-way:

- `PassiveSTJS` may adapt `PassiveIntegration`
- `PassiveIntegration` never depends on `PassiveSTJS`

## PassiveSTJS Package Layout

The new mod should start with three top-level areas:

- `com.pickaid.passivestjs`
  - mod entrypoint and dependency detection
- `com.pickaid.passivestjs.kubejs`
  - KubeJS event groups, JS event wrappers, script APIs
- `com.pickaid.passivestjs.compat`
  - compatibility bridges

Initial compatibility bridges:

- `compat.skilltree`
- `compat.passiveintegration`

This keeps future integrations from collapsing back into one flat package.

## Ammo Burst Architecture

The split only works if `PassiveIntegration` exposes a stable core contract for `ammoBurst`.

That contract is a Forge event layer plus a small query surface. KubeJS logic must sit outside that core.

### Internal State Model

`ammoBurst` should move from a simple active flag to an explicit runtime state:

- `OFF`
- `ACTIVE`
- `ZERO_SUSTAIN`

Meaning:

- `OFF`: not active
- `ACTIVE`: normal burst is active and energy follows the standard drain and regen rules
- `ZERO_SUSTAIN`: burst remains active at zero energy under external control

### Zero Sustain Rules

`ZERO_SUSTAIN` has these rules:

- the burst still counts as active
- current energy is fixed at `0`
- normal regen and drain logic are paused
- a scheduled sustain event runs using values set when the sustain mode begins
- a manual key press still goes through the normal shutdown path
- forced shutdown reasons such as config disable or unlock loss still go through the pre-end event

The runtime should store at least:

- `sourceReason`
- `startDelayTicks`
- `intervalTicks`
- `nextTriggerTick`
- `runIndex`
- `sustainAgeTicks`

## Forge Event Model in PassiveIntegration

The Forge event layer is the public integration contract for `ammoBurst`.

### Event List

- `AmmoBurstTryStartEvent`
- `AmmoBurstStartEvent`
- `AmmoBurstFailEvent`
- `AmmoBurstAboutToEndEvent`
- `AmmoBurstSustainEvent`
- `AmmoBurstEndEvent`

### AmmoBurstTryStartEvent

Triggered when the user attempts to start `ammoBurst` while it is not already active.

Properties:

- `player`
- `server`
- `currentEnergy`
- `maxEnergy`
- `regenPerSecond`
- `drainPerSecond`
- `activationCost`
- `hasUnlock`
- `hasSupportedGun`

Mutability:

- may cancel start
- may modify `currentEnergy`
- may modify `maxEnergy`
- may modify `regenPerSecond`
- may modify `drainPerSecond`
- may modify `activationCost`
- may ignore unlock requirement
- may ignore supported gun requirement

### AmmoBurstStartEvent

Triggered only after start succeeds.

Properties:

- `player`
- `server`
- `remainingEnergy`
- `maxEnergy`
- `regenPerSecond`
- `drainPerSecond`
- `activationCost`

Mutability:

- read-only

### AmmoBurstFailEvent

Triggered when the user attempts to start `ammoBurst` but the attempt fails.

Properties:

- `player`
- `server`
- `reason`
- `currentEnergy`
- `maxEnergy`
- `regenPerSecond`
- `drainPerSecond`
- `activationCost`

Recommended failure reasons:

- `DISABLED`
- `CANCELED`
- `MISSING_ENERGY`
- `MISSING_UNLOCK`
- `MISSING_GUN`

Mutability:

- read-only

### AmmoBurstAboutToEndEvent

Triggered whenever `ammoBurst` is about to stop for any reason. This is not the final end event. It exists so external systems can decide what happens next.

Properties:

- `player`
- `server`
- `reason`
- `currentEnergy`
- `maxEnergy`
- `regenPerSecond`
- `drainPerSecond`
- `activationCost`
- `drainThisStep`

Decision model:

- `END_NOW`
- `REFUND_AND_CONTINUE`
- `ENTER_ZERO_SUSTAIN`

Mutable fields:

- `refundEnergy`
- `sustainStartDelayTicks`
- `sustainIntervalTicks`

Rules:

- `END_NOW` proceeds to the final end
- `REFUND_AND_CONTINUE` restores energy and returns to `ACTIVE`
- `ENTER_ZERO_SUSTAIN` switches to `ZERO_SUSTAIN`

This event replaces the idea of a cancellable final end. The final end event remains final.

### AmmoBurstSustainEvent

Triggered while `ammoBurst` is in `ZERO_SUSTAIN`.

Properties:

- `player`
- `server`
- `sourceEndReason`
- `activationCost`
- `drainPerSecond`
- `drainThisStep`
- `runIndex`
- `sustainAgeTicks`
- `currentEnergy`, fixed at `0`

Decision model:

- `CONTINUE`
- `TERMINATE`
- `EXIT_WITH_REFUND`

Mutable fields:

- `refundEnergy`

Rules:

- `CONTINUE` keeps `ZERO_SUSTAIN`
- `TERMINATE` ends the burst for real
- `EXIT_WITH_REFUND` restores energy and returns to `ACTIVE`

### AmmoBurstEndEvent

Triggered only when `ammoBurst` really ends.

Properties:

- `player`
- `server`
- `finalReason`
- `sourceReason`
- `currentEnergy`
- `maxEnergy`
- `passedThroughZeroSustain`

Mutability:

- read-only

This event is never canceled and never reopens the burst.

## Manual Toggle Behavior

When the player presses the key while `ammoBurst` is already active or in `ZERO_SUSTAIN`, the system should not use the start path again.

Instead it should go through:

- `AmmoBurstAboutToEndEvent(reason = MANUAL)`

That keeps all exit behavior on one path.

## KubeJS API in PassiveSTJS

`PassiveSTJS` listens to the Forge events from `PassiveIntegration` and republishes them as KubeJS events.

These are wrapper events, not raw Forge event objects.

### Event Group

- `PassiveSTJSEvents`

### Registration Rules

- Skill Tree events are always registered because `PassiveSTJS` requires `skilltree` and `kubejs`
- `ammoBurst` events are registered only if `passiveintegration` is loaded

### Script Event Names

- `ammoBurstTryStart`
- `ammoBurstStart`
- `ammoBurstFail`
- `ammoBurstAboutToEnd`
- `ammoBurstSustain`
- `ammoBurstEnd`

### Script Wrapper Rules

The JS API should use explicit intent methods instead of hidden combinations of cancel flags and magic field writes.

#### ammoBurstTryStart

Provides:

- readable and writable energy and rate fields
- `cancel()`
- ignore requirement controls

#### ammoBurstStart

Provides:

- read-only start context

#### ammoBurstFail

Provides:

- read-only failure context

#### ammoBurstAboutToEnd

Provides explicit methods:

- `endNow()`
- `refundAndContinue(amount)`
- `enterZeroSustain(startDelayTicks, intervalTicks)`

#### ammoBurstSustain

Provides explicit methods:

- `continueSustain()`
- `terminate()`
- `exitWithRefund(amount)`

#### ammoBurstEnd

Provides:

- read-only final end context

These wrapper methods keep scripts readable and prevent users from depending on internal event ordering.

## Migration Plan

The split should happen in this order.

### Step 1: Add Forge Events to PassiveIntegration

Before removing any KubeJS code, `PassiveIntegration` should publish the new Forge event layer for `ammoBurst`.

This keeps the core usable while the bridge mod is being built.

### Step 2: Create PassiveSTJS as an Independent Forge Mod

Start the new mod with:

- its own `build.gradle`
- its own `mods.toml`
- required dependencies on `skilltree` and `kubejs`
- optional dependency on `passiveintegration`

### Step 3: Move Existing KubeJS Support into PassiveSTJS

Move all current KubeJS support out of `PassiveIntegration`, including:

- KubeJS event group registration
- JS wrapper event classes
- Skill Tree script-facing APIs
- optional `ammoBurst` KubeJS bridge

### Step 4: Bridge PassiveIntegration Ammo Burst into KubeJS

If `passiveintegration` is present:

- subscribe to the new Forge events
- wrap them as `PassiveSTJSEvents`
- expose the approved script methods

If `passiveintegration` is absent:

- do not register `ammoBurst` script events
- keep the rest of `PassiveSTJS` working

### Step 5: Remove KubeJS Support from PassiveIntegration

Once `PassiveSTJS` is in place:

- remove the `kubejs` package from `PassiveIntegration`
- remove old KubeJS event registration
- remove old script event names
- remove KubeJS-specific branching from core `ammoBurst` logic

## Release Strategy

Use a two-release migration instead of one large cutover.

### Release A

`PassiveIntegration` adds the new Forge event layer but still ships the old KubeJS support temporarily.

### Release B

`PassiveSTJS` is released, and a follow-up `PassiveIntegration` release removes its KubeJS layer.

This reduces the chance of users getting stuck between two half-finished versions.

## Verification

The split is not complete until these checks pass.

### PassiveIntegration

- builds and runs without `kubejs`
- `ammoBurst` works without script support
- Forge event flow matches the approved lifecycle
- `ZERO_SUSTAIN` transitions are stable
- manual shutdown, unlock loss, config disable, and energy depletion all route through `AmmoBurstAboutToEndEvent`

### PassiveSTJS

- refuses to load without `skilltree` and `kubejs`
- loads without `passiveintegration`
- still exposes Skill Tree script support when `passiveintegration` is absent
- only registers `ammoBurst` script events when `passiveintegration` is present
- translates Forge event decisions into the JS wrapper API correctly

### Integration Verification

- no script uses `PassiveIntegrationEvents`
- scripts use only `PassiveSTJSEvents`
- `refundAndContinue` restores energy and resumes `ACTIVE`
- `enterZeroSustain` keeps the burst active at zero energy
- sustain scheduling obeys `startDelayTicks` and `intervalTicks`
- `terminate()` ends the burst and emits the final end event
- `exitWithRefund(amount)` restores energy and exits `ZERO_SUSTAIN`

## Deliverables

- an independent Forge mod named `PassiveSTJS`
- removal of KubeJS support from `PassiveIntegration`
- a stable `ammoBurst` Forge event contract in `PassiveIntegration`
- a stable `PassiveSTJSEvents` KubeJS API
- optional `ammoBurst` KubeJS support when `PassiveIntegration` is installed
