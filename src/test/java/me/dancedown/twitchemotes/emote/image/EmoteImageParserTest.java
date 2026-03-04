package me.dancedown.twitchemotes.emote.image;

import me.dancedown.twitchemotes.emote.type.EmoteFormat;
import me.dancedown.twitchemotes.network.NetworkHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmoteImageParserTest {
    @Test
    public void staticEmoteImageParserTest() {
        byte[] arr = NetworkHandler.download("https://cdn.7tv.app/emote/01F6P9RP3G0005F831QFQJJKGQ/1x.png");
        EmoteImageParser parser = new EmoteImageParser("01F6P9RP3G0005F831QFQJJKGQ", "pepeLost", 1);
        Assertions.assertThrows(NullPointerException.class, () -> {
            try(EmoteImage image = parser.parse(arr, EmoteFormat.PNG)) {
                Assertions.assertNotNull(image);
            }
        });
    }

    @Test
    public void staticWebpEmoteImageParserTest() {
        byte[] arr = NetworkHandler.download("https://cdn.7tv.app/emote/01F6P9RP3G0005F831QFQJJKGQ/1x.webp");
        EmoteImageParser parser = new EmoteImageParser("01F6P9RP3G0005F831QFQJJKGQ", "pepeLost", 1);
        Assertions.assertThrows(NullPointerException.class, () -> {
            try(EmoteImage image = parser.parse(arr, EmoteFormat.WEBP)) {
                Assertions.assertNotNull(image);
            }
        });
    }

    @Test
    public void animatedGifEmoteImageParserTest() {
        byte[] arr = NetworkHandler.download("https://cdn.7tv.app/emote/01K7AEPB9WMY7PHA9DFZRPPVEQ/1x.gif");
        EmoteImageParser parser = new EmoteImageParser("01K7AEPB9WMY7PHA9DFZRPPVEQ", "when", 1);
        Assertions.assertThrows(NullPointerException.class, () -> {
            try(EmoteImage image = parser.parse(arr, EmoteFormat.GIF)) {
                Assertions.assertNotNull(image);
            }
        });
    }

    @Test
    public void animatedWebpEmoteImageParserTest() {
        byte[] arr = NetworkHandler.download("https://cdn.7tv.app/emote/01K7AEPB9WMY7PHA9DFZRPPVEQ/1x.webp");
        EmoteImageParser parser = new EmoteImageParser("01K7AEPB9WMY7PHA9DFZRPPVEQ", "when", 1);
        Assertions.assertThrows(NullPointerException.class, () -> {
            try(EmoteImage image = parser.parse(arr, EmoteFormat.WEBP)) {
                Assertions.assertNotNull(image);
            }
        });
    }
}
