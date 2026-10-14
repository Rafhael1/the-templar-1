package br.com.thecruzader.level;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.XmlReader;
import java.util.ArrayList;

/** Explicit, finite campaign manifest. Missing fields and unknown elements are authoring errors. */
public final class CampaignLoader {
    public static final String PATH = "levels/campaign.xml";
    private CampaignLoader() {}
    public static Campaign load(FileHandle file) {
        var root = new XmlReader().parse(file);
        if (!root.getName().equals("campaign")) throw new IllegalArgumentException("Expected campaign root");
        var levels = new ArrayList<LevelDefinition>();
        for (int i = 0; i < root.getChildCount(); i++) {
            var level = root.getChild(i);
            if (!level.getName().equals("level")) throw new IllegalArgumentException("Unknown campaign element: " + level.getName());
            var regions = new ArrayList<LevelDefinition.Region>();
            var decorations = new ArrayList<LevelDefinition.Decoration>();
            LevelDefinition.Interior interior = null;
            for (int j = 0; j < level.getChildCount(); j++) {
                var child = level.getChild(j);
                switch (child.getName()) {
                    case "interior" -> {
                        if (interior != null) throw new IllegalArgumentException("Duplicate interior definition");
                        var lights = new ArrayList<LevelData.Point>();
                        for (int k = 0; k < child.getChildCount(); k++) {
                            var light = child.getChild(k);
                            if (!light.getName().equals("light")) throw new IllegalArgumentException("Unknown interior element: " + light.getName());
                            lights.add(new LevelData.Point(light.getFloatAttribute("x"), light.getFloatAttribute("y")));
                        }
                        interior = new LevelDefinition.Interior(child.getAttribute("exterior"), child.getAttribute("background"),
                                child.getAttribute("foreground"), child.getAttribute("door"), child.getAttribute("doorFrame"), lights);
                    }
                    case "region" -> regions.add(new LevelDefinition.Region(child.getFloatAttribute("start"),
                            child.getAttribute("texture"), child.getAttribute("announcement")));
                    case "decoration" -> {
                        var kind = LevelDefinition.DecorationKind.valueOf(child.getAttribute("kind"));
                        int rgb = 0;
                        if (kind == LevelDefinition.DecorationKind.FLAG) {
                            String color = child.getAttribute("color");
                            if (!color.matches("[0-9a-fA-F]{6}")) throw new IllegalArgumentException("Flag color requires six hex digits");
                            rgb = Integer.parseInt(color, 16);
                        }
                        decorations.add(new LevelDefinition.Decoration(kind, child.getFloatAttribute("x"),
                                child.getFloatAttribute("y"), rgb));
                    }
                    default -> throw new IllegalArgumentException("Unknown level element: " + child.getName());
                }
            }
            if (interior == null) throw new IllegalArgumentException("Level requires interior: " + level.getAttribute("id"));
            var texts = new LevelDefinition.TextKeys(level.getAttribute("chapter"), level.getAttribute("title"),
                    level.getAttribute("story"), level.getAttribute("travelObjective"), level.getAttribute("bossObjective"),
                    level.getAttribute("bossName"), level.getAttribute("completionTitle"), level.getAttribute("completionText"));
            levels.add(new LevelDefinition(level.getAttribute("id"), level.getAttribute("map"),
                    level.getFloatAttribute("blendWidth"), texts, regions, decorations, interior));
        }
        return new Campaign(levels);
    }
}
