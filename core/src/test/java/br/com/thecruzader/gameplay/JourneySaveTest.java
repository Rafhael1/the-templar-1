package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.Campaign;
import br.com.thecruzader.level.CampaignLoader;
import br.com.thecruzader.level.LevelLoader;
import br.com.thecruzader.persistence.JourneySave;
import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JourneySaveTest {
    @TempDir
    Path directory;
    private final FileHandle assets = new FileHandle(System.getProperty("assets.path"));
    private final Campaign campaign = CampaignLoader.load(assets.child(CampaignLoader.PATH));

    private JourneySave save() {
        return new JourneySave(directory.resolve("saves/progress.properties"), CampaignLoader.PATH, campaign);
    }

    @Test
    void victoryIsPersistedBeforeContinuingAndRestoresTheNextLevelWithRewards() {
        assertTrue(save().load().isEmpty());
        Journey journey = new Journey(campaign);
        GameWorld world = enter(journey);
        win(world);
        save().saveVictory(journey);

        assertEquals(campaign.levels().getFirst(), journey.currentLevel());
        Journey resumed = save().load().orElseThrow();
        assertEquals(campaign.levels().get(1), resumed.currentLevel());
        assertEquals(campaign.levels().getFirst().reward().stream().toList(), resumed.unlockedAbilities());
        GameWorld fresh = enter(resumed);
        assertEquals(resumed.unlockedAbilities(), fresh.player().abilities());
        assertEquals(3, fresh.player().specialCharges(0));
        assertEquals(GameWorld.State.PLAYING, fresh.state());
        assertEquals(GameRules.DEFAULT.playerHearts(), fresh.player().health());
        assertEquals(0, fresh.time());
        assertFalse(fresh.checkpoint());
        assertEquals(0, fresh.collectedBandageCount());
    }

    @Test
    void unfinishedLevelsAndDeathsCannotOverwriteSavedProgress() throws IOException {
        Journey first = new Journey(campaign);
        win(enter(first));
        save().saveVictory(first);
        Path file = directory.resolve("saves/progress.properties");
        String saved = Files.readString(file);

        Journey resumed = save().load().orElseThrow();
        assertThrows(IllegalStateException.class, () -> save().saveVictory(resumed));
        GameWorld world = enter(resumed);
        assertThrows(IllegalStateException.class, () -> save().saveVictory(resumed));
        world.player().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(GameWorld.State.DEAD, world.state());
        assertThrows(IllegalStateException.class, () -> save().saveVictory(resumed));
        assertEquals(saved, Files.readString(file));
    }

    @Test
    void finalVictoryReplacesEarlierProgressAndClearStartsANewSlot() throws IOException {
        Journey journey = new Journey(campaign);
        win(enter(journey));
        save().saveVictory(journey);
        for (int i = 1; i < campaign.levels().size(); i++) {
            journey.continueAfterVictory();
            GameWorld world = enter(journey);
            journey.currentLevel().conversations().stream().filter(c -> c.required())
                .forEach(c -> world.confirmBriefing(c.textKey()));
            win(world);
            save().saveVictory(journey);
        }
        assertTrue(Files.readString(directory.resolve("saves/progress.properties"))
            .contains("completedLevel=" + campaign.levels().getLast().id()));
        assertTrue(save().load().isEmpty());
        try (var files = Files.list(directory.resolve("saves"))) {
            assertEquals(1, files.count());
        }
        save().clear();
        assertFalse(Files.exists(directory.resolve("saves/progress.properties")));
        assertTrue(save().load().isEmpty());
        save().clear();
    }

    @Test
    void invalidSavesFailWithoutDiscardingTheFile() throws IOException {
        Path file = directory.resolve("saves/progress.properties");
        Files.createDirectories(file.getParent());
        for (String contents : List.of(
            "broken",
            "version=99\ncampaign=" + CampaignLoader.PATH + "\ncompletedLevel=exiles-road",
            "version=1\ncampaign=other.xml\ncompletedLevel=exiles-road",
            "version=1\ncampaign=" + CampaignLoader.PATH + "\ncompletedLevel=unknown")) {
            Files.writeString(file, contents);
            assertThrows(IllegalStateException.class, () -> save().load());
            assertEquals(contents, Files.readString(file));
        }
    }

    @Test
    void diskErrorsAreReported() throws IOException {
        Path blocked = directory.resolve("blocked");
        Files.writeString(blocked, "regular file");
        JourneySave unwritable = new JourneySave(blocked.resolve("progress.properties"), CampaignLoader.PATH, campaign);
        Journey journey = new Journey(campaign);
        win(enter(journey));
        assertThrows(IllegalStateException.class, () -> unwritable.saveVictory(journey));
    }

    private GameWorld enter(Journey journey) {
        return journey.enterLevel(LevelLoader.load(assets.child(journey.currentLevel().mapPath())));
    }

    private static void win(GameWorld world) {
        world.player().x = world.level().arena().x() + 8;
        world.player().y = world.boss().y();
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(world.arenaEntered());
        world.boss().health = 0;
        world.campDefense().ifPresent(defense -> {
            world.player().x = defense.formation().x() + 100;
            world.player().invulnerable = 100;
            for (int i = 0; i < 46 * 120; i++) world.update(GameWorld.STEP, PlayerInput.NONE);
        });
        world.update(GameWorld.STEP, PlayerInput.NONE);
        for (int i = 0; i < 600; i++) world.update(GameWorld.STEP, PlayerInput.NONE);
        world.player().x = world.level().exit().x();
        world.player().y = world.level().exit().y();
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertEquals(GameWorld.State.WON, world.state());
    }
}
