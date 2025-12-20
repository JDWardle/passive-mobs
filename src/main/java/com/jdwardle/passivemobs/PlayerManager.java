package com.jdwardle.passivemobs;

public class PlayerManager {
    // This tracks if a player should appear aggressive to mobs or not. The
    // behavior of when this is set to true is determined by the players
    // configured aggression level.
    private Boolean aggressive;

    private int tickCount;

    private final int deaggroTicks;

    // This tracks the last time the player was set to aggressive.
    private int lastAggressionTimestamp;

    PlayerManager(int deaggroTicks) {
        this.deaggroTicks = deaggroTicks;
        this.aggressive = false;
        this.lastAggressionTimestamp = 0;
    }

    PlayerManager() {
        this(Config.DEFAULT_DEAGGRO_TICKS.get());
    }

    public Boolean isPlayerAggressive() {
        return this.aggressive;
    }

    public void setPlayerAggressive(Boolean isAggressive) {
        this.aggressive = isAggressive;

        if (this.aggressive) {
            this.lastAggressionTimestamp = tickCount;
        }
    }

    public void resetAggression() {
        this.aggressive = false;
        this.lastAggressionTimestamp = 0;
    }

    public int getLastAggressionTimestamp() {
        return this.lastAggressionTimestamp;
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

        if ((tickCount - lastAggressionTimestamp) >= this.deaggroTicks) {
            setPlayerAggressive(false);
        }
    }

    // Returns true if the player can be targeted by mobs. Based on their
    // current aggression state and configured aggression level.
    public Boolean canBeTargeted(AggressionLevel aggressionLevel) {
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

    // Returns true if the player can be damaged by mobs. Based on their current
    // aggression state and configured aggression level.
    public Boolean canBeDamaged(AggressionLevel aggressionLevel) {
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
}
