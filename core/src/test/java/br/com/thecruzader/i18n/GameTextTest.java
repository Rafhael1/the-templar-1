package br.com.thecruzader.i18n;

import static org.junit.jupiter.api.Assertions.*;
import com.badlogic.gdx.files.FileHandle;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GameTextTest {
    @TempDir Path temporary;
    private FileHandle assets() { return new FileHandle(System.getProperty("assets.path")); }
    private GameText texts() { return new GameText(assets().child("i18n"), Language.PT); }
    private FileHandle copyCatalogs() {
        FileHandle directory = new FileHandle(temporary.toFile());
        for (String file : new String[] {"messages_pt.properties", "messages_en.properties"})
            assets().child("i18n/" + file).copyTo(directory.child(file));
        return directory;
    }
    @Test void languageChangesAllLookupsAndKeepsTheGameName() {
        GameText texts = texts();
        assertEquals("INSTRUÇÕES", texts.text("menu.instructions"));
        texts.setLanguage(Language.EN);
        assertEquals("INSTRUCTIONS", texts.text("menu.instructions"));
        assertEquals("DEFEAT THE GUARDIAN", texts.text("hud.guardian"));
        assertEquals("O Templário", texts.text("game.name"));
        texts.setLanguage(Language.PT);
        assertEquals("PÃO DA VIDA - Até dois corações restaurados.", texts.text("notice.bread"));
    }
    @Test void changingLanguageUsesTheAlreadyLoadedCatalogs() {
        FileHandle directory = copyCatalogs();
        GameText texts = new GameText(directory, Language.PT);
        directory.child("messages_pt.properties").delete();
        directory.child("messages_en.properties").delete();
        texts.setLanguage(Language.EN);
        assertEquals("QUIT", texts.text("menu.quit"));
        texts.setLanguage(Language.PT);
        assertEquals("SAIR", texts.text("menu.quit"));
    }
    @Test void missingTranslationFailsWhenCatalogsLoad() {
        FileHandle directory = copyCatalogs();
        FileHandle english = directory.child("messages_en.properties");
        english.writeString(english.readString("UTF-8").replace("menu.quit=QUIT\n", ""), false, "UTF-8");
        assertThrows(IllegalArgumentException.class, () -> new GameText(directory, Language.PT));
    }
    @Test void unknownAndBlankTranslationsFailInsteadOfShowingAnotherLanguage() {
        assertThrows(IllegalArgumentException.class, () -> texts().text("missing.key"));
        FileHandle directory = copyCatalogs();
        FileHandle english = directory.child("messages_en.properties");
        english.writeString(english.readString("UTF-8").replace("menu.quit=QUIT", "menu.quit="), false, "UTF-8");
        assertThrows(IllegalArgumentException.class, () -> new GameText(directory, Language.PT));
    }
    @Test void formattingAndLineBreaksWorkInBothLanguages() {
        GameText texts = texts();
        assertEquals("RESOLUÇÃO: 1920 x 1080", texts.format("options.resolution", 1920, 1080));
        assertTrue(texts.text("intro.council.text").contains("\n"));
        texts.setLanguage(Language.EN);
        assertEquals("RESOLUTION: 1920 x 1080", texts.format("options.resolution", 1920, 1080));
        assertEquals("2/10", texts.format("hud.breadCount", 2, 10));
    }
    @Test void fontCoversAllCharactersInBothCatalogs() {
        Set<Integer> glyphs = new HashSet<>();
        for (String line : assets().child("ui/interface.fnt").readString().split("\n"))
            if (line.startsWith("char id=")) glyphs.add(Integer.parseInt(line.split(" ")[1].substring(3)));
        for (String file : new String[] {"messages_pt.properties", "messages_en.properties"}) {
            String source = assets().child("i18n/" + file).readString("UTF-8");
            source.codePoints().filter(c -> !Character.isWhitespace(c)).forEach(c ->
                    assertTrue(glyphs.contains(c), "Font is missing character: " + Character.toString(c)));
        }
    }
}
