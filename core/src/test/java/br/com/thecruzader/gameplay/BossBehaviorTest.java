package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BossBehaviorTest {
    /**
     * A different skill set using the same boss and enemy lifecycle.
     */
    private static final class SwordBossBehavior extends BossBehavior {
        int skillsStarted;

        SwordBossBehavior() {
            super(20, 90, .2, .1, .4);
        }

        @Override
        boolean updateSkill(Enemy enemy, double tempo) {
            return false;
        }

        @Override
        void selectSkill(Enemy enemy, double tempo) {
            skillsStarted++;
            enemy.attack.start();
            enemy.hitPlayer = false;
            enemy.state = Enemy.State.WINDUP;
        }

        @Override
        boolean contactAttackActive(Enemy enemy) {
            return enemy.attack.phase() == Attack.Phase.ACTIVE;
        }

        @Override
        Bounds contactAttackBounds(Enemy enemy) {
            return enemy.weaponBounds();
        }
    }

    private final LevelData level = new LevelData(3200, 544,
        new LevelData.Point(100, 64), new LevelData.Point(1400, 64),
        List.of(new Bounds(600, 64, 28, 28)), new Bounds(2950, 64, 40, 176),
        new Bounds(3010, 64, 110, 100), new Bounds(2200, 64, 750, 256),
        List.of(new Bounds(0, 0, 3200, 64)),
        List.of(new LevelData.Spawn(2700, 64, LevelData.EnemyKind.GUARDIAN)));

    @Test
    void differentSkillsReuseApproachAttackRecoveryAndDeath() {
        SwordBossBehavior behavior = new SwordBossBehavior();
        Enemy boss = new Enemy(new LevelData.Spawn(100, 64, LevelData.EnemyKind.GUARDIAN), behavior);
        Player player = new Player(new LevelData.Point(500, 64));
        EnemySystem system = new EnemySystem(new CollisionWorld(level), new java.util.ArrayList<>());
        var arrows = new ArrayList<Arrow>();
        boss.grounded = true;
        assertEquals(20, boss.health());
        assertEquals(90, boss.range());
        system.update(boss, player, .01, arrows, false);
        assertEquals(Enemy.State.CHASE, boss.state());
        assertEquals(0, behavior.skillsStarted);
        player.x = 170;
        system.update(boss, player, .01, arrows, false);
        assertEquals(Enemy.State.WINDUP, boss.state());
        assertEquals(1, behavior.skillsStarted);
        system.update(boss, player, .21, arrows, false);
        assertEquals(Enemy.State.ATTACK, boss.state());
        new CombatSystem().resolve(player, List.of(boss), new ArrayList<>());
        assertEquals(2, player.health());
        system.update(boss, player, .11, arrows, false);
        assertEquals(Enemy.State.RECOVER, boss.state());
        system.update(boss, player, .41, arrows, false);
        assertEquals(2, behavior.skillsStarted);
        boss.health = 0;
        system.update(boss, player, .01, arrows, false);
        assertEquals(Enemy.State.DEAD, boss.state());
        assertEquals(2, behavior.skillsStarted);
    }
}
