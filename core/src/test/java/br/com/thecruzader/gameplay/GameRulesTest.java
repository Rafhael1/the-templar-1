package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameRulesTest {
    private GameWorld world(int hearts) {
        LevelData level = new LevelData(3200, 544,
            new LevelData.Point(100, 64), new LevelData.Point(1400, 64),
            List.of(new Bounds(600, 64, 28, 28)), new Bounds(2950, 64, 40, 176),
            new Bounds(3010, 64, 110, 100), new Bounds(2200, 64, 750, 256),
            List.of(new Bounds(0, 0, 3200, 64)),
            List.of(new LevelData.Spawn(2700, 64, LevelData.EnemyKind.GUARDIAN)));
        return new GameWorld(level, new GameRules(hearts));
    }

    @Test
    void defaultIsThreeHearts() {
        Player player = new Player(new LevelData.Point(100, 64));
        assertEquals(3, player.health());
        assertEquals(3, player.maxHealth());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 7})
    void bandagesUseConfiguredCapacity(int hearts) {
        GameWorld world = world(hearts);
        Player player = world.player();
        assertEquals(hearts, player.health());
        player.x = 600;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertFalse(world.bandageCollected(0));
        if (hearts == 1) return; // Zero health is death, so one-heart sessions cannot need healing.
        player.health = hearts - 1;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(hearts, player.health());
        assertTrue(world.bandageCollected(0));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 7})
    void checkpointAndRespawnRestoreConfiguredCapacity(int hearts) {
        GameWorld world = world(hearts);
        world.player().x = 1400;
        world.player().health = Math.max(1, hearts - 1);
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(world.checkpoint());
        assertEquals(hearts, world.player().health());
        world.player().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.respawn();
        assertEquals(hearts, world.player().maxHealth());
        assertEquals(hearts, world.player().health());
        assertEquals(1400, world.player().x());
    }

    @Test
    void rejectsNonPositiveHearts() {
        assertThrows(IllegalArgumentException.class, () -> new GameRules(0));
        assertThrows(IllegalArgumentException.class, () -> new GameRules(-1));
    }
}
