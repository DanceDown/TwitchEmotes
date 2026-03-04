package me.dancedown.twitchemotes.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.dancedown.twitchemotes.TwitchEmotes;
import me.dancedown.twitchemotes.network.ProviderType;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TwitchEmotesConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public String twitchDisplayName;
    public String twitchUserName;
    public String twitchChannelName = "bastighg";
    public String twitchUserId;
    public String twitchChannelId = "38121996";
    public String twitchClientId;
    public String twitchOAuthToken;
    public boolean enabled = true;
    public boolean loadTwitch = true;
    public boolean load7TV = true;
    public boolean loadBTTV = true;
    public boolean loadFFZ = true;
    public boolean includeUnlisted = true;
    public int preferredQuality = 4;
    public boolean animateEmotes = true;
    public boolean overlayEmotes = true;

    protected transient File configFilePath;

    private TwitchEmotesConfig(File file) {
        this.configFilePath = file;
    }

    /**
     * Creates a default config
     * @param file The file where it should be saved when calling <code>save()</code>
     * @return A new default TwitchEmotesConfig
     */
    public static TwitchEmotesConfig create(File file) {
        return new TwitchEmotesConfig(file);
    }

    /**
     * Loads a config file and parses it.
     * @param file The file to load
     * @return A new TwitchEmotesConfig class
     */
    public static TwitchEmotesConfig load(File file) {

        TwitchEmotesConfig instance;

        try (FileReader reader = new FileReader(file)) {
            instance = GSON.fromJson(reader, TwitchEmotesConfig.class);
            instance.configFilePath = file;
        } catch (IOException e) {
            instance = new TwitchEmotesConfig(file);
            instance.save();
        }

        return instance;
    }

    /**
     * Saves the config file.
     */
    public void save() {
        try (FileWriter writer = new FileWriter(configFilePath)) {
            GSON.toJson(this, writer);
        } catch (IOException ex) {
            TwitchEmotes.LOGGER.error("Config file couldn't be saved: ", ex);
        }
    }

    /**
     * Checks if the current config is valid and ready to be used.
     * @return <code>true</code> if valid, else <code>false</code>
     */
    public boolean isValid() {
        if(!(twitchDisplayName == null && twitchUserName == null && twitchUserId == null && twitchOAuthToken == null && twitchClientId == null ||
                twitchDisplayName != null && twitchUserName != null && twitchUserId != null && twitchOAuthToken != null && twitchClientId != null))
            return false;
        if(preferredQuality > 4 || preferredQuality < 1)
            return false;

        return configFilePath != null && configFilePath.exists() && configFilePath.canWrite();

    }

    /**
     * @param type The provider type to check
     * @return If the provider is enabled
     */
    public boolean isProviderEnabled(ProviderType type) {
        return switch(type) {
            case TWITCH -> loadTwitch;
            case STV -> load7TV;
            case BTTV -> loadBTTV;
            case FFZ -> loadFFZ;
        };
    }
}
