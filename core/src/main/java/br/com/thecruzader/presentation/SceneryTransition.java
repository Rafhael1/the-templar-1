package br.com.thecruzader.presentation;

/** Region scenery blends across a stretch of road, rather than switching the whole screen. */
public final class SceneryTransition {
    private static final float REGION_LENGTH = 2240;
    private static final float BLEND_WIDTH = 640;
    public static final int REGIONS = 4;

    private SceneryTransition() {}

    public static int regionAt(double x) {
        return Math.max(0, Math.min(REGIONS - 1, (int) (x / REGION_LENGTH)));
    }

    public static float weight(int region, float x) {
        float entering = region == 0 ? 1 : blend(x, region * REGION_LENGTH);
        float leaving = region == REGIONS - 1 ? 0 : blend(x, (region + 1) * REGION_LENGTH);
        return entering * (1 - leaving);
    }

    private static float blend(float x, float boundary) {
        float t = Math.max(0, Math.min(1, (x - boundary + BLEND_WIDTH / 2) / BLEND_WIDTH));
        return t * t * (3 - 2 * t);
    }
}
