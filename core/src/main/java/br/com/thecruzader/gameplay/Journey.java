package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Campaign;
import br.com.thecruzader.level.LevelData;
import br.com.thecruzader.level.LevelDefinition;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Campaign progress; each level starts a fresh world.
 */
public final class Journey {
    private final Campaign campaign;
    private final GameRules rules;
    private final Set<SpecialAbility> unlocked = EnumSet.noneOf(SpecialAbility.class);
    private int index;
    private GameWorld world;

    public Journey(Campaign campaign) {
        this(campaign, GameRules.DEFAULT);
    }

    public Journey(Campaign campaign, GameRules rules) {
        this.campaign = Objects.requireNonNull(campaign);
        this.rules = Objects.requireNonNull(rules);
    }

    public static Journey startingAt(Campaign campaign, int levelIndex) {
        Objects.checkIndex(levelIndex, campaign.levels().size());
        var journey = new Journey(campaign);
        journey.index = levelIndex;
        for (int i = 0; i < levelIndex; i++)
            campaign.levels().get(i).reward().ifPresent(journey.unlocked::add);
        return journey;
    }

    public List<SpecialAbility> unlockedAbilities() {
        return List.copyOf(unlocked);
    }

    public String completedLevelId() {
        if (world == null || world.state() != GameWorld.State.WON)
            throw new IllegalStateException("Saving progress requires victory");
        return currentLevel().id();
    }

    public LevelDefinition currentLevel() { return campaign.levels().get(index); }
    public boolean hasNextLevel() { return index + 1 < campaign.levels().size(); }
    public GameWorld enterLevel(LevelData data) {
        if (world != null) throw new IllegalStateException("Current level is already running");
        LevelDefinition definition = currentLevel();
        var briefings = definition.conversations().stream()
            .filter(LevelDefinition.Conversation::required).map(LevelDefinition.Conversation::textKey).toList();
        world = new GameWorld(data, rules, unlockedAbilities(), () -> definition.reward().ifPresent(unlocked::add),
            definition.reward(), briefings);
        return world;
    }
    public void continueAfterVictory() {
        if (world == null || world.state() != GameWorld.State.WON)
            throw new IllegalStateException("Level transition requires victory");
        boolean next = hasNextLevel();
        index = next ? index + 1 : 0;
        if (!next) unlocked.clear();
        world = null;
    }
}
