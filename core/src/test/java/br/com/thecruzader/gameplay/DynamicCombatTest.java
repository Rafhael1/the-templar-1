package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DynamicCombatTest {
    private LevelData level() {
        return new LevelData(3200, 544, new LevelData.Point(100, 64), new LevelData.Point(1400, 64),
            List.of(new Bounds(600, 232, 22, 30)), new Bounds(2950, 64, 40, 176),
            new Bounds(3010, 64, 110, 100), new Bounds(2200, 64, 750, 256),
            List.of(new Bounds(0, 0, 3200, 64)),
            List.of(new LevelData.Spawn(2700, 64, LevelData.EnemyKind.GUARDIAN)));
    }

    private Enemy enemy(double x, LevelData.EnemyKind kind) {
        Enemy enemy = new Enemy(new LevelData.Spawn(x, 64, kind));
        enemy.grounded = true;
        return enemy;
    }

    private PlayerInput input(boolean attack, boolean dodge, boolean block) {
        return new PlayerInput(0, false, attack, dodge, false, block);
    }

    @Test
    void shieldPressOpensOneShortWindowAndHoldingCannotRefreshIt() {
        GameWorld world = new GameWorld(level());
        world.update(GameWorld.STEP, input(false, false, true));
        assertEquals(.12, world.player().parryWindow, .001);
        for (int i = 0; i < 30; i++) world.update(GameWorld.STEP, input(false, false, true));
        assertEquals(0, world.player().parryWindow);
        var events = new ArrayList<GameEvent>();
        assertFalse(CombatSystem.hitPlayer(world.player(), 180, false, events));
        assertEquals(List.of(GameEvent.BLOCK), events);
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.update(GameWorld.STEP, input(false, false, true));
        assertTrue(world.player().parryWindow > 0);
    }

    @Test
    void holdingShieldDuringAnAttackDoesNotGrantParryWhenAttackEnds() {
        GameWorld world = new GameWorld(level());
        world.update(GameWorld.STEP, input(true, false, false));
        for (int i = 0; i < 60; i++) world.update(GameWorld.STEP, input(false, false, true));
        assertTrue(world.player().blocking());
        assertEquals(0, world.player().parryWindow);
    }

    @Test
    void parryInterruptsMeleeAndGuardianDashWithoutKnockingBossBack() {
        for (LevelData.EnemyKind kind : List.of(LevelData.EnemyKind.SENTINEL, LevelData.EnemyKind.GUARDIAN)) {
            Player player = new Player(new LevelData.Point(100, 64));
            player.blocking = true;
            player.parryWindow = .12;
            Enemy enemy = enemy(120, kind);
            enemy.facing = -1;
            enemy.comboSword = true;
            if (enemy.boss()) enemy.state = Enemy.State.DASH;
            else {
                enemy.attack.start();
                enemy.attack.update(.46);
            }
            var events = new ArrayList<GameEvent>();
            new CombatSystem().resolve(player, List.of(enemy), events);
            assertEquals(List.of(GameEvent.PARRY), events);
            assertEquals(player.maxHealth(), player.health());
            assertEquals(enemy.boss() ? .40 : .45, enemy.stagger(), .001);
            assertFalse(enemy.attack.running());
            assertFalse(enemy.comboSword);
            assertEquals(enemy.boss() ? 0 : 110, enemy.knockback);
        }
    }

    @Test
    void rearHitsCannotBeParriedAndOneWindowCannotParryTwice() {
        Player player = new Player(new LevelData.Point(100, 64));
        player.blocking = true;
        player.parryWindow = .12;
        var events = new ArrayList<GameEvent>();
        assertTrue(CombatSystem.hitPlayer(player, 180, false, events));
        assertFalse(CombatSystem.hitPlayer(player, 180, false, events));
        assertEquals(List.of(GameEvent.PARRY, GameEvent.BLOCK), events);
        player.parryWindow = .12;
        assertFalse(CombatSystem.hitPlayer(player, 60, false, events));
        assertEquals(player.maxHealth() - 1, player.health());
    }

    @Test
    void damageFeedbackOnlyStartsOnHealthLossAndFreezesWithPause() {
        GameWorld world = new GameWorld(level());
        Player player = world.player();
        var events = new ArrayList<GameEvent>();
        player.blocking = true;
        CombatSystem.hitPlayer(player, 180, false, events);
        assertEquals(0, player.hurtFlash());
        player.parryWindow = .12;
        CombatSystem.hitPlayer(player, 180, false, events);
        assertEquals(0, player.hurtFlash());
        player.blocking = false;
        CombatSystem.hitPlayer(player, 180, false, events);
        assertTrue(player.hurtFlash() > 0);
        assertEquals(player.maxHealth() - 1, player.health());
        events.clear();
        CombatSystem.hurtPlayer(player, events);
        assertTrue(events.isEmpty(), "Invulnerability must not replay the impact");
        double flash = player.hurtFlash();
        world.setPaused(true);
        for (int i = 0; i < 60; i++) world.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(flash, player.hurtFlash());
        world.setPaused(false);
        for (int i = 0; i < 40; i++) world.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(0, player.hurtFlash());
        assertTrue(player.invulnerable() > 0, "Impact ends before the invulnerability blink");
        player.health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.respawn();
        assertEquals(0, world.player().hurtFlash());
    }

    @Test
    void timedShieldAlsoParriesAndRemovesArrows() {
        Player player = new Player(new LevelData.Point(240, 64));
        player.facing = -1;
        player.blocking = true;
        player.parryWindow = .12;
        var arrows = new ArrayList<>(List.of(new Arrow(100, 90, 400, 90)));
        var events = new ArrayList<GameEvent>();
        new ProjectileSystem().update(arrows, player, new CollisionWorld(level()), false, .5, events);
        assertTrue(arrows.isEmpty());
        assertEquals(List.of(GameEvent.PARRY), events);
        assertEquals(player.maxHealth(), player.health());
    }

    @Test
    void latePressBuffersOneAttackAndKeepsItsRequestedDirection() {
        GameWorld world = new GameWorld(level());
        Player player = world.player();
        player.attack.start();
        player.attack.update(.34);
        player.y = 180;
        world.update(GameWorld.STEP, new PlayerInput(0, false, true, false, true, false));
        assertEquals(1, player.attack.sequence());
        for (int i = 0; i < 13; i++) world.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(2, player.attack.sequence());
        assertEquals(Player.AttackDirection.DOWN, player.attackDirection());
        for (int i = 0; i < 60; i++) world.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(2, player.attack.sequence());
    }

    @Test
    void earlyAttackPressIsNotBufferedAndShieldClearsQueuedAttack() {
        GameWorld world = new GameWorld(level());
        world.update(GameWorld.STEP, input(true, false, false));
        world.update(GameWorld.STEP, input(true, false, false));
        for (int i = 0; i < 60; i++) world.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(1, world.player().attack.sequence());
        world.player().attack.start();
        world.player().attack.update(.34);
        world.update(GameWorld.STEP, input(true, false, false));
        for (int i = 0; i < 20; i++) world.update(GameWorld.STEP, input(false, false, true));
        assertEquals(2, world.player().attack.sequence());
        assertTrue(world.player().blocking());
    }

    @Test
    void dodgeOnlyCancelsLateRecoveryAndDiscardsBufferedAttack() {
        GameWorld world = new GameWorld(level());
        world.update(GameWorld.STEP, input(true, false, false));
        world.update(GameWorld.STEP, input(false, true, false));
        assertFalse(world.player().dodging());
        world.player().attack.update(.33);
        world.update(GameWorld.STEP, input(true, true, false));
        assertTrue(world.player().dodging());
        assertFalse(world.player().attack.running());
        assertEquals(0, world.player().attackBuffer);
    }

    @Test
    void downwardHitBouncesOnlyOnceEvenWhenItHitsSeveralEnemies() {
        Player player = new Player(new LevelData.Point(100, 150));
        player.attackDirection = Player.AttackDirection.DOWN;
        player.attack.start();
        player.attack.update(.11);
        Enemy first = enemy(104, LevelData.EnemyKind.SENTINEL);
        Enemy second = enemy(108, LevelData.EnemyKind.SENTINEL);
        CombatSystem combat = new CombatSystem();
        combat.resolve(player, List.of(first), new ArrayList<>());
        assertEquals(420, player.vy);
        player.vy = 300;
        combat.resolve(player, List.of(second), new ArrayList<>());
        assertEquals(300, player.vy);
        assertEquals(2, first.health());
        assertEquals(2, second.health());
        assertFalse(player.grounded());
    }

    @Test
    void ordinaryHitsCannotCancelCommittedEnemyAttacks() {
        for (LevelData.EnemyKind kind : LevelData.EnemyKind.values()) {
            Player player = new Player(new LevelData.Point(100, 64));
            player.attack.start();
            player.attack.update(.11);
            Enemy enemy = enemy(150, kind);
            enemy.facing = 1; // Strike the unguarded rear even when a shield stays raised.
            enemy.attack.start();
            new CombatSystem().resolve(player, List.of(enemy), new ArrayList<>());
            assertEquals(enemy.maxHealth() - 1, enemy.health());
            assertTrue(enemy.attack.running());
            assertEquals(0, enemy.stagger());
            assertEquals(0, enemy.knockback);
        }
    }

    @Test
    void idleEnemyHitReactsBrieflyButAnotherHitCannotExtendStagger() {
        Player player = new Player(new LevelData.Point(100, 64));
        Enemy enemy = enemy(150, LevelData.EnemyKind.SENTINEL);
        CombatSystem combat = new CombatSystem();
        player.attack.start();
        player.attack.update(.11);
        combat.resolve(player, List.of(enemy), new ArrayList<>());
        assertEquals(.10, enemy.stagger(), .001);
        assertEquals(110, enemy.knockback);
        enemy.stagger = .02;
        player.attack.cancel();
        player.attack.start();
        player.attack.update(.11);
        combat.resolve(player, List.of(enemy), new ArrayList<>());
        assertEquals(.02, enemy.stagger(), .001);
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
    void committedSentinelStillStrikesAfterBeingHitDuringWindup() {
        Player player = new Player(new LevelData.Point(100, 64));
        Enemy sentinel = enemy(140, LevelData.EnemyKind.SENTINEL);
        sentinel.facing = -1;
        sentinel.attack.start();
        sentinel.attack.update(.25);
        player.attack.start();
        player.attack.update(.11);
        CombatSystem combat = new CombatSystem();
        combat.resolve(player, List.of(sentinel), new ArrayList<>());
        assertEquals(2, sentinel.health());
        sentinel.attack.update(.21);
        combat.resolve(player, List.of(sentinel), new ArrayList<>());
        assertEquals(player.maxHealth() - 1, player.health());
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
    void staggerKnockbackStillRespectsWalls() {
        LevelData base = level();
        LevelData walled = new LevelData(base.width(), base.height(), base.spawn(), base.checkpoint(),
            base.bandages(), base.gate(), base.exit(), base.arena(),
            List.of(new Bounds(0, 0, 3200, 64), new Bounds(185, 64, 32, 120)), base.enemies());
        Enemy enemy = enemy(150, LevelData.EnemyKind.SENTINEL);
        enemy.stagger = .18;
        enemy.knockback = 190;
        CollisionWorld collision = new CollisionWorld(walled);
        EnemySystem system = new EnemySystem(collision, new java.util.ArrayList<>());
        for (int i = 0; i < 20; i++) {
            system.update(enemy, new Player(base.spawn()), GameWorld.STEP, new ArrayList<>(), false);
            collision.move(enemy, GameWorld.STEP, false);
            assertTrue(enemy.bounds().right() <= 185);
        }
    }

    @Test
    void archerRetreatsBeforeStartingAnotherShotButKeepsCommittedWindup() {
        Enemy archer = enemy(300, LevelData.EnemyKind.ARCHER);
        Player player = new Player(new LevelData.Point(240, 64));
        EnemySystem system = new EnemySystem(new CollisionWorld(level()), new java.util.ArrayList<>());
        system.update(archer, player, GameWorld.STEP, new ArrayList<>(), false);
        assertEquals(Enemy.State.CHASE, archer.state());
        assertTrue(archer.vx > 0);
        archer.attack.start();
        system.update(archer, player, GameWorld.STEP, new ArrayList<>(), false);
        assertEquals(Enemy.State.WINDUP, archer.state());
        assertEquals(0, archer.vx);
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
    void hitStopFreezesSimulationAndPauseAndRetainsPressedJump() {
        LevelData base = level();
        GameWorld world = new GameWorld(new LevelData(base.width(), base.height(), base.spawn(), base.checkpoint(),
            base.bandages(), base.gate(), base.exit(), base.arena(), base.solids(),
            List.of(new LevelData.Spawn(150, 64, LevelData.EnemyKind.SENTINEL), base.enemies().getFirst())));
        world.player().attack.start();
        world.player().attack.update(.11);
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(world.hitStopped());
        double time = world.time();
        world.setPaused(true);
        for (int i = 0; i < 10; i++) world.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(world.hitStopped());
        assertEquals(time, world.time());
        world.setPaused(false);
        world.update(GameWorld.STEP, new PlayerInput(0, true, false, false, false, false));
        while (world.hitStopped()) world.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(time, world.time());
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(world.player().vy > 0);
        assertFalse(world.player().grounded());
        world.player().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.respawn();
        assertFalse(world.hitStopped());
        assertEquals(0, world.player().attackBuffer);
        assertEquals(0, world.player().parryWindow);
        assertEquals(0, world.player().parryFlash());
    }

    @Test
    void killingBlowFlashExpiresInsteadOfShakingCameraForever() {
        Enemy enemy = enemy(150, LevelData.EnemyKind.SENTINEL);
        enemy.health = 0;
        enemy.flash = .14;
        EnemySystem system = new EnemySystem(new CollisionWorld(level()), new java.util.ArrayList<>());
        for (int i = 0; i < 20; i++)
            system.update(enemy, new Player(level().spawn()),
                GameWorld.STEP, new ArrayList<>(), false);
        assertEquals(0, enemy.flash());
        assertEquals(Enemy.State.DEAD, enemy.state());
    }
}
