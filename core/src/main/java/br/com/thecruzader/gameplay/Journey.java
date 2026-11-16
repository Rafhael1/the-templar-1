package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Campaign;
import br.com.thecruzader.level.LevelData;
import br.com.thecruzader.level.LevelDefinition;

import java.util.*;

/** Campaign progress stays in memory; each level starts a fresh world. */
public final class Journey {
    private final Campaign campaign;
    private final GameRules rules;
    private final Set<SpecialAbility> unlocked = EnumSet.noneOf(SpecialAbility.class);
    private List<SpecialAbility> equipped = List.of();
    private boolean selectionRequired;
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
            journey.unlocked.add(campaign.levels().get(i).reward());
        journey.selectionRequired = levelIndex > 0;
        return journey;
    }

    public List<SpecialAbility> unlockedAbilities() {
        return List.copyOf(unlocked);
    }

    public boolean selectionRequired() {
        return selectionRequired;
    }

    public void selectAbilities(List<SpecialAbility> abilities) {
        if (!selectionRequired || world != null) throw new IllegalStateException("Selection requires a new level");
        if (abilities.size() > 2 || new HashSet<>(abilities).size() != abilities.size()
            || !unlocked.containsAll(abilities))
            throw new IllegalArgumentException("Select up to two unlocked abilities");
        equipped = List.copyOf(abilities);
        selectionRequired = false;
    }
    public LevelDefinition currentLevel() { return campaign.levels().get(index); }
    public boolean hasNextLevel() { return index + 1 < campaign.levels().size(); }
    public GameWorld enterLevel(LevelData data) {
        if (world != null) throw new IllegalStateException("Current level is already running");
        if (selectionRequired) throw new IllegalStateException("Choose abilities before entering this level");
        LevelDefinition definition = currentLevel();
        world = new GameWorld(data, rules, equipped, () -> unlocked.add(definition.reward()),
            definition.reward());
        return world;
    }
    public void continueAfterVictory() {
        if (world == null || world.state() != GameWorld.State.WON)
            throw new IllegalStateException("Level transition requires victory");
        boolean next = hasNextLevel();
        index = next ? index + 1 : 0;
        selectionRequired = next;
        equipped = List.of();
        if (!next) unlocked.clear();
        world = null;
    }
}
