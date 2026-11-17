package br.com.thecruzader.level;

import br.com.thecruzader.gameplay.SpecialAbility;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Authored content belonging to one level; coordinates use the simulation's upward Y axis. */
public record LevelDefinition(String id, String mapPath, double blendWidth, TextKeys texts,
                              List<Region> regions, List<Decoration> decorations, Interior interior,
                              Optional<SpecialAbility> reward, List<Conversation> conversations) {
    public record Conversation(double x, double y, String textKey, String spritePath, boolean required) {
        public Conversation {
            if (!Double.isFinite(x) || !Double.isFinite(y) || x < 0 || y < 0)
                throw new IllegalArgumentException("Invalid conversation position");
            requireText(textKey);
            requireText(spritePath);
        }

        public List<String> textKeys() {
            return List.of(textKey + ".title", textKey + ".body");
        }
    }
    public record TextKeys(String chapter, String title, String story, String travelObjective,
                           String arenaNotice, String bossObjective, String bossName, String exitObjective,
                           String completionTitle, String completionText) {
        public TextKeys {
            for (String key : List.of(chapter, title, story, travelObjective, arenaNotice, bossObjective,
                bossName, exitObjective, completionTitle, completionText))
                requireText(key);
        }
        public List<String> keys() {
            return List.of(chapter, title, story, travelObjective, arenaNotice, bossObjective, bossName,
                exitObjective, completionTitle, completionText);
        }
    }
    public record Region(double start, String texturePath, String announcementKey) {
        public Region {
            if (!Double.isFinite(start) || start < 0) throw new IllegalArgumentException("Invalid region start");
            requireText(texturePath);
            requireText(announcementKey);
        }
    }
    public record Interior(String exterior, String background, String foreground, String door, String doorFrame,
            List<LevelData.Point> lights) {
        public Interior {
            for (String path : List.of(exterior, background, foreground, door, doorFrame)) requireText(path);
            lights = List.copyOf(lights);
            for (var light : lights)
                if (light.x() < 0 || light.x() >= 960 || light.y() < 0 || light.y() >= 540)
                    throw new IllegalArgumentException("Interior lights must be within the 960 x 540 room view");
        }
        public List<String> textures() { return List.of(exterior, background, foreground, door, doorFrame); }
    }

    public enum DecorationKind {
        CRATES, FLAG, SIGN, BRAZIER, TORCH,
        ARCH, SHRINE, GRAVES, DEAD_TREE, WAGON, PENNANT
    }
    public record Decoration(DecorationKind kind, double x, double y, int flagRgb) {
        public Decoration {
            Objects.requireNonNull(kind);
            if (!Double.isFinite(x) || !Double.isFinite(y) || x < 0 || y < 0)
                throw new IllegalArgumentException("Invalid decoration position");
        }
    }
    public LevelDefinition {
        Objects.requireNonNull(reward);
        requireText(id);
        requireText(mapPath);
        Objects.requireNonNull(texts);
        Objects.requireNonNull(interior);
        if (!Double.isFinite(blendWidth) || blendWidth <= 0)
            throw new IllegalArgumentException("Blend width must be finite and positive");
        regions = List.copyOf(regions);
        decorations = List.copyOf(decorations);
        conversations = List.copyOf(conversations);
        if (conversations.stream().map(Conversation::textKey).distinct().count() != conversations.size())
            throw new IllegalArgumentException("Duplicate conversation text key: " + id);
        if (regions.isEmpty() || regions.getFirst().start() != 0)
            throw new IllegalArgumentException("Regions must begin at zero");
        for (int i = 1; i < regions.size(); i++)
            if (regions.get(i).start() - regions.get(i - 1).start() < blendWidth)
                throw new IllegalArgumentException("Region boundaries must be ordered and blends must not overlap");
    }
    public void validateMap(LevelData data) {
        if (regions.getLast().start() >= data.width())
            throw new IllegalArgumentException("Region outside map: " + id);
        for (Decoration decoration : decorations)
            if (decoration.x() >= data.width() || decoration.y() >= LevelValidator.VIEW_HEIGHT)
                throw new IllegalArgumentException("Decoration outside visible map: " + id);
        for (Conversation conversation : conversations) {
            if (conversation.x() >= data.arenaEntrance().x() || conversation.y() + 72 > LevelValidator.VIEW_HEIGHT
                || data.solids().stream().noneMatch(s -> s.top() == conversation.y()
                && conversation.x() >= s.x() && conversation.x() < s.right()))
                throw new IllegalArgumentException("Conversation must stand on the road: " + id);
        }
    }
    private static void requireText(String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Content names must not be blank");
    }

    public boolean hasRequiredConversations() {
        return conversations.stream().anyMatch(Conversation::required);
    }
}
