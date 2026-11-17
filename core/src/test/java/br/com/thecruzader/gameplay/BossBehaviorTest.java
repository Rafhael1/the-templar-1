package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        boolean updateSkill(Enemy enemy, double tempo, java.util.List<Arrow> arrows) {
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

    private LevelData level(Bounds... obstacles) {
        var solids = new ArrayList<>(List.of(new Bounds(0, 0, 6400, 64)));
        solids.addAll(List.of(obstacles));
        return new LevelData(6400, 544, new LevelData.Point(100, 64), new LevelData.Point(3008, 64),
            List.of(new Bounds(742, 232, 28, 28)), new Bounds(6176, 64, 40, 176),
            new Bounds(6240, 64, 128, 100), new Bounds(64, 64, 6112, 256), solids,
            List.of(new LevelData.Spawn(5900, 64, LevelData.EnemyKind.GUARDIAN)));
    }

    private Enemy enemy(double x, LevelData.EnemyKind kind) {
        Enemy enemy = new Enemy(new LevelData.Spawn(x, 64, kind));
        enemy.grounded = true;
        return enemy;
    }

    @Test
    void differentSkillsReuseApproachAttackRecoveryAndDeath() {
        SwordBossBehavior behavior = new SwordBossBehavior();
        Enemy boss = new Enemy(new LevelData.Spawn(100, 64, LevelData.EnemyKind.GUARDIAN), behavior);
        Player player = new Player(new LevelData.Point(500, 64));
        EnemySystem system = new EnemySystem(new CollisionWorld(level()), new java.util.ArrayList<>());
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

    @Test
    void bossParryBonusIsConsumedByOnlyOneCounterattackAndExpires() {
        Player player = new Player(new LevelData.Point(100, 64));
        Enemy boss = enemy(150, LevelData.EnemyKind.GUARDIAN);
        boss.stagger = .40;
        boss.riposteReady = true;
        CombatSystem combat = new CombatSystem();
        player.attack.start();
        player.attack.update(.11);
        combat.resolve(player, List.of(boss), new ArrayList<>());
        assertEquals(boss.maxHealth() - 2, boss.health());
        assertFalse(boss.vulnerable());
        player.attack.cancel();
        player.attack.start();
        player.attack.update(.11);
        combat.resolve(player, List.of(boss), new ArrayList<>());
        assertEquals(boss.maxHealth() - 3, boss.health());
        boss.stagger = GameWorld.STEP;
        boss.riposteReady = true;
        new EnemySystem(new CollisionWorld(level()), new java.util.ArrayList<>()).update(boss, player, GameWorld.STEP,
            new ArrayList<>(), false);
        assertFalse(boss.vulnerable());
        assertFalse(boss.riposteReady);
    }

    @Test
    void bothGuardianHeaviesBreakHeldGuardButRespectParryAndDodge() {
        for (Enemy.State state : List.of(Enemy.State.DASH, Enemy.State.JUMP)) {
            for (int defense = 0; defense < 3; defense++) {
                Player player = new Player(new LevelData.Point(100, 64));
                player.blocking = defense != 2;
                player.parryWindow = defense == 1 ? .12 : 0;
                player.invulnerable = defense == 2 ? .24 : 0;
                Enemy boss = enemy(120, LevelData.EnemyKind.GUARDIAN);
                boss.state = state;
                var events = new ArrayList<GameEvent>();
                new CombatSystem().resolve(player, List.of(boss), events);
                assertEquals(player.maxHealth() - (defense == 0 ? 1 : 0), player.health());
                assertEquals(defense == 0 ? List.of(GameEvent.HURT)
                    : defense == 1 ? List.of(GameEvent.PARRY) : List.of(), events);
            }
        }
    }

    @Test
    void woundedGuardianChainsDashIntoTelegraphedSwordThenLeavesPunishWindow() {
        Enemy boss = enemy(300, LevelData.EnemyKind.GUARDIAN);
        boss.health = boss.maxHealth() / 2;
        boss.nextMove = 1;
        EnemySystem system = new EnemySystem(new CollisionWorld(level()), new java.util.ArrayList<>());
        Player player = new Player(new LevelData.Point(240, 64));
        var arrows = new ArrayList<Arrow>();
        system.update(boss, player, .01, arrows, false);
        assertEquals(Enemy.State.DASH_WINDUP, boss.state());
        system.update(boss, player, .37, arrows, false);
        assertEquals(Enemy.State.DASH, boss.state());
        system.update(boss, player, .29, arrows, false);
        assertEquals(Enemy.State.WINDUP, boss.state());
        assertTrue(boss.attack.running());
        system.update(boss, player, .56, arrows, false);
        assertEquals(Enemy.State.ATTACK, boss.state());
        system.update(boss, player, .16, arrows, false);
        assertEquals(Enemy.State.RECOVER, boss.state());
        assertFalse(boss.vulnerable(), "Missed heavies do not grant free bonus damage");
        assertFalse(boss.comboSword);
    }

    @Test
    void vulnerableBossTakesDoubleDamageButOrdinaryStaggerDoesNot() {
        for (LevelData.EnemyKind kind : List.of(LevelData.EnemyKind.SENTINEL, LevelData.EnemyKind.GUARDIAN)) {
            Player player = new Player(new LevelData.Point(100, 64));
            player.attack.start();
            player.attack.update(.11);
            Enemy enemy = enemy(150, kind);
            enemy.stagger = .40;
            enemy.riposteReady = true;
            new CombatSystem().resolve(player, List.of(enemy), new ArrayList<>());
            assertEquals(enemy.maxHealth() - (enemy.boss() ? 2 : 1), enemy.health());
        }
    }

    @Test
    void bossShieldStopsFrontHitsOncePerSwing() {
        Enemy boss = enemy(145, LevelData.EnemyKind.GUARDIAN);
        boss.state = Enemy.State.BLOCK;
        boss.facing = -1;
        Player player = new Player(new LevelData.Point(100, 64));
        player.attack.start();
        player.attack.update(.11);
        var events = new ArrayList<GameEvent>();
        CombatSystem combat = new CombatSystem();
        combat.resolve(player, List.of(boss), events);
        combat.resolve(player, List.of(boss), events);
        assertEquals(boss.maxHealth(), boss.health());
        assertEquals(List.of(GameEvent.BLOCK), events);
    }

    @Test
    void bossShieldLeavesRearAndDownwardAttacksOpen() {
        for (boolean downward : new boolean[]{false, true}) {
            Enemy boss = enemy(145, LevelData.EnemyKind.GUARDIAN);
            boss.state = Enemy.State.BLOCK;
            boss.facing = -1;
            Player player = new Player(new LevelData.Point(downward ? 150 : 210, downward ? 140 : 64));
            player.facing = -1;
            player.attackDirection = downward ? Player.AttackDirection.DOWN : Player.AttackDirection.SIDE;
            player.attack.start();
            player.attack.update(.11);
            new CombatSystem().resolve(player, List.of(boss), new ArrayList<>());
            assertEquals(boss.maxHealth() - 1, boss.health());
        }
    }

    @Test
    void bossTelegraphsDashesThenLeapsAndRecoversOnLanding() {
        Enemy boss = enemy(100, LevelData.EnemyKind.GUARDIAN);
        boss.grounded = true;
        Player player = new Player(new LevelData.Point(320, 64));
        EnemySystem system = new EnemySystem(new CollisionWorld(level()), new java.util.ArrayList<>());
        var arrows = new ArrayList<Arrow>();
        system.update(boss, player, .01, arrows, true);
        assertEquals(Enemy.State.BLOCK, boss.state());
        system.update(boss, player, .76, arrows, true);
        assertEquals(Enemy.State.RECOVER, boss.state());
        system.update(boss, player, .26, arrows, true);
        assertEquals(Enemy.State.DASH_WINDUP, boss.state());
        assertEquals(0, boss.vx);
        system.update(boss, player, .46, arrows, true);
        assertEquals(Enemy.State.DASH, boss.state());
        system.update(boss, player, .01, arrows, true);
        assertEquals(480, boss.vx);
        system.update(boss, player, .3, arrows, true);
        assertEquals(Enemy.State.RECOVER, boss.state());
        system.update(boss, player, .66, arrows, true);
        assertEquals(Enemy.State.JUMP_WINDUP, boss.state());
        system.update(boss, player, .51, arrows, true);
        assertEquals(Enemy.State.JUMP, boss.state());
        assertTrue(boss.vy > 0);
        assertFalse(boss.grounded);
        CollisionWorld collision = new CollisionWorld(level());
        double highest = boss.y;
        for (int tick = 0; tick < 110; tick++) {
            system.update(boss, player, GameWorld.STEP, arrows, true);
            collision.move(boss, GameWorld.STEP, false);
            highest = Math.max(highest, boss.y);
        }
        assertTrue(highest > 150);
        assertEquals(64, boss.y(), .001);
        assertEquals(Enemy.State.RECOVER, boss.state());
    }

    @Test
    void woundedBossHasFasterTelegraphs() {
        Enemy boss = enemy(100, LevelData.EnemyKind.GUARDIAN);
        boss.grounded = true;
        boss.health = boss.maxHealth() / 2;
        new EnemySystem(new CollisionWorld(level()), new java.util.ArrayList<>()).update(boss, new Player(new LevelData.Point(300, 64)),
            .01, new ArrayList<>(), true);
        assertEquals(.6, boss.actionTime, .001);
    }

    @Test
    void bossDashBreaksHeldShieldAndDealsOneHitPerMove() {
        for (boolean block : new boolean[]{false, true}) {
            Player player = new Player(new LevelData.Point(100, 64));
            player.blocking = block;
            player.facing = 1;
            Enemy boss = enemy(120, LevelData.EnemyKind.GUARDIAN);
            boss.state = Enemy.State.DASH;
            var events = new ArrayList<GameEvent>();
            CombatSystem combat = new CombatSystem();
            combat.resolve(player, List.of(boss), events);
            player.invulnerable = 0;
            combat.resolve(player, List.of(boss), events);
            assertEquals(player.maxHealth() - 1, player.health());
            assertEquals(1, events.size());
        }
    }
}
