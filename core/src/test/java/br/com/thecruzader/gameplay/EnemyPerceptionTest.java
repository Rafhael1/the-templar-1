package br.com.thecruzader.gameplay;

import static org.junit.jupiter.api.Assertions.*;
import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EnemyPerceptionTest {
    private CollisionWorld terrain(Bounds... obstacles) {
        var solids = new ArrayList<>(List.of(new Bounds(0, 0, 3200, 64)));
        solids.addAll(List.of(obstacles));
        return new CollisionWorld(new LevelData(3200, 544, new LevelData.Point(96, 64),
                new LevelData.Point(1400, 64), List.of(new Bounds(800, 72, 28, 28)),
                new Bounds(2950, 64, 40, 176), new Bounds(3010, 64, 110, 100),
                new Bounds(2200, 64, 750, 256), solids,
                List.of(new LevelData.Spawn(2700, 64, LevelData.EnemyKind.GUARDIAN))));
    }
    private Enemy sentinel(double x) {
        var enemy = new Enemy(new LevelData.Spawn(x, 64, LevelData.EnemyKind.SENTINEL));
        enemy.grounded = true;
        return enemy;
    }
    private void advance(EnemySystem system, CollisionWorld terrain, Enemy enemy, Player player, int ticks) {
        for (int i = 0; i < ticks; i++) {
            system.update(enemy, player, GameWorld.STEP, new ArrayList<>(), true);
            terrain.move(enemy, GameWorld.STEP, true);
        }
    }

    @Test void seeingANearbyPlayerFirstPausesToNoticeThenPursues() {
        var terrain = terrain();
        var system = new EnemySystem(terrain);
        var enemy = sentinel(500);
        var player = new Player(new LevelData.Point(300, 64));
        advance(system, terrain, enemy, player, 24);
        assertEquals(Enemy.State.NOTICE, enemy.state());
        assertEquals(500, enemy.x());
        assertFalse(enemy.attack.running());
        advance(system, terrain, enemy, player, 36);
        assertEquals(Enemy.State.CHASE, enemy.state());
        assertTrue(enemy.x() < 500);
    }

    @Test void playerBehindThePatrolRemainsUnnoticedUntilClose() {
        var terrain = terrain();
        var system = new EnemySystem(terrain);
        var enemy = sentinel(500);
        var player = new Player(new LevelData.Point(700, 64));
        advance(system, terrain, enemy, player, 120);
        assertEquals(Enemy.State.PATROL, enemy.state());
        assertEquals(0, enemy.alertTime);
        player.x = enemy.x + 50;
        advance(system, terrain, enemy, player, 1);
        assertEquals(Enemy.State.WINDUP, enemy.state());
        assertEquals(1, enemy.facing());
    }

    @Test void wallsAndPlatformUndersidesPreventInitialDetection() {
        for (boolean platform : new boolean[] {false, true}) {
            var terrain = platform ? terrain(new Bounds(448, 128, 160, 32))
                    : terrain(new Bounds(560, 64, 32, 200));
            var system = new EnemySystem(terrain);
            var enemy = sentinel(platform ? 480 : 500);
            enemy.facing = 1;
            var player = new Player(new LevelData.Point(platform ? 520 : 650, platform ? 160 : 64));
            advance(system, terrain, enemy, player, 120);
            assertEquals(Enemy.State.PATROL, enemy.state());
            assertEquals(0, enemy.alertTime);
            assertEquals(0, enemy.attack.sequence());
        }
    }

    @Test void aBriefGlimpseDoesNotAccumulateAcrossSeparateEncounters() {
        var terrain = terrain();
        var system = new EnemySystem(terrain);
        var enemy = sentinel(500);
        var player = new Player(new LevelData.Point(300, 64));
        advance(system, terrain, enemy, player, 24);
        player.x = 1400;
        advance(system, terrain, enemy, player, 1);
        assertEquals(Enemy.State.PATROL, enemy.state());
        player.x = 300;
        advance(system, terrain, enemy, player, 24);
        assertEquals(Enemy.State.NOTICE, enemy.state());
        assertEquals(0, enemy.alertTime);
    }

    @Test void losingSightKeepsTheLastSeenPositionInsteadOfTrackingThroughTheWall() {
        var terrain = terrain(new Bounds(600, 64, 32, 200));
        var system = new EnemySystem(terrain);
        var enemy = sentinel(400);
        enemy.facing = 1;
        var player = new Player(new LevelData.Point(570, 64));
        advance(system, terrain, enemy, player, 60);
        double lastSeen = enemy.targetX;
        player.x = 700;
        advance(system, terrain, enemy, player, 60);
        assertEquals(lastSeen, enemy.targetX);
        assertEquals(Enemy.State.CHASE, enemy.state());
        advance(system, terrain, enemy, player, 240);
        assertEquals(Enemy.State.PATROL, enemy.state());
        assertTrue(enemy.calmTime > 0);
    }

    @Test void beingHitImmediatelyBreaksCalmAndAlertsTheEnemy() {
        var enemy = sentinel(500);
        enemy.calmTime = 4;
        var player = new Player(new LevelData.Point(460, 64));
        player.attack.start();
        player.attack.update(.11);
        new CombatSystem().resolve(player, List.of(enemy), new ArrayList<>());
        assertEquals(2, enemy.health());
        assertEquals(0, enemy.calmTime);
        assertTrue(enemy.alertTime > 0);
        assertEquals(player.x + Player.WIDTH / 2 - enemy.width() / 2, enemy.targetX);
    }
}
