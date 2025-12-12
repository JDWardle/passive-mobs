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
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class CustomCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("myaggression")
                .executes(CustomCommands::getMyAggressionLevel)
                .then(Commands.argument("level", StringArgumentType.string())
                        .suggests(CustomCommands::suggestLevel)
                        .executes(context -> setMyAggressionLevel(context, context.getArgument("level", String.class)))
                )
        );

        dispatcher.register(Commands.literal("aggression")
                .then(Commands.argument("player", StringArgumentType.string())
                        .suggests(CustomCommands::suggestPlayers)
                        .executes(context -> getPlayerAggressionLevel(context, context.getArgument("player", String.class)))
                        .then(Commands.argument("level", StringArgumentType.string())
                                .suggests(CustomCommands::suggestLevel)
                                .executes(context -> setPlayerAggressionLevel(context, context.getArgument("player", String.class), context.getArgument("level", String.class)))
                        )
                )
        );
    }

    private static CompletableFuture<Suggestions> suggestLevel(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Arrays.stream(AggressionLevel.values()).map(AggressionLevel::toString).forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestPlayers(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        CommandSourceStack source = context.getSource();

        Arrays.stream(source.getServer().getPlayerNames()).forEach(builder::suggest);
        return builder.buildFuture();
    }

    public static int setMyAggressionLevel(CommandContext<CommandSourceStack> context, String aggressionLevel) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        if (!source.isPlayer()) {
            return 0;
        }

        PlayerSettings playerSettings = source.getPlayerOrException().getData(PassiveMobs.PLAYER_SETTINGS);

        return setAggressionLevel(aggressionLevel, source, playerSettings);
    }

    public static int getMyAggressionLevel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        if (!source.isPlayer()) {
            return 0;
        }

        PlayerSettings playerSettings = source.getPlayerOrException().getData(PassiveMobs.PLAYER_SETTINGS);

        source.sendSuccess(() -> Component.literal("Aggression level: " + playerSettings.getAggressionLevel().toString()), false);
        return 1;
    }

    public static int setPlayerAggressionLevel(CommandContext<CommandSourceStack> context, String playerName, String aggressionLevel) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        Player player = context.getSource().getServer().getPlayerList().getPlayer(playerName);
        if (player == null) {
            source.sendFailure(Component.literal("Invalid player name"));
            return 0;
        }

        PlayerSettings playerSettings = source.getPlayerOrException().getData(PassiveMobs.PLAYER_SETTINGS);

        return setAggressionLevel(aggressionLevel, source, playerSettings);
    }

    public static int getPlayerAggressionLevel(CommandContext<CommandSourceStack> context, String playerName) {
        CommandSourceStack source = context.getSource();

        Player player = context.getSource().getServer().getPlayerList().getPlayer(playerName);
        if (player == null) {
            source.sendFailure(Component.literal("Invalid player name"));
            return 0;
        }

        PlayerSettings playerSettings = player.getData(PassiveMobs.PLAYER_SETTINGS);

        source.sendSuccess(() -> Component.literal("Player aggression level: " + playerSettings.getAggressionLevel().toString()), false);
        return 1;
    }

    private static int setAggressionLevel(String aggressionLevel, CommandSourceStack source, PlayerSettings playerSettings) {
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
}
