package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.CampaignLoader;
import br.com.thecruzader.level.LevelLoader;
import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CheckpointRechargeTest {
    private GameWorld world() {
        var assets = new FileHandle(System.getProperty("assets.path"));
        var journey = Journey.startingAt(CampaignLoader.load(assets.child(CampaignLoader.PATH)), 3);
        return journey.enterLevel(LevelLoader.load(assets.child(journey.currentLevel().mapPath())));
    }

    private void spend(GameWorld world) {
        for (int slot = 0; slot < 2; slot++) {
            assertTrue(world.player().startSpecial(slot));
            for (int i = 0; i < 70; i++) world.update(GameWorld.STEP, PlayerInput.NONE);
            assertEquals(2, world.player().specialCharges(slot));
        }
    }

    private void arrive(GameWorld world) {
        world.player().x = world.level().checkpoint().x();
        world.player().y = world.level().checkpoint().y();
        world.update(GameWorld.STEP, PlayerInput.NONE);
        assertTrue(world.checkpoint());
        assertFull(world);
    }

    private void assertFull(GameWorld world) {
        assertEquals(List.of(SpecialAbility.GOLDEN_STRIKE, SpecialAbility.BOW), world.player().abilities());
        assertEquals(3, world.player().specialCharges(0));
        assertEquals(3, world.player().specialCharges(1));
    }

    @Test
    void arrivingAndReenteringRechargeBothPowersWithoutContinuousRefills() {
        var world = world();
        spend(world);
        arrive(world);
        spend(world); // Remaining inside the checkpoint must not refund every attack.
        world.player().x -= 120;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        arrive(world);
    }

    @Test
    void respawningAtTheCheckpointRefillsTheBowAsWellAsGoldenStrike() {
        var world = world();
        arrive(world);
        world.player().x -= 120;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        spend(world);
        world.player().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.respawn();
        assertEquals(world.level().checkpoint().x(), world.player().x());
        assertFull(world);
        spend(world); // A cast on the first respawn step must still consume its charge.
    }

    @Test
    void dyingBeforeActivatingACheckpointDoesNotRefillBowCharges() {
        var world = world();
        spend(world);
        world.player().health = 0;
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.respawn();
        assertFalse(world.checkpoint());
        assertEquals(2, world.player().specialCharges(1));
    }
}
