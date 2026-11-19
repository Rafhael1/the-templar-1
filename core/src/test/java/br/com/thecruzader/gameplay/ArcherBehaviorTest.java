package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArcherBehaviorTest {
    private CollisionWorld terrain(Bounds... platforms) {
        var solids = new ArrayList<>(List.of(new Bounds(0, 0, 3200, 64)));
        solids.addAll(List.of(platforms));
        return new CollisionWorld(new LevelData(3200, 544, new LevelData.Point(100, 64),
            new LevelData.Point(1400, 64), List.of(new Bounds(650, 64, 28, 28)),
            new Bounds(2950, 64, 40, 176), new Bounds(3010, 64, 110, 100),
            new Bounds(2200, 64, 750, 256), solids,
            List.of(new LevelData.Spawn(2700, 64, LevelData.EnemyKind.GUARDIAN))));
    }

    @Test
    void closeRangeShootsFirstThenBackstepsWithoutTurningItsBack() {
        var collision = terrain();
        var system = new EnemySystem(collision, new ArrayList<>());
        var archer = new Enemy(new LevelData.Spawn(300, 64, LevelData.EnemyKind.ARCHER));
        archer.grounded = true;
        var player = new Player(new LevelData.Point(240, 64));
        var arrows = new ArrayList<Arrow>();
        double start = archer.x();
        for (int tick = 0; tick < 60 && arrows.isEmpty(); tick++) {
            system.update(archer, player, GameWorld.STEP, arrows, true);
            collision.move(archer, GameWorld.STEP, true);
            assertEquals(start, archer.x(), .001, "Shoot before trying to create distance");
        }
        assertEquals(1, arrows.size(), "First shot must leave within half a second at close range");
        for (int tick = 0; tick < 20; tick++) {
            system.update(archer, player, GameWorld.STEP, arrows, true);
            collision.move(archer, GameWorld.STEP, true);
        }
        assertTrue(archer.x() > start);
        assertEquals(-1, archer.facing());
        for (int tick = 0; tick < 3 * 120; tick++) {
            system.update(archer, player, GameWorld.STEP, arrows, true);
            collision.move(archer, GameWorld.STEP, true);
        }
        assertTrue(arrows.size() >= 3, "Retreat must not delay the next shots indefinitely");
    }

    @Test
    void corneredArcherHoldsThePlatformEdgeAndKeepsShooting() {
        var collision = terrain(new Bounds(300, 160, 80, 32));
        var system = new EnemySystem(collision, new ArrayList<>());
        var archer = new Enemy(new LevelData.Spawn(350, 192, LevelData.EnemyKind.ARCHER));
        archer.grounded = true;
        var player = new Player(new LevelData.Point(310, 192));
        var arrows = new ArrayList<Arrow>();
        for (int tick = 0; tick < 3 * 120; tick++) {
            system.update(archer, player, GameWorld.STEP, arrows, true);
            collision.move(archer, GameWorld.STEP, true);
            assertEquals(350, archer.x(), .001);
            assertEquals(192, archer.y(), .001);
        }
        assertTrue(arrows.size() >= 3);
    }
}
