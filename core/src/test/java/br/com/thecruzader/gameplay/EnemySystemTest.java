package br.com.thecruzader.gameplay;

import static org.junit.jupiter.api.Assertions.*;

import br.com.thecruzader.level.LevelData;
import br.com.thecruzader.level.Bounds;
import java.util.List;
import org.junit.jupiter.api.Test;

class EnemySystemTest {
    private final Enemy enemy = new Enemy(new LevelData.Spawn(500, 64, LevelData.EnemyKind.SENTINEL));
    private final Player player = new Player(new LevelData.Point(850, 64));

    private final CollisionWorld collision = new CollisionWorld(new LevelData(3200, 544,
            new LevelData.Point(96, 64), new LevelData.Point(1450, 64),
            List.of(new Bounds(742, 232, 22, 30)), new Bounds(2950, 64, 40, 176),
            new Bounds(3010, 64, 110, 100), new Bounds(64, 64, 2886, 256), List.of(new Bounds(0, 0, 3200, 64)),
            List.of(new LevelData.Spawn(2700, 64, LevelData.EnemyKind.GUARDIAN))));

    private final EnemySystem system = new EnemySystem(collision);

    private void advance(int ticks) {
        for (int i = 0; i < ticks; i++) {
            system.update(enemy, player, GameWorld.STEP, new java.util.ArrayList<>(), true);
            collision.move(enemy, GameWorld.STEP, true);
        }
    }

    @Test
    void distantEnemiesKeepPatrollingWhenThePlayerIsOnScreen() {
        advance(60);
        assertEquals(Enemy.State.PATROL, enemy.state());
        assertEquals(0, enemy.alertTime);
        assertTrue(enemy.x() < 500);
    }

    @Test
    void followsJumpWithoutWastingAttacks() {
        player.x = 600;
        EnemyPerception.alert(enemy, player);
        player.y = 220;
        advance(120);
        assertEquals(Enemy.State.CHASE, enemy.state());
        assertFalse(enemy.attack.running());
        assertEquals(player.x + Player.WIDTH / 2, enemy.x + enemy.bounds().width() / 2, .001);
    }

    @Test
    void remembersTargetBrieflyThenReturnsToPatrol() {
        player.x = 700;
        enemy.facing = 1;
        advance(60);
        player.x = 1400;
        advance(120);
        assertEquals(Enemy.State.CHASE, enemy.state());
        advance(121);
        assertEquals(Enemy.State.PATROL, enemy.state());
        assertTrue(enemy.calmTime > 0);
    }

    @Test
    void usesWeaponReachOnEitherSide() {
        player.x = 575;
        advance(1);
        assertEquals(Enemy.State.WINDUP, enemy.state());
        Enemy left = new Enemy(new LevelData.Spawn(500, 64, LevelData.EnemyKind.SENTINEL));
        player.x = 427;
        system.update(left, player, GameWorld.STEP, new java.util.ArrayList<>(), true);
        assertEquals(Enemy.State.WINDUP, left.state());
        assertEquals(-1, left.facing());
    }

    @Test
    void commitsToAttackDirectionAndRestartsAfterRecovery() {
        player.x = 550;
        advance(1);
        player.x = 450;
        advance(68);
        assertEquals(Enemy.State.ATTACK, enemy.state());
        assertEquals(1, enemy.facing());
        advance(130);
        assertEquals(2, enemy.attack.sequence());
        assertEquals(-1, enemy.facing());
    }

    @Test
    void createsSpaceWhenPlayerOverlapsInsteadOfGettingStuck() {
        player.x = 500;
        advance(60);
        assertTrue(enemy.attack.running());
        assertTrue(enemy.weaponBounds().overlaps(player.bounds()));
    }
}
