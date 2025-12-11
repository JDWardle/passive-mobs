package com.jdwardle.passivemobs;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class PlayerSettings implements ValueIOSerializable {
    private AggressionLevel aggressionLevel;

    PlayerSettings() {
        this.aggressionLevel = AggressionLevel.getLevel(Config.DEFAULT_AGGRESSION_LEVEL.get());
    }

    public AggressionLevel getAggressionLevel() {
        return aggressionLevel;
    }

    public void setAggressionLevel(AggressionLevel aggressionLevel) {
        this.aggressionLevel = aggressionLevel;
    }


    public void serialize(ValueOutput valueOutput) {
        valueOutput.putString("aggressionLevel", aggressionLevel.toString());
    }

    public void deserialize(ValueInput valueInput) {
        aggressionLevel = AggressionLevel.getLevel(valueInput.getStringOr("aggressionLevel", Config.DEFAULT_AGGRESSION_LEVEL.get()));
    }
}
