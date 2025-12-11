package com.jdwardle.passivemobs;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class CustomCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("aggression")
            .executes(CustomCommands::getPlayerAggressionLevel)
            .then(Commands.argument("level", StringArgumentType.string())
                    .suggests(CustomCommands::suggestLevel)
                    .executes(context -> setPlayerAggressionLevel(context, context.getArgument("level", String.class)))
            )
        );
    }

    private static CompletableFuture<Suggestions> suggestLevel(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Arrays.stream(AggressionLevel.values()).map(AggressionLevel::toString).forEach(builder::suggest);
        return builder.buildFuture();
    }

    public static int setPlayerAggressionLevel(CommandContext<CommandSourceStack> context, String aggressionLevel) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        if (!source.isPlayer()) {
            return 0;
        }

        PlayerSettings playerSettings = source.getPlayerOrException().getData(PassiveMobs.PLAYER_SETTINGS);

        AggressionLevel level;
        try {
            level = AggressionLevel.getLevel(aggressionLevel);
        } catch (IllegalStateException e) {
            source.sendFailure(Component.literal("Invalid aggression level"));
            return 0;
        }

        playerSettings.setAggressionLevel(level);

        source.sendSuccess(() -> Component.literal("Aggression level set to " + level.toString()), false);
        return 1;
    }

    public static int getPlayerAggressionLevel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        if (!source.isPlayer()) {
            return 0;
        }

        PlayerSettings playerSettings = source.getPlayerOrException().getData(PassiveMobs.PLAYER_SETTINGS);

        source.sendSuccess(() -> Component.literal("Aggression level: " + playerSettings.getAggressionLevel().toString()), false);
        return 1;
    }
}
