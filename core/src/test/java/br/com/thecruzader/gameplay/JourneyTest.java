package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.Campaign;
import br.com.thecruzader.level.CampaignLoader;
import br.com.thecruzader.level.LevelLoader;
import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JourneyTest {
    private final FileHandle assets = new FileHandle(System.getProperty("assets.path"));
    private final FileHandle testAssets = new FileHandle(System.getProperty("test.assets.path"));
    private final Campaign campaign = CampaignLoader.load(testAssets.child("levels/test-campaign.xml"));

    @Test
    void customHeartsPersistAcrossLevelTransitions() {
        Journey journey = new Journey(campaign, new GameRules(7));
        GameWorld first = journey.enterLevel(LevelLoader.load(assets.child(journey.currentLevel().mapPath())));
        assertEquals(7, first.player().health());
        complete(first);
        journey.continueAfterVictory();
        GameWorld second = journey.enterLevel(LevelLoader.load(testAssets.child(journey.currentLevel().mapPath())));
        assertEquals(7, second.player().maxHealth());
        assertEquals(7, second.player().health());
    }

    @Test void twoLevelJourneyAdvancesResetsWorldAndRestartsAfterFinalVictory() {
        Journey journey = new Journey(campaign);
        GameWorld first = journey.enterLevel(LevelLoader.load(assets.child(campaign.levels().getFirst().mapPath())));
        assertThrows(IllegalStateException.class, journey::continueAfterVictory);
        assertThrows(IllegalStateException.class, () -> journey.enterLevel(first.level()));
        first.player().x = first.level().bandages().get(1).x();
        first.player().health = 1;
        first.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(first.collectedBandageCount() > 0);
        first.player().x = first.level().checkpoint().x();
        first.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(first.checkpoint());
        complete(first);
        assertTrue(journey.hasNextLevel());
        journey.continueAfterVictory();
        assertEquals("test-road", journey.currentLevel().id());
        assertFalse(journey.hasNextLevel());
        assertThrows(IllegalStateException.class, journey::continueAfterVictory);
        assertEquals(java.util.List.of(SpecialAbility.GOLDEN_STRIKE), journey.unlockedAbilities());
        GameWorld second = journey.enterLevel(LevelLoader.load(testAssets.child(journey.currentLevel().mapPath())));
        assertEquals(journey.unlockedAbilities(), second.player().abilities());
        assertEquals(3, second.player().specialCharges(0));
        assertNotSame(first, second);
        assertNotSame(first.player(), second.player());
        assertEquals(GameRules.DEFAULT.playerHearts(), second.player().health());
        assertFalse(second.checkpoint());
        assertEquals(0, second.collectedBandageCount());
        assertEquals(0, second.time());
        assertTrue(second.arrows().isEmpty());
        assertFalse(second.arenaEntered());
        assertTrue(second.gateLocked());
        // Death stays in the current level and does not advance campaign progress.
        second.player().health = 0;
        second.update(GameWorld.STEP, PlayerInput.NONE);
        assertThrows(IllegalStateException.class, journey::continueAfterVictory);
        second.respawn();
        assertEquals("test-road", journey.currentLevel().id());
        assertEquals(second.level().spawn().x(), second.player().x());
        complete(second);
        journey.continueAfterVictory();
        assertEquals("exiles-road", journey.currentLevel().id());
        assertTrue(journey.hasNextLevel());
        GameWorld restarted = journey.enterLevel(first.level());
        assertEquals(GameWorld.State.PLAYING, restarted.state());
        assertFalse(restarted.checkpoint());
        assertTrue(journey.unlockedAbilities().isEmpty());
        assertTrue(restarted.player().abilities().isEmpty());
        assertEquals(0, restarted.collectedBandageCount());
    }

    @Test
    void bossRewardUnlocksBeforeExitAndSurvivesDeathInTheSameLevel() {
        Journey journey = new Journey(campaign);
        GameWorld world = journey.enterLevel(LevelLoader.load(assets.child(journey.currentLevel().mapPath())));
        assertTrue(world.player().abilities().isEmpty());
        assertTrue(journey.unlockedAbilities().isEmpty());
        world.player().x = world.level().arena().x() + 8;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.boss().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(java.util.List.of(SpecialAbility.GOLDEN_STRIKE), journey.unlockedAbilities());
        assertEquals(GameWorld.State.PLAYING, world.state());
        world.player().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.respawn();
        assertEquals(java.util.List.of(SpecialAbility.GOLDEN_STRIKE), journey.unlockedAbilities());
        assertTrue(world.player().abilities().isEmpty());
    }

    private static void complete(GameWorld world) {
        world.player().x = world.level().arena().x() + 8;
        world.player().y = world.level().arena().y();
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(world.arenaEntered());
        world.boss().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        for (int tick = 0; tick < 600; tick++) world.update(GameWorld.STEP, PlayerInput.NONE);
        world.player().x = world.level().exit().x();
        world.player().y = world.level().exit().y();
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(GameWorld.State.WON, world.state());
    }
}
