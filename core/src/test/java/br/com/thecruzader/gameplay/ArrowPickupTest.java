package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrowPickupTest {
    private GameWorld world(List<SpecialAbility> abilities) {
        GameWorld world = new GameWorld(new LevelData(3200, 544,
            new LevelData.Point(100, 64), new LevelData.Point(1400, 64),
            List.of(new Bounds(740, 232, 22, 30)), new Bounds(2950, 64, 40, 176), new Bounds(3010, 64, 110, 100),
            new Bounds(2000, 64, 950, 256), List.of(new Bounds(0, 0, 3200, 64)),
            List.of(new LevelData.Spawn(300, 64, LevelData.EnemyKind.ARCHER),
                new LevelData.Spawn(600, 64, LevelData.EnemyKind.SENTINEL),
                new LevelData.Spawn(2700, 64, LevelData.EnemyKind.GUARDIAN))),
            GameRules.DEFAULT, abilities, () -> {
        });
        world.enemies().forEach(enemy -> enemy.stagger = 10);
        return world;
    }

    private void tick(GameWorld world, int count) {
        for (int i = 0; i < count; i++) world.update(GameWorld.STEP, PlayerInput.NONE);
    }

    private void shoot(GameWorld world, int slot) {
        world.update(GameWorld.STEP, new PlayerInput(0, false, false, false, false, false, slot));
        tick(world, 80);
    }

    @Test
    void bowKillDropsOneArrowAndWalkingOverItRestoresOneCharge() {
        GameWorld world = world(List.of(SpecialAbility.GOLDEN_STRIKE, SpecialAbility.BOW));
        Enemy archer = world.enemies().getFirst();
        archer.health = .7;
        shoot(world, 1);
        assertEquals(0, archer.health());
        assertEquals(1, world.arrowPickups().size());
        assertEquals(2, world.player().specialCharges(1));
        tick(world, 60);
        assertEquals(1, world.arrowPickups().size());

        world.player().x = world.arrowPickups().getFirst().x();
        tick(world, 1);
        assertEquals(3, world.player().specialCharges(1));
        assertEquals(3, world.player().specialCharges(0));
        assertTrue(world.arrowPickups().isEmpty());
        assertTrue(world.drainEvents().contains(GameEvent.ARROW_PICKUP));
    }

    @Test
    void swordKillAlsoDropsAnArrow() {
        GameWorld world = world(List.of(SpecialAbility.BOW));
        world.player().x = 255;
        world.enemies().getFirst().health = 1;
        world.update(GameWorld.STEP, new PlayerInput(0, false, true, false, false, false, -1));
        tick(world, 30);
        assertEquals(0, world.enemies().getFirst().health());
        assertEquals(1, world.arrowPickups().size());
    }

    @Test
    void deadArchersBeforeBowIsEquippedNeverDropRetroactivelyAndOtherEnemiesDoNotDrop() {
        GameWorld world = world(List.of(SpecialAbility.GOLDEN_STRIKE));
        world.enemies().getFirst().health = 0;
        tick(world, 1);
        assertTrue(world.arrowPickups().isEmpty());
        world.player().equipUnlockedBow();
        world.enemies().get(1).health = 0;
        tick(world, 10);
        assertTrue(world.arrowPickups().isEmpty());
    }

    @Test
    void fullQuiverLeavesPickupUntilAnArrowIsSpent() {
        GameWorld world = world(List.of(SpecialAbility.BOW));
        world.enemies().getFirst().health = 0;
        tick(world, 1);
        world.player().x = world.arrowPickups().getFirst().x();
        tick(world, 10);
        assertEquals(3, world.player().specialCharges(0));
        assertEquals(1, world.arrowPickups().size());
        shoot(world, 0);
        assertEquals(3, world.player().specialCharges(0));
        assertTrue(world.arrowPickups().isEmpty());
    }

    @Test
    void pickupFallsOntoPlatformAndPauseFreezesIt() {
        GameWorld world = world(List.of(SpecialAbility.BOW));
        Enemy archer = world.enemies().getFirst();
        archer.y = 180;
        archer.health = 0;
        tick(world, 1);
        ArrowPickup pickup = world.arrowPickups().getFirst();
        double y = pickup.y();
        world.setPaused(true);
        tick(world, 60);
        assertEquals(y, pickup.y());
        world.setPaused(false);
        tick(world, 120);
        assertEquals(64, pickup.y());

        ArrowPickup falling = new ArrowPickup(300, 180);
        for (int i = 0; i < 120; i++)
            falling.update(GameWorld.STEP,
                List.of(new Bounds(0, 0, 3200, 64), new Bounds(280, 90, 80, 20)));
        assertEquals(110, falling.y());
    }

    @Test
    void respawnClearsOldDropsAndAllowsNewArcherDrops() {
        GameWorld world = world(List.of(SpecialAbility.BOW));
        world.enemies().getFirst().health = 0;
        tick(world, 1);
        assertEquals(1, world.arrowPickups().size());
        world.player().health = 0;
        tick(world, 1);
        world.respawn();
        assertTrue(world.arrowPickups().isEmpty());
        world.enemies().getFirst().health = 0;
        tick(world, 1);
        assertEquals(1, world.arrowPickups().size());
    }
}
