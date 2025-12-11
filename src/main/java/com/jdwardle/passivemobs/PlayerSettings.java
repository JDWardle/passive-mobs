package com.jdwardle.passivemobs;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class PlayerSettings implements ValueIOSerializable {
    private AggressionLevel aggressionLevel;
    private Boolean aggressive;
    private int aggressiveTimestamp;

    PlayerSettings() {
        this.aggressionLevel = AggressionLevel.getLevel(Config.DEFAULT_AGGRESSION_LEVEL.get());
        this.aggressive = false;
        this.aggressiveTimestamp = 0;
    }

    public AggressionLevel getAggressionLevel() {
        return aggressionLevel;
    }

    public Boolean isAggressive() {
        return aggressive;
    }

    public int getAggressiveTimestamp() {
        return aggressiveTimestamp;
    }

    public void setAggressionLevel(AggressionLevel aggressionLevel) {
        this.aggressionLevel = aggressionLevel;
    }

    public void setAggressive(Boolean aggressive) {
        this.aggressive = aggressive;
    }

    public void setAggressiveTimestamp(int aggressiveTimestamp) {
        this.aggressiveTimestamp = aggressiveTimestamp;
    }

    public void serialize(ValueOutput valueOutput) {
        valueOutput.putString("aggressionLevel", aggressionLevel.toString());
        valueOutput.putBoolean("aggressive", aggressive);
        valueOutput.putInt("aggressiveTimestamp", aggressiveTimestamp);
    }

    public void deserialize(ValueInput valueInput) {
        aggressionLevel = AggressionLevel.getLevel(valueInput.getStringOr("aggressionLevel", Config.DEFAULT_AGGRESSION_LEVEL.get()));
        aggressive = valueInput.getBooleanOr("aggressive", false);
        aggressiveTimestamp = valueInput.getIntOr("aggressiveTimestamp", 0);
    }
}
