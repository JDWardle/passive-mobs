package com.jdwardle.passivemobs;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import static com.jdwardle.passivemobs.PassiveMobs.LOGGER;
import static com.jdwardle.passivemobs.PassiveMobs.PLAYER_SETTINGS;

public class EventHandler {
    // Set up the player manager for the user that just logged in.
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        PlayerSettings playerSettings = player.getData(PLAYER_SETTINGS);

        PlayerManagerStore.computeIfAbsent(player.getUUID(), new PlayerManager(playerSettings.getAggressionLevel()));

        LOGGER.debug("Player {} joined with aggro level: {}", player.getDisplayName().getString(), playerSettings.getAggressionLevel());
    }

    // Clean up the tracked player manager for the logged-out player.
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        PlayerManagerStore.remove(player.getUUID());
    }

    // Handles progressing the player's aggression timer.
    @SubscribeEvent
    public static void onPrePlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();

        PlayerManagerStore
                .get(player.getUUID())
                .ifPresent(PlayerManager::tick);
    }

    // Handles setting a player to aggressive if they attack a mob. This will
    // enable mob targeting of the player even if their aggro level is set to
    // passive.
    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
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
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
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
    public static void onRegisterCommandsEvent(RegisterCommandsEvent event) {
        CustomCommands.register(event.getDispatcher());
    }
}
