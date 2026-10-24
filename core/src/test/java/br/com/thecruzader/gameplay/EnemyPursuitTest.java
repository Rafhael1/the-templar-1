package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnemyPursuitTest {
    private CollisionWorld terrain(Bounds... obstacles) {
        var solids = new ArrayList<>(List.of(new Bounds(0, 0, 3200, 64)));
        solids.addAll(List.of(obstacles));
        return new CollisionWorld(new LevelData(3200, 544, new LevelData.Point(96, 64),
                new LevelData.Point(1400, 64), List.of(new Bounds(800, 72, 28, 28)),
                new Bounds(2950, 64, 40, 176), new Bounds(3010, 64, 110, 100),
                new Bounds(2200, 64, 750, 256), solids,
                List.of(new LevelData.Spawn(2700, 64, LevelData.EnemyKind.GUARDIAN))));
    }
    private void tick(EnemySystem system, CollisionWorld terrain, Enemy enemy, Player player) {
        enemy.previousX = enemy.x;
        enemy.previousY = enemy.y;
        system.update(enemy, player, GameWorld.STEP, new ArrayList<>(), true);
        terrain.move(enemy, GameWorld.STEP, true);
        for (Bounds solid : terrain.level().solids()) assertFalse(enemy.bounds().overlaps(solid));
    }

    @Test void sentinelsJumpOntoSolidPlatformsFromEitherSide() {
        for (int direction : new int[] {-1, 1}) {
            CollisionWorld terrain = terrain(new Bounds(448, 128, 160, 32));
            EnemySystem system = new EnemySystem(terrain, new java.util.ArrayList<>());
            Enemy enemy = new Enemy(new LevelData.Spawn(direction > 0 ? 300 : 760, 64, LevelData.EnemyKind.SENTINEL));
            Player player = new Player(new LevelData.Point(520, 160));
            enemy.facing = direction;
            boolean jumped = false, landed = false;
            for (int tick = 0; tick < 360; tick++) {
                tick(system, terrain, enemy, player);
                jumped |= enemy.vy() > 0;
                landed |= enemy.grounded && Math.abs(enemy.y() - 160) < .01;
            }
            assertTrue(jumped, "Enemy should launch, not jump against the platform underside");
            assertTrue(landed, "Enemy must actually reach the top from direction " + direction);
        }
    }

    @Test void followsTheTemplarAcrossMoreThanOnePlatform() {
        CollisionWorld terrain = terrain(new Bounds(448, 128, 160, 32), new Bounds(672, 192, 160, 32));
        EnemySystem system = new EnemySystem(terrain, new java.util.ArrayList<>());
        Enemy enemy = new Enemy(new LevelData.Spawn(420, 64, LevelData.EnemyKind.SENTINEL));
        Player player = new Player(new LevelData.Point(740, 224));
        // This route test begins after acquisition; intervening platforms now correctly obstruct sight.
        EnemyPerception.alert(enemy, player);
        boolean first = false, second = false;
        for (int tick = 0; tick < 720; tick++) {
            tick(system, terrain, enemy, player);
            first |= enemy.grounded && Math.abs(enemy.y() - 160) < .01;
            second |= enemy.grounded && Math.abs(enemy.y() - 224) < .01;
        }
        assertTrue(first);
        assertTrue(second);
    }

    @Test void archersPursueWhenTheirShotIsBlockedByThePlatform() {
        CollisionWorld terrain = terrain(new Bounds(448, 128, 160, 32));
        EnemySystem system = new EnemySystem(terrain, new java.util.ArrayList<>());
        Enemy enemy = new Enemy(new LevelData.Spawn(480, 64, LevelData.EnemyKind.ARCHER));
        Player player = new Player(new LevelData.Point(520, 160));
        EnemyPerception.alert(enemy, player);
        assertFalse(terrain.clearShot(495, 96, 534, 186, true));
        boolean landed = false;
        for (int tick = 0; tick < 720; tick++) {
            tick(system, terrain, enemy, player);
            landed |= enemy.grounded && Math.abs(enemy.y() - 160) < .01;
        }
        assertTrue(landed);
    }

    @Test void pursuitCrossesTheOldSpawnBoundaryThenExpiresWithoutImmediateReacquisition() {
        CollisionWorld terrain = terrain();
        EnemySystem system = new EnemySystem(terrain, new java.util.ArrayList<>());
        Enemy enemy = new Enemy(new LevelData.Spawn(100, 64, LevelData.EnemyKind.SENTINEL));
        Player player = new Player(new LevelData.Point(400, 64));
        EnemyPerception.alert(enemy, player);
        for (int tick = 0; tick < 5 * 120; tick++) {
            player.x = enemy.x + 300;
            tick(system, terrain, enemy, player);
        }
        assertTrue(enemy.x() > 600, "No clamp at spawn + 230");
        for (int tick = 0; tick < 8 * 120; tick++) {
            player.x = enemy.x + 300;
            tick(system, terrain, enemy, player);
        }
        assertEquals(Enemy.State.PATROL, enemy.state());
        assertTrue(enemy.calmTime > 0);
        assertEquals(0, enemy.alertTime);
    }

    @Test void unreachableTargetsAreAbandonedAndTallWallsStaySolid() {
        CollisionWorld terrain = terrain(new Bounds(600, 64, 64, 300));
        EnemySystem system = new EnemySystem(terrain, new java.util.ArrayList<>());
        Enemy enemy = new Enemy(new LevelData.Spawn(550, 64, LevelData.EnemyKind.SENTINEL));
        Player player = new Player(new LevelData.Point(750, 64));
        EnemyPerception.alert(enemy, player);
        for (int tick = 0; tick < 4 * 120; tick++) tick(system, terrain, enemy, player);
        assertTrue(enemy.x() + enemy.width() <= 600);
        assertEquals(Enemy.State.PATROL, enemy.state());
        assertTrue(enemy.calmTime > 0);
    }

    @Test void unalertedPlatformEnemiesPatrolWithoutFallingOff() {
        CollisionWorld terrain = terrain(new Bounds(448, 128, 160, 32));
        EnemySystem system = new EnemySystem(terrain, new java.util.ArrayList<>());
        Enemy enemy = new Enemy(new LevelData.Spawn(520, 160, LevelData.EnemyKind.SENTINEL));
        Player player = new Player(new LevelData.Point(2000, 64));
        for (int tick = 0; tick < 20 * 120; tick++) {
            tick(system, terrain, enemy, player);
            assertEquals(160, enemy.y(), .01);
            assertTrue(enemy.x() >= 448 && enemy.x() + enemy.width() <= 608);
        }
    }
    @Test void reachesAPlatformOnlyOneTileWide() {
        CollisionWorld terrain = terrain(new Bounds(448, 128, 32, 32));
        EnemySystem system = new EnemySystem(terrain, new java.util.ArrayList<>());
        Enemy enemy = new Enemy(new LevelData.Spawn(300, 64, LevelData.EnemyKind.SENTINEL));
        Player player = new Player(new LevelData.Point(450, 160));
        enemy.facing = 1;
        boolean landed = false;
        for (int tick = 0; tick < 360; tick++) {
            tick(system, terrain, enemy, player);
            landed |= enemy.grounded && Math.abs(enemy.y() - 160) < .01;
        }
        assertTrue(landed);
    }
    @Test void openGatesDoNotRemainInvisibleBarriersForArcherSight() {
        CollisionWorld terrain = terrain();
        assertFalse(terrain.clearShot(2800, 100, 3100, 100, true));
        assertTrue(terrain.clearShot(2800, 100, 3100, 100, false));
        terrain.setArenaClosed(true);
        assertFalse(terrain.clearShot(2100, 100, 2300, 100, true));
        assertTrue(terrain.clearShot(2100, 100, 2300, 100, false));
    }
}
