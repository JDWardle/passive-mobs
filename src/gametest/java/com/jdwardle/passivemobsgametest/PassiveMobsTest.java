package com.jdwardle.passivemobsgametest;

import com.jdwardle.passivemobs.AggressionLevel;
import com.jdwardle.passivemobs.Config;
import com.jdwardle.passivemobs.PassiveMobs;
import com.jdwardle.passivemobs.PlayerSettings;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.conf.ClientConfiguration;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.impl.MutableTestFramework;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.Objects;

@Mod(PassiveMobsTest.MODID)
public class PassiveMobsTest {
    public static final String MODID = "passivemobsgametest";
    public static final String VERSION = PassiveMobs.VERSION;
    private static final ResourceLocation TEST_FRAMEWORK_ID = ResourceLocation.fromNamespaceAndPath(MODID, "tests");

    public static final Logger LOGGER = LogUtils.getLogger();

    public PassiveMobsTest(IEventBus modEventBus, ModContainer modContainer) {
        MutableTestFramework testFramework = FrameworkConfiguration.builder(TEST_FRAMEWORK_ID) // The ID of the framework. Used by logging, primarily
                .clientConfiguration(() -> ClientConfiguration.builder() // Client-side compatibility configuration. This is COMPLETLY optional, but it is recommended for ease of use.
                        .toggleOverlayKey(GLFW.GLFW_KEY_J) // The key used to toggle the tests overlay
                        .openManagerKey(GLFW.GLFW_KEY_N) // The key used to open the Test Manager screen
                        .build())
                .build().create();

        testFramework.init(modEventBus, modContainer);

        LOGGER.info("Passive Mobs Test loaded version {}!", VERSION);
    }

    @EmptyTemplate(floor = true)
    @GameTest(template = "empty")
    @TestHolder(
            description = "Ensure that the player manager is created for a player when they are logged in.",
            title = "player_manager_created",
            enabledByDefault = true
    )
    static void onPlayerJoinTest(final DynamicTest test) {
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL))
                .thenExecute(player -> {
                    helper.assertTrue(player.hasData(PassiveMobs.PLAYER_SETTINGS), "player should have settings");

                    PlayerSettings settings = player.getData(PassiveMobs.PLAYER_SETTINGS);
                    helper.assertTrue(settings.getAggressionLevel() == AggressionLevel.getLevel(Config.DEFAULT_AGGRESSION_LEVEL.get()), "new player aggression level should be set to the default");
                    helper.assertTrue(Objects.equals(settings.getVersion(), PlayerSettings.CURRENT_VERSION), "new player settings data version should match the default");
                })
                .thenSucceed());
    }

    @EmptyTemplate(floor = true)
    @GameTest(template = "empty")
    @TestHolder(
            description = "Ensure that mobs target players on normal difficulty",
            title = "normal_mob_targeting",
            enabledByDefault = true
    )
    static void playerCommandsTest(final DynamicTest test) {
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL))
                .thenExecute(player -> {
                    PlayerSettings settings = player.getData(PassiveMobs.PLAYER_SETTINGS);
                    settings.setAggressionLevel(AggressionLevel.NORMAL);

                    Zombie entity = helper.spawn(EntityType.ZOMBIE, 0, 0, 5);
//                    entity.getTarget()
                    // tap into target entity event somehow.
                })
                .thenSucceed());
    }
}
