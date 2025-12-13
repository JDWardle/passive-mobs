package com.jdwardle.passivemobs;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class PlayerSettings implements ValueIOSerializable {
    private static final String AGGRESSION_LEVEL_KEY = "aggressionLevel";
    private static final String VERSION_KEY = "version";

    // Allows for migrating the data model of PlayerSettings to a different
    // version if needed.
    private static final String CURRENT_VERSION = "1.0.0";

    private AggressionLevel aggressionLevel;
    private String version;

    PlayerSettings() {
        this.aggressionLevel = AggressionLevel.getLevel(Config.DEFAULT_AGGRESSION_LEVEL.get());
    }

    public AggressionLevel getAggressionLevel() {
        return aggressionLevel;
    }

    public void setAggressionLevel(AggressionLevel aggressionLevel) {
        this.aggressionLevel = aggressionLevel;
    }

    public String getVersion() {
        return version;
    }

    public void serialize(ValueOutput valueOutput) {
        valueOutput.putString(AGGRESSION_LEVEL_KEY, aggressionLevel.toString());
        valueOutput.putString(VERSION_KEY, version);
    }

    public void deserialize(ValueInput valueInput) {
        aggressionLevel = AggressionLevel.getLevel(valueInput.getStringOr(AGGRESSION_LEVEL_KEY, Config.DEFAULT_AGGRESSION_LEVEL.get()));
        version = valueInput.getStringOr(VERSION_KEY, CURRENT_VERSION);
    }
}
