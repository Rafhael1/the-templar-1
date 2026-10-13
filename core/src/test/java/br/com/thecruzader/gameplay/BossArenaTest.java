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
            System.getProperty("assets.path") + "/maps/exiles-road.tmx")); }
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
        assertFalse(world.arenaLocked());
        assertFalse(world.gateLocked());
        assertEquals(1, world.drainEvents().stream().filter(e -> e == GameEvent.BOSS_DEFEATED).count());
        for (int tick = 0; tick < 60; tick++) world.update(GameWorld.STEP,
                new PlayerInput(-1, false, false, true, false, false));
        assertTrue(world.player().x() < world.arenaEntrance().x());
        world.player().x = world.level().gate().right() + 2;
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
}
