package com.pickaid.passiveintegration.compat.cgm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pickaid.passiveintegration.compat.cgm.bonus.CgmFlashGrenadeDurationAppliedBonus;
import com.pickaid.passiveintegration.compat.cgm.bonus.CgmStunGrenadeDurationTakenBonus;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CgmCarrierDatapackFixtureTest {
    @Test
    void datapackFixtureUsesPassiveIntegrationBonusTypesAndSkillTreeShape() {
        JsonObject stunSkill = read("/data/passiveintegration_test/skills/cgm_stun_duration_taken.json");
        JsonObject flashSkill = read("/data/passiveintegration_test/skills/cgm_flash_duration_applied.json");
        JsonObject tree = read("/data/passiveintegration_test/skill_trees/cgm_grenade_control.json");

        assertEquals("passiveintegration_test:cgm_stun_duration_taken", stunSkill.get("id").getAsString());
        assertEquals("passiveintegration:cgm_stun_grenade_duration_taken_reduction",
                stunSkill.getAsJsonArray("bonuses").get(0).getAsJsonObject().get("type").getAsString());
        assertEquals("passiveintegration:cgm_flash_grenade_duration_applied_bonus",
                flashSkill.getAsJsonArray("bonuses").get(0).getAsJsonObject().get("type").getAsString());
        assertTrue(tree.getAsJsonArray("skillIds").size() >= 2);

        CgmStunGrenadeDurationTakenBonus takenBonus = new CgmStunGrenadeDurationTakenBonus.Serializer()
                .deserialize(stunSkill.getAsJsonArray("bonuses").get(0).getAsJsonObject());
        CgmFlashGrenadeDurationAppliedBonus appliedBonus = new CgmFlashGrenadeDurationAppliedBonus.Serializer()
                .deserialize(flashSkill.getAsJsonArray("bonuses").get(0).getAsJsonObject());

        assertEquals(1.0D, takenBonus.levels(), 0.0001D);
        assertEquals(1.0D, appliedBonus.levels(), 0.0001D);
    }

    private static JsonObject read(String path) {
        try (InputStreamReader reader = new InputStreamReader(
                CgmCarrierDatapackFixtureTest.class.getResourceAsStream(path),
                StandardCharsets.UTF_8
        )) {
            return new JsonParser().parse(reader).getAsJsonObject();
        } catch (Exception e) {
            throw new AssertionError("Failed to read fixture " + path, e);
        }
    }
}
