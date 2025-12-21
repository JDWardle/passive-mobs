package com.jdwardle.passivemobs;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(PassiveMobs.MODID)
public class PassiveMobs {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "passivemobs";
    public static final String VERSION = "1.0.2";

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // Holds all attachment types for this mod.
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, PassiveMobs.MODID);

    // Defines the difficulty attachment type to be used on players.
    public static final Supplier<AttachmentType<PlayerSettings>> PLAYER_SETTINGS = ATTACHMENT_TYPES.register(
            "player_settings", () -> AttachmentType.serializable(PlayerSettings::new).copyOnDeath().build()
    );

    public PassiveMobs(IEventBus modEventBus, ModContainer modContainer) {
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

        LOGGER.info("Passive Mobs loaded version {}!", VERSION);
    }

    // Set up the player manager for the user that just logged in.
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        PlayerSettings playerSettings = player.getData(PLAYER_SETTINGS);

        PlayerManagerStore.computeIfAbsent(player.getUUID(), new PlayerManager(playerSettings.getAggressionLevel()));

        LOGGER.debug("Player {} joined with aggro level: {}", player.getDisplayName().getString(), playerSettings.getAggressionLevel());
    }

    // Clean up the tracked player manager for the logged-out player.
    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        PlayerManagerStore.remove(player.getUUID());
    }

    // Handles progressing the player's aggression timer.
    @SubscribeEvent
    public void onPrePlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();

        PlayerManagerStore
                .get(player.getUUID())
                .ifPresent(PlayerManager::tick);
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
        if (damageSource instanceof Player player) {
            PlayerManagerStore
                    .get(player.getUUID())
                    .ifPresentOrElse(manager -> {
                        manager.playerHurtEntity(damageTarget);
                    }, () -> {
                        LOGGER.warn("Damaging player {} has no manager!", player.getDisplayName().getString());
                    });
        }

        // If the target is a player and the damage source is a monster, check
        // if they can damage the player.
        if (damageTarget instanceof Player player) {
            PlayerManagerStore
                    .get(player.getUUID())
                    .ifPresentOrElse(manager -> {
                        event.setCanceled(!manager.canHurtPlayer(damageSource));
                    }, () -> {
                        LOGGER.warn("Damaged player {} has no manager!", player.getDisplayName().getString());
                    });
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

        // Only handle targets that are instances of a Player.
        if (!(newTarget instanceof Player player)) {
            return;
        }

        PlayerManagerStore
                .get(player.getUUID())
                .ifPresentOrElse(manager -> {
                    // Set the new target to null if the player cannot be
                    // targeted.
                    if (!manager.canTargetPlayer(entity)) {
                        event.setNewAboutToBeSetTarget(null);
                    }
                }, () -> {
                    LOGGER.warn("Targeted player {} has no manager!", player.getDisplayName().getString());
                });
    }

    @SubscribeEvent
    public void onRegisterCommandsEvent(RegisterCommandsEvent event) {
        CustomCommands.register(event.getDispatcher());
    }
}
