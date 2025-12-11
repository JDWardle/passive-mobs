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
    public static final Supplier<AttachmentType<PlayerSettings>> PLAYER_SETTINGS = ATTACHMENT_TYPES.register(
        "player_settings", () -> AttachmentType.serializable(PlayerSettings::new).copyOnDeath().build()
    );

    public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the Deferred Register to the mod event bus so attachment
        // types get registered.
        ATTACHMENT_TYPES.register(modEventBus);

        // Register ourselves for server and other game events we are interested
        // in.
        // Note that this is necessary if and only if we want *this* class to
        // respond directly to events. Do not add this line if there are no
        // @SubscribeEvent-annotated functions in this class.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the
        // config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        PlayerSettings playerSettings = player.getData(PLAYER_SETTINGS);
        // TODO: This might be better to track on a player manager class instead of storing it.
        playerSettings.setAggressiveTimestamp(0);
        playerSettings.setAggressive(false);
        LOGGER.info("Player {} aggro Level: {}", player.getDisplayName(), playerSettings.getAggressionLevel());
    }

    // Handles resetting the player aggression level after the configured number
    // of ticks have passed since the player was last aggressive.
    @SubscribeEvent
    public void onPrePlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        PlayerSettings playerSettings = player.getData(PLAYER_SETTINGS);
        if (!playerSettings.isAggressive()) {
            return;
        }

        if (player.tickCount - playerSettings.getAggressiveTimestamp() > Config.DEFAULT_DEAGGRO_TICKS.get()) {
            playerSettings.setAggressive(false);

            LOGGER.info("Player {} is no longer aggressive!", player.getDisplayName().getString());
        }
    }

    // Handles setting a player to aggressive if they attack a mob. This will
    // enable mob targeting of the player even if their aggro level is set to
    // passive.
    @SubscribeEvent
    public void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        Entity damageSource = event.getSource().getEntity();
        LivingEntity damageTarget = event.getEntity();

        // If the damage source was a player and the target was a monster, set
        // the player to aggressive.
        if (damageSource instanceof Player player && damageTarget instanceof Monster) {
            PlayerSettings playerSettings = player.getData(PLAYER_SETTINGS);
            playerSettings.setAggressive(true);
            playerSettings.setAggressiveTimestamp(player.tickCount);

            LOGGER.info("Player {} is now aggressive!", player.getDisplayName().getString());
        }
    }

    // Handles target changes for monster living entities. Prevents passive
    // players from being targeted unless they are currently aggressive.
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

        PlayerSettings playerSettings = player.getData(PLAYER_SETTINGS);

        // Non-passive player targeting works without any changes.
        if (!playerSettings.getAggressionLevel().equals(AggressionLevel.PASSIVE)) {
            return;
        }

        // Aggressive passive players will be targeted like normal.
        if (playerSettings.isAggressive()) {
            return;
        }

        // Disable monster targeting for passive players.
        event.setNewAboutToBeSetTarget(null);
    }

    @SubscribeEvent
    public void onRegisterCommandsEvent(RegisterCommandsEvent event) {
        CustomCommands.register(event.getDispatcher());
    }
}
