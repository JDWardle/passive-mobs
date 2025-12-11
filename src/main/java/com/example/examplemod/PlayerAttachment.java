package com.example.examplemod;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class PlayerAttachment implements ValueIOSerializable {
    private String difficulty;
    private Boolean aggressive;
    private int aggressiveTimestamp;

    PlayerAttachment() {
        this.difficulty = Config.DEFAULT_DIFFICULTY.get();
        this.aggressive = false;
        this.aggressiveTimestamp = 0;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public Boolean isAggressive() {
        return aggressive;
    }

    public int getAggressiveTimestamp() {
        return aggressiveTimestamp;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setAggressive(Boolean aggressive) {
        this.aggressive = aggressive;
    }

    public void setAggressiveTimestamp(int aggressiveTimestamp) {
        this.aggressiveTimestamp = aggressiveTimestamp;
    }

    public void serialize(ValueOutput valueOutput) {
        valueOutput.putString("difficulty", difficulty);
        valueOutput.putBoolean("aggressive", aggressive);
        valueOutput.putInt("aggressiveTimestamp", aggressiveTimestamp);
    }

    public void deserialize(ValueInput valueInput) {
        difficulty = valueInput.getStringOr("difficulty", Config.DEFAULT_DIFFICULTY.get());
        aggressive = valueInput.getBooleanOr("aggressive", false);
        aggressiveTimestamp = valueInput.getIntOr("aggressiveTimestamp", 0);
    }
}
