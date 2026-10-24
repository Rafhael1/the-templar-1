package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnemyEffectsTest {
    private LevelData level(LevelData.EnemyKind kind, Bounds... obstacles) {
        var solids = new ArrayList<>(List.of(new Bounds(0, 0, 3200, 64)));
        solids.addAll(List.of(obstacles));
        return new LevelData(3200, 544, new LevelData.Point(100, 64), new LevelData.Point(1400, 64),
            List.of(new Bounds(650, 72, 28, 28)), new Bounds(2950, 64, 40, 176),
            new Bounds(3010, 64, 110, 100), new Bounds(2200, 64, 750, 256), solids,
            List.of(new LevelData.Spawn(kind == LevelData.EnemyKind.OIL_THROWER ? 300 : 165, 64, kind),
                new LevelData.Spawn(2700, 64, LevelData.EnemyKind.GUARDIAN)));
    }

    private List<GameEvent> until(GameWorld world, GameEvent last) {
        var events = new ArrayList<GameEvent>();
        for (int tick = 0; tick < 400 && !events.contains(last); tick++) {
            world.update(GameWorld.STEP, PlayerInput.NONE);
            events.addAll(world.drainEvents());
        }
        assertTrue(events.contains(last), last.name());
        return events;
    }

    @Test
    void maceCuesAreOrderedAndImpactEmittedOnceEvenOnAMiss() {
        GameWorld world = new GameWorld(level(LevelData.EnemyKind.BRUTE));
        until(world, GameEvent.MACE_LIFT);
        world.player().x = 500;
        var events = until(world, GameEvent.MACE_IMPACT);
        assertTrue(events.indexOf(GameEvent.MACE_SWING) < events.indexOf(GameEvent.MACE_IMPACT));
        assertEquals(1, events.stream().filter(e -> e == GameEvent.MACE_IMPACT).count());
        assertEquals(CombatEffect.Kind.MACE_IMPACT, world.effects().getFirst().kind());
        for (int i = 0; i < 30; i++) world.update(GameWorld.STEP, PlayerInput.NONE);
        assertFalse(world.drainEvents().contains(GameEvent.MACE_IMPACT));
    }

    @Test
    void cancelledWindupDoesNotProduceAnImpact() {
        GameWorld world = new GameWorld(level(LevelData.EnemyKind.BRUTE));
        until(world, GameEvent.MACE_LIFT);
        world.enemies().getFirst().health = 0;
        for (int i = 0; i < 160; i++) world.update(GameWorld.STEP, PlayerInput.NONE);
        assertFalse(world.drainEvents().contains(GameEvent.MACE_IMPACT));
        assertTrue(world.effects().isEmpty());
    }

    @Test
    void spearAndThrowSoundsFollowTheirReleaseInsteadOfEveryAnimationFrame() {
        for (var kind : List.of(LevelData.EnemyKind.SPEARMAN, LevelData.EnemyKind.OIL_THROWER)) {
            var release = kind == LevelData.EnemyKind.SPEARMAN ? GameEvent.SPEAR_THRUST : GameEvent.OIL_THROW;
            var prepare = kind == LevelData.EnemyKind.SPEARMAN ? GameEvent.SPEAR_DRAW : GameEvent.OIL_PRIME;
            var events = until(new GameWorld(level(kind)), release);
            assertEquals(1, events.stream().filter(e -> e == prepare).count());
            assertEquals(1, events.stream().filter(e -> e == release).count());
            assertTrue(events.indexOf(prepare) < events.indexOf(release));
        }
    }

    @Test
    void potImpactProducesOneShatterAndOnlyFloorsIgnite() {
        for (boolean wall : new boolean[]{false, true}) {
            var map = wall ? level(LevelData.EnemyKind.OIL_THROWER, new Bounds(200, 64, 1, 300))
                : level(LevelData.EnemyKind.OIL_THROWER);
            var pots = new ArrayList<>(List.of(new OilPot(100, 104, 500, 64)));
            var events = new ArrayList<GameEvent>();
            var effects = new ArrayList<CombatEffect>();
            var system = new OilSystem();
            var player = new Player(new LevelData.Point(1000, 64));
            var collision = new CollisionWorld(map);
            for (int tick = 0; tick < 180; tick++)
                system.update(pots, player, collision, false, GameWorld.STEP, events, effects);
            assertEquals(1, events.stream().filter(e -> e == GameEvent.POT_SHATTER).count());
            assertEquals(wall ? 0 : 1, events.stream().filter(e -> e == GameEvent.FIRE_IGNITE).count());
            assertEquals(wall ? 1 : 2, effects.size());
        }
    }

    @Test
    void effectsFreezeOnPauseExpireAndClearOnRespawn() {
        GameWorld world = new GameWorld(level(LevelData.EnemyKind.BRUTE));
        until(world, GameEvent.MACE_IMPACT);
        var effect = world.effects().getFirst();
        assertThrows(UnsupportedOperationException.class, () -> world.effects().clear());
        double age = effect.age();
        world.setPaused(true);
        for (int i = 0; i < 60; i++) world.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(age, effect.age());
        world.setPaused(false);
        world.player().x = 500;
        for (int i = 0; i < 60; i++) world.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(world.effects().isEmpty());
        world.player().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.respawn();
        assertTrue(world.effects().isEmpty());
    }
}
