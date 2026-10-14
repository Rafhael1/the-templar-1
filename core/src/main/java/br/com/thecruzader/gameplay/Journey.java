package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Campaign;
import br.com.thecruzader.level.LevelData;
import br.com.thecruzader.level.LevelDefinition;
import java.util.Objects;

/** Campaign progress stays in memory; each level starts a fresh world. */
public final class Journey {
    private final Campaign campaign;
    private int index;
    private GameWorld world;

    public Journey(Campaign campaign) { this.campaign = Objects.requireNonNull(campaign); }
    public LevelDefinition currentLevel() { return campaign.levels().get(index); }
    public boolean hasNextLevel() { return index + 1 < campaign.levels().size(); }
    public GameWorld enterLevel(LevelData data) {
        if (world != null) throw new IllegalStateException("Current level is already running");
        world = new GameWorld(data);
        return world;
    }
    public void continueAfterVictory() {
        if (world == null || world.state() != GameWorld.State.WON)
            throw new IllegalStateException("Level transition requires victory");
        index = hasNextLevel() ? index + 1 : 0;
        world = null;
    }
}
