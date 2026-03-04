package me.dancedown.twitchemotes.network;

import me.dancedown.twitchemotes.config.TwitchEmotesConfig;
import me.dancedown.twitchemotes.emote.type.Emote;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class STVEmoteProviderTest {
    @Test
    public void stvGlobalEmoteProviderTest() {
        TwitchEmotesConfig config = TwitchEmotesConfig.create(null);
        config.load7TV = true;
        config.preferredQuality = 1;
        List<Emote> emotes = new STVEmoteProvider().getGlobalEmotes(config);
        Assertions.assertFalse(emotes.isEmpty());
        Assertions.assertTrue(emotes.stream().anyMatch(emote -> emote.name().equals("glorp")));
    }

    @Test
    public void stvChannelEmoteProviderTest() {
        TwitchEmotesConfig config = TwitchEmotesConfig.create(null);
        config.load7TV = true;
        config.preferredQuality = 1;
        config.twitchChannelId = "152741417";
        config.twitchChannelName = "dancedown";
        List<Emote> emotes = new STVEmoteProvider().getChannelEmotes(config);
        Assertions.assertFalse(emotes.isEmpty());
        Assertions.assertTrue(emotes.stream().anyMatch(emote -> emote.name().equals("WankgeHomi")));
        Assertions.assertTrue(emotes.stream().anyMatch(emote -> emote.name().equals("catKiss")));
        Assertions.assertTrue(emotes.stream().anyMatch(emote -> emote.name().equals("catCute")));
        Assertions.assertTrue(emotes.stream().anyMatch(emote -> emote.name().equals("Smile")));
        Assertions.assertTrue(emotes.stream().anyMatch(emote -> emote.name().equals("oopsie")));
    }
}
