package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnemyVariantsTest {
    private LevelData level(Bounds... obstacles) {
        var solids = new ArrayList<>(List.of(new Bounds(0, 0, 3200, 64)));
        solids.addAll(List.of(obstacles));
        return new LevelData(3200, 544, new LevelData.Point(100, 64), new LevelData.Point(1400, 64),
            List.of(new Bounds(650, 64, 28, 28)), new Bounds(2950, 64, 40, 176),
            new Bounds(3010, 64, 110, 100), new Bounds(2200, 64, 750, 256), solids,
            List.of(new LevelData.Spawn(2700, 64, LevelData.EnemyKind.GUARDIAN)));
    }

    private Enemy enemy(LevelData.EnemyKind kind) {
        Enemy enemy = new Enemy(new LevelData.Spawn(300, 64, kind));
        enemy.grounded = true;
        enemy.facing = -1;
        return enemy;
    }

    private Player striking(double x, double y, boolean down) {
        Player player = new Player(new LevelData.Point(x, y));
        player.facing = x < 300 ? 1 : -1;
        player.attackDirection = down ? Player.AttackDirection.DOWN : Player.AttackDirection.SIDE;
        player.attack.start();
        player.attack.update(.11);
        return player;
    }

    @Test
    void commonEnemiesApproachOrRetreatBrisklyAccordingToTheirWeapon() {
        for (LevelData.EnemyKind kind : LevelData.EnemyKind.values()) {
            if (kind.boss) continue;
            Enemy enemy = enemy(kind);
            boolean ranged = kind == LevelData.EnemyKind.ARCHER || kind == LevelData.EnemyKind.OIL_THROWER;
            Player player = new Player(new LevelData.Point(ranged ? 240 : 100, 64));
            EnemyPerception.alert(enemy, player);
            new EnemySystem(new CollisionWorld(level()), new ArrayList<>()).update(
                enemy, player, GameWorld.STEP, new ArrayList<>(), false);
            assertEquals(Enemy.State.CHASE, enemy.state(), kind.name());
            assertTrue(Math.abs(enemy.vx) >= 80, "Brisk movement for " + kind);
            assertEquals(ranged ? 1 : -1, Math.signum(enemy.vx), kind.name());
        }
    }

    @Test
    void shieldGuardsFrontIncludingRecoveryButAllowsRearAndAirHits() {
        for (int scenario = 0; scenario < 4; scenario++) {
            Enemy enemy = enemy(LevelData.EnemyKind.SHIELD_BEARER);
            if (scenario == 3) {
                enemy.attack.start();
                enemy.attack.update(1.10);
                enemy.updateAttackState();
            }
            Player player = striking(scenario == 1 ? 335 : scenario == 2 ? 300 : 260,
                scenario == 2 ? 140 : 64, scenario == 2);
            var events = new ArrayList<GameEvent>();
            new CombatSystem().resolve(player, List.of(enemy), events);
            boolean guarded = scenario == 0 || scenario == 3;
            assertEquals(enemy.maxHealth() - (guarded ? 0 : 1), enemy.health(), "scenario=" + scenario);
            assertTrue(events.contains(guarded ? GameEvent.BLOCK : GameEvent.HIT));
            if (scenario == 2) assertEquals(420, player.vy);
        }
    }

    @Test
    void shieldStaysRaisedDuringWindupAndParryCreatesAnOpening() {
        Enemy enemy = enemy(LevelData.EnemyKind.SHIELD_BEARER);
        assertTrue(enemy.blocking());
        enemy.attack.start();
        assertTrue(enemy.blocking());
        enemy.attack.update(.66);
        Player player = new Player(new LevelData.Point(260, 64));
        player.blocking = true;
        player.parryWindow = .12;
        new CombatSystem().resolve(player, List.of(enemy), new ArrayList<>());
        assertFalse(enemy.attack.running());
        assertFalse(enemy.blocking());
        assertEquals(.45, enemy.stagger());
        assertEquals(player.maxHealth(), player.health());
        new EnemySystem(new CollisionWorld(level()), new ArrayList<>()).update(
            enemy, player, .46, new ArrayList<>(), false);
        assertTrue(enemy.blocking(), "Guard returns when the parry stagger ends");
    }

    @Test
    void shieldIsLoweredWhileStrikingAndBothSwordsCanConnect() {
        Enemy enemy = enemy(LevelData.EnemyKind.SHIELD_BEARER);
        enemy.attack.start();
        enemy.attack.update(.66);
        Player player = striking(260, 64, false);
        var events = new ArrayList<GameEvent>();
        new CombatSystem().resolve(player, List.of(enemy), events);
        assertEquals(enemy.maxHealth() - 1, enemy.health());
        assertEquals(player.maxHealth() - 1, player.health());
        assertEquals(List.of(GameEvent.HIT, GameEvent.HURT), events);
        assertFalse(enemy.blocking());
    }

    @Test
    void guardDropsForStrikeAndReturnsAfterTheShieldIsRaised() {
        Enemy enemy = enemy(LevelData.EnemyKind.SHIELD_BEARER);
        enemy.attack.start();
        enemy.attack.update(.64);
        assertTrue(enemy.blocking());
        enemy.attack.update(.02);
        assertEquals(Attack.Phase.ACTIVE, enemy.attack.phase());
        assertFalse(enemy.blocking());
        enemy.attack.update(.16);
        assertEquals(Attack.Phase.RECOVERY, enemy.attack.phase());
        assertFalse(enemy.blocking());
        enemy.attack.update(.24);
        assertTrue(enemy.blocking());
    }

    @Test
    void bruteCommitsToDirectionAndLeavesALongRecoveryAfterAMiss() {
        Enemy enemy = enemy(LevelData.EnemyKind.BRUTE);
        Player player = new Player(new LevelData.Point(240, 64));
        EnemyPerception.alert(enemy, player);
        EnemySystem system = new EnemySystem(new CollisionWorld(level()), new ArrayList<>());
        system.update(enemy, player, .01, new ArrayList<>(), false);
        assertEquals(Enemy.State.WINDUP, enemy.state());
        assertTrue(enemy.heavyAttack());
        player.x = 350;
        system.update(enemy, player, .94, new ArrayList<>(), false);
        assertEquals(Enemy.State.WINDUP, enemy.state());
        system.update(enemy, player, .02, new ArrayList<>(), false);
        assertEquals(Enemy.State.ATTACK, enemy.state());
        assertEquals(-1, enemy.facing());
        new CombatSystem().resolve(player, List.of(enemy), new ArrayList<>());
        assertEquals(player.maxHealth(), player.health());
        system.update(enemy, player, .18, new ArrayList<>(), false);
        system.update(enemy, player, 1.20, new ArrayList<>(), false);
        assertEquals(Enemy.State.RECOVER, enemy.state());
        assertEquals(1, enemy.attack.sequence());
    }

    @Test
    void maceBreaksHeldShieldButCanBeParriedOrDodgedFromEitherSide() {
        for (int facing : new int[]{-1, 1})
            for (int defense = 0; defense < 3; defense++) {
                Enemy enemy = enemy(LevelData.EnemyKind.BRUTE);
                enemy.facing = facing;
                enemy.attack.start();
                enemy.attack.update(.96);
                Player player = new Player(new LevelData.Point(facing < 0 ? 240 : 350, 64));
                player.facing = -facing;
                player.blocking = defense != 2;
                player.parryWindow = defense == 1 ? .12 : 0;
                player.invulnerable = defense == 2 ? .2 : 0;
                var events = new ArrayList<GameEvent>();
                new CombatSystem().resolve(player, List.of(enemy), events);
                assertEquals(player.maxHealth() - (defense == 0 ? 1 : 0), player.health());
                if (defense == 1) {
                    assertEquals(List.of(GameEvent.PARRY), events);
                    assertFalse(enemy.attack.running());
                }
            }
    }

    @Test
    void oilThrowerFiresOnePotAtCommittedTargetAndHasNoContactDamage() {
        Enemy enemy = enemy(LevelData.EnemyKind.OIL_THROWER);
        Player player = new Player(new LevelData.Point(100, 64));
        EnemyPerception.alert(enemy, player);
        var pots = new ArrayList<OilPot>();
        var system = new EnemySystem(new CollisionWorld(level()), pots);
        system.update(enemy, player, .01, new ArrayList<>(), false);
        assertEquals(Enemy.State.WINDUP, enemy.state());
        assertEquals(114, enemy.aimX());
        player.x = 200;
        system.update(enemy, player, .84, new ArrayList<>(), false);
        assertTrue(pots.isEmpty());
        system.update(enemy, player, .02, new ArrayList<>(), false);
        system.update(enemy, player, .01, new ArrayList<>(), false);
        assertEquals(1, pots.size());
        assertEquals((114 - 315) / OilPot.FLIGHT, pots.getFirst().vx, .001);
        assertFalse(enemy.behavior.contactAttackActive(enemy));
        assertThrows(IllegalStateException.class, () -> enemy.behavior.contactAttackBounds(enemy));
    }

    @Test
    void blockedArcPreventsThrowEvenWhenDirectSightIsClear() {
        CollisionWorld collision = new CollisionWorld(level(new Bounds(0, 148, 800, 32)));
        assertTrue(collision.clearShot(315, 104, 114, 64, false));
        Enemy enemy = enemy(LevelData.EnemyKind.OIL_THROWER);
        Player player = new Player(new LevelData.Point(100, 64));
        enemy.targetX = player.x;
        assertFalse(((GroundEnemyBehavior) enemy.behavior).canAttack(enemy, player, collision, false));
    }

    private void advanceOil(List<OilPot> pots, Player player, CollisionWorld collision, int ticks) {
        var system = new OilSystem();
        for (int i = 0; i < ticks; i++)
            system.update(pots, player, collision, false, GameWorld.STEP, new ArrayList<>(), new ArrayList<>());
    }

    @Test
    void potLandsThenFireIgnoresShieldRespectsInvulnerabilityAndExpires() {
        var pots = new ArrayList<>(List.of(new OilPot(100, 104, 300, 64)));
        Player player = new Player(new LevelData.Point(1000, 64));
        CollisionWorld collision = new CollisionWorld(level());
        advanceOil(pots, player, collision, 120);
        assertEquals(1, pots.size());
        assertTrue(pots.getFirst().burning());
        assertEquals(64, pots.getFirst().y());
        assertEquals(72, pots.getFirst().fireBounds().width(), .001);
        player.x = 290;
        player.blocking = true;
        player.parryWindow = .12;
        advanceOil(pots, player, collision, 1);
        assertEquals(player.maxHealth() - 1, player.health());
        advanceOil(pots, player, collision, 10);
        assertEquals(player.maxHealth() - 1, player.health());
        advanceOil(pots, player, collision, 400);
        assertTrue(pots.isEmpty());
    }

    @Test
    void potShattersOnWallsAndDoesNotPassThroughThinTerrain() {
        var pots = new ArrayList<>(List.of(new OilPot(100, 104, 500, 64)));
        Player player = new Player(new LevelData.Point(1000, 64));
        advanceOil(pots, player, new CollisionWorld(level(new Bounds(200, 64, 1, 300))), 120);
        assertTrue(pots.isEmpty());
    }

    @Test
    void flameStopsAtPlatformEdgeAndAdjacentWall() {
        var pots = new ArrayList<>(List.of(new OilPot(200, 250, 300, 160)));
        CollisionWorld collision = new CollisionWorld(level(new Bounds(260, 128, 60, 32), new Bounds(315, 160, 5, 60)));
        advanceOil(pots, new Player(new LevelData.Point(1000, 64)), collision, 120);
        assertEquals(1, pots.size());
        assertTrue(pots.getFirst().burning());
        assertEquals(315, pots.getFirst().fireBounds().right(), .001);
        assertTrue(pots.getFirst().fireBounds().x() >= 260);
    }

    @Test
    void potsCanBeBlockedAndParriedButRearHitsHurt() {
        for (int defense = 0; defense < 3; defense++) {
            var pots = new ArrayList<>(List.of(new OilPot(100, 104, 300, 64)));
            Player player = new Player(new LevelData.Point(190, 152));
            player.blocking = true;
            player.facing = defense == 2 ? 1 : -1;
            player.parryWindow = defense == 1 ? .12 : 0;
            advanceOil(pots, player, new CollisionWorld(level()), 90);
            assertTrue(pots.isEmpty());
            assertEquals(player.maxHealth() - (defense == 2 ? 1 : 0), player.health());
            if (defense == 1) assertTrue(player.parryFlash > 0);
        }
    }

    @Test
    void worldClearsPotsOnRespawnArenaEntryAndBossDefeat() {
        for (int transition = 0; transition < 3; transition++) {
            GameWorld emptyWorld = new GameWorld(level());
            // Public views cannot be used to inject hazards.
            assertThrows(UnsupportedOperationException.class,
                () -> emptyWorld.oilPots().add(new OilPot(100, 104, 300, 64)));
            // Spawn through a real thrower instead of modifying world collections.
            LevelData base = level();
            GameWorld world = new GameWorld(new LevelData(base.width(), base.height(), base.spawn(), base.checkpoint(),
                base.bandages(), base.gate(), base.exit(), base.arena(), base.solids(),
                List.of(new LevelData.Spawn(300, 64, LevelData.EnemyKind.OIL_THROWER), base.enemies().getFirst())));
            for (int tick = 0; tick < 180 && world.oilPots().isEmpty(); tick++)
                world.update(GameWorld.STEP, PlayerInput.NONE);
            assertFalse(world.oilPots().isEmpty());
            if (transition == 0) {
                world.player().health = 0;
                world.update(GameWorld.STEP, PlayerInput.NONE);
                world.respawn();
            } else {
                world.player().x = 2250;
                world.update(GameWorld.STEP, PlayerInput.NONE);
                if (transition == 2) {
                    assertTrue(world.oilPots().isEmpty());
                    Enemy thrower = world.enemies().getFirst();
                    thrower.x = 2500;
                    thrower.attack.cancel();
                    EnemyPerception.alert(thrower, world.player());
                    for (int tick = 0; tick < 180 && world.oilPots().isEmpty(); tick++)
                        world.update(GameWorld.STEP, PlayerInput.NONE);
                    assertFalse(world.oilPots().isEmpty());
                    world.guardian().health = 0;
                    world.update(GameWorld.STEP, PlayerInput.NONE);
                }
            }
            assertTrue(world.oilPots().isEmpty());
        }
    }
}
