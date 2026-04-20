package com.pickaid.passiveintegration.mixins;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PassiveIntegrationMixinBoundaryTest {
    @Test
    void mixinConfigKeepsOnlyExplicitCompatMixinsAndNoLegacySkilltreeOwnership() throws IOException {
        Path projectRoot = Path.of("").toAbsolutePath();
        Path mixinConfig = projectRoot.resolve("src/main/resources/mixins.passiveintegration.json");
        Path serverSkilltreeMixins = projectRoot.resolve("src/main/java/com/pickaid/passiveintegration/mixins/skilltree");
        Path clientSkilltreeMixins = projectRoot.resolve("src/main/java/com/pickaid/passiveintegration/mixins/client/skilltree");
        JsonObject mixinConfigJson = new JsonParser().parse(Files.readString(mixinConfig)).getAsJsonObject();
        JsonArray mixins = mixinConfigJson.getAsJsonArray("mixins");
        JsonArray clientMixins = mixinConfigJson.getAsJsonArray("client");
        JsonArray serverMixins = mixinConfigJson.getAsJsonArray("server");

        assertEquals("com.pickaid.passiveintegration.mixins", mixinConfigJson.get("package").getAsString());
        assertEquals(1, mixins.size());
        assertEquals("compat.cgm.ThrowableStunGrenadeEntityMixin", mixins.get(0).getAsString());
        assertEquals(0, clientMixins.size());
        assertEquals(0, serverMixins.size());
        assertFalse(Files.exists(serverSkilltreeMixins));
        assertFalse(Files.exists(clientSkilltreeMixins));
    }
}
