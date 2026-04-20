package com.pickaid.passiveintegration;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

class BurstBoundaryTest {
    @Test
    void activeTreeNoLongerContainsAmmoburstPackage() {
        assertFalse(Files.exists(Path.of("src/main/java/com/pickaid/passiveintegration/events/ammoburst")));
    }
}
