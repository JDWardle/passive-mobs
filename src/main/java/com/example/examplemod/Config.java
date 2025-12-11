package com.example.examplemod;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> DEFAULT_DIFFICULTY = BUILDER
            .comment("The default difficulty for new players")
            .define("defaultDifficulty", "peaceful");

    static final ModConfigSpec SPEC = BUILDER.build();
}
