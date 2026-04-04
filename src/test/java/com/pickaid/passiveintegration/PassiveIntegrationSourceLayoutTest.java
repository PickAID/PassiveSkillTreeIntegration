package com.pickaid.passiveintegration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PassiveIntegrationSourceLayoutTest {
    @Test
    void keepsStableTaskOneStructure() throws IOException {
        Path projectRoot = Path.of("").toAbsolutePath();
        Path movedEntrypoint = projectRoot.resolve("src/main/java/com/pickaid/passiveintegration/PassiveIntegration.java");
        Path oldEntrypoint = projectRoot.resolve("src/main/java/org/crychicteam/passiveintegration/PassiveIntegration.java");
        Path removedMixin = projectRoot.resolve("src/main/java/org/crychicteam/passiveintegration/mixins/conditions/EnchantedConditionMixin.java");
        Path mixinConfig = projectRoot.resolve("src/main/resources/mixins.passiveintegration.json");
        JsonObject mixinConfigJson = new JsonParser().parse(Files.readString(mixinConfig)).getAsJsonObject();
        JsonArray mixins = mixinConfigJson.getAsJsonArray("mixins");

        assertTrue(Files.exists(movedEntrypoint));
        assertFalse(Files.exists(oldEntrypoint));
        assertFalse(Files.exists(removedMixin));
        assertTrue(PassiveIntegration.class.getPackageName().equals("com.pickaid.passiveintegration"));
        assertFalse(containsString(mixins, "conditions.EnchantedConditionMixin"));
    }

    private static boolean containsString(JsonArray array, String expected) {
        for (int i = 0; i < array.size(); i++) {
            if (expected.equals(array.get(i).getAsString())) {
                return true;
            }
        }
        return false;
    }
}
