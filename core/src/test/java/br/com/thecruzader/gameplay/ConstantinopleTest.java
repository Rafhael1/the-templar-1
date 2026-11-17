package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.*;
import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConstantinopleTest {
    private final FileHandle assets = new FileHandle(System.getProperty("assets.path"));
    private final Campaign production = CampaignLoader.load(assets.child(CampaignLoader.PATH));

    @Test
    void bowChargesRefillOnlyOnTheNextLevel() {
        var testAssets = new FileHandle(System.getProperty("test.assets.path"));
        var third = CampaignLoader.load(testAssets.child("levels/test-campaign.xml")).levels().getLast();
        var first = production.levels().getFirst();
        var second = production.levels().get(1);
        var journey = new Journey(new Campaign(List.of(first, second, third)));
        var road = journey.enterLevel(LevelLoader.load(assets.child(first.mapPath())));
        defeatBoss(road);
        leave(road);
        journey.continueAfterVictory();
        journey.selectAbilities(List.of(SpecialAbility.GOLDEN_STRIKE));
        var world = journey.enterLevel(LevelLoader.load(assets.child(second.mapPath())));
        assertEquals(LevelData.EnemyKind.HARBOR_CAPTAIN, world.boss().kind());
        assertFalse(journey.unlockedAbilities().contains(SpecialAbility.BOW));
        defeatBoss(world);
        assertEquals(List.of(SpecialAbility.GOLDEN_STRIKE, SpecialAbility.BOW), journey.unlockedAbilities());
        assertEquals(journey.unlockedAbilities(), world.player().abilities());
        assertEquals(3, world.player().specialCharges(1));
        finishBossRelease(world);
        fire(world, 1);
        assertEquals(2, world.player().specialCharges(1));
        world.player().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.respawn();
        assertEquals(2, world.player().specialCharges(1));
        defeatBoss(world);
        assertEquals(2, world.player().specialCharges(1));

        leave(world);
        journey.continueAfterVictory();
        journey.selectAbilities(List.of(SpecialAbility.BOW));
        var next = journey.enterLevel(LevelLoader.load(testAssets.child(third.mapPath())));
        assertEquals(List.of(SpecialAbility.BOW), next.player().abilities());
        assertEquals(3, next.player().specialCharges(0));
    }

    @Test
    void eachProjectileHitsOnceAtSeventyPercentAndTerrainStopsTheShot() {
        var world = bowRange(List.of(new Bounds(0, 0, 1800, 64)));
        var target = world.enemies().getFirst();
        for (int remaining = 2; remaining >= 0; remaining--) {
            fire(world, 0);
            assertEquals(remaining, world.player().specialCharges(0));
            assertEquals(target.maxHealth() - .7 * (3 - remaining), target.health(), 1e-9);
            assertEquals(1, world.drainEvents().stream().filter(event -> event == GameEvent.BOW_SHOT).count());
        }
        fire(world, 0);
        assertEquals(target.maxHealth() - 2.1, target.health(), 1e-9);
        assertTrue(world.arrows().isEmpty());
        assertFalse(world.drainEvents().contains(GameEvent.BOW_SHOT));

        var blocked = bowRange(List.of(new Bounds(0, 0, 1800, 64), new Bounds(175, 64, 1, 128)));
        fire(blocked, 0);
        assertEquals(2, blocked.player().specialCharges(0));
        var behindWall = blocked.enemies().getFirst();
        assertEquals(behindWall.maxHealth(), behindWall.health());
        assertTrue(blocked.arrows().isEmpty());
    }

    @Test
    void captainTelegraphsOneHeavyArrowThatBreaksAStandingGuard() {
        var world = bowRange(List.of(new Bounds(0, 0, 1800, 64)));
        world.player().x = 1040;
        var shield = new PlayerInput(0, false, false, false, false, true, -1);
        for (int tick = 0; tick < 20 && world.boss().state() != Enemy.State.SHOT_WINDUP; tick++)
            world.update(GameWorld.STEP, shield);
        assertEquals(Enemy.State.SHOT_WINDUP, world.boss().state());
        assertTrue(world.drainEvents().contains(GameEvent.CAPTAIN_DRAW));
        for (int tick = 0; tick < 60; tick++) world.update(GameWorld.STEP, shield);
        assertTrue(world.arrows().isEmpty());
        assertEquals(Enemy.State.SHOT_WINDUP, world.boss().state());
        for (int tick = 0; tick < 90 && world.player().health() == 3; tick++)
            world.update(GameWorld.STEP, shield);
        assertEquals(1, world.player().health());
        assertTrue(world.player().blocking());
        assertEquals(1, world.drainEvents().stream().filter(event -> event == GameEvent.CAPTAIN_SHOT).count());
        assertTrue(world.arrows().isEmpty());
    }

    @Test
    void heavyArrowCanBeParriedOrDodged() {
        var world = bowRange(List.of(new Bounds(0, 0, 1800, 64)));
        var collision = new CollisionWorld(world.level());
        var projectiles = new ProjectileSystem();
        var player = world.player();
        player.blocking = true;
        player.parryWindow = .12;
        var arrows = new ArrayList<>(List.of(new Arrow(160, 100, 150, 100, Arrow.Kind.CAPTAIN)));
        var events = new ArrayList<GameEvent>();
        projectiles.update(arrows, player, List.of(), collision, true, .1, events);
        assertEquals(List.of(GameEvent.PARRY), events);
        assertEquals(3, player.health());
        assertTrue(arrows.isEmpty());

        player.blocking = false;
        player.invulnerable = .2;
        arrows.add(new Arrow(160, 100, 150, 100, Arrow.Kind.CAPTAIN));
        projectiles.update(arrows, player, List.of(), collision, true, .1, events);
        assertEquals(List.of(GameEvent.PARRY), events);
        assertEquals(3, player.health());
        assertTrue(arrows.isEmpty());
    }

    private static GameWorld bowRange(List<Bounds> solids) {
        var data = new LevelData(1800, 544, new LevelData.Point(100, 64), new LevelData.Point(840, 64),
            List.of(new Bounds(300, 64, 24, 28)), new Bounds(1660, 64, 32, 256),
            new Bounds(1740, 64, 50, 96), new Bounds(1000, 64, 660, 256), solids,
            List.of(new LevelData.Spawn(260, 64, LevelData.EnemyKind.SENTINEL),
                new LevelData.Spawn(1300, 64, LevelData.EnemyKind.HARBOR_CAPTAIN)));
        var world = new GameWorld(data, GameRules.DEFAULT, List.of(SpecialAbility.BOW), () -> {
        });
        world.enemies().getFirst().stagger = 10;
        return world;
    }

    private static void fire(GameWorld world, int slot) {
        world.update(GameWorld.STEP, new PlayerInput(0, false, false, false, false, false, slot));
        for (int tick = 0; tick < 80; tick++) world.update(GameWorld.STEP, PlayerInput.NONE);
    }

    private static void defeatBoss(GameWorld world) {
        world.player().x = world.level().arena().x() + 8;
        world.player().y = world.level().arena().y();
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.boss().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(world.bossDefeated());
    }

    private static void finishBossRelease(GameWorld world) {
        for (int tick = 0; tick < 600 && world.bossReleaseActive(); tick++)
            world.update(GameWorld.STEP, PlayerInput.NONE);
        assertFalse(world.bossReleaseActive(), "Boss release must finish within five seconds");
    }

    private static void leave(GameWorld world) {
        finishBossRelease(world);
        world.player().x = world.level().exit().x() + 8;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(GameWorld.State.WON, world.state());
    }
}
