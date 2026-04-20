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
        assertTrue(buildGradleText.contains("providers.gradleProperty(\"gunCoreJar\")"));
        assertTrue(buildGradleText.contains("System.getenv(\"GUN_CORE_JAR\")"));
        assertTrue(buildGradleText.contains("file(\"libs\")"));
        assertTrue(buildGradleText.contains("file(\"../GunCore/build/libs\")"));
        assertTrue(buildGradleText.contains("file(\"../libs\")"));
        assertTrue(buildGradleText.contains("file(\"libraries/${config.mc_version}/all\")"));
        assertTrue(buildGradleText.contains("!candidate.name.endsWith(\"-sources.jar\")"));
        assertTrue(buildGradleText.contains("Multiple GunCore jars found"));
        assertTrue(buildGradleText.contains("-PgunCoreJar"));
        assertTrue(buildGradleText.contains("GUN_CORE_JAR"));
        assertTrue(buildGradleText.contains("compileOnly fg.deobf(gunCoreDependency)"));
        assertTrue(buildGradleText.contains("runtimeOnly fg.deobf(gunCoreDependency)"));

        assertTrue(modsTomlText.contains("modId=\"guncore\""));
        assertTrue(modsTomlText.contains("mandatory=true"));
        assertTrue(modsTomlText.contains("versionRange=\"[0.1.0,)\""));
    }
}
