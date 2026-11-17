package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;
import br.com.thecruzader.level.LevelLoader;
import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameWorldTest {
    private static final double STEP = GameWorld.STEP;

    private static GameWorld world() {
        return new GameWorld(
                LevelLoader.load(
                        new FileHandle(
                                System.getProperty("assets.path") + "/levels/exiles-road/maps/exiles-road.tmx")));
    }

    private static GameWorld terrainWorld() {
        LevelData level = world().level();
        return new GameWorld(new LevelData(level.width(), level.height(), level.spawn(), level.checkpoint(),
                level.bandages(), level.gate(), level.exit(), level.arena(), level.solids(),
                level.enemies().stream().filter(e -> e.kind() == LevelData.EnemyKind.GUARDIAN).toList()));
    }

    private static void advance(GameWorld world, int count, PlayerInput input) {
        for (int i = 0; i < count; i++) world.update(STEP, input);
    }

    private GameWorld configuredWorld(int hearts) {
        LevelData level = new LevelData(3200, 544,
            new LevelData.Point(100, 64), new LevelData.Point(1400, 64),
            List.of(new Bounds(600, 64, 28, 28)), new Bounds(2950, 64, 40, 176),
            new Bounds(3010, 64, 110, 100), new Bounds(2200, 64, 750, 256),
            List.of(new Bounds(0, 0, 3200, 64)),
            List.of(new LevelData.Spawn(2700, 64, LevelData.EnemyKind.GUARDIAN)));
        return new GameWorld(level, new GameRules(hearts));
    }

    @Test
    void renderedPositionInterpolatesBetweenSimulationStepsAndResetsOnRespawn() {
        GameWorld w = world();
        double start = w.player().x();
        w.update(STEP, new PlayerInput(1, false, false, false, false, false, -1));
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
        w.update(STEP, new PlayerInput(0, true, false, false, false, false, -1));
        assertTrue(w.player().y() > 64);
        advance(w, 130, PlayerInput.NONE);
        assertEquals(64, w.player().y(), .001);
        assertTrue(w.player().grounded());
    }

    @Test
    void airborneJumpCannotResetVerticalVelocity() {
        GameWorld w = world();
        w.update(STEP, PlayerInput.NONE);
        w.update(STEP, new PlayerInput(0, true, false, false, false, false, -1));
        advance(w, 10, PlayerInput.NONE);
        double velocity = w.player().vy;
        w.update(STEP, new PlayerInput(0, true, false, false, false, false, -1));
        assertTrue(w.player().vy < velocity);
    }

    @Test
    void firstPlatformIsReachableFromItsLeftEdge() {
        GameWorld w = terrainWorld();
        w.player().x = 390;
        w.update(STEP, PlayerInput.NONE);
        w.update(STEP, new PlayerInput(1, true, false, false, false, false, -1));
        advance(w, 62, new PlayerInput(1, false, false, false, false, false, -1));
        advance(w, 30, PlayerInput.NONE);
        assertTrue(w.player().x() > 420);
        assertEquals(160, w.player().y(), .001);
    }

    @Test
    void bandageRouteIsReachableThroughJumpsAndReturnsToRoad() {
        GameWorld w = terrainWorld();
        w.player().health = 1;
        w.player().x = 390;
        w.update(STEP, PlayerInput.NONE);
        w.update(STEP, new PlayerInput(1, true, false, false, false, false, -1));
        advance(w, 92, new PlayerInput(1, false, false, false, false, false, -1));
        assertEquals(160, w.player().y(), .001);
        w.update(STEP, new PlayerInput(1, true, false, false, false, false, -1));
        advance(w, 100, new PlayerInput(1, false, false, false, false, false, -1));
        assertTrue(w.collectedBandageCount() > 0, "Bandage must be reachable through the platform route");
        assertEquals(224, w.player().y(), .001);
        w.update(STEP, new PlayerInput(1, true, false, false, false, false, -1));
        advance(w, 110, new PlayerInput(1, false, false, false, false, false, -1));
        assertEquals(160, w.player().y(), .001);
        advance(w, 80, new PlayerInput(1, false, false, false, false, false, -1));
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
                                base.bandages(),
                                base.gate(),
                                base.exit(), base.arena(),
                                solids,
                                base.enemies()));
        w.update(STEP, PlayerInput.NONE);
        w.update(STEP, new PlayerInput(0, true, false, false, false, false, -1));
        for (int i = 0; i < 50; i++) {
            w.update(STEP, PlayerInput.NONE);
            assertTrue(w.player().y() + Player.HEIGHT <= 140.001);
        }
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
        w.update(STEP, new PlayerInput(0, false, false, true, false, false, -1));
        assertEquals(p.maxHealth(), p.health());
        assertTrue(p.dodging());
        assertTrue(p.invulnerable() > 0);
    }

    @Test
    void dodgeCooldownRejectsRepeatedRequests() {
        GameWorld w = world();
        w.update(STEP, new PlayerInput(0, false, false, true, false, false, -1));
        advance(w, 30, PlayerInput.NONE);
        double before = w.player().dodgeCooldown();
        w.update(STEP, new PlayerInput(0, false, false, true, false, false, -1));
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
        assertEquals(w.player().maxHealth(), w.player().health());
        w.player().health = 0;
        w.update(STEP, PlayerInput.NONE);
        assertEquals(GameWorld.State.DEAD, w.state());
        w.respawn();
        assertEquals(GameWorld.State.PLAYING, w.state());
        assertEquals(w.level().checkpoint().x(), w.player().x());
        assertEquals(w.player().maxHealth(), w.player().health());
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
    void bandageHealsOnceAndPersistsAcrossDeath() {
        GameWorld w = world();
        w.player().x = w.level().bandages().getFirst().x();
        w.player().y = w.level().bandages().getFirst().y();
        w.player().health = 1;
        w.update(STEP, PlayerInput.NONE);
        assertTrue(w.collectedBandageCount() > 0);
        assertEquals(3, w.player().health());
        w.update(STEP, PlayerInput.NONE);
        assertEquals(3, w.player().health());
        w.player().health = 0;
        w.update(STEP, PlayerInput.NONE);
        w.respawn();
        assertTrue(w.collectedBandageCount() > 0);
    }

    @Test
    void gatePhysicallyBlocksExitUntilGuardianDies() {
        GameWorld w = world();
        w.player().x = w.level().gate().x() - 50;
        advance(w, 100, new PlayerInput(1, false, false, false, false, false, -1));
        assertTrue(w.player().bounds().right() <= w.level().gate().x());
        assertEquals(GameWorld.State.PLAYING, w.state());
        w.enemies().stream().filter(Enemy::boss).findFirst().orElseThrow().health = 0;
        advance(w, 700, new PlayerInput(1, false, false, false, false, false, -1));
        assertEquals(GameWorld.State.WON, w.state());
    }

    @Test
    void pauseAndTerminalStatesFreezeSimulation() {
        GameWorld w = world();
        w.setPaused(true);
        advance(w, 20, new PlayerInput(1, true, true, true, false, false, -1));
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
        w.update(STEP, new PlayerInput(0, false, true, false, false, false, -1));
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
        w.update(STEP, new PlayerInput(1, false, true, false, false, false, -1));
        assertEquals(1, w.player().facing());
        assertEquals(Attack.Phase.WINDUP, w.player().attack().phase());
    }

    @Test
    void completeLevelUsingOnlyPlayerCommands() {
        int heavyWindup = 0;
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
                    attack = (target.archer() || target.state() == Enemy.State.RECOVER) && Math.abs(distance) < 80;
                    if (p.attack().running() || Math.abs(distance) < 50) direction = 0;
                }
            }
            if (target != null && target.kind() == LevelData.EnemyKind.SHIELD_BEARER
                && target.state() == Enemy.State.WINDUP) {
                block = target.attackProgress() >= .9;
            }
            boolean heavy = target != null && target.boss()
                && (target.state() == Enemy.State.DASH || target.state() == Enemy.State.JUMP
                || target.state() == Enemy.State.DASH_WINDUP || target.state() == Enemy.State.JUMP_WINDUP);
            boolean preparingHeavy = heavy && (target.state() == Enemy.State.DASH_WINDUP
                || target.state() == Enemy.State.JUMP_WINDUP);
            heavyWindup = preparingHeavy ? heavyWindup + (w.hitStopped() ? 0 : 1) : 0;
            double warningDuration = heavy && target.state() == Enemy.State.DASH_WINDUP ? .45 : .50;
            if (target != null && target.health() <= target.maxHealth() / 2) warningDuration *= .8;
            boolean timedParry = preparingHeavy && heavyWindup * GameWorld.STEP >= warningDuration - .07
                && Math.abs(target.x() - p.x()) < 80 && !p.attack().running();
            if (heavy) {
                block = timedParry || (!preparingHeavy && p.blocking());
                direction = block ? (target.x() >= p.x() ? 1 : -1)
                    : preparingHeavy && Math.abs(target.x() - p.x()) < 80 ? 0
                    : target.x() >= p.x() ? -1 : 1;
            }
            if (heavy && (target.state() == Enemy.State.JUMP_WINDUP
                || target.state() == Enemy.State.JUMP)) {
                block = false;
                direction = target.x() >= p.x() ? -1 : 1;
            }
            if (target != null && target.boss() && p.attack().running()) attack = false;
            boolean dodge = heavy && !block && Math.abs(target.x() - p.x()) < 110
                && (target.state() == Enemy.State.DASH || (target.state() == Enemy.State.JUMP
                && Math.abs(target.y() - p.y()) < 80));
            if (dodge && target.state() == Enemy.State.JUMP)
                direction = target.x() >= p.x() ? 1 : -1;
            boolean jump = p.grounded() && direction > 0 && w.level().solids().stream()
                .anyMatch(solid -> solid.top() > p.y()
                            && solid.x() >= p.x() + Player.WIDTH
                            && solid.x() - (p.x() + Player.WIDTH) < 85
                    && solid.top() - p.y() < 160);
            w.update(STEP, new PlayerInput(direction, jump, attack, dodge, false, block, -1));
        }
        assertEquals(
                GameWorld.State.WON,
                w.state(),
                "Route failed at x=" + w.player().x() + ", health=" + w.player().health());
    }

    @Test
    void defaultIsThreeHearts() {
        Player player = new Player(new LevelData.Point(100, 64));
        assertEquals(3, player.health());
        assertEquals(3, player.maxHealth());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 7})
    void bandagesUseConfiguredCapacity(int hearts) {
        GameWorld world = configuredWorld(hearts);
        Player player = world.player();
        assertEquals(hearts, player.health());
        player.x = 600;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertFalse(world.bandageCollected(0));
        if (hearts == 1) return; // Zero health is death, so one-heart sessions cannot need healing.
        player.health = hearts - 1;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(hearts, player.health());
        assertTrue(world.bandageCollected(0));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 7})
    void checkpointAndRespawnRestoreConfiguredCapacity(int hearts) {
        GameWorld world = configuredWorld(hearts);
        world.player().x = 1400;
        world.player().health = Math.max(1, hearts - 1);
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(world.checkpoint());
        assertEquals(hearts, world.player().health());
        world.player().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.respawn();
        assertEquals(hearts, world.player().maxHealth());
        assertEquals(hearts, world.player().health());
        assertEquals(1400, world.player().x());
    }

    @Test
    void rejectsNonPositiveHearts() {
        assertThrows(IllegalArgumentException.class, () -> new GameRules(0));
        assertThrows(IllegalArgumentException.class, () -> new GameRules(-1));
    }

    @Test
    void multipleBandagePickupsHealIndependentlyAndStayCollectedAfterRespawn() {
        GameWorld world = new GameWorld(LevelLoader.load(new FileHandle(
            System.getProperty("assets.path") + "/levels/exiles-road/maps/exiles-road.tmx")));
        for (int index : new int[]{1, 2}) {
            Bounds bandage = world.level().bandages().get(index);
            world.player().x = bandage.x();
            world.player().y = bandage.y();
            world.player().health = 1;
            world.update(GameWorld.STEP, PlayerInput.NONE);
            assertEquals(world.player().maxHealth(), world.player().health());
            assertTrue(world.bandageCollected(index));
            world.player().health = 1;
            world.update(GameWorld.STEP, PlayerInput.NONE);
            assertEquals(1, world.player().health(), "Consumed bandage cannot heal twice");
        }
        assertEquals(2, world.collectedBandageCount());
        world.player().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.respawn();
        assertEquals(2, world.collectedBandageCount());
        assertFalse(world.bandageCollected(0));
        assertTrue(world.arrows().isEmpty());
    }

    @Test
    void hitStopFreezesSimulationAndPauseAndRetainsPressedJump() {
        LevelData base = terrainWorld().level();
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
        world.update(GameWorld.STEP, new PlayerInput(0, true, false, false, false, false, -1));
        for (int tick = 0; tick < 12 && world.hitStopped(); tick++)
            world.update(GameWorld.STEP, PlayerInput.NONE);
        assertFalse(world.hitStopped(), "Hit stop must end within 100 ms");
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
}
