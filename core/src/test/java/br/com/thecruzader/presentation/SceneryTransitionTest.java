package br.com.thecruzader.presentation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SceneryTransitionTest {
    private final br.com.thecruzader.level.LevelDefinition level = br.com.thecruzader.level.CampaignLoader.load(
            new com.badlogic.gdx.files.FileHandle(System.getProperty("assets.path") + "/levels/campaign.xml")).levels().getFirst();
    private final SceneryTransition transition = new SceneryTransition(level);
    @Test void blendsKeepCoverageAcrossTheEntireRoad() {
        for (int x = 0; x <= 8960; x += 16) {
            float sum = 0;
            for (int region = 0; region < level.regions().size(); region++) {
                float weight = transition.weight(region, x);
                assertTrue(weight >= 0 && weight <= 1);
                sum += weight;
            }
            assertEquals(1, sum, .00001, "Background coverage at x=" + x);
        }
    }
    @Test void crossingBordersDoesNotSwapTheEntireScenery() {
        for (int boundary : new int[] {2240, 4480, 6720}) {
            int incoming = transition.regionAt(boundary);
            assertEquals(.5f, transition.weight(incoming, boundary), .00001);
            assertTrue(Math.abs(transition.weight(incoming, boundary + 1)
                    - transition.weight(incoming, boundary - 1)) < .005);
            assertEquals(0, transition.weight(incoming, boundary - 320), .00001);
            assertEquals(1, transition.weight(incoming, boundary + 320), .00001);
        }
    }
    @Test void aDifferentLevelUsesItsOwnRegionBoundariesAndAnyRegionCount() {
        var regions = java.util.List.of(
                new br.com.thecruzader.level.LevelDefinition.Region(0, "a", "first"),
                new br.com.thecruzader.level.LevelDefinition.Region(900, "b", "second"),
                new br.com.thecruzader.level.LevelDefinition.Region(1500, "c", "third"));
        var different = new SceneryTransition(new br.com.thecruzader.level.LevelDefinition(
                "different", "map", 200, level.texts(), regions, java.util.List.of(), level.interior()));
        assertEquals(0, different.regionAt(899));
        assertEquals(1, different.regionAt(900));
        assertEquals(2, different.regionAt(2000));
        assertEquals(.5f, different.weight(1, 900), .00001);
        for (int x = 0; x < 2500; x++) {
            float sum = 0;
            for (int region = 0; region < regions.size(); region++) sum += different.weight(region, x);
            assertEquals(1, sum, .00001);
        }
    }
}
