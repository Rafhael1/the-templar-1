package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.CampaignLoader;
import br.com.thecruzader.level.LevelLoader;
import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DorylaeumTest {
    private final FileHandle assets = new FileHandle(System.getProperty("assets.path"));
    private final Journey journey = Journey.startingAt(CampaignLoader.load(assets.child(CampaignLoader.PATH)), 3);

    private GameWorld enterCamp() {
        var world = journey.enterLevel(LevelLoader.load(assets.child(journey.currentLevel().mapPath())));
        journey.currentLevel().conversations().forEach(c -> world.confirmBriefing(c.textKey()));
        world.player().x = world.level().arena().x() + world.level().arena().width() / 2;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(world.arenaEntered());
        return world;
    }

    @Test
    void leavingFormationOnlyPausesReliefAndCannotKillThePlayer() {
        var w = enterCamp();
        var d = w.campDefense().orElseThrow();
        w.player().x = w.level().arena().x() + 20;
        w.boss().stagger = 100;
        int remaining = d.secondsRemaining();
        for (int i = 0; i < 60 * 120; i++) w.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(GameWorld.State.PLAYING, w.state());
        assertEquals(3, w.player().health());
        assertEquals(remaining, d.secondsRemaining());
        assertFalse(d.reinforcementsArrived());
        w.player().health = 0;
        w.update(GameWorld.STEP, PlayerInput.NONE);
        w.respawn();
        assertEquals(45, d.secondsRemaining());
        assertFalse(w.arenaEntered());
        assertFalse(w.briefingsPending());
        assertTrue(w.arrows().isEmpty());
    }

    @Test
    void killingTheCommanderBeforeReliefImmediatelyEndsDefenseAndReleasesTheExit() {
        var w = enterCamp();
        var d = w.campDefense().orElseThrow();
        assertEquals(List.of(SpecialAbility.GOLDEN_STRIKE, SpecialAbility.BOW), w.player().abilities());
        assertFalse(d.reinforcementsArrived());
        w.boss().state = Enemy.State.RIDER_VOLLEY;
        w.boss().actionTime = 0;
        w.boss().aimX = w.player().x();
        w.boss().aimY = w.player().y() + 26;
        w.update(GameWorld.STEP, PlayerInput.NONE);
        assertFalse(w.arrows().isEmpty());
        w.drainEvents();
        w.boss().health = 0;
        w.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(w.bossDefeated());
        assertTrue(w.bossReleaseActive());
        assertTrue(w.arrows().isEmpty());
        assertTrue(d.reinforcementsArrived());
        var events = new ArrayList<>(w.drainEvents());
        while (w.bossReleaseActive()) w.update(GameWorld.STEP, PlayerInput.NONE);
        // After killing the boss, leaving formation or waiting must never cause camp death.
        w.player().x = w.level().arena().x() + 20;
        int health = w.player().health();
        for (int tick = 0; tick < 50 * 120; tick++) {
            w.update(GameWorld.STEP, PlayerInput.NONE);
            events.addAll(w.drainEvents());
        }
        assertEquals(health, w.player().health());
        assertEquals(GameWorld.State.PLAYING, w.state());
        assertFalse(w.gateLocked());
        assertEquals(1, java.util.Collections.frequency(events, GameEvent.BOSS_DEFEATED));
        assertEquals(1, java.util.Collections.frequency(events, GameEvent.REINFORCEMENTS));
        w.player().x = w.level().exit().x();
        w.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(GameWorld.State.WON, w.state());
        journey.continueAfterVictory();
        assertEquals("exiles-road", journey.currentLevel().id());
    }

    @Test
    void aFinishingSwordStrikeOutsideFormationImmediatelyEndsTheFight() {
        var w = enterCamp();
        var d = w.campDefense().orElseThrow();
        w.player().x = w.level().arena().x() + 40;
        w.player().invulnerable = 100;
        w.boss().stagger = 100;
        for (int i = 0; i < 20 * 120; i++) w.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(GameWorld.State.PLAYING, w.state());
        w.boss().x = w.player().x + 40;
        w.boss().health = 1;
        w.boss().stagger = 0;
        w.boss().state = Enemy.State.RECOVER;
        w.boss().actionTime = 10;
        w.player().facing = 1;
        w.player().attack.start();
        w.player().attack.update(.11);
        w.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(0, w.boss().health());
        assertTrue(w.bossDefeated());
        assertEquals(3, w.player().health());
        assertEquals(GameWorld.State.PLAYING, w.state());
        assertTrue(w.bossReleaseActive());
    }

    @Test
    void pausedDefenseDoesNotAdvance() {
        var w = enterCamp();
        w.player().x = w.level().arena().x() + 20;
        w.setPaused(true);
        for (int tick = 0; tick < 120; tick++) w.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(45, w.campDefense().orElseThrow().secondsRemaining());
    }

    @Test
    void noAuxiliaryShootsWhileTheCommanderIsInactiveAndReliefStillRequiresHisDefeat() {
        var w = enterCamp();
        w.boss().stagger = 100;
        w.player().invulnerable = 100;
        for (int i = 0; i < 46 * 120; i++) w.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(w.campDefense().orElseThrow().reinforcementsArrived());
        assertEquals(w.boss().maxHealth(), w.boss().health());
        assertTrue(w.gateLocked());
        assertFalse(w.bossReleaseActive());
        assertTrue(w.arrows().isEmpty());
        for (int i = 0; i < 6 * 120; i++) w.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(w.arrows().isEmpty());
        w.boss().health = 0;
        w.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(w.bossReleaseActive());
    }

    @Test
    void arrivingAlliesRestoreHealthAndPowersOnlyOnceAndNeverAttack() {
        var w = enterCamp();
        var d = w.campDefense().orElseThrow();
        w.boss().stagger = 100;
        w.player().health = 1;
        assertTrue(w.player().startSpecial(0));
        for (int i = 0; i < 70; i++) w.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(w.player().startSpecial(1));
        var events = new ArrayList<GameEvent>();
        for (int i = 0; i < 46 * 120; i++) {
            w.update(GameWorld.STEP, PlayerInput.NONE);
            events.addAll(w.drainEvents());
        }
        assertTrue(d.reinforcementsArrived());
        assertEquals(3, w.player().health());
        assertEquals(3, w.player().specialCharges(0));
        assertEquals(3, w.player().specialCharges(1));
        assertEquals(1, java.util.Collections.frequency(events, GameEvent.REINFORCEMENTS));
        w.boss().stagger = 100;
        w.player().health = 2;
        for (int i = 0; i < 120; i++) w.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(2, w.player().health(), "Support is one restoration, not permanent healing");
        assertTrue(w.arrows().isEmpty());
    }

    @Test
    void jumpingAboveTheHorseClearsItsChargeHitbox() {
        var w = enterCamp();
        w.boss().x = w.player().x + 10;
        w.boss().state = Enemy.State.RIDER_CHARGE;
        w.player().y = w.boss().y + 60;
        new CombatSystem().resolve(w.player(), List.of(w.boss()), new ArrayList<>());
        assertEquals(3, w.player().health());
        assertNull(w.player().lastDamageCause());
    }

    @Test
    void movingVolleyAndHeavyChargeUseDifferentWindowsAndCanBeParried() {
        var w = enterCamp();
        var e = w.boss();
        var arrows = new ArrayList<Arrow>();
        var system = new EnemySystem(new CollisionWorld(w.level()), new ArrayList<>());
        e.state = Enemy.State.RIDER_VOLLEY;
        e.actionTime = 0;
        e.aimX = w.player().x;
        e.aimY = w.player().y + 24;
        for (int i = 0; i < 155; i++) system.update(e, w.player(), GameWorld.STEP, arrows, true);
        assertEquals(3, arrows.size());
        assertFalse(e.behavior.contactAttackActive(e));
        e.x = w.player().x + Player.WIDTH + 2;
        e.facing = -1;
        e.state = Enemy.State.RIDER_CHARGE;
        e.actionTime = .5;
        w.player().blocking = true;
        w.player().facing = 1;
        w.player().parryWindow = .12;
        e.x = w.player().x + 10;
        var events = new ArrayList<GameEvent>();
        new CombatSystem().resolve(w.player(), List.of(e), events);
        assertTrue(events.contains(GameEvent.PARRY));
        assertTrue(e.vulnerable());
        assertEquals(Enemy.State.RECOVER, e.state());
        e.stagger = 0;
        e.hitPlayer = false;
        e.state = Enemy.State.RIDER_CHARGE;
        w.player().parryWindow = 0;
        new CombatSystem().resolve(w.player(), List.of(e), events);
        assertEquals(2, w.player().health());
        assertEquals(Player.DamageCause.CHARGE, w.player().lastDamageCause());
    }
}
