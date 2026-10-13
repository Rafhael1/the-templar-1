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

class GameWorldTest {
    private static final double STEP = GameWorld.STEP;

    private static GameWorld world() {
        return new GameWorld(
                LevelLoader.load(
                        new FileHandle(
                                System.getProperty("assets.path") + "/maps/exiles-road.tmx")));
    }

    private static void advance(GameWorld world, int count, PlayerInput input) {
        for (int i = 0; i < count; i++) world.update(STEP, input);
    }

    @Test
    void renderedPositionInterpolatesBetweenSimulationStepsAndResetsOnRespawn() {
        GameWorld w = world();
        double start = w.player().x();
        w.update(STEP, new PlayerInput(1, false, false, false, false, false));
        assertEquals(start, w.player().interpolatedX(0), .001);
        assertEquals(w.player().x(), w.player().interpolatedX(1), .001);
        assertEquals((start + w.player().x()) / 2, w.player().interpolatedX(.5), .001);
        w.player().health = 0;
        w.update(STEP, PlayerInput.NONE);
        w.respawn();
        assertEquals(w.player().x(), w.player().interpolatedX(0), .001);
        assertEquals(w.player().y(), w.player().interpolatedY(0), .001);
    }

    @Test
    void jumpFromGroundReturnsToGround() {
        GameWorld w = world();
        w.update(STEP, PlayerInput.NONE);
        w.update(STEP, new PlayerInput(0, true, false, false, false, false));
        assertTrue(w.player().y() > 64);
        advance(w, 130, PlayerInput.NONE);
        assertEquals(64, w.player().y(), .001);
        assertTrue(w.player().grounded());
    }

    @Test
    void airborneJumpCannotResetVerticalVelocity() {
        GameWorld w = world();
        w.update(STEP, PlayerInput.NONE);
        w.update(STEP, new PlayerInput(0, true, false, false, false, false));
        advance(w, 10, PlayerInput.NONE);
        double velocity = w.player().vy;
        w.update(STEP, new PlayerInput(0, true, false, false, false, false));
        assertTrue(w.player().vy < velocity);
    }

    @Test
    void firstPlatformIsReachableFromItsLeftEdge() {
        GameWorld w = world();
        w.player().x = 390;
        w.update(STEP, PlayerInput.NONE);
        w.update(STEP, new PlayerInput(1, true, false, false, false, false));
        advance(w, 62, new PlayerInput(1, false, false, false, false, false));
        advance(w, 30, PlayerInput.NONE);
        assertTrue(w.player().x() > 420);
        assertEquals(160, w.player().y(), .001);
    }

    @Test
    void relicRouteIsReachableThroughJumpsAndReturnsToRoad() {
        GameWorld w = world();
        w.player().health = 3;
        w.player().x = 390;
        w.update(STEP, PlayerInput.NONE);
        w.update(STEP, new PlayerInput(1, true, false, false, false, false));
        advance(w, 92, new PlayerInput(1, false, false, false, false, false));
        assertEquals(160, w.player().y(), .001);
        w.update(STEP, new PlayerInput(1, true, false, false, false, false));
        advance(w, 100, new PlayerInput(1, false, false, false, false, false));
        assertTrue(w.collectedBreadCount() > 0, "Relic must be reachable through the platform route");
        assertEquals(224, w.player().y(), .001);
        w.update(STEP, new PlayerInput(1, true, false, false, false, false));
        advance(w, 110, new PlayerInput(1, false, false, false, false, false));
        assertEquals(160, w.player().y(), .001);
        advance(w, 80, new PlayerInput(1, false, false, false, false, false));
        advance(w, 80, PlayerInput.NONE);
        assertEquals(64, w.player().y(), .001);
    }

    @Test
    void ceilingStopsAscent() {
        LevelData base = world().level();
        var solids = new ArrayList<>(base.solids());
        solids.add(new Bounds(60, 140, 200, 24));
        GameWorld w =
                new GameWorld(
                        new LevelData(
                                base.width(),
                                base.height(),
                                base.spawn(),
                                base.checkpoint(),
                                base.breads(),
                                base.gate(),
                                base.exit(), base.arena(),
                                solids,
                                base.enemies()));
        w.update(STEP, PlayerInput.NONE);
        w.update(STEP, new PlayerInput(0, true, false, false, false, false));
        for (int i = 0; i < 50; i++) {
            w.update(STEP, PlayerInput.NONE);
            assertTrue(w.player().y() + Player.HEIGHT <= 140.001);
        }
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
    void dodgeAvoidsAnActiveEnemyStrike() {
        GameWorld w = world();
        Player p = w.player();
        Enemy e = w.enemies().getFirst();
        e.x = p.x + 45;
        e.facing = -1;
        e.attack.start();
        e.attack.update(.60);
        w.update(STEP, new PlayerInput(0, false, false, true, false, false));
        assertEquals(5, p.health());
        assertTrue(p.dodging());
        assertTrue(p.invulnerable() > 0);
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
        assertEquals(4, p.health());
        p.invulnerable = 0;
        combat.resolve(p, List.of(e), events);
        assertEquals(4, p.health());
    }

    @Test
    void dodgeCooldownRejectsRepeatedRequests() {
        GameWorld w = world();
        w.update(STEP, new PlayerInput(0, false, false, true, false, false));
        advance(w, 30, PlayerInput.NONE);
        double before = w.player().dodgeCooldown();
        w.update(STEP, new PlayerInput(0, false, false, true, false, false));
        assertFalse(w.player().dodging());
        assertTrue(w.player().dodgeCooldown() < before);
    }

    @Test
    void checkpointHealsAndControlsRespawn() {
        GameWorld w = world();
        w.player().x = w.level().checkpoint().x();
        w.player().health = 2;
        w.update(STEP, PlayerInput.NONE);
        assertTrue(w.checkpoint());
        assertEquals(5, w.player().health());
        w.player().health = 0;
        w.update(STEP, PlayerInput.NONE);
        assertEquals(GameWorld.State.DEAD, w.state());
        w.respawn();
        assertEquals(GameWorld.State.PLAYING, w.state());
        assertEquals(w.level().checkpoint().x(), w.player().x());
        assertEquals(5, w.player().health());
    }

    @Test
    void respawnBeforeCheckpointReturnsToStart() {
        GameWorld w = world();
        w.player().x = 500;
        w.player().health = 0;
        w.update(STEP, PlayerInput.NONE);
        w.respawn();
        assertEquals(w.level().spawn().x(), w.player().x());
        assertFalse(w.checkpoint());
    }

    @Test
    void relicHealsOnceAndPersistsAcrossDeath() {
        GameWorld w = world();
        w.player().x = w.level().breads().getFirst().x();
        w.player().y = w.level().breads().getFirst().y();
        w.player().health = 1;
        w.update(STEP, PlayerInput.NONE);
        assertTrue(w.collectedBreadCount() > 0);
        assertEquals(3, w.player().health());
        w.update(STEP, PlayerInput.NONE);
        assertEquals(3, w.player().health());
        w.player().health = 0;
        w.update(STEP, PlayerInput.NONE);
        w.respawn();
        assertTrue(w.collectedBreadCount() > 0);
    }

    @Test
    void gatePhysicallyBlocksExitUntilGuardianDies() {
        GameWorld w = world();
        w.player().x = w.level().gate().x() - 50;
        advance(w, 100, new PlayerInput(1, false, false, false, false, false));
        assertTrue(w.player().bounds().right() <= w.level().gate().x());
        assertEquals(GameWorld.State.PLAYING, w.state());
        w.enemies().stream().filter(Enemy::boss).findFirst().orElseThrow().health = 0;
        advance(w, 100, new PlayerInput(1, false, false, false, false, false));
        assertEquals(GameWorld.State.WON, w.state());
    }

    @Test
    void pauseAndTerminalStatesFreezeSimulation() {
        GameWorld w = world();
        w.setPaused(true);
        advance(w, 20, new PlayerInput(1, true, true, true, false, false));
        assertEquals(0, w.time());
        assertEquals(w.level().spawn().x(), w.player().x());
        w.setPaused(false);
        w.player().health = 0;
        w.update(STEP, PlayerInput.NONE);
        double time = w.time();
        advance(w, 20, PlayerInput.NONE);
        assertEquals(time, w.time());
    }

    @Test
    void eventsAreDeliveredOnlyOnce() {
        GameWorld w = world();
        w.update(STEP, new PlayerInput(0, false, true, false, false, false));
        assertTrue(w.drainEvents().contains(GameEvent.SWING));
        assertTrue(w.drainEvents().isEmpty());
    }

    @Test
    void invalidStepsAndInvalidRespawnFailFast() {
        GameWorld w = world();
        assertThrows(IllegalArgumentException.class, () -> w.update(Double.NaN, PlayerInput.NONE));
        assertThrows(IllegalArgumentException.class, () -> w.update(.1, PlayerInput.NONE));
        assertThrows(IllegalStateException.class, w::respawn);
    }

    @Test
    void directionAndAttackOnSameStepUseTheRequestedFacing() {
        GameWorld w = world();
        w.player().facing = -1;
        w.update(STEP, new PlayerInput(1, false, true, false, false, false));
        assertEquals(1, w.player().facing());
        assertEquals(Attack.Phase.WINDUP, w.player().attack().phase());
    }

    @Test
    void completeLevelUsingOnlyPlayerCommands() {
        GameWorld w = world();
        for (int tick = 0; tick < 180 * 120 && w.state() == GameWorld.State.PLAYING; tick++) {
            Player p = w.player();
            Enemy target =
                    w.enemies().stream()
                            .filter(e -> e.health() > 0 && Math.abs(e.y() - p.y()) < 80)
                            .min(java.util.Comparator.comparingDouble(e -> Math.abs(e.x() - p.x())))
                            .orElse(null);
            int direction = 1;
            boolean attack = false, block = false;
            if (target != null && Math.abs(target.x() - p.x()) < 180) {
                double distance = target.x() - p.x();
                direction = distance >= 0 ? 1 : -1;
                if (target.state() == Enemy.State.WINDUP || target.state() == Enemy.State.ATTACK
                        || target.state() == Enemy.State.DASH_WINDUP || target.state() == Enemy.State.DASH
                        || target.state() == Enemy.State.JUMP_WINDUP || target.state() == Enemy.State.JUMP) {
                    block = true;
                } else {
                    attack = target.state() == Enemy.State.RECOVER && Math.abs(distance) < 80;
                    if (p.attack().running() || Math.abs(distance) < 50) direction = 0;
                }
            }
            boolean jump = p.grounded() && direction > 0 && w.level().solids().stream()
                    .anyMatch(solid -> solid.y() == 64 && solid.top() > 64
                            && solid.x() >= p.x() + Player.WIDTH
                            && solid.x() - (p.x() + Player.WIDTH) < 85
                            && solid.top() > p.y() && solid.top() - p.y() < 160);
            w.update(STEP, new PlayerInput(direction, jump, attack, false, false, block));
        }
        assertEquals(
                GameWorld.State.WON,
                w.state(),
                "Route failed at x=" + w.player().x() + ", health=" + w.player().health());
    }
}
