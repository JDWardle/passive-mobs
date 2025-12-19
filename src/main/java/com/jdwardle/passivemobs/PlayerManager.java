package com.jdwardle.passivemobs;

import net.minecraft.world.entity.player.Player;

import static com.jdwardle.passivemobs.PassiveMobs.PLAYER_SETTINGS;

public class PlayerManager {
    // This tracks if a player should appear aggressive to mobs or not. The
    // behavior of when this is set to true is determined by the players
    // configured aggression level.
    private Boolean aggressive;

    // This tracks the last time the player was set to aggressive.
    private int lastAggressionTimestamp;

    private final Player player;
    private final PlayerSettings playerSettings;

    PlayerManager(Player player) {
        this.player = player;
        this.playerSettings = player.getData(PLAYER_SETTINGS);
        this.aggressive = false;
        this.lastAggressionTimestamp = 0;
    }

    public Boolean isPlayerAggressive() {
        return this.aggressive;
    }

    public void setPlayerAggressive(Boolean isAggressive) {
        this.aggressive = isAggressive;

        if (this.aggressive) {
            this.lastAggressionTimestamp = player.tickCount;
        }
    }

    // This handles the aggression timer. Called on some sort of regular
    // interval, this will check the player tick count and compare it to their
    // lastAggressionTimestamp to determine if a player should go from
    // aggressive to non-aggressive.
    public void tick() {
        if (player.isDeadOrDying()) {
            return;
        }

        if (!isPlayerAggressive()) {
            return;
        }

        if ((player.tickCount - lastAggressionTimestamp) >= Config.DEFAULT_DEAGGRO_TICKS.get()) {
            setPlayerAggressive(false);
        }
    }

    // Returns true if the player can be targeted by mobs. Based on their
    // current aggression state and configured aggression level.
    public Boolean canBeTargeted() {
        switch (this.playerSettings.getAggressionLevel()) {
            case AggressionLevel.NORMAL -> {
                return true;
            }
            case AggressionLevel.PASSIVE -> {
                return isPlayerAggressive();
            }
            case AggressionLevel.PEACEFUL -> {
                return false;
            }
        }

        return false;
    }

    // Returns true if the player can be damaged by mobs. Based on their current
    // aggression state and configured aggression level.
    public Boolean canBeDamaged() {
        switch (this.playerSettings.getAggressionLevel()) {
            case AggressionLevel.NORMAL, AggressionLevel.PASSIVE -> {
                return true;
            }
            case AggressionLevel.PEACEFUL -> {
                return false;
            }
        }

        return false;
    }
}
