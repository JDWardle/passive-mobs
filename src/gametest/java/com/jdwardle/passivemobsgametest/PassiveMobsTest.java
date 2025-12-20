package com.jdwardle.passivemobsgametest;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
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

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(PassiveMobsTest.MODID)
public class PassiveMobsTest {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "passivemobsgametest";
    public static final String VERSION = "1.0.1";
    private static final ResourceLocation TEST_FRAMEWORK_ID = ResourceLocation.fromNamespaceAndPath(MODID, "tests");

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public PassiveMobsTest(IEventBus modEventBus, ModContainer modContainer) {
        // Register ourselves for server and other game events we are interested
        // in.
        // Note that this is necessary if and only if we want *this* class to
        // respond directly to events. Do not add this line if there are no
        // @SubscribeEvent-annotated functions in this class.
//        NeoForge.EVENT_BUS.register(this);

        setupTestFramework(modEventBus, modContainer);

        LOGGER.info("Passive Mobs Test loaded version {}!", VERSION);
    }

    private void setupTestFramework(IEventBus modEventBus, ModContainer modContainer) {
        // The ID of the framework. Used by logging, primarily
        // Client-side compatibility configuration. This is COMPLETLY optional, but it is recommended for ease of use.
        // The key used to toggle the tests overlay
        // The key used to open the Test Manager screen
        MutableTestFramework testFramework = FrameworkConfiguration.builder(TEST_FRAMEWORK_ID) // The ID of the framework. Used by logging, primarily
                .clientConfiguration(() -> ClientConfiguration.builder() // Client-side compatibility configuration. This is COMPLETLY optional, but it is recommended for ease of use.
                        .toggleOverlayKey(GLFW.GLFW_KEY_J) // The key used to toggle the tests overlay
                        .openManagerKey(GLFW.GLFW_KEY_N) // The key used to open the Test Manager screen
                        .build())
                .build().create(); // Build and store the InternalTestFramework. We use the "internal" version because we want to access methods not usually exposed, like the init method

        // Initialise this framework, using the mod event bus of the currently loading mod, and the container of the currently loading mod.
        // The container is used for collecting annotations.
        // This method will collect and register tests, structure templates, group data, and will fire init listeners.
        testFramework.init(modEventBus, modContainer);
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
//                .thenExecute(player -> {
//                    helper.assertTrue(player.hasData(, "player should have settings");
//PassiveMobs.PLAYER_SETTINGS)
//                    PlayerSettings settings = player.getData(PassiveMobs.PLAYER_SETTINGS);
//                    helper.assertTrue(settings.getAggressionLevel() == AggressionLevel.getLevel(Config.DEFAULT_AGGRESSION_LEVEL.get()), "new player aggression level should be set to the default");
//                })
                .thenSucceed());
    }
}
