package br.com.thecruzader.gameplay;

import static org.junit.jupiter.api.Assertions.*;
import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.LevelData;
import br.com.thecruzader.level.LevelLoader;
import com.badlogic.gdx.files.FileHandle;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class BossArenaTest {
    private LevelData level() { return LevelLoader.load(new FileHandle(
            System.getProperty("assets.path") + "/levels/exiles-road/maps/exiles-road.tmx")); }
    private GameWorld entered() {
        GameWorld world = new GameWorld(level());
        world.player().x = world.level().arena().x() + 8;
        world.player().invulnerable = 100;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        return world;
    }
    @Test void guardianWaitsForEntryAndEncounterStartsOnlyOnce() {
        GameWorld world = new GameWorld(level());
        world.player().x = world.level().arena().x() - 40;
        world.player().invulnerable = 100;
        for (int tick = 0; tick < 150; tick++) world.update(GameWorld.STEP, PlayerInput.NONE);
        assertFalse(world.arenaLocked());
        assertEquals(8576, world.guardian().x(), .001);
        assertEquals(0, world.guardian().attack.sequence());
        world.drainEvents();
        world.player().x = world.level().arena().x();
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(world.arenaLocked());
        assertEquals(1, world.drainEvents().stream().filter(e -> e == GameEvent.ARENA_START).count());
        for (int tick = 0; tick < 120; tick++) world.update(GameWorld.STEP, PlayerInput.NONE);
        assertFalse(world.drainEvents().contains(GameEvent.ARENA_START));
        assertNotEquals(Enemy.State.PATROL, world.guardian().state());
    }
    @Test void playerCannotWalkDashOrJumpOutOfEitherGate() {
        LevelData level = level();
        CollisionWorld collision = new CollisionWorld(level);
        collision.setArenaClosed(true);
        for (int direction : new int[] {-1, 1}) {
            for (boolean jump : new boolean[] {false, true}) {
                Player player = new Player(new LevelData.Point(direction < 0 ? level.arena().x() + 8
                        : level.arena().right() - Player.WIDTH - 8, 64));
                player.dodge = .2;
                player.vx = direction * 500;
                player.vy = jump ? 570 : 0;
                for (int tick = 0; tick < 120; tick++) {
                    player.vy -= 1450 * GameWorld.STEP;
                    collision.move(player, GameWorld.STEP, true);
                    assertTrue(player.x() >= level.arena().x() - .001);
                    assertTrue(player.x() + Player.WIDTH <= level.arena().right() + .001);
                }
            }
        }
    }
    @Test void guardianDashesAndJumpsStayInsideTheArena() {
        LevelData level = level();
        CollisionWorld collision = new CollisionWorld(level);
        collision.setArenaClosed(true);
        for (int direction : new int[] {-1, 1}) {
            Enemy boss = new Enemy(new LevelData.Spawn(direction < 0 ? level.arena().x() + 4
                    : level.arena().right() - 46, 64, LevelData.EnemyKind.GUARDIAN));
            boss.vx = direction * 580;
            boss.vy = 570;
            for (int tick = 0; tick < 120; tick++) {
                collision.move(boss, GameWorld.STEP, true);
                assertTrue(boss.x() >= level.arena().x() - .001);
                assertTrue(boss.bounds().right() <= level.arena().right() + .001);
            }
        }
    }
    @Test void sealedEntranceStopsArrowsFromTheRoad() {
        LevelData level = level();
        CollisionWorld collision = new CollisionWorld(level);
        collision.setArenaClosed(true);
        Player player = new Player(new LevelData.Point(level.arena().x() + 60, 64));
        var arrows = new ArrayList<>(List.of(new Arrow(level.arenaEntrance().x() - 24, 96,
                player.x(), 96)));
        for (int tick = 0; tick < 90; tick++) new ProjectileSystem().update(arrows, player, collision,
                true, GameWorld.STEP, new ArrayList<>());
        assertTrue(arrows.isEmpty());
        assertEquals(Player.MAX_HEALTH, player.health());
    }
    @Test void defeatingGuardianOpensBothEndsAndAllowsVictory() {
        GameWorld world = entered();
        world.drainEvents();
        world.guardian().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(world.arenaLocked());
        assertTrue(world.gateLocked());
        assertEquals(1, world.drainEvents().stream().filter(e -> e == GameEvent.BOSS_DEFEATED).count());
        for (int tick = 0; tick < 560; tick++) world.update(GameWorld.STEP, PlayerInput.NONE);
        assertFalse(world.arenaLocked());
        assertFalse(world.gateLocked());
        assertFalse(world.bossReleaseActive());
        assertEquals(1, world.drainEvents().stream().filter(e -> e == GameEvent.GATES_OPEN).count());
        for (int tick = 0; tick < 60; tick++) world.update(GameWorld.STEP,
                new PlayerInput(-1, false, false, true, false, false));
        assertTrue(world.player().x() < world.arenaEntrance().x());
        assertFalse(world.insideArena());
        world.player().x = world.level().arena().x() + 8;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(world.insideArena());
        assertFalse(world.arenaLocked());
        assertFalse(world.drainEvents().contains(GameEvent.ARENA_START));
        world.player().x = world.level().gate().right() + 2;
        assertFalse(world.insideArena());
        for (int tick = 0; tick < 120; tick++) world.update(GameWorld.STEP,
                new PlayerInput(1, false, false, false, false, false));
        assertEquals(GameWorld.State.WON, world.state());
    }
    @Test void deathAndRespawnResetTheEncounterAndReopenTheEntrance() {
        GameWorld world = new GameWorld(level());
        world.player().x = world.level().checkpoint().x();
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.player().x = world.level().arena().x() + 8;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(world.arenaLocked());
        world.player().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.respawn();
        assertFalse(world.arenaEntered());
        assertFalse(world.insideArena());
        assertTrue(world.gateLocked());
        assertEquals(world.level().checkpoint().x(), world.player().x(), .001);
        world.player().x = world.arenaEntrance().right() - 12;
        world.player().invulnerable = 100;
        for (int tick = 0; tick < 60; tick++) world.update(GameWorld.STEP,
                new PlayerInput(-1, false, false, false, false, false));
        assertTrue(world.player().x() < world.arenaEntrance().x());
    }
    @Test void pausedWorldDoesNotStartTheFight() {
        GameWorld world = new GameWorld(level());
        world.player().x = world.level().arena().x() + 8;
        world.setPaused(true);
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertFalse(world.arenaEntered());
        assertTrue(world.drainEvents().isEmpty());
    }

    @Test void angelsKeepBothGatesClosedUntilOpeningFinishesAndHoldCombatUntilTheyAscend() {
        GameWorld world = entered();
        world.guardian().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        double x = world.player().x();
        var input = new PlayerInput(1, true, true, true, false, false);
        for (int tick = 0; tick < 360; tick++) world.update(GameWorld.STEP, input);
        assertTrue(world.gateLocked());
        assertTrue(world.arenaLocked());
        assertEquals(x, world.player().x());
        assertFalse(world.player().attack().running());
        for (int tick = 0; tick < 60; tick++) world.update(GameWorld.STEP, input);
        assertFalse(world.gateLocked());
        assertTrue(world.bossReleaseActive());
        assertEquals(x, world.player().x());
        for (int tick = 0; tick < 150; tick++) world.update(GameWorld.STEP, PlayerInput.NONE);
        assertFalse(world.bossReleaseActive());
        world.update(GameWorld.STEP, new PlayerInput(1, false, false, false, false, false));
        assertTrue(world.player().x() > x);
    }

    @Test void pausingStopsTheReleaseClockAndGateOpeningEventsOccurOnce() {
        GameWorld world = entered();
        world.guardian().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        for (int tick = 0; tick < 120; tick++) world.update(GameWorld.STEP, PlayerInput.NONE);
        double elapsed = world.bossReleaseElapsed();
        world.setPaused(true);
        for (int tick = 0; tick < 600; tick++) world.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(elapsed, world.bossReleaseElapsed());
        assertTrue(world.gateLocked());
        world.setPaused(false);
        for (int tick = 0; tick < 600; tick++) world.update(GameWorld.STEP, PlayerInput.NONE);
        var events = world.drainEvents();
        assertEquals(1, events.stream().filter(e -> e == GameEvent.BOSS_DEFEATED).count());
        assertEquals(1, events.stream().filter(e -> e == GameEvent.GATES_OPEN).count());
        for (int tick = 0; tick < 600; tick++) world.update(GameWorld.STEP, PlayerInput.NONE);
        assertFalse(world.drainEvents().contains(GameEvent.GATES_OPEN));
    }

    @Test void respawnClearsAnInterruptedAngelSequence() {
        GameWorld world = entered();
        world.guardian().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.player().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(GameWorld.State.DEAD, world.state());
        world.respawn();
        assertFalse(world.bossDefeated());
        assertFalse(world.bossReleaseActive());
        assertEquals(0, world.bossReleaseElapsed());
        assertTrue(world.gateLocked());
        assertTrue(world.drainEvents().isEmpty());
    }
}
