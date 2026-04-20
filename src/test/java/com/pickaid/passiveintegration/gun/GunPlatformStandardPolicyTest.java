package com.pickaid.passiveintegration.gun;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class GunPlatformStandardPolicyTest {
    @Test
    void passiveIntegrationKeepsPstCompatAndProgressionOutOfGunCore() throws IOException {
        Path root = projectRoot();
        String entrySource = Files.readString(root.resolve("src/main/java/com/pickaid/passiveintegration/PassiveIntegration.java"));
        String bootstrapSource = Files.readString(root.resolve(
                "src/main/java/com/pickaid/passiveintegration/bootstrap/PassiveIntegrationBootstrap.java"));
        String carrierRegistrySource = Files.readString(root.resolve(
                "src/main/java/com/pickaid/passiveintegration/compat/carrier/CarrierRegistry.java"));
        String optionalGatesSource = Files.readString(root.resolve(
                "src/main/java/com/pickaid/passiveintegration/optional/CarrierGates.java"));
        String cgmCatalogSource = Files.readString(root.resolve(
                "src/main/java/com/pickaid/passiveintegration/compat/cgm/CgmCarrierCatalog.java"));

        assertTrue(entrySource.contains("import com.pickaid.passiveintegration.bootstrap.PassiveIntegrationBootstrap;"));
        assertTrue(entrySource.contains("PassiveIntegrationBootstrap.init(modBus);"));
        assertTrue(bootstrapSource.contains("import com.pickaid.passiveintegration.bridge.carrier.CarrierDebugBridge;"));
        assertTrue(bootstrapSource.contains("import com.pickaid.passiveintegration.compat.carrier.CarrierRegistry;"));
        assertTrue(bootstrapSource.contains("import com.pickaid.passiveintegration.compat.carrier.binding.CarrierBinders;"));
        assertTrue(bootstrapSource.contains("import com.pickaid.passiveintegration.compat.cgm.CgmCarrierCatalog;"));
        assertTrue(bootstrapSource.contains("CarrierBinders binders = new CarrierBinders"));
        assertTrue(bootstrapSource.contains("carrierRegistry = new CarrierRegistry(binders);"));
        assertTrue(bootstrapSource.contains("carrierRegistry.bindAll(List.<CarrierCatalog>of(new CgmCarrierCatalog()));"));
        assertTrue(bootstrapSource.contains("carrierDebugBridge = new CarrierDebugBridge(carrierRegistry);"));
        assertTrue(carrierRegistrySource.contains("CarrierGateResult gateResult = entry.gate().evaluate();"));
        assertTrue(optionalGatesSource.contains("return () -> ModList.get().isLoaded(modId)"));
        assertTrue(cgmCatalogSource.contains("CarrierGates.modLoaded(\"cgm\")"));
        assertTrue(cgmCatalogSource.contains("CarrierGates.classPresent(\"com.mrcrayfish.guns.entity.ThrowableStunGrenadeEntity\")"));

        assertFalse(entrySource.contains("com.pickaid.guncore"));
        assertFalse(bootstrapSource.contains("com.pickaid.guncore"));
        assertFalse(carrierRegistrySource.contains("com.pickaid.guncore"));
        assertFalse(optionalGatesSource.contains("com.pickaid.guncore"));
        assertFalse(cgmCatalogSource.contains("com.pickaid.guncore"));

        assertTrue(Files.exists(root.resolve("src/main/java/com/pickaid/passiveintegration/compat/carrier/CarrierRegistry.java")));
        assertTrue(Files.exists(root.resolve("src/main/java/com/pickaid/passiveintegration/optional/CarrierGates.java")));

        Path mainPackageRoot = root.resolve("src/main/java/com/pickaid/passiveintegration");
        Set<String> topLevelPackages;
        try (Stream<Path> stream = Files.list(mainPackageRoot)) {
            topLevelPackages = stream
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toSet());
        }
        assertTrue(topLevelPackages.containsAll(Set.of("bootstrap", "bridge", "compat", "config", "mixins", "optional")),
                "PassiveIntegration should keep carrier/bootstrap ownership packages for Task 1");

        for (String forbiddenPath : List.of(
                "src/main/java/com/pickaid/passiveintegration/compat/gun",
                "src/main/java/com/pickaid/passiveintegration/bridge/gun",
                "src/main/java/com/pickaid/passiveintegration/helper/gun",
                "src/main/java/com/pickaid/passiveintegration/optional/gun",
                "src/test/java/com/pickaid/passiveintegration/bridge/gun",
                "src/test/java/com/pickaid/passiveintegration/compat/gun",
                "src/test/java/com/pickaid/passiveintegration/testsupport/gun",
                "src/main/java/com/pickaid/guncore")) {
            assertFalse(Files.exists(root.resolve(forbiddenPath)), forbiddenPath + " should be absent");
        }
    }

    private static Path projectRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        for (Path candidate = current; candidate != null; candidate = candidate.getParent()) {
            if (hasGradleProjectMarker(candidate) && Files.exists(candidate.resolve("src/main/java/com/pickaid/passiveintegration"))) {
                return candidate;
            }
        }
        fail("Could not locate PassiveIntegration project root from " + current);
        return current;
    }

    private static boolean hasGradleProjectMarker(Path candidate) {
        return Files.exists(candidate.resolve("build.gradle"))
                || Files.exists(candidate.resolve("build.gradle.kts"))
                || Files.exists(candidate.resolve("settings.gradle"))
                || Files.exists(candidate.resolve("settings.gradle.kts"));
    }
}
