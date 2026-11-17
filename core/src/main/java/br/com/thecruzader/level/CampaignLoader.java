package br.com.thecruzader.level;

import br.com.thecruzader.gameplay.SpecialAbility;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.XmlReader;

import java.util.ArrayList;
import java.util.Optional;

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
            levels.add(parseLevel(level));
        }
        return new Campaign(levels);
    }

    private static LevelDefinition parseLevel(XmlReader.Element level) {
        var regions = new ArrayList<LevelDefinition.Region>();
        var decorations = new ArrayList<LevelDefinition.Decoration>();
        var conversations = new ArrayList<LevelDefinition.Conversation>();
        LevelDefinition.Interior interior = null;
        for (int i = 0; i < level.getChildCount(); i++) {
            var child = level.getChild(i);
            switch (child.getName()) {
                case "interior" -> {
                    if (interior != null) throw new IllegalArgumentException("Duplicate interior definition");
                    interior = parseInterior(child);
                }
                case "region" -> regions.add(new LevelDefinition.Region(child.getFloatAttribute("start"),
                    child.getAttribute("texture"), child.getAttribute("announcement")));
                case "conversation" -> conversations.add(new LevelDefinition.Conversation(child.getFloatAttribute("x"),
                    child.getFloatAttribute("y"), child.getAttribute("text"), child.getAttribute("sprite"),
                    child.getBooleanAttribute("required", false)));
                case "decoration" -> decorations.add(parseDecoration(child));
                default -> throw new IllegalArgumentException("Unknown level element: " + child.getName());
            }
        }
        if (interior == null)
            throw new IllegalArgumentException("Level requires interior: " + level.getAttribute("id"));
        var texts = new LevelDefinition.TextKeys(level.getAttribute("chapter"), level.getAttribute("title"),
            level.getAttribute("story"), level.getAttribute("travelObjective"), level.getAttribute("arenaNotice"),
            level.getAttribute("bossObjective"), level.getAttribute("bossName"), level.getAttribute("exitObjective"),
            level.getAttribute("completionTitle"), level.getAttribute("completionText"));
        return new LevelDefinition(level.getAttribute("id"), level.getAttribute("map"),
            level.getFloatAttribute("blendWidth"), texts, regions, decorations, interior,
            Optional.ofNullable(level.getAttribute("reward", null)).map(SpecialAbility::valueOf), conversations);
    }

    private static LevelDefinition.Interior parseInterior(XmlReader.Element child) {
        var lights = new ArrayList<LevelData.Point>();
        for (int i = 0; i < child.getChildCount(); i++) {
            var light = child.getChild(i);
            if (!light.getName().equals("light"))
                throw new IllegalArgumentException("Unknown interior element: " + light.getName());
            lights.add(new LevelData.Point(light.getFloatAttribute("x"), light.getFloatAttribute("y")));
        }
        return new LevelDefinition.Interior(child.getAttribute("exterior"), child.getAttribute("background"),
            child.getAttribute("foreground"), child.getAttribute("door"), child.getAttribute("doorFrame"), lights);
    }

    private static LevelDefinition.Decoration parseDecoration(XmlReader.Element child) {
        var kind = LevelDefinition.DecorationKind.valueOf(child.getAttribute("kind"));
        int rgb = 0;
        if (kind == LevelDefinition.DecorationKind.FLAG) {
            String color = child.getAttribute("color");
            if (!color.matches("[0-9a-fA-F]{6}"))
                throw new IllegalArgumentException("Flag color requires six hex digits");
            rgb = Integer.parseInt(color, 16);
        }
        return new LevelDefinition.Decoration(kind, child.getFloatAttribute("x"),
            child.getFloatAttribute("y"), rgb);
    }
}
