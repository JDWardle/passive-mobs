package com.jdwardle.passivemobs;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> DEFAULT_AGGRESSION_LEVEL = BUILDER
            .comment("The default aggression level for new players (normal|passive|peaceful)")
            .define("defaultAggressionLevel", AggressionLevel.PASSIVE.toString());

    public static final ModConfigSpec.ConfigValue<Integer> DEFAULT_DEAGGRO_TICKS = BUILDER
            .comment("The number of player ticks it takes to remove the aggressive flag from a player")
            .defineInRange("deaggroTicks", 500, 0, Integer.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();
}
