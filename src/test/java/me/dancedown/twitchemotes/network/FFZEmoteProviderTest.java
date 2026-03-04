package me.dancedown.twitchemotes.network;

import me.dancedown.twitchemotes.config.TwitchEmotesConfig;
import me.dancedown.twitchemotes.emote.type.Emote;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class FFZEmoteProviderTest {
    @Test
    void ffzGlobalEmotesProviderTest() {
        TwitchEmotesConfig config = TwitchEmotesConfig.create(null);
        config.loadFFZ = true;
        config.preferredQuality = 1;
        List<Emote> emotes = new FFZEmoteProvider().getGlobalEmotes(config);
        Assertions.assertFalse(emotes.isEmpty());
        Assertions.assertTrue(emotes.stream().anyMatch(emote -> emote.name().equals("LilZ")));
    }

    @Test
    void ffzChannelEmotesProviderTest() {
        TwitchEmotesConfig config = TwitchEmotesConfig.create(null);
        config.loadFFZ = true;
        config.preferredQuality = 1;
        config.twitchChannelName = "dancedown";
        config.twitchChannelId = "152741417";
        List<Emote> emotes = new FFZEmoteProvider().getChannelEmotes(config);
        Assertions.assertFalse(emotes.isEmpty());
        Assertions.assertTrue(emotes.stream().anyMatch(emote -> emote.name().equals("Sadcatthumbsup")));
    }
}
