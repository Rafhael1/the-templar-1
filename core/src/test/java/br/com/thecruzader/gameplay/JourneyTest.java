package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.Campaign;
import br.com.thecruzader.level.CampaignLoader;
import br.com.thecruzader.level.LevelData;
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
        GameWorld second = journey.enterLevel(LevelLoader.load(testAssets.child(journey.currentLevel().mapPath())));
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
        assertEquals(0, restarted.collectedBandageCount());
    }

    private static void complete(GameWorld world) {
        int heavyWindup = 0;
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
                    attack = (target.archer() || target.state() == Enemy.State.RECOVER) && Math.abs(distance) < 80;
                    if (player.attack().running() || Math.abs(distance) < 50) direction = 0;
                }
            }
            if (target != null && target.kind() == LevelData.EnemyKind.SHIELD_BEARER
                && target.state() == Enemy.State.WINDUP) {
                block = target.attackProgress() >= .9;
            }
            boolean heavy = target != null && target.boss()
                && (target.state() == Enemy.State.DASH || target.state() == Enemy.State.JUMP
                || target.state() == Enemy.State.DASH_WINDUP || target.state() == Enemy.State.JUMP_WINDUP);
            boolean preparingHeavy = heavy && (target.state() == Enemy.State.DASH_WINDUP
                || target.state() == Enemy.State.JUMP_WINDUP);
            heavyWindup = preparingHeavy ? heavyWindup + (world.hitStopped() ? 0 : 1) : 0;
            double warningDuration = heavy && target.state() == Enemy.State.DASH_WINDUP ? .45 : .50;
            if (target != null && target.health() <= target.maxHealth() / 2) warningDuration *= .8;
            boolean timedParry = preparingHeavy && heavyWindup * GameWorld.STEP >= warningDuration - .07
                && Math.abs(target.x() - player.x()) < 80 && !player.attack().running();
            if (heavy) {
                block = timedParry || (!preparingHeavy && player.blocking());
                direction = block ? (target.x() >= player.x() ? 1 : -1)
                    : preparingHeavy && Math.abs(target.x() - player.x()) < 80 ? 0
                    : target.x() >= player.x() ? -1 : 1;
            }
            if (heavy && (target.state() == Enemy.State.JUMP_WINDUP
                || target.state() == Enemy.State.JUMP)) {
                block = false;
                direction = target.x() >= player.x() ? -1 : 1;
            }
            if (target != null && target.boss() && player.attack().running()) attack = false;
            boolean dodge = heavy && !block && Math.abs(target.x() - player.x()) < 110
                && (target.state() == Enemy.State.DASH || (target.state() == Enemy.State.JUMP
                && Math.abs(target.y() - player.y()) < 80));
            if (dodge && target.state() == Enemy.State.JUMP)
                direction = target.x() >= player.x() ? 1 : -1;
            boolean jump = player.grounded() && direction > 0 && world.level().solids().stream()
                .anyMatch(solid -> solid.top() > player.y()
                            && solid.x() >= player.x() + Player.WIDTH
                            && solid.x() - (player.x() + Player.WIDTH) < 85
                            && solid.top() - player.y() < 160);
            world.update(GameWorld.STEP, new PlayerInput(direction, jump, attack, dodge, false, block));

        }
        assertEquals(GameWorld.State.WON, world.state(), "Failed at x=" + world.player().x());
    }
}
