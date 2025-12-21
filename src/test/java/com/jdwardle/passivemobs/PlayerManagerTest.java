package com.jdwardle.passivemobs;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerManagerTest {
    @Test
    void isPlayerAggressive() {
        PlayerManager manager = new PlayerManager(AggressionLevel.NORMAL);

        assertFalse(manager.isPlayerAggressive());

        //noinspection DataFlowIssue
        manager.playerHurtEntity(new Zombie(null));

        assertTrue(manager.isPlayerAggressive());
    }

    @Test
    void tick() {
        PlayerManager manager = new PlayerManager(AggressionLevel.NORMAL, 2);

        manager.tick();

        assertFalse(manager.isPlayerAggressive());

        //noinspection DataFlowIssue
        manager.playerHurtEntity(new Zombie(null));

        // After 2 ticks player should no longer be aggressive.
        manager.tick();

        assertTrue(manager.isPlayerAggressive());

        manager.tick();

        assertFalse(manager.isPlayerAggressive());
    }

    @Test
    void canTargetPlayer() {
        //noinspection DataFlowIssue
        Entity target = new Zombie(null);

        PlayerManager manager = new PlayerManager(AggressionLevel.NORMAL, 2);
        assertTrue(manager.canTargetPlayer(target));

        manager.setAggressionLevel(AggressionLevel.PASSIVE);
        assertFalse(manager.canTargetPlayer(target));

        manager.setAggressionLevel(AggressionLevel.PEACEFUL);
        assertFalse(manager.canTargetPlayer(target));

        // Set player to aggressive by hurting the entity.
        manager.playerHurtEntity(target);

        manager.setAggressionLevel(AggressionLevel.NORMAL);
        assertTrue(manager.canTargetPlayer(target));

        manager.setAggressionLevel(AggressionLevel.PASSIVE);
        assertTrue(manager.canTargetPlayer(target));

        manager.setAggressionLevel(AggressionLevel.PEACEFUL);
        assertFalse(manager.canTargetPlayer(target));

        // Deaggro after 2 ticks.
        manager.tick();
        manager.tick();

        manager.setAggressionLevel(AggressionLevel.NORMAL);
        assertTrue(manager.canTargetPlayer(target));

        manager.setAggressionLevel(AggressionLevel.PASSIVE);
        assertFalse(manager.canTargetPlayer(target));

        manager.setAggressionLevel(AggressionLevel.PEACEFUL);
        assertFalse(manager.canTargetPlayer(target));
    }

    @Test
    void canBeDamaged() {
        //noinspection DataFlowIssue
        Entity target = new Zombie(null);

        PlayerManager manager = new PlayerManager(AggressionLevel.NORMAL);
        assertTrue(manager.canHurtPlayer(target));

        manager.setAggressionLevel(AggressionLevel.PASSIVE);
        assertTrue(manager.canHurtPlayer(target));

        manager.setAggressionLevel(AggressionLevel.PEACEFUL);
        assertFalse(manager.canTargetPlayer(target));

        // Set player to aggressive by hurting the entity.
        manager.playerHurtEntity(target);

        // No changes expected.

        manager.setAggressionLevel(AggressionLevel.NORMAL);
        assertTrue(manager.canHurtPlayer(target));

        manager.setAggressionLevel(AggressionLevel.PASSIVE);
        assertTrue(manager.canHurtPlayer(target));

        manager.setAggressionLevel(AggressionLevel.PEACEFUL);
        assertFalse(manager.canTargetPlayer(target));

        // Deaggro after 2 ticks.
        manager.tick();
        manager.tick();

        // No changes expected, again.
        manager.setAggressionLevel(AggressionLevel.NORMAL);
        assertTrue(manager.canHurtPlayer(target));

        manager.setAggressionLevel(AggressionLevel.PASSIVE);
        assertTrue(manager.canHurtPlayer(target));

        manager.setAggressionLevel(AggressionLevel.PEACEFUL);
        assertFalse(manager.canTargetPlayer(target));
    }
}