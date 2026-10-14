package br.com.thecruzader.gameplay;

import static org.junit.jupiter.api.Assertions.*;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;
import br.com.thecruzader.level.LevelLoader;
import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

class EnemyCollisionTest {
    private CollisionWorld collision(Bounds... obstacles) {
        var solids = new ArrayList<>(List.of(new Bounds(0, 0, 3200, 64)));
        solids.addAll(List.of(obstacles));
        return new CollisionWorld(new LevelData(3200, 544,
                new LevelData.Point(96, 64), new LevelData.Point(1450, 64),
                List.of(new Bounds(742, 232, 22, 30)), new Bounds(2950, 64, 40, 176),
                new Bounds(3010, 64, 110, 100), new Bounds(64, 64, 2886, 256), solids,
                List.of(new LevelData.Spawn(2700, 64, LevelData.EnemyKind.GUARDIAN))));
    }

    private Enemy enemy(double x, double y) {
        return new Enemy(new LevelData.Spawn(x, y, LevelData.EnemyKind.SENTINEL));
    }

    @Test
    void climbsStairsInActualWorldWithoutEnteringTerrain() {
        GameWorld world = new GameWorld(LevelLoader.load(new FileHandle(
                System.getProperty("assets.path") + "/levels/exiles-road/maps/exiles-road.tmx")));
        Enemy enemy = world.enemies().get(3);
        world.player().x = 2500;
        world.player().y = 160;
        EnemyPerception.alert(enemy, world.player());
        double highest = enemy.y();
        for (int tick = 0; tick < 300; tick++) {
            world.update(GameWorld.STEP, PlayerInput.NONE);
            highest = Math.max(highest, enemy.y());
            for (Bounds solid : world.level().solids())
                assertFalse(enemy.bounds().overlaps(solid), "Enemy penetrated stair terrain");
        }
        assertTrue(enemy.x() >= 2336);
        assertTrue(highest >= 160);
        assertTrue(enemy.y() >= 96 && enemy.y() <= 160);
        assertTrue(Math.abs(enemy.x() - world.player().x()) < 100);
    }

    @Test
    void tallWallsStopMovementFromBothSides() {
        CollisionWorld collision = collision(new Bounds(200, 64, 64, 128));
        for (int direction : new int[] {-1, 1}) {
            Enemy enemy = enemy(direction > 0 ? 150 : 280, 64);
            enemy.vx = direction * 125;
            for (int tick = 0; tick < 120; tick++) collision.move(enemy, GameWorld.STEP, false);
            assertEquals(direction > 0 ? 170 : 264, enemy.x(), .001);
            assertEquals(64, enemy.y(), .001);
        }
    }

    @Test
    void descendsToGroundInsteadOfFloating() {
        CollisionWorld collision = collision(new Bounds(200, 64, 64, 32));
        Enemy enemy = enemy(220, 96);
        enemy.vx = 125;
        for (int tick = 0; tick < 120; tick++) collision.move(enemy, GameWorld.STEP, false);
        assertEquals(64, enemy.y(), .001);
        assertTrue(enemy.grounded);
    }

    @Test
    void stepUpRequiresHeadroom() {
        CollisionWorld collision = collision(new Bounds(200, 64, 64, 32),
                new Bounds(190, 120, 100, 32));
        Enemy enemy = enemy(150, 64);
        enemy.vx = 125;
        for (int tick = 0; tick < 120; tick++) collision.move(enemy, GameWorld.STEP, false);
        assertEquals(170, enemy.x(), .001);
        assertEquals(64, enemy.y(), .001);
    }

    @Test
    void gravityContinuesDuringAttack() {
        CollisionWorld collision = collision();
        Enemy enemy = enemy(200, 180);
        Player player = new Player(new LevelData.Point(240, 64));
        enemy.attack.start();
        new EnemySystem(collision).update(enemy, player, GameWorld.STEP, new java.util.ArrayList<>(), true);
        collision.move(enemy, GameWorld.STEP, false);
        assertTrue(enemy.y() < 180);
        assertEquals(0, enemy.vx);
    }

    @Test
    void lockedGateStopsEnemiesAndOpenGateAllowsPassage() {
        CollisionWorld collision = collision();
        Enemy enemy = enemy(2900, 64);
        enemy.vx = 125;
        for (int tick = 0; tick < 120; tick++) collision.move(enemy, GameWorld.STEP, true);
        assertEquals(2920, enemy.x(), .001);
        for (int tick = 0; tick < 120; tick++) collision.move(enemy, GameWorld.STEP, false);
        assertTrue(enemy.x() > 2990);
    }
}
