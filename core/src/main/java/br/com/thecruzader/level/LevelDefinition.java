package br.com.thecruzader.level;

import java.util.List;
import java.util.Objects;

/** Authored content belonging to one level; coordinates use the simulation's upward Y axis. */
public record LevelDefinition(String id, String mapPath, double blendWidth, TextKeys texts,
        List<Region> regions, List<Decoration> decorations, Interior interior) {
    public record TextKeys(String chapter, String title, String story, String travelObjective,
            String bossObjective, String bossName, String completionTitle, String completionText) {
        public TextKeys {
            for (String key : List.of(chapter, title, story, travelObjective, bossObjective,
                    bossName, completionTitle, completionText)) requireText(key);
        }
        public List<String> keys() {
            return List.of(chapter, title, story, travelObjective, bossObjective, bossName,
                    completionTitle, completionText);
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
    public enum DecorationKind { CRATES, FLAG, SIGN, BRAZIER, TORCH }
    public record Decoration(DecorationKind kind, double x, double y, int flagRgb) {
        public Decoration {
            Objects.requireNonNull(kind);
            if (!Double.isFinite(x) || !Double.isFinite(y) || x < 0 || y < 0)
                throw new IllegalArgumentException("Invalid decoration position");
        }
    }
    public LevelDefinition {
        requireText(id);
        requireText(mapPath);
        Objects.requireNonNull(texts);
        Objects.requireNonNull(interior);
        if (!Double.isFinite(blendWidth) || blendWidth <= 0)
            throw new IllegalArgumentException("Blend width must be finite and positive");
        regions = List.copyOf(regions);
        decorations = List.copyOf(decorations);
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
    }
    private static void requireText(String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Content names must not be blank");
    }
}
