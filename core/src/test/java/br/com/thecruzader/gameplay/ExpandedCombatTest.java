package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;
import br.com.thecruzader.level.LevelLoader;
import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExpandedCombatTest {
    private LevelData level(Bounds... obstacles) {
        var solids = new ArrayList<>(List.of(new Bounds(0, 0, 6400, 64)));
        solids.addAll(List.of(obstacles));
        return new LevelData(6400, 544, new LevelData.Point(100, 64), new LevelData.Point(3008, 64),
                List.of(new Bounds(742, 232, 28, 28)), new Bounds(6176, 64, 40, 176),
                new Bounds(6240, 64, 128, 100), new Bounds(64, 64, 6112, 256), solids,
                List.of(new LevelData.Spawn(5900, 64, LevelData.EnemyKind.GUARDIAN)));
    }

    private Enemy enemy(double x, LevelData.EnemyKind kind) {
        return new Enemy(new LevelData.Spawn(x, 64, kind));
    }

    @Test
    void multipleBandagePickupsHealIndependentlyAndStayCollectedAfterRespawn() {
        GameWorld world = new GameWorld(LevelLoader.load(new FileHandle(
                System.getProperty("assets.path") + "/levels/exiles-road/maps/exiles-road.tmx")));
        for (int index : new int[] {1, 2}) {
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
    void bandageWaitsUntilPlayerNeedsHealing() {
        GameWorld world = new GameWorld(level());
        world.player().x = 742;
        world.player().y = 232;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertFalse(world.bandageCollected(0));
        world.player().health = world.player().maxHealth() - 1;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(world.bandageCollected(0));
        assertEquals(world.player().maxHealth(), world.player().health());
    }

    @Test
    void respawnClearsArrowsAlreadyInFlight() {
        LevelData base = level();
        GameWorld world = new GameWorld(new LevelData(base.width(), base.height(), base.spawn(), base.checkpoint(),
                base.bandages(), base.gate(), base.exit(), base.arena(), base.solids(),
                List.of(base.enemies().getFirst(), new LevelData.Spawn(300, 64, LevelData.EnemyKind.ARCHER))));
        for (int tick = 0; tick < 240 && world.arrows().isEmpty(); tick++)
            world.update(GameWorld.STEP, new PlayerInput(0, false, false, false, false, true));
        assertFalse(world.arrows().isEmpty());
        world.player().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.respawn();
        assertTrue(world.arrows().isEmpty());
    }

    @Test
    void archerTracksDuringPreparationThenFiresOnceAtTheUpdatedAimPoint() {
        Enemy archer = enemy(100, LevelData.EnemyKind.ARCHER);
        archer.facing = 1;
        Player player = new Player(new LevelData.Point(350, 64));
        var arrows = new ArrayList<Arrow>();
        EnemySystem system = new EnemySystem(new CollisionWorld(level()), new java.util.ArrayList<>());
        for (int tick = 0; tick < 50; tick++) system.update(archer, player, GameWorld.STEP, arrows, true);
        assertEquals(Enemy.State.WINDUP, archer.state());
        assertTrue(arrows.isEmpty());
        double aimX = archer.aimX;
        player.x = 400;
        player.y = 130;
        system.update(archer, player, GameWorld.STEP, arrows, true);
        assertEquals(414, archer.aimX);
        assertEquals(156, archer.aimY);
        for (int tick = 0; tick < 95; tick++) system.update(archer, player, GameWorld.STEP, arrows, true);
        assertEquals(1, arrows.size());
        assertEquals(364, aimX);
        assertEquals(Math.toDegrees(Math.atan2(156 - 96, 414 - 115)), arrows.getFirst().angle(), .001);
        assertEquals(Enemy.State.RECOVER, archer.state());
    }

    @Test
    void archerTurnsDuringPreparationButReleasedArrowsDoNotTrackThePlayer() {
        Enemy archer = enemy(500, LevelData.EnemyKind.ARCHER);
        Player player = new Player(new LevelData.Point(300, 64));
        EnemyPerception.alert(archer, player);
        var arrows = new ArrayList<Arrow>();
        EnemySystem system = new EnemySystem(new CollisionWorld(level()), new ArrayList<>());
        system.update(archer, player, GameWorld.STEP, arrows, false);
        assertEquals(Enemy.State.WINDUP, archer.state());
        assertEquals(-1, archer.facing());
        player.x = 700;
        player.y = 130;
        system.update(archer, player, GameWorld.STEP, arrows, false);
        assertEquals(1, archer.facing());
        assertEquals(714, archer.aimX());
        assertEquals(156, archer.aimY());
        for (int tick = 0; tick < 100 && arrows.isEmpty(); tick++)
            system.update(archer, player, GameWorld.STEP, arrows, false);
        assertEquals(1, arrows.size());
        Arrow arrow = arrows.getFirst();
        double angle = arrow.angle();
        assertTrue(arrow.vx > 0 && arrow.vy > 0);
        player.x = 300;
        player.y = 64;
        system.update(archer, player, .1, arrows, false);
        assertEquals(angle, arrow.angle());
        assertEquals(714, archer.aimX());
    }

    @Test
    void archerKeepsLastVisibleAimWhenThePlayerMovesBehindAWall() {
        Enemy archer = enemy(100, LevelData.EnemyKind.ARCHER);
        Player player = new Player(new LevelData.Point(300, 64));
        EnemyPerception.alert(archer, player);
        var arrows = new ArrayList<Arrow>();
        EnemySystem system = new EnemySystem(new CollisionWorld(level(new Bounds(450, 64, 32, 240))), new ArrayList<>());
        system.update(archer, player, GameWorld.STEP, arrows, false);
        assertEquals(Enemy.State.WINDUP, archer.state());
        player.x = 500;
        for (int tick = 0; tick < 100 && arrows.isEmpty(); tick++)
            system.update(archer, player, GameWorld.STEP, arrows, false);
        assertEquals(1, arrows.size());
        assertEquals(314, archer.aimX());
        assertEquals(90, archer.aimY());
    }

    @Test
    void archerDoesNotDealMeleeDamage() {
        Enemy archer = enemy(145, LevelData.EnemyKind.ARCHER);
        archer.facing = -1;
        archer.attack.start();
        archer.attack.update(.66);
        Player player = new Player(new LevelData.Point(100, 64));
        new CombatSystem().resolve(player, List.of(archer), new ArrayList<>());
        assertEquals(player.maxHealth(), player.health());
    }

    @Test
    void arrowsHitTerrainBeforePlayerEvenAcrossAnEntireWallInOneStep() {
        Player player = new Player(new LevelData.Point(240, 64));
        var arrows = new ArrayList<>(List.of(new Arrow(100, 90, 400, 90)));
        var events = new ArrayList<GameEvent>();
        new ProjectileSystem().update(arrows, player, new CollisionWorld(level(new Bounds(200, 64, 1, 120))),
                false, .5, events);
        assertTrue(arrows.isEmpty());
        assertEquals(player.maxHealth(), player.health());
        assertTrue(events.isEmpty());
    }

    @Test
    void arrowsCanBeBlockedFromTheFrontButHurtFromBehind() {
        for (int facing : new int[] {-1, 1}) {
            Player player = new Player(new LevelData.Point(240, 64));
            player.blocking = true;
            player.facing = facing;
            var arrows = new ArrayList<>(List.of(new Arrow(100, 90, 400, 90)));
            var events = new ArrayList<GameEvent>();
            new ProjectileSystem().update(arrows, player, new CollisionWorld(level()), false, .5, events);
            assertTrue(arrows.isEmpty());
            assertEquals(facing < 0 ? player.maxHealth() : player.maxHealth() - 1, player.health());
            assertEquals(List.of(facing < 0 ? GameEvent.BLOCK : GameEvent.HURT), events);
        }
    }

    @Test
    void arrowsExpireWithoutHittingAnything() {
        Player player = new Player(new LevelData.Point(1000, 64));
        var arrows = new ArrayList<>(List.of(new Arrow(100, 300, 500, 300)));
        arrows.getFirst().life = .01;
        new ProjectileSystem().update(arrows, player, new CollisionWorld(level()), false, .02, new ArrayList<>());
        assertTrue(arrows.isEmpty());
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
        for (boolean downward : new boolean[] {false, true}) {
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
        for (boolean block : new boolean[] {false, true}) {
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
