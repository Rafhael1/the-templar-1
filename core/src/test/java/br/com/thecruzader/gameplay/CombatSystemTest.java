package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CombatSystemTest {
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
        return new PlayerInput(0, false, attack, dodge, false, block, -1);
    }

    private GameWorld world() {
        return new GameWorld(new LevelData(3200, 544,
            new LevelData.Point(100, 64), new LevelData.Point(1400, 64),
            List.of(new Bounds(740, 232, 22, 30)), new Bounds(2950, 64, 40, 176),
            new Bounds(3010, 64, 110, 100), new Bounds(64, 64, 2886, 256), List.of(new Bounds(0, 0, 3200, 64)),
            List.of(new LevelData.Spawn(620, 64, LevelData.EnemyKind.SENTINEL), new LevelData.Spawn(2700, 64, LevelData.EnemyKind.GUARDIAN))));
    }

    private Enemy strike(double x, int facing) {
        Enemy enemy = new Enemy(new LevelData.Spawn(x, 64, LevelData.EnemyKind.SENTINEL));
        enemy.facing = facing;
        enemy.attack.start();
        enemy.attack.update(.56);
        return enemy;
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
        new ProjectileSystem().update(arrows, player, List.of(), new CollisionWorld(level()), false, .5, events);
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
        world.update(GameWorld.STEP, new PlayerInput(0, false, true, false, true, false, -1));
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
    void simultaneousJumpAndDownAttackLocksDirection() {
        GameWorld w = world();
        w.update(GameWorld.STEP, PlayerInput.NONE);
        w.update(GameWorld.STEP, new PlayerInput(0, true, true, false, true, false, -1));
        assertFalse(w.player().grounded());
        assertEquals(Player.AttackDirection.DOWN, w.player().attackDirection());
        w.update(GameWorld.STEP, new PlayerInput(-1, false, true, false, false, false, -1));
        assertEquals(Player.AttackDirection.DOWN, w.player().attackDirection());
        assertEquals(1, w.player().attack().sequence());
    }

    @Test
    void groundedDownInputUsesSideAttack() {
        GameWorld w = world();
        w.update(GameWorld.STEP, PlayerInput.NONE);
        w.update(GameWorld.STEP, new PlayerInput(0, false, true, false, true, false, -1));
        assertEquals(Player.AttackDirection.SIDE, w.player().attackDirection());
    }

    @Test
    void downwardAttackRequiresLandingBeforeReuseEvenAfterAMissOrBounce() {
        for (boolean hit : new boolean[]{false, true}) {
            GameWorld w = world();
            Player p = w.player();
            p.y = 300;
            PlayerInput downAttack = new PlayerInput(0, false, true, false, true, false, -1);
            w.update(GameWorld.STEP, downAttack);
            p.attack.update(.11);
            if (hit) {
                Enemy below = new Enemy(new LevelData.Spawn(p.x, p.y - 70, LevelData.EnemyKind.SENTINEL));
                new CombatSystem().resolve(p, List.of(below), new ArrayList<>());
                assertEquals(420, p.vy);
            }
            for (int i = 0; i < 45; i++) w.update(GameWorld.STEP, downAttack);
            assertFalse(p.grounded());
            assertFalse(p.attack.running());
            assertEquals(1, p.attack.sequence(), "Repeated presses cannot chain downward attacks in the air");
            w.update(GameWorld.STEP, new PlayerInput(0, false, true, false, false, false, -1));
            assertEquals(2, p.attack.sequence(), "Side attacks remain available in the air");
            assertEquals(Player.AttackDirection.SIDE, p.attackDirection());
            for (int i = 0; i < 180; i++) w.update(GameWorld.STEP, PlayerInput.NONE);
            assertTrue(p.grounded());
            w.update(GameWorld.STEP, new PlayerInput(0, true, true, false, true, false, -1));
            assertEquals(3, p.attack.sequence());
            assertEquals(Player.AttackDirection.DOWN, p.attackDirection());
        }
    }

    @Test
    void downwardAttackHitsBelowOnceAndStopsDealingDamageOnLanding() {
        Player p = new Player(new LevelData.Point(100, 150));
        p.attackDirection = Player.AttackDirection.DOWN;
        Enemy below = new Enemy(new LevelData.Spawn(104, 64, LevelData.EnemyKind.SENTINEL));
        Enemy beside = new Enemy(new LevelData.Spawn(150, 150, LevelData.EnemyKind.SENTINEL));
        var events = new ArrayList<GameEvent>();
        CombatSystem combat = new CombatSystem();
        p.attack.start();
        combat.resolve(p, List.of(below, beside), events);
        assertEquals(3, below.health());
        p.attack.update(.11);
        combat.resolve(p, List.of(below, beside), events);
        combat.resolve(p, List.of(below, beside), events);
        assertEquals(2, below.health());
        assertEquals(3, beside.health());
        p.grounded = true;
        below.lastPlayerAttack = -1;
        combat.resolve(p, List.of(below), events);
        assertEquals(2, below.health());
    }

    @Test
    void shieldBlocksFrontFromEitherFacingButNotRear() {
        for (int facing : new int[]{-1, 1}) {
            Player p = new Player(new LevelData.Point(100, 64));
            p.blocking = true;
            p.facing = facing;
            Enemy front = strike(100 + facing * 45, -facing);
            var events = new ArrayList<GameEvent>();
            CombatSystem combat = new CombatSystem();
            combat.resolve(p, List.of(front), events);
            combat.resolve(p, List.of(front), events);
            assertEquals(p.maxHealth(), p.health());
            assertEquals(List.of(GameEvent.BLOCK), events);
            assertTrue(p.blockFlash() > 0);
            Enemy rear = strike(100 - facing * 45, facing);
            combat.resolve(p, List.of(rear), events);
            assertEquals(p.maxHealth() - 1, p.health());
        }
    }

    @Test
    void blockingSlowsMovementPreventsAttackAndDodgeAndEndsOnRelease() {
        GameWorld w = world();
        w.update(GameWorld.STEP, new PlayerInput(1, false, true, true, false, true, -1));
        assertTrue(w.player().blocking());
        assertEquals(70, w.player().speed());
        assertFalse(w.player().attack().running());
        assertFalse(w.player().dodging());
        w.update(GameWorld.STEP, new PlayerInput(1, false, true, false, false, false, -1));
        assertFalse(w.player().blocking());
        assertTrue(w.player().attack().running());
        w.update(GameWorld.STEP, new PlayerInput(0, false, false, false, false, true, -1));
        assertFalse(w.player().blocking());
    }

    @Test
    void shieldCanBlockInAirButCannotInterruptDodge() {
        GameWorld w = world();
        w.update(GameWorld.STEP, PlayerInput.NONE);
        w.update(GameWorld.STEP, new PlayerInput(0, true, false, false, false, true, -1));
        assertFalse(w.player().grounded());
        assertTrue(w.player().blocking());
        w.update(GameWorld.STEP, new PlayerInput(0, false, false, true, false, false, -1));
        assertTrue(w.player().dodging());
        w.update(GameWorld.STEP, new PlayerInput(0, false, false, false, false, true, -1));
        assertFalse(w.player().blocking());
    }

    @Test
    void windupRecoveryAndActiveWindowControlDamage() {
        Player p = new Player(new LevelData.Point(100, 64));
        Enemy e = new Enemy(new LevelData.Spawn(145, 64, LevelData.EnemyKind.SENTINEL));
        var events = new ArrayList<GameEvent>();
        CombatSystem combat = new CombatSystem();
        p.attack.start();
        combat.resolve(p, List.of(e), events);
        assertEquals(3, e.health());
        p.attack.update(.11);
        combat.resolve(p, List.of(e), events);
        assertEquals(2, e.health());
        combat.resolve(p, List.of(e), events);
        assertEquals(2, e.health(), "One hit per target per swing");
        p.attack.update(.13);
        assertEquals(Attack.Phase.RECOVERY, p.attack.phase());
        e.lastPlayerAttack = -1;
        combat.resolve(p, List.of(e), events);
        assertEquals(2, e.health());
        p.attack.update(1);
        assertTrue(p.attack.start());
        p.attack.update(.11);
        combat.resolve(p, List.of(e), events);
        assertEquals(1, e.health());
    }

    @Test
    void attackCannotRestartDuringRecovery() {
        Attack attack = new Attack(.1, .12, .22);
        attack.start();
        attack.update(.25);
        assertEquals(Attack.Phase.RECOVERY, attack.phase());
        assertFalse(attack.start());
    }

    @Test
    void enemyStrikeDealsDamageOnlyOnce() {
        Player p = new Player(new LevelData.Point(100, 64));
        Enemy e = new Enemy(new LevelData.Spawn(145, 64, LevelData.EnemyKind.SENTINEL));
        e.attack.start();
        e.attack.update(.60);
        var events = new ArrayList<GameEvent>();
        CombatSystem combat = new CombatSystem();
        combat.resolve(p, List.of(e), events);
        assertEquals(p.maxHealth() - 1, p.health());
        p.invulnerable = 0;
        combat.resolve(p, List.of(e), events);
        assertEquals(p.maxHealth() - 1, p.health());
    }
}
