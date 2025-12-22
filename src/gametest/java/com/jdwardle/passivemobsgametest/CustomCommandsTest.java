package com.jdwardle.passivemobsgametest;

import com.jdwardle.passivemobs.AggressionLevel;
import com.jdwardle.passivemobs.PassiveMobs;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.level.GameType;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.gametest.GameTestPlayer;

import java.util.Optional;

import static com.jdwardle.passivemobsgametest.PassiveMobsTest.makeTickingMockServerPlayerInLevel;

@ForEachTest(
        groups = "commands",
        idPrefix = "custom_commands_test_"
)
public class CustomCommandsTest {
    @EmptyTemplate(floor = true)
    @GameTest(template = "empty")
    @TestHolder(
            description = "Ensure that a player can retrieve their own aggression level.",
            title = "get_myaggression",
            enabledByDefault = true
    )
    static void getMyaggressionCommandTest(final DynamicTest test) {
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL))
                .thenExecute(player -> {
                    try {
                        int output = helper.getLevel().getServer().getCommands().getDispatcher().execute("myaggression", player.createCommandSourceStack());
                        helper.assertTrue(output == 1, "command should return 1");
                    } catch (CommandSyntaxException e) {
                        test.fail(e.getMessage());
                    }
                })
                .thenSucceed());
    }

    @EmptyTemplate(floor = true)
    @GameTest(template = "empty")
    @TestHolder(
            description = "Ensure that a player can set their own aggression level.",
            title = "set_myaggression",
            enabledByDefault = true
    )
    static void setMyaggressionCommandTest(final DynamicTest test) {
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL))
                .thenExecute(player -> {
                    helper.assertValueEqual(AggressionLevel.PASSIVE, player.getData(PassiveMobs.PLAYER_SETTINGS).getAggressionLevel(), "player aggression level should be set to passive");

                    try {
                        int output = helper.getLevel().getServer().getCommands().getDispatcher().execute("myaggression normal", player.createCommandSourceStack());
                        helper.assertTrue(output == 1, "command should return 1");
                    } catch (CommandSyntaxException e) {
                        test.fail(e.getMessage());
                    }

                    helper.assertValueEqual(AggressionLevel.NORMAL, player.getData(PassiveMobs.PLAYER_SETTINGS).getAggressionLevel(), "player aggression level should be set to normal");
                })
                .thenSucceed());
    }

    @EmptyTemplate(floor = true)
    @GameTest(template = "empty")
    @TestHolder(
            description = "Ensure that only an op player can retrieve other players aggression level.",
            title = "get_player_aggression",
            enabledByDefault = true
    )
    static void getPlayerAggressionLevelCommandTest(final DynamicTest test) {
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL))
                .thenExecute(player -> {
                    try {
                        helper.getLevel().getServer().getCommands().getDispatcher().execute("aggression test-mock-player", player.createCommandSourceStack());
                        test.fail("non-op player should not be able to execute the command");
                    } catch (CommandSyntaxException e) {
                        helper.assertValueEqual("Unknown or incomplete command. See below for error at position 0: <--[HERE]", e.getMessage(), "non-op player should not be able to execute the command");
                    }

                    helper.getLevel().getServer().getPlayerList().op(new NameAndId(player.getGameProfile()), Optional.of(4), Optional.empty());

                    try {
                        int output = helper.getLevel().getServer().getCommands().getDispatcher().execute("aggression test-mock-player", player.createCommandSourceStack());
                        helper.assertTrue(output == 1, "command should return 1");
                    } catch (CommandSyntaxException e) {
                        test.fail(e.getMessage());
                    }
                })
                .thenSucceed());
    }

    @EmptyTemplate(floor = true)
    @GameTest(template = "empty")
    @TestHolder(
            description = "Ensure that an only an op player can set other players aggression level.",
            title = "set_player_aggression",
            enabledByDefault = true
    )
    static void setPlayerAggressionLevelCommandTest(final DynamicTest test) {
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL))
                .thenExecute(player -> {
                    GameTestPlayer otherPlayer = makeTickingMockServerPlayerInLevel(helper, GameType.SURVIVAL, "test-mock-player-2");

                    helper.assertValueEqual(AggressionLevel.PASSIVE, player.getData(PassiveMobs.PLAYER_SETTINGS).getAggressionLevel(), "player aggression level should be set to passive");
                    helper.assertValueEqual(AggressionLevel.PASSIVE, otherPlayer.getData(PassiveMobs.PLAYER_SETTINGS).getAggressionLevel(), "other player aggression level should be set to passive");

                    try {
                        helper.getLevel().getServer().getCommands().getDispatcher().execute("aggression test-mock-player-2 normal", player.createCommandSourceStack());
                        test.fail("non-op player should not be able to execute the command");
                    } catch (CommandSyntaxException e) {
                        helper.assertValueEqual("Unknown or incomplete command. See below for error at position 0: <--[HERE]", e.getMessage(), "non-op player should not be able to execute the command");
                    }

                    helper.assertValueEqual(AggressionLevel.PASSIVE, player.getData(PassiveMobs.PLAYER_SETTINGS).getAggressionLevel(), "player aggression level should be set to passive");
                    helper.assertValueEqual(AggressionLevel.PASSIVE, otherPlayer.getData(PassiveMobs.PLAYER_SETTINGS).getAggressionLevel(), "other player aggression level should be set to passive");

                    helper.getLevel().getServer().getPlayerList().op(new NameAndId(player.getGameProfile()), Optional.of(4), Optional.empty());

                    try {
                        int output = helper.getLevel().getServer().getCommands().getDispatcher().execute("aggression test-mock-player-2 normal", player.createCommandSourceStack());
                        helper.assertTrue(output == 1, "command should return 1");
                    } catch (CommandSyntaxException e) {
                        test.fail(e.getMessage());
                    }

                    helper.assertValueEqual(AggressionLevel.PASSIVE, player.getData(PassiveMobs.PLAYER_SETTINGS).getAggressionLevel(), "player aggression level should be set to passive");
                    helper.assertValueEqual(AggressionLevel.NORMAL, otherPlayer.getData(PassiveMobs.PLAYER_SETTINGS).getAggressionLevel(), "other player aggression level should be set to normal");
                })
                .thenSucceed());
    }
}
