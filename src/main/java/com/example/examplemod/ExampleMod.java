package com.example.examplemod;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.*;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ExampleMod.MODID)
public class ExampleMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "examplemod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // Holds all attachment types for this mod.
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ExampleMod.MODID);

    // Defines the difficulty attachment type to be used on players.
    public static final Supplier<AttachmentType<PlayerAttachment>> PLAYER_ATTACHMENT = ATTACHMENT_TYPES.register(
        "player_attachment", () -> AttachmentType.serializable(PlayerAttachment::new).copyOnDeath().build()
    );

    public static final Supplier<AttachmentType<MonsterAttachment>> MONSTER_ATTACHMENT = ATTACHMENT_TYPES.register(
        "monster_attachment", () -> AttachmentType.serializable(MonsterAttachment::new).build()
    );

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the Deferred Register to the mod event bus so attachment types get registered.
        ATTACHMENT_TYPES.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        PlayerAttachment playerAttachment = player.getData(PLAYER_ATTACHMENT);
        // TODO: This might be better to track on a player manager class instead of storing it.
        playerAttachment.setAggressiveTimestamp(0);
        playerAttachment.setAggressive(false);
        LOGGER.info("Player {} Difficulty: {}", player.getDisplayName(), playerAttachment.getDifficulty());
    }

    @SubscribeEvent
    public void onPrePlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        PlayerAttachment playerAttachment = player.getData(PLAYER_ATTACHMENT);
        if (!playerAttachment.isAggressive()) {
            return;
        }

        if (player.tickCount - playerAttachment.getAggressiveTimestamp() > Config.DEFAULT_DEAGGRO_TICKS.get()) {
            playerAttachment.setAggressive(false);

            LOGGER.info("Player {} is no longer aggressive!", player.getDisplayName().getString());
        }
    }

    @SubscribeEvent
    public void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        Entity damageSource = event.getSource().getEntity();
        LivingEntity damageTarget = event.getEntity();

        // If the damage source was a player and the target was a monster, set
        // them to aggressive. Allows for tracking players even if they're in
        // peaceful difficulty.
        if (damageSource instanceof Player player && damageTarget instanceof Monster) {
            PlayerAttachment playerAttachment = player.getData(PLAYER_ATTACHMENT);
            playerAttachment.setAggressive(true);
            playerAttachment.setAggressiveTimestamp(player.tickCount);

            LOGGER.info("Player {} is now aggressive!", player.getDisplayName().getString());
        }

        // Non-player targets behave as normal.
        if (!(damageTarget instanceof Player player)) {
            return;
        }

        // If the source of damage was not from a monster or player, ignore it.
        // Allows for damage types to behave normally such as lava, hunger,
        // drowning, etc.
        if (!(damageSource instanceof Monster) && !(damageSource instanceof Player)) {
            return;
        }

        PlayerAttachment playerAttachment = player.getData(PLAYER_ATTACHMENT);

        // Non-peaceful difficulties work without any changes.
        if (!playerAttachment.getDifficulty().equals("peaceful")) {
            return;
        }

        // If a damage source is a monster, check to see if the target player
        // is aggressive.
        if (damageSource instanceof Monster monster && playerAttachment.isAggressive()) {
            LOGGER.info("Player {} accepts damage from monster {}", player.getDisplayName().getString(), monster.getDisplayName().getString());
            return;
        }

        // Ignore all damage from monsters and players.
        event.setCanceled(true);
    }

    // Handles target changes for monster living entities. Prevents peaceful
    // players from being targeted unless they directly damaged the targeting
    // monster.
    @SubscribeEvent
    public void onLivingChangeTarget(LivingChangeTargetEvent event) {
        Entity entity = event.getEntity();
        // Target that this event is originally going to be set will not be
        // modified by setNewAboutToBeSetTarget(). Useful if we ever need to
        // do anything with the original target.
        Entity newTarget = event.getOriginalAboutToBeSetTarget();

        // Behave normally for non-monsters entities.
        if (!(entity instanceof Monster monster)) {
            return;
        }

        // Behave normally for non-player targets.
        if (!(newTarget instanceof Player player)) {
            return;
        }

        PlayerAttachment playerAttachment = player.getData(PLAYER_ATTACHMENT);

        // Non-peaceful difficulties work without any changes.
        if (!playerAttachment.getDifficulty().equals("peaceful")) {
            return;
        }

        // Aggressive peaceful players will be targeted like normal.
        if (playerAttachment.isAggressive()) {
            return;
        }

        // Disable monster targeting for peaceful difficulty.
        event.setNewAboutToBeSetTarget(null);
    }

    @SubscribeEvent
    public void onRegisterCommandsEvent(RegisterCommandsEvent event) {
        CustomCommands.register(event.getDispatcher());
    }
}
