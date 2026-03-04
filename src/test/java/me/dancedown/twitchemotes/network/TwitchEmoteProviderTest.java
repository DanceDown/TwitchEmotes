package me.dancedown.twitchemotes.network;

import me.dancedown.twitchemotes.config.TwitchEmotesConfig;
import me.dancedown.twitchemotes.emote.type.Emote;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TwitchEmoteProviderTest {

    /**
     * Only tests scalar API or file as login data is not provided in the code
     */
    @Test
    void twitchGlobalEmotesProviderTest() {
        TwitchEmotesConfig config = TwitchEmotesConfig.create(null);
        config.loadTwitch = true;
        config.preferredQuality = 1;
        List<Emote> emotes = new TwitchEmoteProvider().getGlobalEmotes(config);
        Assertions.assertFalse(emotes.isEmpty());
        Assertions.assertTrue(emotes.stream().anyMatch(emote -> emote.name().equals("PopNemo")));
    }

    /**
     * Only tests scalar API or file as login data is not provided in the code.
     * May not work if the channel owner decides to rename their emote
     */
    @Test
    void twitchChannelEmotesProviderTest() {
        TwitchEmotesConfig config = TwitchEmotesConfig.create(null);
        config.loadTwitch = true;
        config.preferredQuality = 1;
        config.twitchChannelName = "bastighg";
        config.twitchChannelId = "38121996";
        List<Emote> emotes = new TwitchEmoteProvider().getChannelEmotes(config);
        Assertions.assertFalse(emotes.isEmpty());
        Assertions.assertTrue(emotes.stream().anyMatch(emote -> emote.name().equals("ghgBlanket")));
    }
}
