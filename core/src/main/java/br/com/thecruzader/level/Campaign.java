package br.com.thecruzader.level;

import java.util.HashSet;
import java.util.List;

/** Ordered levels. There is one registry for gameplay, validation and tooling. */
public record Campaign(List<LevelDefinition> levels) {
    public Campaign {
        levels = List.copyOf(levels);
        if (levels.isEmpty()) throw new IllegalArgumentException("Campaign requires a level");
        var ids = new HashSet<String>();
        for (LevelDefinition level : levels)
            if (!ids.add(level.id())) throw new IllegalArgumentException("Duplicate level ID: " + level.id());
    }
}
