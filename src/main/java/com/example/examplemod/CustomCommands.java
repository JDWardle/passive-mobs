package com.example.examplemod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CustomCommands {
    public static final List<String> DIFFICULTIES = List.of("peaceful", "hard");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("player-difficulty")
            .executes(CustomCommands::getPlayerDifficulty)
            .then(Commands.argument("difficulty", StringArgumentType.string())
                    .suggests(CustomCommands::suggestDifficulty)
                    .executes(context -> setPlayerDifficulty(context, context.getArgument("difficulty", String.class)))
            )
        );
    }

    private static CompletableFuture<Suggestions> suggestDifficulty(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        DIFFICULTIES.forEach(builder::suggest);
        return builder.buildFuture();
    }

    public static int setPlayerDifficulty(CommandContext<CommandSourceStack> context, String difficulty) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        if (!source.isPlayer()) {
            return 0;
        }

        PlayerDifficulty playerDifficulty = source.getPlayerOrException().getData(ExampleMod.DIFFICULTY);

        if (!DIFFICULTIES.contains(difficulty)) {
            source.sendFailure(Component.literal("Invalid difficulty"));
            return 0;
        }

        playerDifficulty.setDifficulty(difficulty);

        source.sendSuccess(() -> Component.literal("Difficulty set to " + difficulty), false);
        return 1;
    }

    public static int getPlayerDifficulty(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        if (!source.isPlayer()) {
            return 0;
        }

        PlayerDifficulty playerDifficulty = source.getPlayerOrException().getData(ExampleMod.DIFFICULTY);

        source.sendSuccess(() -> Component.literal("Difficulty: " + playerDifficulty.getDifficulty()), false);
        return 1;
    }
}
