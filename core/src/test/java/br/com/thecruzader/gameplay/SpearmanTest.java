package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SpearmanTest {
    private LevelData level(List<Bounds> solids) {
        return new LevelData(3200, 544, new LevelData.Point(96, 64), new LevelData.Point(1450, 64),
            List.of(new Bounds(742, 232, 22, 30)), new Bounds(2950, 64, 40, 176),
            new Bounds(3010, 64, 110, 100), new Bounds(2200, 64, 750, 256), solids,
            List.of(new LevelData.Spawn(2700, 64, LevelData.EnemyKind.GUARDIAN)));
    }

    private Enemy spearman() {
        Enemy enemy = new Enemy(new LevelData.Spawn(300, 64, LevelData.EnemyKind.SPEARMAN));
        enemy.grounded = true;
        return enemy;
    }

    @Test
    void startsBeyondSwordReachOnEitherSideAndCommitsUntilRecoveryEnds() {
        for (int facing : new int[]{-1, 1}) {
            Enemy enemy = spearman();
            Player player = new Player(new LevelData.Point(facing > 0 ? 420 : 190, 64));
            EnemySystem system = new EnemySystem(new CollisionWorld(level(List.of(new Bounds(0, 0, 3200, 64)))), new java.util.ArrayList<>());
            EnemyPerception.alert(enemy, player);
            system.update(enemy, player, .01, new ArrayList<>(), false);
            assertEquals(Enemy.State.WINDUP, enemy.state());
            assertEquals(facing, enemy.facing());
            assertEquals(3, enemy.health());
            assertEquals(104, enemy.range());
            player.x = facing > 0 ? 190 : 420;
            system.update(enemy, player, .59, new ArrayList<>(), false);
            assertEquals(Enemy.State.WINDUP, enemy.state());
            system.update(enemy, player, .02, new ArrayList<>(), false);
            assertEquals(Enemy.State.ATTACK, enemy.state());
            assertEquals(facing, enemy.facing());
            system.update(enemy, player, .14, new ArrayList<>(), false);
            assertEquals(Enemy.State.RECOVER, enemy.state());
            system.update(enemy, player, .85, new ArrayList<>(), false);
            assertEquals(Enemy.State.RECOVER, enemy.state());
            assertEquals(1, enemy.attack.sequence());
            assertEquals(facing, enemy.facing());
            assertEquals(0, enemy.vx);
        }
    }

    @Test
    void thrustCanBeBlockedParriedDodgedOrJumpedButHitsAnExposedPlayer() {
        for (int defense = 0; defense < 5; defense++) {
            Enemy enemy = spearman();
            enemy.attack.start();
            enemy.attack.update(.61);
            Player player = new Player(new LevelData.Point(190, defense == 3 ? 102 : 64));
            player.blocking = defense == 0 || defense == 1;
            player.parryWindow = defense == 1 ? .12 : 0;
            player.invulnerable = defense == 2 ? .20 : 0;
            var events = new ArrayList<GameEvent>();
            new CombatSystem().resolve(player, List.of(enemy), events);
            assertEquals(player.maxHealth() - (defense == 4 ? 1 : 0), player.health());
            assertEquals(switch (defense) {
                case 0 -> List.of(GameEvent.BLOCK);
                case 1 -> List.of(GameEvent.PARRY);
                case 4 -> List.of(GameEvent.HURT);
                default -> List.of();
            }, events);
            if (defense == 1) {
                assertFalse(enemy.attack.running());
                assertEquals(.45, enemy.stagger());
            }
        }
    }

    @Test
    void crossingBehindDuringWindupAvoidsTheCommittedThrust() {
        Enemy enemy = spearman();
        enemy.attack.start();
        enemy.attack.update(.61);
        Player player = new Player(new LevelData.Point(340, 64));
        new CombatSystem().resolve(player, List.of(enemy), new ArrayList<>());
        assertEquals(player.maxHealth(), player.health());
    }

    @Test
    void airborneDownwardStrikeDamagesAndBouncesOffSpearman() {
        Enemy enemy = spearman();
        Player player = new Player(new LevelData.Point(300, 140));
        player.attackDirection = Player.AttackDirection.DOWN;
        player.attack.start();
        player.attack.update(.11);
        new CombatSystem().resolve(player, List.of(enemy), new ArrayList<>());
        assertEquals(2, enemy.health());
        assertEquals(420, player.vy);
    }

    @Test
    void aMissLeavesTimeForACounterattackWithoutCancellingRecovery() {
        Enemy enemy = spearman();
        enemy.attack.start();
        enemy.attack.update(.75);
        Player player = new Player(new LevelData.Point(260, 64));
        player.attack.start();
        player.attack.update(.11);
        new CombatSystem().resolve(player, List.of(enemy), new ArrayList<>());
        assertEquals(2, enemy.health());
        assertEquals(Attack.Phase.RECOVERY, enemy.attack.phase());
        assertEquals(player.maxHealth(), player.health());
    }

    @Test
    void wallBetweenPlayerAndSpearPreventsStartingAThrust() {
        Enemy enemy = spearman();
        Player player = new Player(new LevelData.Point(190, 64));
        CollisionWorld collision = new CollisionWorld(level(List.of(new Bounds(0, 0, 3200, 64),
            new Bounds(250, 64, 16, 120))));
        EnemyPerception.alert(enemy, player);
        new EnemySystem(collision, new java.util.ArrayList<>()).update(enemy, player, .01, new ArrayList<>(), false);
        assertFalse(enemy.attack.running());
    }
}
