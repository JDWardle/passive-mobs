package com.jdwardle.passivemobsgametest;

import com.jdwardle.passivemobs.AggressionLevel;
import com.jdwardle.passivemobs.Config;
import com.jdwardle.passivemobs.PassiveMobs;
import com.jdwardle.passivemobs.PlayerSettings;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
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
            description = "Ensure that the player settings is created for a player when they are logged in.",
            title = "player_settings_created",
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
            description = "Ensure that mobs target and hurt players on normal difficulty",
            title = "normal_mob_targeting",
            enabledByDefault = true
    )
    static void normalMobTargetingTest(final DynamicTest test) {
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL))
                .thenExecute(player -> {
                    helper.setNight();
                    player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(0, 0, 5))));

                    PlayerSettings settings = player.getData(PassiveMobs.PLAYER_SETTINGS);
                    settings.setAggressionLevel(AggressionLevel.NORMAL);

                    Zombie entity = helper.spawn(EntityType.ZOMBIE, 5, 0, 0);

                    helper.assertTrue(entity.getTarget() == null, "entity initial target should be null");

                    entity.setTarget(player);

                    helper.assertTrue(entity.getTarget() == player, "entity target should be player");

                    entity.doHurtTarget(helper.getLevel(), player);

                    helper.assertTrue(player.getLastHurtByMob() == entity, "player should have been damaged by entity");

                    entity.remove(Entity.RemovalReason.DISCARDED);
                })
                .thenExecute(test::pass)
                .thenSucceed());
    }

    @EmptyTemplate(floor = true)
    @GameTest(template = "empty")
    @TestHolder(
            description = "Ensure that mobs cannot target or hurt peaceful players",
            title = "peaceful_mob_targeting",
            enabledByDefault = true
    )
    static void peacefulMobTargetingTest(final DynamicTest test) {
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL))
                .thenExecute(player -> {
                    helper.setNight();
                    player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(0, 0, 5))));

                    PlayerSettings settings = player.getData(PassiveMobs.PLAYER_SETTINGS);
                    settings.setAggressionLevel(AggressionLevel.PEACEFUL);

                    Zombie entity = helper.spawn(EntityType.ZOMBIE, 5, 0, 0);

                    helper.assertTrue(entity.getTarget() == null, "entity initial target should be null");

                    entity.setTarget(player);

                    helper.assertTrue(entity.getTarget() == null, "entity target should remain null");

                    entity.doHurtTarget(helper.getLevel(), player);

                    helper.assertTrue(player.getLastHurtByMob() == null, "player should not have been hurt by entity");

                    // No targeting or damage from mobs even if the player
                    // becomes aggressive.
                    player.attack(entity);

                    entity.setTarget(player);

                    helper.assertTrue(entity.getTarget() == null, "entity target should remain null");
                    helper.assertTrue(player.getLastHurtByMob() == null, "player should not have been hurt by entity");

                    entity.remove(Entity.RemovalReason.DISCARDED);
                })
                .thenExecute(test::pass)
                .thenSucceed());
    }

    @EmptyTemplate(floor = true)
    @GameTest(template = "empty")
    @TestHolder(
            description = "Ensure that mobs can hurt but not target non-aggressive passive players",
            title = "passive_mob_targeting",
            enabledByDefault = true
    )
    static void passiveMobTargetingTest(final DynamicTest test) {
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL))
                .thenExecute(player -> {
                    helper.setNight();

                    player.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(0, 0, 5))));

                    PlayerSettings settings = player.getData(PassiveMobs.PLAYER_SETTINGS);
                    settings.setAggressionLevel(AggressionLevel.PASSIVE);

                    Zombie entity = helper.spawn(EntityType.ZOMBIE, 5, 0, 0);

                    helper.assertTrue(entity.getTarget() == null, "entity initial target should be null");

                    entity.setTarget(player);

                    helper.assertTrue(entity.getTarget() == null, "entity target should remain null");
                    entity.doHurtTarget(helper.getLevel(), player);

                    // Passive difficulty allows mob damage but not targeting.
                    helper.assertTrue(player.getLastHurtByMob() == entity, "player should have been hurt by entity");
                    helper.assertTrue(entity.getTarget() == null, "entity target should still be null");

                    // Player becomes aggressive so should become targetable by
                    // the entity.
                    player.attack(entity);
                    entity.setTarget(player);

                    helper.assertTrue(entity.getTarget() == player, "entity target should player");

                    entity.remove(Entity.RemovalReason.DISCARDED);
                })
                // TODO: Figure out deaggro testing.
                .thenExecute(test::pass)
                .thenSucceed());
    }
}
