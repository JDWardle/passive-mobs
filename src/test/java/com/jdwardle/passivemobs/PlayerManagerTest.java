package com.jdwardle.passivemobs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerManagerTest {
    @Test
    void isPlayerAggressive() {
        PlayerManager manager = new PlayerManager();

        assertFalse(manager.isPlayerAggressive());

        manager.setPlayerAggressive(true);

        assertTrue(manager.isPlayerAggressive());
    }

    @Test
    void setPlayerAggressive() {
        PlayerManager manager = new PlayerManager();

        manager.setPlayerAggressive(true);
        assertTrue(manager.isPlayerAggressive());
        assertEquals(0, manager.getLastAggressionTimestamp());

        manager.tick();

        manager.setPlayerAggressive(false);
        assertFalse(manager.isPlayerAggressive());
        assertEquals(0, manager.getLastAggressionTimestamp());

        manager.tick();

        manager.setPlayerAggressive(true);
        assertTrue(manager.isPlayerAggressive());
        assertEquals(2, manager.getLastAggressionTimestamp());
    }

    @Test
    void tick() {
        PlayerManager manager = new PlayerManager(2);

        manager.tick();

        assertFalse(manager.isPlayerAggressive());

        manager.setPlayerAggressive(true);
        manager.tick();

        assertTrue(manager.isPlayerAggressive());

        manager.tick();
        assertFalse(manager.isPlayerAggressive());
    }

    @Test
    void canBeTargeted() {
        PlayerManager manager = new PlayerManager();

        assertTrue(manager.canBeTargeted(AggressionLevel.NORMAL));
        assertFalse(manager.canBeTargeted(AggressionLevel.PASSIVE));
        assertFalse(manager.canBeTargeted(AggressionLevel.PEACEFUL));

        manager.setPlayerAggressive(true);
        assertTrue(manager.canBeTargeted(AggressionLevel.NORMAL));
        assertTrue(manager.canBeTargeted(AggressionLevel.PASSIVE));
        assertFalse(manager.canBeTargeted(AggressionLevel.PEACEFUL));

        manager.setPlayerAggressive(false);
        assertTrue(manager.canBeTargeted(AggressionLevel.NORMAL));
        assertFalse(manager.canBeTargeted(AggressionLevel.PASSIVE));
        assertFalse(manager.canBeTargeted(AggressionLevel.PEACEFUL));
    }

    @Test
    void canBeDamaged() {
        PlayerManager manager = new PlayerManager();

        assertTrue(manager.canBeDamaged(AggressionLevel.NORMAL));
        assertTrue(manager.canBeDamaged(AggressionLevel.PASSIVE));
        assertFalse(manager.canBeDamaged(AggressionLevel.PEACEFUL));

        manager.setPlayerAggressive(true);
        assertTrue(manager.canBeDamaged(AggressionLevel.NORMAL));
        assertTrue(manager.canBeDamaged(AggressionLevel.PASSIVE));
        assertFalse(manager.canBeDamaged(AggressionLevel.PEACEFUL));

        manager.setPlayerAggressive(false);
        assertTrue(manager.canBeDamaged(AggressionLevel.NORMAL));
        assertTrue(manager.canBeDamaged(AggressionLevel.PASSIVE));
        assertFalse(manager.canBeDamaged(AggressionLevel.PEACEFUL));
    }
}