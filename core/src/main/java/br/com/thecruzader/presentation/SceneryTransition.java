package br.com.thecruzader.presentation;

import br.com.thecruzader.level.LevelDefinition;

/** Authored boundaries blend continuously without overlapping transition intervals. */
public final class SceneryTransition {
    private final LevelDefinition level;
    public SceneryTransition(LevelDefinition level) { this.level = level; }
    public int regionAt(double x) {
        int region = 0;
        while (region + 1 < level.regions().size() && x >= level.regions().get(region + 1).start()) region++;
        return region;
    }
    public float weight(int region, float x) {
        float entering = region == 0 ? 1 : blend(x, level.regions().get(region).start());
        float leaving = region == level.regions().size() - 1 ? 0 : blend(x, level.regions().get(region + 1).start());
        return entering * (1 - leaving);
    }
    private float blend(float x, double boundary) {
        float t = (float) Math.max(0, Math.min(1, (x - boundary + level.blendWidth() / 2) / level.blendWidth()));
        return t * t * (3 - 2 * t);
    }
}
