package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpecialAbilityTest {
    private static final List<SpecialAbility> EQUIPPED = List.of(SpecialAbility.GOLDEN_STRIKE);

    private Player player() {
        Player player = new Player(new LevelData.Point(100, 64));
        player.equip(EQUIPPED);
        return player;
    }

    private GameWorld world() {
        return new GameWorld(new LevelData(3200, 544,
            new LevelData.Point(100, 64), new LevelData.Point(1400, 64),
            List.of(new Bounds(740, 232, 22, 30)), new Bounds(2950, 64, 40, 176),
            new Bounds(3010, 64, 110, 100), new Bounds(2000, 64, 950, 256),
            List.of(new Bounds(0, 0, 3200, 64)),
            List.of(new LevelData.Spawn(2700, 64, LevelData.EnemyKind.GUARDIAN))),
            GameRules.DEFAULT, EQUIPPED, () -> {
        });
    }

    private PlayerInput special(int slot) {
        return new PlayerInput(0, false, false, false, false, false, slot);
    }

    @Test
    void goldenStrikeDealsExactlyTwoPointFiveOncePerTargetAndStacksWithRiposte() {
        Player player = player();
        Enemy enemy = new Enemy(new LevelData.Spawn(145, 64, LevelData.EnemyKind.GUARDIAN));
        var events = new ArrayList<GameEvent>();
        assertTrue(player.startSpecial(0));
        player.attack.update(.11);
        CombatSystem combat = new CombatSystem();
        combat.resolve(player, List.of(enemy), events);
        combat.resolve(player, List.of(enemy), events);
        assertEquals(enemy.maxHealth() - 2.5, enemy.health());
        assertEquals(1, events.size());
        player.attack.update(1);
        enemy.riposteReady = true;
        enemy.stagger = .4;
        assertTrue(player.startSpecial(0));
        player.attack.update(.11);
        combat.resolve(player, List.of(enemy), events);
        assertEquals(enemy.maxHealth() - 7.5, enemy.health());
    }

    @Test
    void threeActivationsConsumeChargesEvenOnMissAndCannotRepeatDuringAttack() {
        Player player = player();
        for (int remaining = 2; remaining >= 0; remaining--) {
            assertTrue(player.startSpecial(0));
            assertTrue(player.goldenStrike());
            assertFalse(player.startSpecial(0));
            assertEquals(remaining, player.specialCharges(0));
            player.attack.update(1);
            assertFalse(player.goldenStrike());
        }
        assertFalse(player.startSpecial(0));
        assertFalse(player.startSpecial(1));
        assertFalse(new Player(new LevelData.Point(100, 64)).startSpecial(0));
    }

    @Test
    void shieldStillBlocksGoldenStrikeFromTheFront() {
        Player player = player();
        Enemy enemy = new Enemy(new LevelData.Spawn(145, 64, LevelData.EnemyKind.SHIELD_BEARER));
        enemy.grounded = true;
        player.startSpecial(0);
        player.attack.update(.11);
        var events = new ArrayList<GameEvent>();
        new CombatSystem().resolve(player, List.of(enemy), events);
        assertEquals(enemy.maxHealth(), enemy.health());
        assertEquals(List.of(GameEvent.BLOCK), events);
    }

    @Test
    void blockedDodgingOrBusyPlayerDoesNotSpendCharges() {
        Player player = player();
        player.blocking = true;
        assertFalse(player.startSpecial(0));
        player.blocking = false;
        player.dodge = .2;
        assertFalse(player.startSpecial(0));
        player.dodge = 0;
        player.attack.start();
        assertFalse(player.startSpecial(0));
        assertEquals(3, player.specialCharges(0));
    }

    @Test
    void pauseNormalAttackAndRespawnKeepTheirOwnRules() {
        GameWorld world = world();
        world.setPaused(true);
        world.update(GameWorld.STEP, special(0));
        assertEquals(3, world.player().specialCharges(0));
        world.setPaused(false);
        world.update(GameWorld.STEP, special(0));
        assertTrue(world.player().goldenStrike());
        assertEquals(List.of(GameEvent.SPECIAL_SWING), world.drainEvents());
        for (int i = 0; i < 60; i++) world.update(GameWorld.STEP, PlayerInput.NONE);
        world.update(GameWorld.STEP, new PlayerInput(0, false, true, false, false, false, -1));
        assertFalse(world.player().goldenStrike());
        assertEquals(1, world.player().attackDamageMultiplier());
        world.player().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.respawn();
        assertEquals(EQUIPPED, world.player().abilities());
        assertEquals(3, world.player().specialCharges(0));
        assertFalse(world.player().goldenStrike());
    }

    @Test
    void specialPressDuringHitStopSurvivesUntilSimulationResumes() {
        GameWorld world = world();
        Enemy enemy = world.guardian();
        world.player().x = 2010;
        world.player().previousX = 2010;
        enemy.x = 2050;
        enemy.previousX = 2050;
        enemy.stagger = .4;
        enemy.riposteReady = true;
        world.update(GameWorld.STEP, new PlayerInput(0, false, true, false, false, false, -1));
        for (int i = 0; i < 30 && !world.hitStopped(); i++) world.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(world.hitStopped());
        // Isolate the input queue from the normal attack's busy-state rejection.
        world.player().attack.cancel();
        world.update(GameWorld.STEP, special(0));
        for (int i = 0; i < 8; i++) world.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(2, world.player().specialCharges(0));
        assertTrue(world.player().goldenStrike());
    }
}
