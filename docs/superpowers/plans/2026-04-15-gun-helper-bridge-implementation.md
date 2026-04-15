# Gun Helper Bridge Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild `PassiveIntegration` gun support as a stable Java bridge plus per-platform helpers, then expose those classes through `PassiveBurst` KJS dynamic imports without moving gun logic into `PassiveSTJS`.

**Architecture:** Restore the gun API in `PassiveIntegration` as three layers: core normalized contracts, platform adapters, and public per-platform helpers with read-only view objects. Keep `PassiveBurst` thin by adding a small reflective KJS binding layer that loads bridge and helper classes only if they exist.

**Tech Stack:** Forge 1.20.1 / NeoForged Gradle, Java 17, JUnit 5, KubeJS bindings, Curse Maven optional mod dependencies

---

## File Structure

### PassiveIntegration core API

- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/GunPlatformId.java`
  Responsibility: normalized platform ids and string lookup.
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/GunWeaponSnapshot.java`
  Responsibility: shared normalized gun snapshot plus deep data helpers.
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/GunHitContext.java`
  Responsibility: normalized hit metadata.
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/GunPlatformAdapter.java`
  Responsibility: adapter contract for gun platforms.
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/GunPlatformRegistry.java`
  Responsibility: adapter registration and platform resolution.
- Create: `src/main/java/com/pickaid/passiveintegration/service/gun/GunDataService.java`
  Responsibility: bridge-facing orchestration over the registry.
- Create: `src/main/java/com/pickaid/passiveintegration/service/gun/GunDataBridge.java`
  Responsibility: public static bridge API.

### PassiveIntegration public helpers and views

- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/helper/TaczGunHelper.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/helper/VanillaAnimatedGunsHelper.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/helper/ScorchedGuns2Helper.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/helper/JustEnoughGunsHelper.java`
  Responsibility: public Java helper entrypoints for each platform.
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/helper/view/TaczGunView.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/helper/view/VanillaAnimatedGunsView.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/helper/view/ScorchedGuns2View.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/helper/view/JustEnoughGunsView.java`
  Responsibility: read-only platform-specific view objects.

### PassiveIntegration platform adapters and bootstrap

- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/tacz/TaczGunPlatformAdapter.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/vanillaanimatedguns/VanillaAnimatedGunsPlatformAdapter.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/scorchedguns2/ScorchedGuns2PlatformAdapter.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/justenoughguns/JustEnoughGunsPlatformAdapter.java`
  Responsibility: extract normalized snapshots and hit context from each mod.
- Create: `src/main/java/com/pickaid/passiveintegration/bootstrap/IntegrationBootstrap.java`
- Create: `src/main/java/com/pickaid/passiveintegration/bootstrap/LoadedModSet.java`
  Responsibility: install the default gun registry once during mod startup.
- Modify: `src/main/java/com/pickaid/passiveintegration/PassiveIntegration.java`
  Responsibility: call the bootstrap during common runtime registration.
- Modify: `build.gradle`
  Responsibility: restore optional gun mod dependencies.

### PassiveIntegration tests

- Modify: `src/test/java/com/pickaid/passiveintegration/PassiveIntegrationSourceLayoutTest.java`
  Responsibility: allow the new gun API while still forbidding old legacy util helpers.
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/GunPlatformAdapterContractTest.java`
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/GunDataServiceTest.java`
- Create: `src/test/java/com/pickaid/passiveintegration/bootstrap/IntegrationBootstrapTest.java`
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/tacz/TaczGunPlatformAdapterTest.java`
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/vanillaanimatedguns/VanillaAnimatedGunsPlatformAdapterTest.java`
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/scorchedguns2/ScorchedGuns2PlatformAdapterTest.java`
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/justenoughguns/JustEnoughGunsPlatformAdapterTest.java`
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/helper/TaczGunHelperTest.java`
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/helper/VanillaAnimatedGunsHelperTest.java`
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/helper/ScorchedGuns2HelperTest.java`
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/helper/JustEnoughGunsHelperTest.java`

### PassiveBurst KJS bridge

- Create: `PassiveBurst/src/main/java/com/pickaid/passiveburst/kubejs/runtime/GunBindingsJS.java`
  Responsibility: reflective access to `GunDataBridge` and per-platform helpers.
- Modify: `PassiveBurst/src/main/java/com/pickaid/passiveburst/kubejs/Bindings.java`
  Responsibility: expose `gun()` alongside `burst()`.
- Modify: `PassiveBurst/src/test/java/com/pickaid/passiveburst/kubejs/PublicApiSurfaceTest.java`
  Responsibility: assert the public binding surface now contains both roots.
- Create: `PassiveBurst/src/test/java/com/pickaid/passiveburst/kubejs/GunBindingsJSTest.java`
  Responsibility: verify dynamic class loading and null fallback behavior.

## Task 1: Restore Gun Dependency and Source Boundary

**Files:**
- Modify: `build.gradle`
- Modify: `src/test/java/com/pickaid/passiveintegration/PassiveIntegrationSourceLayoutTest.java`
- Test: `src/test/java/com/pickaid/passiveintegration/PassiveIntegrationSourceLayoutTest.java`

- [ ] **Step 1: Write the failing boundary assertions**

```java
@Test
void sourceSetAllowsNewGunApiButStillForbidsLegacyHelpers() throws IOException {
    Path projectRoot = Path.of("").toAbsolutePath();
    Path legacyGunCompatHelper = projectRoot.resolve("src/main/java/com/pickaid/passiveintegration/util/GunCompatHelper.java");
    Path legacyTaczUtilHelpers = projectRoot.resolve("src/main/java/com/pickaid/passiveintegration/util/TaczUtilHelpers.java");
    Path newGunApiRoot = projectRoot.resolve("src/main/java/com/pickaid/passiveintegration/integration/gun");
    String buildGradleText = Files.readString(projectRoot.resolve("build.gradle"));

    assertFalse(Files.exists(legacyGunCompatHelper));
    assertFalse(Files.exists(legacyTaczUtilHelpers));
    assertTrue(buildGradleText.contains("timeless-and-classics-zero"));
    assertTrue(buildGradleText.contains("vanilla-animated-guns"));
    assertTrue(buildGradleText.contains("scorched-guns"));
    assertTrue(buildGradleText.contains("just-enough-guns"));
    assertFalse(projectRoot.resolve("src/main/java/com/pickaid/passiveintegration/kubejs/gun").toFile().exists());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `bash ./gradlew test --tests com.pickaid.passiveintegration.PassiveIntegrationSourceLayoutTest --console=plain`
Expected: FAIL because `build.gradle` does not yet contain the required optional gun dependencies.

- [ ] **Step 3: Write the minimal dependency and boundary update**

```groovy
// build.gradle
dependencies {
    // ...
    compileOnly fg.deobf("curse.maven:timeless-and-classics-zero-1028108:7745481")
    runtimeOnly fg.deobf("curse.maven:timeless-and-classics-zero-1028108:7745481")

    compileOnly fg.deobf("curse.maven:vanilla-animated-guns-1357310:7056729")
    runtimeOnly fg.deobf("curse.maven:vanilla-animated-guns-1357310:7056729")

    compileOnly fg.deobf("curse.maven:scorched-guns-802940:7232063")
    runtimeOnly fg.deobf("curse.maven:scorched-guns-802940:7232063")

    compileOnly fg.deobf("curse.maven:just-enough-guns-820727:6808139")
    runtimeOnly fg.deobf("curse.maven:just-enough-guns-820727:6808139")
}
```

```java
// PassiveIntegrationSourceLayoutTest.java
assertFalse(Files.exists(gunCompatHelper));
assertFalse(Files.exists(taczUtilHelpers));
assertFalse(projectRoot.resolve("src/main/java/com/pickaid/passiveintegration/kubejs/gun").toFile().exists());
assertTrue(buildGradleText.contains("timeless-and-classics-zero"));
assertTrue(buildGradleText.contains("vanilla-animated-guns"));
assertTrue(buildGradleText.contains("scorched-guns"));
assertTrue(buildGradleText.contains("just-enough-guns"));
```

- [ ] **Step 4: Run test to verify it passes**

Run: `bash ./gradlew test --tests com.pickaid.passiveintegration.PassiveIntegrationSourceLayoutTest --console=plain`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add build.gradle src/test/java/com/pickaid/passiveintegration/PassiveIntegrationSourceLayoutTest.java
git commit -m "build: restore passiveintegration gun dependencies"
```

### Task 2: Create Core Gun Bridge Contracts

**Files:**
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/GunPlatformId.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/GunWeaponSnapshot.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/GunHitContext.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/GunPlatformAdapter.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/GunPlatformRegistry.java`
- Create: `src/main/java/com/pickaid/passiveintegration/service/gun/GunDataService.java`
- Create: `src/main/java/com/pickaid/passiveintegration/service/gun/GunDataBridge.java`
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/GunPlatformAdapterContractTest.java`
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/GunDataServiceTest.java`
- Test: `src/test/java/com/pickaid/passiveintegration/integration/gun/GunPlatformAdapterContractTest.java`
- Test: `src/test/java/com/pickaid/passiveintegration/integration/gun/GunDataServiceTest.java`

- [ ] **Step 1: Write the failing core contract tests**

```java
@Test
void richSnapshotsExposeNormalizedFieldsAndDeepHelpers() {
    JsonObject raw = new JsonObject();
    raw.addProperty("burstCount", 3);
    JsonObject data = new JsonObject();
    data.add("raw", raw);

    GunWeaponSnapshot snapshot = new GunWeaponSnapshot(
            GunPlatformId.TACZ,
            "tacz:ak_alpha_item",
            "tacz:ak_alpha",
            "rifle",
            "burst",
            List.of("semi", "burst"),
            "magazine",
            "tacz:rifle_round",
            18,
            30,
            "tacz:rifle_round",
            8.5D,
            false,
            false,
            false,
            true,
            data
    );

    assertEquals("tacz", snapshot.platformId());
    assertEquals("tacz:ak_alpha", snapshot.weaponId());
    assertEquals(3.0D, snapshot.numberData("raw.burstCount"));
}
```

```java
@Test
void resolvesSnapshotFromFirstMatchingAdapter() {
    GunPlatformRegistry registry = new GunPlatformRegistry();
    registry.register(new FakeAdapter(GunPlatformId.TACZ, true, false));
    GunDataService service = new GunDataService(registry);

    assertTrue(service.resolveWeaponSnapshot(ItemStack.EMPTY, null).isPresent());
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `bash ./gradlew test --tests com.pickaid.passiveintegration.integration.gun.GunPlatformAdapterContractTest --tests com.pickaid.passiveintegration.integration.gun.GunDataServiceTest --console=plain`
Expected: FAIL with missing gun core types such as `GunWeaponSnapshot` and `GunDataService`.

- [ ] **Step 3: Write the minimal core implementation**

```java
// GunPlatformId.java
public enum GunPlatformId {
    TACZ("tacz"),
    VANILLA_ANIMATED_GUNS("vanilla_animated_guns"),
    SCORCHED_GUNS_2("scorched_guns_2"),
    JUST_ENOUGH_GUNS("just_enough_guns");

    private final String id;

    GunPlatformId(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static Optional<GunPlatformId> byId(String id) {
        return Arrays.stream(values()).filter(value -> value.id.equals(id)).findFirst();
    }
}
```

```java
// GunPlatformRegistry.java
public final class GunPlatformRegistry {
    private final List<GunPlatformAdapter> adapters = new ArrayList<>();

    public void register(GunPlatformAdapter adapter) {
        adapters.add(Objects.requireNonNull(adapter, "adapter"));
    }

    public List<GunPlatformAdapter> adapters() {
        return List.copyOf(adapters);
    }
}
```

```java
// GunDataService.java
public final class GunDataService {
    private final GunPlatformRegistry registry;

    public GunDataService(GunPlatformRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public Optional<GunWeaponSnapshot> resolveWeaponSnapshot(ItemStack stack, @Nullable LivingEntity holder) {
        if (stack == null) {
            return Optional.empty();
        }
        for (GunPlatformAdapter adapter : registry.adapters()) {
            if (adapter.matchesWeapon(stack)) {
                return adapter.weaponSnapshot(stack, holder);
            }
        }
        return Optional.empty();
    }
}
```

```java
// GunDataBridge.java
public final class GunDataBridge {
    private static volatile GunDataService service = new GunDataService(new GunPlatformRegistry());

    private GunDataBridge() {
    }

    public static void install(GunDataService newService) {
        service = Objects.requireNonNull(newService, "newService");
    }

    public static Optional<GunWeaponSnapshot> snapshot(ItemStack stack, @Nullable LivingEntity holder) {
        return service.resolveWeaponSnapshot(stack, holder);
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `bash ./gradlew test --tests com.pickaid.passiveintegration.integration.gun.GunPlatformAdapterContractTest --tests com.pickaid.passiveintegration.integration.gun.GunDataServiceTest --console=plain`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add \
  src/main/java/com/pickaid/passiveintegration/integration/gun/GunPlatformId.java \
  src/main/java/com/pickaid/passiveintegration/integration/gun/GunWeaponSnapshot.java \
  src/main/java/com/pickaid/passiveintegration/integration/gun/GunHitContext.java \
  src/main/java/com/pickaid/passiveintegration/integration/gun/GunPlatformAdapter.java \
  src/main/java/com/pickaid/passiveintegration/integration/gun/GunPlatformRegistry.java \
  src/main/java/com/pickaid/passiveintegration/service/gun/GunDataService.java \
  src/main/java/com/pickaid/passiveintegration/service/gun/GunDataBridge.java \
  src/test/java/com/pickaid/passiveintegration/integration/gun/GunPlatformAdapterContractTest.java \
  src/test/java/com/pickaid/passiveintegration/integration/gun/GunDataServiceTest.java
git commit -m "feat: add passiveintegration gun bridge core"
```

### Task 3: Implement TacZ Adapter, Helper, and View

**Files:**
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/tacz/TaczGunPlatformAdapter.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/helper/TaczGunHelper.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/helper/view/TaczGunView.java`
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/tacz/TaczGunPlatformAdapterTest.java`
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/helper/TaczGunHelperTest.java`
- Test: `src/test/java/com/pickaid/passiveintegration/integration/gun/tacz/TaczGunPlatformAdapterTest.java`
- Test: `src/test/java/com/pickaid/passiveintegration/integration/gun/helper/TaczGunHelperTest.java`

- [ ] **Step 1: Write the failing TacZ tests**

```java
@Test
void buildSnapshotPreservesDeepTaczFields() throws Exception {
    GunData gunData = allocateWithoutConstructor(GunData.class);
    BulletData bulletData = allocateWithoutConstructor(BulletData.class);
    BurstData burstData = allocateWithoutConstructor(BurstData.class);
    GunReloadData reloadData = allocateWithoutConstructor(GunReloadData.class);

    setField(gunData, "ammoId", ResourceLocation.parse("tacz:rifle_round"));
    setField(gunData, "bulletData", bulletData);
    setField(gunData, "burstData", burstData);
    setField(gunData, "reloadData", reloadData);
    setField(gunData, "fireModeSet", List.of(FireMode.SEMI, FireMode.BURST));

    GunWeaponSnapshot snapshot = TaczGunPlatformAdapter.buildSnapshot(
            testGun(FireMode.BURST, 18, 30, true, 760, 0.35F, false),
            ResourceLocation.parse("tacz:ak_alpha"),
            "rifle",
            gunData,
            new ItemStack(Items.CROSSBOW)
    );

    assertEquals("burst", snapshot.fireMode());
    assertEquals(List.of("semi", "burst"), snapshot.fireModes());
}
```

```java
@Test
void helperReturnsStableViewWithoutLeakingTaczClasses() {
    assertNotNull(TaczGunHelper.INSTANCE);
    assertFalse(TaczGunHelper.INSTANCE.loaded() && TaczGunHelper.INSTANCE.view(ItemStack.EMPTY, null).isPresent());
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `bash ./gradlew test --tests com.pickaid.passiveintegration.integration.gun.tacz.TaczGunPlatformAdapterTest --tests com.pickaid.passiveintegration.integration.gun.helper.TaczGunHelperTest --console=plain`
Expected: FAIL because TacZ adapter, helper, and view classes do not exist yet.

- [ ] **Step 3: Write the minimal TacZ implementation**

```java
// TaczGunPlatformAdapter.java
public final class TaczGunPlatformAdapter implements GunPlatformAdapter {
    @Override
    public GunPlatformId platformId() {
        return GunPlatformId.TACZ;
    }

    @Override
    public boolean matchesWeapon(ItemStack stack) {
        return stack != null && !stack.isEmpty() && IGun.getIGunOrNull(stack) != null;
    }

    @Override
    public Optional<GunWeaponSnapshot> weaponSnapshot(ItemStack stack, @Nullable LivingEntity holder) {
        if (!matchesWeapon(stack)) {
            return Optional.empty();
        }
        IGun gun = IGun.getIGunOrNull(stack);
        CommonGunIndex gunIndex = Optional.ofNullable(gun.getGunId(stack)).flatMap(TimelessAPI::getCommonGunIndex).orElse(null);
        return Optional.of(buildSnapshot(gun, gun.getGunId(stack), gunIndex == null ? null : gunIndex.getType(), gunIndex == null ? null : gunIndex.getGunData(), stack));
    }
}
```

```java
// TaczGunHelper.java
public final class TaczGunHelper {
    public static final TaczGunHelper INSTANCE = new TaczGunHelper();
    private static final TaczGunPlatformAdapter ADAPTER = new TaczGunPlatformAdapter();

    public boolean loaded() {
        return ModList.get().isLoaded("tacz");
    }

    public Optional<GunWeaponSnapshot> snapshot(ItemStack stack, @Nullable LivingEntity holder) {
        return ADAPTER.weaponSnapshot(stack, holder);
    }

    public Optional<TaczGunView> view(ItemStack stack, @Nullable LivingEntity holder) {
        return snapshot(stack, holder).map(TaczGunView::new);
    }
}
```

```java
// TaczGunView.java
public final class TaczGunView {
    private final GunWeaponSnapshot snapshot;

    public TaczGunView(GunWeaponSnapshot snapshot) {
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
    }

    public GunWeaponSnapshot snapshot() {
        return snapshot;
    }

    public List<String> supportedAttachments() {
        return snapshot.stringList("raw.supportedAttachments");
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `bash ./gradlew test --tests com.pickaid.passiveintegration.integration.gun.tacz.TaczGunPlatformAdapterTest --tests com.pickaid.passiveintegration.integration.gun.helper.TaczGunHelperTest --console=plain`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add \
  src/main/java/com/pickaid/passiveintegration/integration/gun/tacz/TaczGunPlatformAdapter.java \
  src/main/java/com/pickaid/passiveintegration/integration/gun/helper/TaczGunHelper.java \
  src/main/java/com/pickaid/passiveintegration/integration/gun/helper/view/TaczGunView.java \
  src/test/java/com/pickaid/passiveintegration/integration/gun/tacz/TaczGunPlatformAdapterTest.java \
  src/test/java/com/pickaid/passiveintegration/integration/gun/helper/TaczGunHelperTest.java
git commit -m "feat: add tacz gun helper view"
```

### Task 4: Implement Vanilla Animated Guns Adapter, Helper, and View

**Files:**
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/vanillaanimatedguns/VanillaAnimatedGunsPlatformAdapter.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/helper/VanillaAnimatedGunsHelper.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/helper/view/VanillaAnimatedGunsView.java`
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/vanillaanimatedguns/VanillaAnimatedGunsPlatformAdapterTest.java`
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/helper/VanillaAnimatedGunsHelperTest.java`
- Test: `src/test/java/com/pickaid/passiveintegration/integration/gun/vanillaanimatedguns/VanillaAnimatedGunsPlatformAdapterTest.java`
- Test: `src/test/java/com/pickaid/passiveintegration/integration/gun/helper/VanillaAnimatedGunsHelperTest.java`

- [ ] **Step 1: Write the failing VAG tests**

```java
@Test
void buildSnapshotExposesRuntimeAndAttachmentFields() throws Exception {
    Gun gun = new Gun();
    Gun.General general = new Gun.General();
    Gun.Projectile projectile = new Gun.Projectile();

    setField(general, "gripType", GripType.ONE_HANDED);
    setField(general, "rate", 540);
    setField(general, "maxAmmo", 24);
    setField(projectile, "item", ResourceLocation.parse("minecraft:arrow"));
    setField(projectile, "damage", 6.5F);

    GunWeaponSnapshot snapshot = VanillaAnimatedGunsPlatformAdapter.buildSnapshot(gun, new ItemStack(Items.CROSSBOW));
    assertEquals("one_handed", snapshot.weaponTypeKey());
}
```

```java
@Test
void helperViewSurfacesGripTypeAndAmmoData() {
    assertNotNull(VanillaAnimatedGunsHelper.INSTANCE);
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `bash ./gradlew test --tests com.pickaid.passiveintegration.integration.gun.vanillaanimatedguns.VanillaAnimatedGunsPlatformAdapterTest --tests com.pickaid.passiveintegration.integration.gun.helper.VanillaAnimatedGunsHelperTest --console=plain`
Expected: FAIL because the VAG adapter, helper, and view do not exist yet.

- [ ] **Step 3: Write the minimal VAG implementation**

```java
public final class VanillaAnimatedGunsPlatformAdapter implements GunPlatformAdapter {
    @Override
    public GunPlatformId platformId() {
        return GunPlatformId.VANILLA_ANIMATED_GUNS;
    }

    @Override
    public boolean matchesWeapon(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof GunItem;
    }

    static GunWeaponSnapshot buildSnapshot(@Nullable Gun gun, ItemStack stack) {
        // Build normalized snapshot and hand-written raw.general/raw.projectile/raw.modules JSON.
    }
}
```

```java
public final class VanillaAnimatedGunsHelper {
    public static final VanillaAnimatedGunsHelper INSTANCE = new VanillaAnimatedGunsHelper();
    private static final VanillaAnimatedGunsPlatformAdapter ADAPTER = new VanillaAnimatedGunsPlatformAdapter();

    public boolean loaded() {
        return ModList.get().isLoaded("vag");
    }

    public Optional<VanillaAnimatedGunsView> view(ItemStack stack, @Nullable LivingEntity holder) {
        return ADAPTER.weaponSnapshot(stack, holder).map(VanillaAnimatedGunsView::new);
    }
}
```

```java
public final class VanillaAnimatedGunsView {
    private final GunWeaponSnapshot snapshot;

    public String gripType() {
        return snapshot.stringData("raw.general.gripType");
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `bash ./gradlew test --tests com.pickaid.passiveintegration.integration.gun.vanillaanimatedguns.VanillaAnimatedGunsPlatformAdapterTest --tests com.pickaid.passiveintegration.integration.gun.helper.VanillaAnimatedGunsHelperTest --console=plain`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add \
  src/main/java/com/pickaid/passiveintegration/integration/gun/vanillaanimatedguns/VanillaAnimatedGunsPlatformAdapter.java \
  src/main/java/com/pickaid/passiveintegration/integration/gun/helper/VanillaAnimatedGunsHelper.java \
  src/main/java/com/pickaid/passiveintegration/integration/gun/helper/view/VanillaAnimatedGunsView.java \
  src/test/java/com/pickaid/passiveintegration/integration/gun/vanillaanimatedguns/VanillaAnimatedGunsPlatformAdapterTest.java \
  src/test/java/com/pickaid/passiveintegration/integration/gun/helper/VanillaAnimatedGunsHelperTest.java
git commit -m "feat: add vanilla animated guns helper view"
```

### Task 5: Implement Scorched Guns 2 Adapter, Helper, and View

**Files:**
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/scorchedguns2/ScorchedGuns2PlatformAdapter.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/helper/ScorchedGuns2Helper.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/helper/view/ScorchedGuns2View.java`
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/scorchedguns2/ScorchedGuns2PlatformAdapterTest.java`
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/helper/ScorchedGuns2HelperTest.java`
- Test: `src/test/java/com/pickaid/passiveintegration/integration/gun/scorchedguns2/ScorchedGuns2PlatformAdapterTest.java`
- Test: `src/test/java/com/pickaid/passiveintegration/integration/gun/helper/ScorchedGuns2HelperTest.java`

- [ ] **Step 1: Write the failing Scorched Guns 2 tests**

```java
@Test
void buildSnapshotPreservesScorchedGunDepth() throws Exception {
    Gun gun = new Gun();
    Gun.General general = new Gun.General();
    Gun.Reloads reloads = new Gun.Reloads();

    setField(general, "weaponType", Gun.WeaponType.rifle);
    setField(general, "fireMode", FireMode.BURST);
    setField(reloads, "reloadType", ReloadType.MAG_FED);
    setField(reloads, "maxAmmo", 36);

    GunWeaponSnapshot snapshot = ScorchedGuns2PlatformAdapter.buildSnapshot(gun, new ItemStack(Items.CROSSBOW));
    assertEquals("burst", snapshot.fireMode());
    assertEquals("mag_fed", snapshot.reloadType());
}
```

```java
@Test
void helperViewExposesReloadTypeAndBurstCount() {
    assertNotNull(ScorchedGuns2Helper.INSTANCE);
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `bash ./gradlew test --tests com.pickaid.passiveintegration.integration.gun.scorchedguns2.ScorchedGuns2PlatformAdapterTest --tests com.pickaid.passiveintegration.integration.gun.helper.ScorchedGuns2HelperTest --console=plain`
Expected: FAIL because the Scorched Guns 2 classes do not exist yet.

- [ ] **Step 3: Write the minimal Scorched Guns 2 implementation**

```java
public final class ScorchedGuns2PlatformAdapter implements GunPlatformAdapter {
    @Override
    public GunPlatformId platformId() {
        return GunPlatformId.SCORCHED_GUNS_2;
    }

    @Override
    public boolean matchesWeapon(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof GunItem;
    }

    static GunWeaponSnapshot buildSnapshot(@Nullable Gun gun, ItemStack stack) {
        // Build normalized snapshot and hand-written raw.general/raw.reloads/raw.projectile/raw.modules JSON.
    }
}
```

```java
public final class ScorchedGuns2Helper {
    public static final ScorchedGuns2Helper INSTANCE = new ScorchedGuns2Helper();

    public boolean loaded() {
        return ModList.get().isLoaded("scguns");
    }
}
```

```java
public final class ScorchedGuns2View {
    private final GunWeaponSnapshot snapshot;

    public int burstCount() {
        Double value = snapshot.numberData("raw.burstCount");
        return value == null ? 0 : value.intValue();
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `bash ./gradlew test --tests com.pickaid.passiveintegration.integration.gun.scorchedguns2.ScorchedGuns2PlatformAdapterTest --tests com.pickaid.passiveintegration.integration.gun.helper.ScorchedGuns2HelperTest --console=plain`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add \
  src/main/java/com/pickaid/passiveintegration/integration/gun/scorchedguns2/ScorchedGuns2PlatformAdapter.java \
  src/main/java/com/pickaid/passiveintegration/integration/gun/helper/ScorchedGuns2Helper.java \
  src/main/java/com/pickaid/passiveintegration/integration/gun/helper/view/ScorchedGuns2View.java \
  src/test/java/com/pickaid/passiveintegration/integration/gun/scorchedguns2/ScorchedGuns2PlatformAdapterTest.java \
  src/test/java/com/pickaid/passiveintegration/integration/gun/helper/ScorchedGuns2HelperTest.java
git commit -m "feat: add scorched guns 2 helper view"
```

### Task 6: Implement Just Enough Guns Adapter, Helper, and View

**Files:**
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/justenoughguns/JustEnoughGunsPlatformAdapter.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/helper/JustEnoughGunsHelper.java`
- Create: `src/main/java/com/pickaid/passiveintegration/integration/gun/helper/view/JustEnoughGunsView.java`
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/justenoughguns/JustEnoughGunsPlatformAdapterTest.java`
- Create: `src/test/java/com/pickaid/passiveintegration/integration/gun/helper/JustEnoughGunsHelperTest.java`
- Test: `src/test/java/com/pickaid/passiveintegration/integration/gun/justenoughguns/JustEnoughGunsPlatformAdapterTest.java`
- Test: `src/test/java/com/pickaid/passiveintegration/integration/gun/helper/JustEnoughGunsHelperTest.java`

- [ ] **Step 1: Write the failing JEG tests**

```java
@Test
void buildSnapshotPrefersGunIdAndKeepsDeepRuntimeFlags() throws Exception {
    Gun gun = new Gun();
    Gun.General general = new Gun.General();
    Gun.Reloads reloads = new Gun.Reloads();

    setField(general, "gripType", GripType.TWO_HANDED);
    setField(general, "fireMode", FireMode.BURST);
    setField(reloads, "reloadType", ReloadType.INVENTORY_FED);

    ItemStack stack = new ItemStack(Items.CROSSBOW);
    stack.getOrCreateTag().putString("GunId", "jeg:storm_rifle");
    stack.getOrCreateTag().putBoolean("IsShooting", true);

    GunWeaponSnapshot snapshot = JustEnoughGunsPlatformAdapter.buildSnapshot(gun, stack);
    assertEquals("jeg:storm_rifle", snapshot.weaponId());
    assertTrue(snapshot.shooting());
}
```

```java
@Test
void helperViewExposesGunIdAndChargeProgress() {
    assertNotNull(JustEnoughGunsHelper.INSTANCE);
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `bash ./gradlew test --tests com.pickaid.passiveintegration.integration.gun.justenoughguns.JustEnoughGunsPlatformAdapterTest --tests com.pickaid.passiveintegration.integration.gun.helper.JustEnoughGunsHelperTest --console=plain`
Expected: FAIL because the JEG classes do not exist yet.

- [ ] **Step 3: Write the minimal JEG implementation**

```java
public final class JustEnoughGunsPlatformAdapter implements GunPlatformAdapter {
    @Override
    public GunPlatformId platformId() {
        return GunPlatformId.JUST_ENOUGH_GUNS;
    }

    @Override
    public boolean matchesWeapon(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof GunItem;
    }

    static GunWeaponSnapshot buildSnapshot(@Nullable Gun gun, ItemStack stack) {
        // Build normalized snapshot and hand-written raw.general/raw.reloads/raw.projectile/raw.modules JSON.
    }
}
```

```java
public final class JustEnoughGunsHelper {
    public static final JustEnoughGunsHelper INSTANCE = new JustEnoughGunsHelper();

    public boolean loaded() {
        return ModList.get().isLoaded("jeg");
    }
}
```

```java
public final class JustEnoughGunsView {
    private final GunWeaponSnapshot snapshot;

    public double chargeProgress() {
        Double value = snapshot.numberData("runtime.chargeProgress");
        return value == null ? 0.0D : value;
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `bash ./gradlew test --tests com.pickaid.passiveintegration.integration.gun.justenoughguns.JustEnoughGunsPlatformAdapterTest --tests com.pickaid.passiveintegration.integration.gun.helper.JustEnoughGunsHelperTest --console=plain`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add \
  src/main/java/com/pickaid/passiveintegration/integration/gun/justenoughguns/JustEnoughGunsPlatformAdapter.java \
  src/main/java/com/pickaid/passiveintegration/integration/gun/helper/JustEnoughGunsHelper.java \
  src/main/java/com/pickaid/passiveintegration/integration/gun/helper/view/JustEnoughGunsView.java \
  src/test/java/com/pickaid/passiveintegration/integration/gun/justenoughguns/JustEnoughGunsPlatformAdapterTest.java \
  src/test/java/com/pickaid/passiveintegration/integration/gun/helper/JustEnoughGunsHelperTest.java
git commit -m "feat: add just enough guns helper view"
```

### Task 7: Wire Default Registry and PassiveIntegration Bootstrap

**Files:**
- Create: `src/main/java/com/pickaid/passiveintegration/bootstrap/IntegrationBootstrap.java`
- Create: `src/main/java/com/pickaid/passiveintegration/bootstrap/LoadedModSet.java`
- Modify: `src/main/java/com/pickaid/passiveintegration/service/gun/GunDataBridge.java`
- Modify: `src/main/java/com/pickaid/passiveintegration/PassiveIntegration.java`
- Create: `src/test/java/com/pickaid/passiveintegration/bootstrap/IntegrationBootstrapTest.java`
- Test: `src/test/java/com/pickaid/passiveintegration/bootstrap/IntegrationBootstrapTest.java`
- Test: `src/test/java/com/pickaid/passiveintegration/integration/gun/GunDataServiceTest.java`

- [ ] **Step 1: Write the failing bootstrap test**

```java
@Test
void bootstrapInstallsOnlyLoadedHelpersIntoTheBridgeRegistry() {
    LoadedModSet loaded = modId -> Set.of("tacz", "vag", "scguns", "jeg").contains(modId);
    IntegrationBootstrap bootstrap = new IntegrationBootstrap(loaded);

    GunPlatformRegistry registry = bootstrap.createGunRegistry();

    assertEquals(Set.of(
            GunPlatformId.TACZ,
            GunPlatformId.VANILLA_ANIMATED_GUNS,
            GunPlatformId.SCORCHED_GUNS_2,
            GunPlatformId.JUST_ENOUGH_GUNS
    ), registry.adapters().stream().map(GunPlatformAdapter::platformId).collect(Collectors.toSet()));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `bash ./gradlew test --tests com.pickaid.passiveintegration.bootstrap.IntegrationBootstrapTest --console=plain`
Expected: FAIL because `IntegrationBootstrap` and `LoadedModSet` do not exist yet.

- [ ] **Step 3: Write the minimal bootstrap implementation**

```java
// LoadedModSet.java
@FunctionalInterface
public interface LoadedModSet {
    boolean isLoaded(String modId);
}
```

```java
// IntegrationBootstrap.java
public final class IntegrationBootstrap {
    private final LoadedModSet loaded;

    public IntegrationBootstrap(LoadedModSet loaded) {
        this.loaded = Objects.requireNonNull(loaded, "loaded");
    }

    public GunPlatformRegistry createGunRegistry() {
        GunPlatformRegistry registry = new GunPlatformRegistry();
        if (loaded.isLoaded("tacz")) registry.register(new TaczGunPlatformAdapter());
        if (loaded.isLoaded("vag")) registry.register(new VanillaAnimatedGunsPlatformAdapter());
        if (loaded.isLoaded("scguns")) registry.register(new ScorchedGuns2PlatformAdapter());
        if (loaded.isLoaded("jeg")) registry.register(new JustEnoughGunsPlatformAdapter());
        return registry;
    }

    public void installGunBridge() {
        GunDataBridge.install(new GunDataService(createGunRegistry()));
    }
}
```

```java
// PassiveIntegration.java
private void registerCommonRuntime() {
    // existing code...
    new IntegrationBootstrap(modId -> ModList.get().isLoaded(modId)).installGunBridge();
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `bash ./gradlew test --tests com.pickaid.passiveintegration.bootstrap.IntegrationBootstrapTest --tests com.pickaid.passiveintegration.integration.gun.GunDataServiceTest --console=plain`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add \
  src/main/java/com/pickaid/passiveintegration/bootstrap/IntegrationBootstrap.java \
  src/main/java/com/pickaid/passiveintegration/bootstrap/LoadedModSet.java \
  src/main/java/com/pickaid/passiveintegration/service/gun/GunDataBridge.java \
  src/main/java/com/pickaid/passiveintegration/PassiveIntegration.java \
  src/test/java/com/pickaid/passiveintegration/bootstrap/IntegrationBootstrapTest.java
git commit -m "feat: bootstrap passiveintegration gun registry"
```

### Task 8: Expose Gun Bridge and Helpers Through PassiveBurst KJS

**Files:**
- Create: `PassiveBurst/src/main/java/com/pickaid/passiveburst/kubejs/runtime/GunBindingsJS.java`
- Modify: `PassiveBurst/src/main/java/com/pickaid/passiveburst/kubejs/Bindings.java`
- Modify: `PassiveBurst/src/test/java/com/pickaid/passiveburst/kubejs/PublicApiSurfaceTest.java`
- Create: `PassiveBurst/src/test/java/com/pickaid/passiveburst/kubejs/GunBindingsJSTest.java`
- Test: `PassiveBurst/src/test/java/com/pickaid/passiveburst/kubejs/PublicApiSurfaceTest.java`
- Test: `PassiveBurst/src/test/java/com/pickaid/passiveburst/kubejs/GunBindingsJSTest.java`

- [ ] **Step 1: Write the failing KJS binding tests**

```java
@Test
void bindingsExposeBurstAndGunRoots() {
    Set<String> names = Stream.of(Bindings.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(Method::getName)
            .collect(Collectors.toSet());

    assertEquals(Set.of("burst", "gun"), names);
}
```

```java
@Test
void gunBindingFailsSoftlyWhenHelperClassIsMissing() {
    GunBindingsJS bindings = new GunBindingsJS(className -> {
        throw new ClassNotFoundException(className);
    });

    assertNull(bindings.tacz());
    assertNull(bindings.bridge());
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `bash ../gradlew test --tests com.pickaid.passiveburst.kubejs.PublicApiSurfaceTest --tests com.pickaid.passiveburst.kubejs.GunBindingsJSTest --console=plain`
Expected: FAIL because `gun()` and `GunBindingsJS` do not exist yet.

- [ ] **Step 3: Write the minimal KJS binding implementation**

```java
// Bindings.java
public final class Bindings {
    public static final Bindings INSTANCE = new Bindings();
    private static final GunBindingsJS GUN = new GunBindingsJS();

    public BurstViewJS burst(Player player) {
        return player instanceof ServerPlayer serverPlayer ? new BurstViewJS(serverPlayer) : null;
    }

    public GunBindingsJS gun() {
        return GUN;
    }
}
```

```java
// GunBindingsJS.java
public final class GunBindingsJS {
    private final ThrowingClassLoader loader;

    public GunBindingsJS() {
        this(Class::forName);
    }

    GunBindingsJS(ThrowingClassLoader loader) {
        this.loader = loader;
    }

    public Object bridge() {
        return load("com.pickaid.passiveintegration.service.gun.GunDataBridge");
    }

    public Object tacz() {
        return load("com.pickaid.passiveintegration.integration.gun.helper.TaczGunHelper");
    }

    public Object vag() {
        return load("com.pickaid.passiveintegration.integration.gun.helper.VanillaAnimatedGunsHelper");
    }

    public Object scorchedGuns2() {
        return load("com.pickaid.passiveintegration.integration.gun.helper.ScorchedGuns2Helper");
    }

    public Object justEnoughGuns() {
        return load("com.pickaid.passiveintegration.integration.gun.helper.JustEnoughGunsHelper");
    }

    private @Nullable Object load(String className) {
        try {
            return loader.load(className);
        } catch (ClassNotFoundException exception) {
            return null;
        }
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `bash ../gradlew clean test --tests com.pickaid.passiveburst.kubejs.PublicApiSurfaceTest --tests com.pickaid.passiveburst.kubejs.GunBindingsJSTest --console=plain`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add \
  PassiveBurst/src/main/java/com/pickaid/passiveburst/kubejs/runtime/GunBindingsJS.java \
  PassiveBurst/src/main/java/com/pickaid/passiveburst/kubejs/Bindings.java \
  PassiveBurst/src/test/java/com/pickaid/passiveburst/kubejs/PublicApiSurfaceTest.java \
  PassiveBurst/src/test/java/com/pickaid/passiveburst/kubejs/GunBindingsJSTest.java
git commit -m "feat: expose gun bridge helpers to passiveburst kubejs"
```

## Verification Matrix

- Root targeted verification:
  - `bash ./gradlew test --tests com.pickaid.passiveintegration.PassiveIntegrationSourceLayoutTest --tests com.pickaid.passiveintegration.integration.gun.GunPlatformAdapterContractTest --tests com.pickaid.passiveintegration.integration.gun.GunDataServiceTest --tests com.pickaid.passiveintegration.bootstrap.IntegrationBootstrapTest --tests com.pickaid.passiveintegration.integration.gun.tacz.TaczGunPlatformAdapterTest --tests com.pickaid.passiveintegration.integration.gun.vanillaanimatedguns.VanillaAnimatedGunsPlatformAdapterTest --tests com.pickaid.passiveintegration.integration.gun.scorchedguns2.ScorchedGuns2PlatformAdapterTest --tests com.pickaid.passiveintegration.integration.gun.justenoughguns.JustEnoughGunsPlatformAdapterTest --console=plain`
- PassiveBurst targeted verification:
  - `bash ../gradlew clean test --tests com.pickaid.passiveburst.kubejs.PublicApiSurfaceTest --tests com.pickaid.passiveburst.kubejs.GunBindingsJSTest --tests com.pickaid.passiveburst.consumer.gun.GunBurstRuntimeTest --console=plain`
- PassiveBurst final safety run:
  - `bash ../gradlew clean test --console=plain`
  - Use `clean` because stale duplicate test output under `PassiveBurst/build/classes/java/test` can produce false negative executor failures.

## Spec Coverage Check

- Unified bridge API: covered by Tasks 2 and 7.
- Per-platform helpers and views: covered by Tasks 3 through 6.
- PassiveBurst dynamic import path: covered by Task 8.
- Boundary rules and no gun KJS in `PassiveIntegration`: covered by Task 1.
- Stable normalized snapshots and platform-specific deep data: covered by Tasks 2 through 6.

## Placeholder Scan

- No deferred markers remain in this plan.
- Every task names exact files, exact tests, and exact commands.

## Type Consistency Check

- Bridge class name is consistently `GunDataBridge`.
- Helper class names remain `TaczGunHelper`, `VanillaAnimatedGunsHelper`, `ScorchedGuns2Helper`, and `JustEnoughGunsHelper`.
- PassiveBurst KJS root method remains `gun()`.
