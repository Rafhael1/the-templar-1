package br.com.thecruzader.persistence;

import br.com.thecruzader.gameplay.Journey;
import br.com.thecruzader.level.Campaign;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * One save slot, replaced atomically after each completed level.
 */
public final class JourneySave {
    private final Path file;
    private final String campaignPath;
    private final Campaign campaign;

    public JourneySave(Path file, String campaignPath, Campaign campaign) {
        this.file = file.toAbsolutePath();
        this.campaignPath = campaignPath;
        this.campaign = campaign;
    }

    public Optional<Journey> load() {
        if (Files.notExists(file)) return Optional.empty();
        Properties progress = new Properties();
        try {
            progress.load(new StringReader(Files.readString(file, StandardCharsets.UTF_8)));
        } catch (IOException error) {
            throw new IllegalStateException("Could not read journey save: " + file, error);
        }
        if (!"1".equals(progress.getProperty("version")))
            throw new IllegalStateException("Unsupported journey save version: " + file);
        if (!campaignPath.equals(progress.getProperty("campaign")))
            throw new IllegalStateException("Journey save belongs to another campaign: " + file);
        String completed = progress.getProperty("completedLevel");
        for (int i = 0; i < campaign.levels().size(); i++) {
            if (campaign.levels().get(i).id().equals(completed))
                return i + 1 < campaign.levels().size()
                    ? Optional.of(Journey.startingAt(campaign, i + 1)) : Optional.empty();
        }
        throw new IllegalStateException("Unknown completed level in journey save: " + completed);
    }

    public void saveVictory(Journey journey) {
        String completed = journey.completedLevelId();
        Properties progress = new Properties();
        progress.setProperty("version", "1");
        progress.setProperty("campaign", campaignPath);
        progress.setProperty("completedLevel", completed);
        Path temporary = null;
        try {
            StringWriter text = new StringWriter();
            progress.store(text, "O Templario - automatic campaign progress");
            Files.createDirectories(file.getParent());
            temporary = Files.createTempFile(file.getParent(), "progress-", ".tmp");
            try (FileChannel channel = FileChannel.open(temporary, WRITE, TRUNCATE_EXISTING)) {
                ByteBuffer bytes = StandardCharsets.UTF_8.encode(text.toString());
                while (bytes.hasRemaining()) channel.write(bytes);
                channel.force(true);
            }
            Files.move(temporary, file, ATOMIC_MOVE, REPLACE_EXISTING);
        } catch (IOException error) {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException cleanupError) {
                    error.addSuppressed(cleanupError);
                }
            }
            throw new IllegalStateException("Could not save journey: " + file, error);
        }
    }

    public void clear() {
        try {
            Files.deleteIfExists(file);
        } catch (IOException error) {
            throw new IllegalStateException("Could not clear journey save: " + file, error);
        }
    }
}
