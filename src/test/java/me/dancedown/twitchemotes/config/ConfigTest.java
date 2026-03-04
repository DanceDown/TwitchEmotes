package me.dancedown.twitchemotes.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigTest {

    private Path tempDir;
    private Path configPath;

    @BeforeEach
    void setup() throws IOException {
        tempDir = Files.createTempDirectory("twitchemotes-test");
        configPath = tempDir.resolve("config.json");

        // Beispiel-Config schreiben
        Files.writeString(configPath, """
        {
          "twitchClientId": "test-client",
          "twitchOAuthToken": "test-token"
        }
        """);
    }

    @AfterEach
    void cleanup() throws IOException {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> p.toFile().delete());
    }

    @Test
    void testLoadConfig() {
        TwitchEmotesConfig config = TwitchEmotesConfig.load(configPath.toFile());
        assertEquals("test-client", config.twitchClientId);
        assertEquals("test-token", config.twitchOAuthToken);
    }

    @Test
    void testSaveConfig() {
        TwitchEmotesConfig config = TwitchEmotesConfig.create(configPath.toFile());
        config.twitchClientId = "saved-client";
        config.twitchOAuthToken = "saved-token";

        config.save();

        // Assuming testLoadConfig succeeded
        TwitchEmotesConfig loaded = TwitchEmotesConfig.load(configPath.toFile());
        assertEquals(config.twitchClientId, loaded.twitchClientId);
        assertEquals(config.twitchOAuthToken, loaded.twitchOAuthToken);
    }
}
