package br.com.thecruzader.gameplay;

import static org.junit.jupiter.api.Assertions.*;
import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class BodyCollisionTest {
    private final BodyCollisionSystem bodies = new BodyCollisionSystem();
    private LevelData level(List<Bounds> extra, double guardianX) {
        var solids = new ArrayList<>(List.of(new Bounds(0, 0, 3200, 64)));
        solids.addAll(extra);
        return new LevelData(3200, 544, new LevelData.Point(100, 64),
                new LevelData.Point(1450, 64), List.of(new Bounds(742, 232, 28, 28)),
                new Bounds(2950, 64, 40, 176), new Bounds(3010, 64, 110, 100), new Bounds(64, 64, 2886, 256), solids,
                List.of(new LevelData.Spawn(guardianX, 64, LevelData.EnemyKind.GUARDIAN)));
    }
    private CollisionWorld terrain(Bounds... walls) { return new CollisionWorld(level(List.of(walls), 2700)); }
    private Enemy enemy(double x) { return new Enemy(new LevelData.Spawn(x, 64, LevelData.EnemyKind.SENTINEL)); }

    @Test void playerCannotWalkThroughEitherSide() {
        for (boolean left : new boolean[] {true, false}) {
            Player player = new Player(new LevelData.Point(left ? 140 : 218, 64));
            Enemy enemy = enemy(180);
            player.x = left ? 157 : 205;
            bodies.resolve(player, List.of(enemy), terrain(), true);
            assertEquals(left ? 152 : 210, player.x(), .001);
            assertFalse(player.bounds().overlaps(enemy.bounds()));
        }
    }
    @Test void advancingEnemyStopsAtStationaryPlayer() {
        Player player = new Player(new LevelData.Point(100, 64));
        Enemy enemy = enemy(132);
        enemy.x = 125;
        bodies.resolve(player, List.of(enemy), terrain(), true);
        assertEquals(100, player.x(), .001);
        assertEquals(128, enemy.x(), .001);
        assertEquals(5, player.health(), "Ordinary body contact does not deal damage");
    }
    @Test void simultaneousApproachSeparatesBothBodies() {
        Player player = new Player(new LevelData.Point(100, 64));
        Enemy enemy = enemy(135);
        player.x = 104;
        enemy.x = 129;
        bodies.resolve(player, List.of(enemy), terrain(), true);
        assertEquals(player.x() + Player.WIDTH, enemy.x(), .001);
        assertTrue(player.x() >= 100 && enemy.x() <= 135);
    }
    @Test void bodiesDoNotPushPlayerIntoWalls() {
        Bounds wall = new Bounds(80, 64, 16, 160);
        Player player = new Player(new LevelData.Point(100, 64));
        Enemy enemy = enemy(108);
        bodies.resolve(player, List.of(enemy), terrain(wall), true);
        assertFalse(player.bounds().overlaps(wall));
        assertFalse(player.bounds().overlaps(enemy.bounds()));
        assertFalse(enemy.bounds().overlaps(wall));
    }
    @Test void jumpsClearBodiesAndFallingPlayerLandsOnThem() {
        Player player = new Player(new LevelData.Point(180, 114));
        Enemy enemy = enemy(180);
        bodies.resolve(player, List.of(enemy), terrain(), true);
        assertEquals(180, player.x(), .001);
        player.y = 110;
        player.vy = -100;
        bodies.resolve(player, List.of(enemy), terrain(), true);
        assertEquals(112, player.y(), .001);
        assertTrue(player.grounded());
        assertFalse(player.bounds().overlaps(enemy.bounds()));
    }
    @Test void corpsesDoNotBlockThePlayer() {
        Player player = new Player(new LevelData.Point(180, 64));
        Enemy enemy = enemy(180);
        enemy.health = 0;
        bodies.resolve(player, List.of(enemy), terrain(), true);
        assertEquals(180, player.x(), .001);
    }
    @Test void bossDashStillHitsOnceBeforeBodySeparation() {
        GameWorld world = new GameWorld(level(List.of(), 135));
        Enemy boss = world.enemies().getFirst();
        boss.state = Enemy.State.DASH;
        boss.actionTime = 1;
        boss.facing = -1;
        for (int i = 0; i < 15; i++) {
            world.update(GameWorld.STEP, PlayerInput.NONE);
            assertFalse(world.player().bounds().overlaps(boss.bounds()));
        }
        assertEquals(4, world.player().health());
        assertEquals(1, world.drainEvents().stream().filter(e -> e == GameEvent.HURT).count());
    }
    @Test void enemiesApproachingFromBothSidesCannotTrapPlayerInsideTheirBodies() {
        Player player = new Player(new LevelData.Point(100, 64));
        Enemy left = enemy(70), right = enemy(128);
        player.x = 102;
        left.x = 71;
        right.x = 127;
        bodies.resolve(player, List.of(left, right), terrain(), true);
        assertFalse(player.bounds().overlaps(left.bounds()));
        assertFalse(player.bounds().overlaps(right.bounds()));
    }
    @Test void walkingStopsAtLivingBodiesInTheSimulation() {
        LevelData base = level(List.of(), 2700);
        GameWorld world = new GameWorld(new LevelData(base.width(), base.height(), base.spawn(),
                base.checkpoint(), base.bandages(), base.gate(), base.exit(), base.arena(), base.solids(),
                List.of(new LevelData.Spawn(180, 64, LevelData.EnemyKind.SENTINEL), base.enemies().getFirst())));
        Enemy enemy = world.enemies().getFirst();
        for (int tick = 0; tick < 150; tick++) {
            world.update(GameWorld.STEP, new PlayerInput(1, false, false, false, false, false));
            assertFalse(world.player().bounds().overlaps(enemy.bounds()));
            assertTrue(world.player().x() + Player.WIDTH <= enemy.x() + .001);
        }
    }
    @Test void dodgePassesThroughSentinelsArchersAndGuardianOnEitherSide() {
        for (LevelData.EnemyKind kind : LevelData.EnemyKind.values()) {
            for (int direction : new int[] {-1, 1}) {
                double playerX = direction == 1 ? 140 : 230;
                LevelData base = level(List.of(), kind == LevelData.EnemyKind.GUARDIAN ? 180 : 2700);
                var enemies = new ArrayList<>(base.enemies());
                if (kind != LevelData.EnemyKind.GUARDIAN) enemies.add(new LevelData.Spawn(180, 64, kind));
                GameWorld world = new GameWorld(new LevelData(base.width(), base.height(),
                        new LevelData.Point(playerX, 64), base.checkpoint(), base.bandages(), base.gate(),
                        base.exit(), base.arena(), base.solids(), enemies));
                Enemy target = world.enemies().stream().filter(e -> e.kind == kind).findFirst().orElseThrow();
                double before = target.x();
                for (int tick = 0; tick < 24; tick++)
                    world.update(GameWorld.STEP, new PlayerInput(direction, false, false, tick == 0, false, false));
                assertEquals(playerX + direction * 100, world.player().x(), .001);
                assertEquals(before, target.x(), .001, "Dodge must not push enemies");
                assertEquals(Player.MAX_HEALTH, world.player().health());
            }
        }
    }
    @Test void dodgeStillCollidesWithWallsAndLockedGate() {
        Bounds wall = new Bounds(160, 64, 32, 200);
        GameWorld wallWorld = new GameWorld(level(List.of(wall), 2700));
        for (int tick = 0; tick < 24; tick++)
            wallWorld.update(GameWorld.STEP, new PlayerInput(1, false, false, tick == 0, false, false));
        assertEquals(wall.x() - Player.WIDTH, wallWorld.player().x(), .001);
        GameWorld gateWorld = new GameWorld(level(List.of(), 2700));
        gateWorld.player().x = gateWorld.level().gate().x() - 45;
        for (int tick = 0; tick < 24; tick++)
            gateWorld.update(GameWorld.STEP, new PlayerInput(1, false, false, tick == 0, false, false));
        assertEquals(gateWorld.level().gate().x() - Player.WIDTH, gateWorld.player().x(), .001);
    }
    @Test void collisionReturnsWhenDodgeEndsInsideABody() {
        Player player = new Player(new LevelData.Point(190, 64));
        Enemy enemy = enemy(180);
        player.dodge = .1;
        bodies.resolve(player, List.of(enemy), terrain(), true);
        assertEquals(190, player.x(), .001);
        player.dodge = 0;
        bodies.resolve(player, List.of(enemy), terrain(), true);
        assertFalse(player.bounds().overlaps(enemy.bounds()));
    }
    @Test void swordTelegraphAndRecoveryGiveTimeToReact() {
        Enemy sentinel = enemy(100);
        sentinel.attack.start();
        sentinel.attack.update(.44);
        assertEquals(Attack.Phase.WINDUP, sentinel.attack.phase());
        sentinel.attack.update(.02);
        assertEquals(Attack.Phase.ACTIVE, sentinel.attack.phase());
        sentinel.attack.update(.16);
        assertEquals(Attack.Phase.RECOVERY, sentinel.attack.phase());
        sentinel.attack.update(.60);
        assertEquals(Attack.Phase.RECOVERY, sentinel.attack.phase());
        sentinel.attack.update(.05);
        assertEquals(Attack.Phase.READY, sentinel.attack.phase());
    }
}
