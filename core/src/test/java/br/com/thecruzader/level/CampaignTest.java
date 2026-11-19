package br.com.thecruzader.level;

import static org.junit.jupiter.api.Assertions.*;
import br.com.thecruzader.i18n.GameText;
import br.com.thecruzader.i18n.Language;
import com.badlogic.gdx.files.FileHandle;
import java.util.List;
import org.junit.jupiter.api.Test;

class CampaignTest {
    private final FileHandle assets = new FileHandle(System.getProperty("assets.path"));
    private final Campaign campaign = CampaignLoader.load(assets.child(CampaignLoader.PATH));

    @Test void everyRegisteredLevelHasValidGeometryAssetsAndTranslations() {
        for (Language language : Language.values()) {
            GameText text = new GameText(assets.child("i18n"), language);
            for (LevelDefinition level : campaign.levels()) {
                LevelData data = LevelLoader.load(assets.child(level.mapPath()));
                level.validateMap(data);
                level.interior().textures().forEach(path -> assertTrue(assets.child(path).exists(), path));
                level.texts().keys().forEach(key -> assertFalse(text.text(key).isBlank()));
                for (var region : level.regions()) {
                    assertTrue(assets.child(region.texturePath()).exists());
                    assertFalse(text.text(region.announcementKey()).isBlank());
                }
                for (var conversation : level.conversations()) {
                    assertTrue(assets.child(conversation.spritePath()).exists());
                    conversation.textKeys().forEach(key -> assertFalse(text.text(key).isBlank()));
                }
            }
        }
    }

    @Test
    void productionLevelsHaveDistinctBosses() {
        var bosses = campaign.levels().stream().map(level -> LevelLoader.load(assets.child(level.mapPath())))
            .map(data -> data.enemies().stream().filter(spawn -> spawn.kind().boss).findFirst().orElseThrow().kind())
            .toList();
        assertEquals(bosses.size(), bosses.stream().distinct().count(), "Every level needs its own boss");
    }

    @Test void emptyCampaignAndDuplicateIdsFail() {
        assertThrows(IllegalArgumentException.class, () -> new Campaign(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new Campaign(List.of(campaign.levels().getFirst(), campaign.levels().getFirst())));
    }

    @Test
    void rewardNamesAndDescriptionsExistInBothLanguages() {
        var texts = new br.com.thecruzader.i18n.GameText(assets.child("i18n"), br.com.thecruzader.i18n.Language.PT);
        for (var language : br.com.thecruzader.i18n.Language.values()) {
            texts.setLanguage(language);
            for (var ability : br.com.thecruzader.gameplay.SpecialAbility.values()) {
                assertFalse(texts.text(ability.textKey()).isBlank());
                assertFalse(texts.text(ability.textKey() + ".description").isBlank());
            }
        }
    }
    @Test void unorderedRegionsAndOverlappingBlendsFail() {
        var base = campaign.levels().getFirst();
        assertThrows(IllegalArgumentException.class, () -> new LevelDefinition("bad", base.mapPath(), 640, base.texts(),
            List.of(new LevelDefinition.Region(100, "texture", "key")), List.of(), base.interior(), base.reward(), List.of()));
        assertThrows(IllegalArgumentException.class, () -> new LevelDefinition("bad", base.mapPath(), 640, base.texts(),
            List.of(new LevelDefinition.Region(0, "texture", "key"), new LevelDefinition.Region(200, "texture", "key")), List.of(), base.interior(), base.reward(), List.of()));
        assertThrows(IllegalArgumentException.class, () -> new LevelDefinition("bad", base.mapPath(), Double.NaN,
            base.texts(), base.regions(), List.of(), base.interior(), base.reward(), List.of()));
    }
    @Test void invalidNumericStateFailsBeforeSimulation() {
        assertThrows(IllegalArgumentException.class, () -> new LevelData.Point(Double.NaN, 0));
        assertThrows(IllegalArgumentException.class, () -> new LevelData.Spawn(0, Double.POSITIVE_INFINITY, LevelData.EnemyKind.GUARDIAN));
        var data = LevelLoader.load(assets.child(campaign.levels().getFirst().mapPath()));
        assertThrows(IllegalArgumentException.class, () -> new LevelData(Double.NaN, data.height(), data.spawn(),
                data.checkpoint(), data.bandages(), data.gate(), data.exit(), data.arena(), data.solids(), data.enemies()));
    }
}
