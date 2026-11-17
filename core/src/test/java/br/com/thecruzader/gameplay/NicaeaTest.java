package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.CampaignLoader;
import br.com.thecruzader.level.LevelData.EnemyKind;
import br.com.thecruzader.level.LevelLoader;
import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NicaeaTest {
    private final FileHandle assets = new FileHandle(System.getProperty("assets.path"));
    private final Journey journey = Journey.startingAt(CampaignLoader.load(assets.child(CampaignLoader.PATH)), 2);
    private static final PlayerInput GUARD = new PlayerInput(0, false, false, false, false, true, -1);

    @Test
    void startingAtNicaeaAutomaticallyEquipsBothUnlockedPowers() {
        var world = enter();
        assertEquals(List.of(SpecialAbility.GOLDEN_STRIKE, SpecialAbility.BOW), world.player().abilities());
        assertEquals(3, world.player().specialCharges(0));
        assertEquals(3, world.player().specialCharges(1));
    }

    @Test
    void theRedoubtRequiresBothBriefingsAndRetainsConfirmationsAfterRespawn() {
        assertEquals("nicaea", journey.currentLevel().id());
        var world = enter();
        var briefings = journey.currentLevel().conversations();
        assertEquals(2, briefings.size());

        world.confirmBriefing(briefings.getFirst().textKey());
        world.confirmBriefing(briefings.getFirst().textKey());
        world.player().x = world.level().checkpoint().x();
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(world.checkpoint());
        world.player().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.respawn();

        var entrance = world.arenaEntrance();
        world.player().x = entrance.x() - Player.WIDTH - 1;
        var right = new PlayerInput(1, false, false, false, false, false, -1);
        for (int step = 0; step < 120; step++) world.update(GameWorld.STEP, right);
        assertTrue(world.player().x() + Player.WIDTH <= entrance.x());
        assertFalse(world.arenaEntered());

        world.confirmBriefing(briefings.getLast().textKey());
        assertFalse(world.arenaEntranceClosed());
        for (int step = 0; step < 120 && !world.arenaEntered(); step++) world.update(GameWorld.STEP, right);
        assertTrue(world.arenaEntered());
        assertTrue(world.arenaEntranceClosed());
    }

    @Test
    void heldGuardStopsTheFirstCutButBreaksOnTheSecond() {
        var world = startDuel();
        var events = new ArrayList<GameEvent>();
        for (int step = 0; step < 150; step++) {
            world.update(GameWorld.STEP, GUARD);
            events.addAll(world.drainEvents());
            if (world.boss().state() == Enemy.State.RECOVER) break;
        }
        assertTrue(events.contains(GameEvent.DUELIST_SLASH));
        assertTrue(events.indexOf(GameEvent.BLOCK) > events.indexOf(GameEvent.DUELIST_SLASH));
        assertTrue(events.indexOf(GameEvent.DUELIST_CROSS) > events.indexOf(GameEvent.BLOCK));
        assertTrue(events.indexOf(GameEvent.HURT) > events.indexOf(GameEvent.DUELIST_CROSS));
        assertEquals(2, world.player().health());
        assertEquals(1, java.util.Collections.frequency(events, GameEvent.DUELIST_SLASH));
        assertEquals(1, java.util.Collections.frequency(events, GameEvent.DUELIST_CROSS));
        assertEquals(Enemy.State.RECOVER, world.boss().state());
    }

    @Test
    void parryingTheFirstCutInterruptsTheComboAndOpensARiposte() {
        var world = startDuel();
        for (int step = 0; step < 120 && !world.boss().vulnerable(); step++) {
            var boss = world.boss();
            boolean parry = boss.state() == Enemy.State.TWIN_WINDUP && boss.actionTime <= .06;
            world.update(GameWorld.STEP, parry ? GUARD : PlayerInput.NONE);
        }
        var events = world.drainEvents();
        assertTrue(events.contains(GameEvent.PARRY));
        assertFalse(events.contains(GameEvent.DUELIST_CROSS));
        assertTrue(world.boss().vulnerable());
        assertEquals(3, world.player().health());
    }

    private GameWorld enter() {
        return journey.enterLevel(LevelLoader.load(assets.child(journey.currentLevel().mapPath())));
    }

    private GameWorld startDuel() {
        var world = enter();
        assertEquals(10240, world.level().width());
        assertEquals(EnemyKind.RELIEF_DUELIST, world.boss().kind());
        journey.currentLevel().conversations().forEach(c -> world.confirmBriefing(c.textKey()));
        world.player().x = world.boss().x() - 59;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(world.arenaEntered());
        return world;
    }
}
