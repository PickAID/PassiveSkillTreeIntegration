package com.pickaid.passiveintegration.gun;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GunCoreDependencyLayoutTest {
    @Test
    void buildAndModsMetadataRequireGunCore() throws IOException {
        Path projectRoot = Path.of("").toAbsolutePath();
        String buildGradleText = Files.readString(projectRoot.resolve("build.gradle"));
        String modsTomlText = Files.readString(projectRoot.resolve("src/main/resources/META-INF/mods.toml"));

        assertTrue(buildGradleText.contains("resolveGunCoreJar()"));
        assertTrue(buildGradleText.contains("GUN_CORE_JAR"));
        assertTrue(buildGradleText.contains("../GunCore/build/libs"));
        assertTrue(buildGradleText.contains("compileOnly fg.deobf(gunCoreDependency)"));
        assertTrue(buildGradleText.contains("runtimeOnly fg.deobf(gunCoreDependency)"));

        assertTrue(modsTomlText.contains("modId=\"guncore\""));
        assertTrue(modsTomlText.contains("mandatory=true"));
        assertTrue(modsTomlText.contains("versionRange=\"[0.1.0,)\""));
    }
}
