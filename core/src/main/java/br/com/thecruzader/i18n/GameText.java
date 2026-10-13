package br.com.thecruzader.i18n;

import com.badlogic.gdx.files.FileHandle;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/** Both UTF-8 catalogs are loaded once. Missing keys/translations are errors, never language fallbacks. */
public final class GameText {
    private final Map<Language, Map<String, String>> catalogs = new EnumMap<>(Language.class);
    private Language language;

    public GameText(FileHandle directory, Language language) {
        for (Language available : Language.values()) {
            Properties properties = new Properties();
            try (Reader reader = directory.child("messages_" + available.fileCode + ".properties")
                    .reader(StandardCharsets.UTF_8.name())) {
                properties.load(reader);
            } catch (IOException error) {
                throw new IllegalStateException("Could not load language " + available, error);
            }
            Map<String, String> catalog = new HashMap<>();
            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                if (value.isBlank()) throw new IllegalArgumentException("Empty translation: " + available + "/" + key);
                catalog.put(key, value);
            }
            catalogs.put(available, Map.copyOf(catalog));
        }
        if (!catalogs.get(Language.PT).keySet().equals(catalogs.get(Language.EN).keySet()))
            throw new IllegalArgumentException("PT and EN catalogs must contain exactly the same keys");
        setLanguage(language);
    }

    public Language language() { return language; }
    public void setLanguage(Language language) { this.language = Objects.requireNonNull(language); }
    public String text(String key) {
        String value = catalogs.get(language).get(key);
        if (value == null) throw new IllegalArgumentException("Unknown translation key: " + key);
        return value;
    }
    public String format(String key, Object... arguments) {
        return String.format(language.locale, text(key), arguments);
    }
}
