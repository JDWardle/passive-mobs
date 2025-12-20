package com.jdwardle.passivemobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(EphemeralTestServerProvider.class)
class PlayerSettingsTest {
    @Test
    public void serialize(MinecraftServer server) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, server.registries().compositeAccess());

        PlayerSettings settings = new PlayerSettings();

        assertTrue(output.isEmpty());
        settings.serialize(output);
        assertFalse(output.isEmpty());
        // TODO: Figure out how to actually look at the output.
    }

    @Test
    void deserialize(MinecraftServer server) {
        CompoundTag tag = new CompoundTag();
        tag.putString(PlayerSettings.AGGRESSION_LEVEL_KEY, "normal");
        tag.putString(PlayerSettings.VERSION_KEY, "1234");

        ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, server.registryAccess(), tag);

        PlayerSettings settings = new PlayerSettings();

        Assertions.assertEquals(AggressionLevel.PASSIVE, settings.getAggressionLevel());
        Assertions.assertEquals(PlayerSettings.CURRENT_VERSION, settings.getVersion());

        settings.deserialize(input);

        Assertions.assertEquals(AggressionLevel.NORMAL, settings.getAggressionLevel());
        Assertions.assertEquals("1234", settings.getVersion());
    }
}