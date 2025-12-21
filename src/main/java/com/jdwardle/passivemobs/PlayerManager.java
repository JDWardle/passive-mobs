package com.jdwardle.passivemobs;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;

import javax.annotation.Nullable;

public class PlayerManager {
    // This tracks if a player should appear aggressive to mobs or not. Entity
    // behavior when this is set to true is determined by the players'
    // configured aggression level.
    private Boolean aggressive;

    // The players configured aggression level.
    private AggressionLevel aggressionLevel;

    private int tickCount;

    // The number of ticks an aggressive player should remain aggressive.
    private final int deaggroTicks;

    // This tracks the last tick that the player was set to aggressive.
    private int lastAggressionTick;

    PlayerManager(AggressionLevel aggressionLevel, int deaggroTicks) {
        this.deaggroTicks = deaggroTicks;
        this.aggressive = false;
        this.lastAggressionTick = 0;
        this.aggressionLevel = aggressionLevel;
    }

    PlayerManager(AggressionLevel aggressionLevel) {
        this(aggressionLevel, Config.DEFAULT_DEAGGRO_TICKS.get());
    }

    public Boolean isPlayerAggressive() {
        return this.aggressive;
    }

    private void setPlayerAggressive(Boolean isAggressive) {
        this.aggressive = isAggressive;

        if (this.aggressive) {
            this.lastAggressionTick = tickCount;
        }
    }

    public void setAggressionLevel(AggressionLevel aggressionLevel) {
        this.aggressionLevel = aggressionLevel;
    }

    // This handles the aggression timer. Called on some sort of regular
    // interval, this will check the player tick count and compare it to their
    // lastAggressionTimestamp to determine if a player should go from
    // aggressive to non-aggressive.
    public void tick() {
        this.tickCount++;

        if (!isPlayerAggressive()) {
            return;
        }

        if ((tickCount - lastAggressionTick) >= this.deaggroTicks) {
            setPlayerAggressive(false);
        }
    }

    // Returns true if the player can be targeted by the provided Entity. Based
    // on their current aggression state and configured aggression level.
    public Boolean canTargetPlayer(@Nullable Entity targetingEntity) {
        // Can be targeted by all non-monster mobs without any other conditions.
        if (!(targetingEntity instanceof Monster)) {
            return true;
        }

        switch (aggressionLevel) {
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

    // Returns true if the player can be hurt by the provided Entity. Based on
    // their current aggression state and configured aggression level.
    public Boolean canHurtPlayer(@Nullable Entity damagingEntity) {
        // Can be damaged by all non-monster mobs without any other conditions.
        if (!(damagingEntity instanceof Monster)) {
            return true;
        }

        switch (aggressionLevel) {
            case AggressionLevel.NORMAL, AggressionLevel.PASSIVE -> {
                return true;
            }
            case AggressionLevel.PEACEFUL -> {
                return false;
            }
        }

        return false;
    }

    // Determines if the player should be set to aggressive based on which type
    // of entity they damaged.
    public void playerHurtEntity(@Nullable Entity damagedEntity) {
        if (!(damagedEntity instanceof Monster)) {
            return;
        }

        setPlayerAggressive(true);
    }
}
