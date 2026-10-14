package br.com.thecruzader.gameplay;

import static org.junit.jupiter.api.Assertions.*;
import br.com.thecruzader.level.*;
import br.com.thecruzader.input.PlayerInput;
import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.Test;

class JourneyTest {
    private final FileHandle assets = new FileHandle(System.getProperty("assets.path"));
    private final FileHandle testAssets = new FileHandle(System.getProperty("test.assets.path"));
    private final Campaign campaign = CampaignLoader.load(testAssets.child("levels/test-campaign.xml"));

    @Test void twoLevelJourneyAdvancesResetsWorldAndRestartsAfterFinalVictory() {
        Journey journey = new Journey(campaign);
        GameWorld first = journey.enterLevel(LevelLoader.load(assets.child(campaign.levels().getFirst().mapPath())));
        assertThrows(IllegalStateException.class, journey::continueAfterVictory);
        assertThrows(IllegalStateException.class, () -> journey.enterLevel(first.level()));
        first.player().x = first.level().bandages().get(1).x();
        first.player().health = 3;
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
        GameWorld second = journey.enterLevel(LevelLoader.load(testAssets.child(journey.currentLevel().mapPath())));
        assertNotSame(first, second);
        assertNotSame(first.player(), second.player());
        assertEquals(Player.MAX_HEALTH, second.player().health());
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
        assertEquals(0, restarted.collectedBandageCount());
    }

    private static void complete(GameWorld world) {
        for (int tick = 0; tick < 180 * 120 && world.state() == GameWorld.State.PLAYING; tick++) {
            Player player = world.player();
            Enemy target = world.enemies().stream().filter(e -> e.health() > 0 && Math.abs(e.y() - player.y()) < 80)
                    .min(java.util.Comparator.comparingDouble(e -> Math.abs(e.x() - player.x()))).orElse(null);
            int direction = 1;
            boolean attack = false, block = false;
            if (target != null && Math.abs(target.x() - player.x()) < 180) {
                double distance = target.x() - player.x();
                direction = distance >= 0 ? 1 : -1;
                block = switch (target.state()) {
                    case WINDUP, ATTACK, DASH_WINDUP, DASH, JUMP_WINDUP, JUMP -> true;
                    default -> false;
                };
                if (!block) {
                    attack = target.state() == Enemy.State.RECOVER && Math.abs(distance) < 80;
                    if (player.attack().running() || Math.abs(distance) < 50) direction = 0;
                }
            }
            boolean jump = player.grounded() && direction > 0 && world.level().solids().stream()
                    .anyMatch(solid -> solid.y() <= player.y() && solid.top() > player.y()
                            && solid.x() >= player.x() + Player.WIDTH
                            && solid.x() - (player.x() + Player.WIDTH) < 85
                            && solid.top() - player.y() < 160);
            world.update(GameWorld.STEP, new PlayerInput(direction, jump, attack, false, false, block));
        }
        assertEquals(GameWorld.State.WON, world.state(), "Failed at x=" + world.player().x());
    }
}
