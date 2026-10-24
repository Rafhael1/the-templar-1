package br.com.thecruzader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class GameVersion {
    public static final String CURRENT = load();

    private GameVersion() {
    }

    private static String load() {
        try (InputStream input = GameVersion.class.getResourceAsStream("/game-version.txt")) {
            if (input == null) throw new IllegalStateException("Missing generated game-version.txt");
            String version = new String(input.readAllBytes(), StandardCharsets.UTF_8).strip();
            if (version.isEmpty()) throw new IllegalStateException("Empty game version");
            return version;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read game version", e);
        }
    }
}
