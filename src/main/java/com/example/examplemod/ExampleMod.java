package com.example.examplemod;

import com.mojang.serialization.Codec;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
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

import java.util.List;
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
    public static final Supplier<AttachmentType<PlayerDifficulty>> DIFFICULTY = ATTACHMENT_TYPES.register(
        "difficulty", () -> AttachmentType.serializable(PlayerDifficulty::new).copyOnDeath().build()
    );

    public static final Supplier<AttachmentType<List>> DAMAGED_BY_PLAYERS = ATTACHMENT_TYPES.register(
      "damaged_by_plaers", () -> AttachmentType.builder(() -> null).serialize(Codec.STRING.listOf().fieldOf()).build()
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
        PlayerDifficulty playerDifficulty = player.getData(DIFFICULTY);
        LOGGER.info("Player {} joined the game", player.getDisplayName());
        LOGGER.info("Difficulty: {}", playerDifficulty.getDifficulty());
    }

    @SubscribeEvent
    public void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Player) {
            if (event.getSource().getEntity() instanceof Monster monster) {
                monster.setData(PLAYER_DAMAGED, true);
            }
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof Monster) && !(event.getSource().getEntity() instanceof Player)) {
            return;
        }

        PlayerDifficulty playerDifficulty = player.getData(DIFFICULTY);
        DamageSource source = event.getSource();

        // Disable monster and player damage for peaceful difficulty.
        // NOTE: Keeps starvation.
        if (playerDifficulty.getDifficulty().equals("peaceful")) {
            if (event.getSource().getEntity() instanceof Monster monster) {
                Boolean playerDamaged = monster.getData(PLAYER_DAMAGED);
                if (playerDamaged) {
                    // You damaged it, you kill it.
                    return;
                }
            }

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getNewAboutToBeSetTarget() instanceof Player player)) {
            return;
        }

        if (!(event.getEntity() instanceof Monster)) {
            return;
        }

        PlayerDifficulty playerDifficulty = player.getData(DIFFICULTY);

        // Disable monster targeting for peaceful difficulty.
        if (playerDifficulty.getDifficulty().equals("peaceful")) {
            if (event.getEntity() instanceof Monster monster) {
                Boolean playerDamaged = monster.getData(PLAYER_DAMAGED);
                if (playerDamaged) {
                    // You damaged it, you kill it.
                    return;
                }
            }

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEntityTickEvent(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof Monster monster)) {
            return;
        }

        if (!(monster.getTarget() instanceof Player player)) {
            return;
        }

        PlayerDifficulty playerDifficulty = player.getData(DIFFICULTY);

        // Disable monster targeting for peaceful difficulty. Useful if the
        // player changed commands while the monster was targeting them.
        if (playerDifficulty.getDifficulty().equals("peaceful")) {
            Boolean playerDamaged = monster.getData(PLAYER_DAMAGED);
            if (playerDamaged) {
                // You damaged it, you kill it.
                return;
            }

            monster.setTarget(null);
        }
    }

    @SubscribeEvent
    public void onRegisterCommandsEvent(RegisterCommandsEvent event) {
        CustomCommands.register(event.getDispatcher());
    }
}
