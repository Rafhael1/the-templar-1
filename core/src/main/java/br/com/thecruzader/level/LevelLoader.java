package br.com.thecruzader.level;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.XmlReader;

import java.util.ArrayList;
import java.util.List;

/** Reads the explicit Tiled object contract; tile rendering is owned by libGDX. */
public final class LevelLoader {
    private LevelLoader() {}

    public static LevelData load(FileHandle file) {
        XmlReader.Element map = new XmlReader().parse(file);
        if (!"orthogonal".equals(map.getAttribute("orientation")))
            throw new IllegalArgumentException("Only orthogonal maps are supported");
        double width = map.getIntAttribute("width") * map.getIntAttribute("tilewidth");
        double height = map.getIntAttribute("height") * map.getIntAttribute("tileheight");
        List<Bounds> solids = new ArrayList<>();
        List<LevelData.Spawn> enemies = new ArrayList<>();
        LevelData.Point spawn = null, checkpoint = null;
        List<Bounds> breads = new ArrayList<>();
        Bounds gate = null, exit = null, arena = null;
        for (XmlReader.Element group : map.getChildrenByName("objectgroup")) {
            String layer = group.getAttribute("name");
            if (!layer.equals("collisions") && !layer.equals("entities")) continue;
            for (XmlReader.Element object : group.getChildrenByName("object")) {
                double x = object.getFloatAttribute("x"),
                        y = height - object.getFloatAttribute("y");
                if (layer.equals("collisions")) {
                    solids.add(rectangle(object, x, y));
                    continue;
                }
                String type = object.getAttribute("type");
                switch (type) {
                    case "spawn" -> {
                        requireUnique(spawn, type);
                        spawn = new LevelData.Point(x, y);
                    }
                    case "checkpoint" -> {
                        requireUnique(checkpoint, type);
                        checkpoint = new LevelData.Point(x, y);
                    }
                    case "relic" -> {
                        breads.add(rectangle(object, x, y));
                    }
                    case "gate" -> {
                        requireUnique(gate, type);
                        gate = rectangle(object, x, y);
                    }
                    case "arena" -> {
                        requireUnique(arena, type);
                        arena = rectangle(object, x, y);
                    }
                    case "exit" -> {
                        requireUnique(exit, type);
                        exit = rectangle(object, x, y);
                    }
                    case "sentinel", "archer", "guardian" ->
                            enemies.add(new LevelData.Spawn(x, y,
                                    LevelData.EnemyKind.valueOf(type.toUpperCase(java.util.Locale.ROOT))));
                    default -> throw new IllegalArgumentException("Unknown entity type: " + type);
                }
            }
        }
        if (spawn == null || checkpoint == null || breads.isEmpty() || gate == null || exit == null || arena == null)
            throw new IllegalArgumentException(
                    "Map requires spawn, checkpoint, relic, gate, exit and arena");
        return new LevelData(width, height, spawn, checkpoint, breads, gate, exit, arena, solids, enemies);
    }

    private static Bounds rectangle(XmlReader.Element object, double x, double top) {
        if (object.getChildByName("polygon") != null || object.getChildByName("polyline") != null
                || object.getChildByName("ellipse") != null || object.getChildByName("point") != null
                || object.getFloatAttribute("rotation", 0) != 0)
            throw new IllegalArgumentException("Collision and area objects must be unrotated rectangles: "
                    + object.getAttribute("name", "unnamed"));
        double width = object.getFloatAttribute("width"),
                height = object.getFloatAttribute("height");
        return new Bounds(x, top - height, width, height);
    }

    private static void requireUnique(Object previous, String type) {
        if (previous != null) throw new IllegalArgumentException("Duplicate entity: " + type);
    }
}
