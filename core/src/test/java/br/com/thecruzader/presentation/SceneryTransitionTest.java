package br.com.thecruzader.presentation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SceneryTransitionTest {
    @Test void blendsKeepCoverageAcrossTheEntireRoad() {
        for (int x = 0; x <= 8960; x += 16) {
            float sum = 0;
            for (int region = 0; region < SceneryTransition.REGIONS; region++) {
                float weight = SceneryTransition.weight(region, x);
                assertTrue(weight >= 0 && weight <= 1);
                sum += weight;
            }
            assertEquals(1, sum, .00001, "Background coverage at x=" + x);
        }
    }
    @Test void crossingBordersDoesNotSwapTheEntireScenery() {
        for (int boundary : new int[] {2240, 4480, 6720}) {
            int incoming = SceneryTransition.regionAt(boundary);
            assertEquals(.5f, SceneryTransition.weight(incoming, boundary), .00001);
            assertTrue(Math.abs(SceneryTransition.weight(incoming, boundary + 1)
                    - SceneryTransition.weight(incoming, boundary - 1)) < .005);
            assertEquals(0, SceneryTransition.weight(incoming, boundary - 320), .00001);
            assertEquals(1, SceneryTransition.weight(incoming, boundary + 320), .00001);
        }
    }
}
