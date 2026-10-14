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
        if (!map.getName().equals("map") || map.getIntAttribute("infinite", 0) != 0)
            throw new IllegalArgumentException("Only finite TMX maps are supported");
        rejectGroupsAndOffsets(map);
        double width = positive(map, "width") * positive(map, "tilewidth");
        double height = positive(map, "height") * positive(map, "tileheight");
        List<Bounds> solids = new ArrayList<>();
        List<LevelData.Spawn> enemies = new ArrayList<>();
        LevelData.Point spawn = null, checkpoint = null;
        List<Bounds> bandages = new ArrayList<>();
        Bounds gate = null, exit = null, arena = null;
        var layers = new java.util.HashSet<String>();
        for (XmlReader.Element group : map.getChildrenByName("objectgroup")) {
            String layer = group.getAttribute("name");
            if (!layer.equals("collisions") && !layer.equals("entities")) continue;
            if (!layers.add(layer)) throw new IllegalArgumentException("Duplicate layer: " + layer);
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
                        spawn = point(object, x, y);
                    }
                    case "checkpoint" -> {
                        requireUnique(checkpoint, type);
                        checkpoint = point(object, x, y);
                    }
                    case "bandage" -> {
                        bandages.add(rectangle(object, x, y));
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
                    case "sentinel", "archer", "guardian" -> {
                        point(object, x, y);
                        enemies.add(new LevelData.Spawn(x, y,
                                LevelData.EnemyKind.valueOf(type.toUpperCase(java.util.Locale.ROOT))));
                    }
                    default -> throw new IllegalArgumentException("Unknown entity type: " + type);
                }
            }
        }
        if (spawn == null || checkpoint == null || bandages.isEmpty() || gate == null || exit == null || arena == null)
            throw new IllegalArgumentException(
                    "Map requires spawn, checkpoint, bandage, gate, exit and arena");
        LevelData data = new LevelData(width, height, spawn, checkpoint, bandages, gate, exit, arena, solids, enemies);
        LevelValidator.validate(data);
        return data;
    }

    private static double positive(XmlReader.Element map, String attribute) {
        int value = map.getIntAttribute(attribute);
        if (value <= 0) throw new IllegalArgumentException("Map " + attribute + " must be positive");
        return value;
    }

    private static void rejectGroupsAndOffsets(XmlReader.Element element) {
        if (element.getName().equals("group"))
            throw new IllegalArgumentException("Nested Tiled groups are unsupported; keep layers at the root");
        if (element.getName().equals("layer") || element.getName().equals("objectgroup")) {
            if (element.getFloatAttribute("offsetx", 0) != 0 || element.getFloatAttribute("offsety", 0) != 0
                    || element.getFloatAttribute("x", 0) != 0 || element.getFloatAttribute("y", 0) != 0
                    || element.getFloatAttribute("parallaxx", 1) != 1 || element.getFloatAttribute("parallaxy", 1) != 1)
                throw new IllegalArgumentException("Tiled layer offsets and parallax are unsupported");
        }
        for (int i = 0; i < element.getChildCount(); i++) rejectGroupsAndOffsets(element.getChild(i));
    }

    private static LevelData.Point point(XmlReader.Element object, double x, double y) {
        if (object.getChildByName("point") == null || object.hasAttribute("gid") || object.getChildByName("text") != null
                || object.getFloatAttribute("rotation", 0) != 0
                || object.getFloatAttribute("width", 0) != 0 || object.getFloatAttribute("height", 0) != 0)
            throw new IllegalArgumentException("Spawn, checkpoint and enemies must be unrotated points");
        return new LevelData.Point(x, y);
    }

    private static Bounds rectangle(XmlReader.Element object, double x, double top) {
        if (object.hasAttribute("gid") || object.getChildByName("text") != null
                || object.getChildByName("polygon") != null || object.getChildByName("polyline") != null
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
