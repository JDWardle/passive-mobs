package com.example.examplemod;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> DEFAULT_DIFFICULTY = BUILDER
            .comment("The default difficulty for new players")
            .define("defaultDifficulty", "peaceful");

    public static final ModConfigSpec.ConfigValue<Integer> DEFAULT_DEAGGRO_TICKS = BUILDER
            .comment("The number of player ticks it takes to remove the aggressive flag from a player")
            .defineInRange("defaultDeaggroTicks", 1000, 0, Integer.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();
}
