package me.dancedown.twitchemotes.network;

import me.dancedown.twitchemotes.config.TwitchEmotesConfig;
import me.dancedown.twitchemotes.emote.type.Emote;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class BTTVEmoteProviderTest {
    @Test
    public void bttvGlobalEmoteProviderTest() {
        TwitchEmotesConfig config = TwitchEmotesConfig.create(null);
        config.loadBTTV = true;
        config.preferredQuality = 1;
        List<Emote> emotes = new BTTVEmoteProvider().getGlobalEmotes(config);
        Assertions.assertFalse(emotes.isEmpty());
        Assertions.assertTrue(emotes.stream().anyMatch(emote -> emote.name().equals("FeelsPumpkinMan")));
    }

    @Test
    public void bttvChannelEmoteProviderTest() {
        TwitchEmotesConfig config = TwitchEmotesConfig.create(null);
        config.loadBTTV = true;
        config.preferredQuality = 1;
        config.twitchChannelId = "152741417";
        config.twitchChannelName = "dancedown";
        List<Emote> emotes = new BTTVEmoteProvider().getChannelEmotes(config);
        Assertions.assertFalse(emotes.isEmpty());
        Assertions.assertTrue(emotes.stream().anyMatch(emote -> emote.name().equals("blobDance")));
    }
}
