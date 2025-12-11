package com.example.examplemod;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class PlayerDifficulty implements ValueIOSerializable {
    private String difficulty;

    PlayerDifficulty() {
        this.difficulty = Config.DEFAULT_DIFFICULTY.get();
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void serialize(ValueOutput valueOutput) {
        valueOutput.putString("difficulty", difficulty);
    }

    public void deserialize(ValueInput valueInput) {
        difficulty = valueInput.getStringOr("difficulty", Config.DEFAULT_DIFFICULTY.get());
    }
}
