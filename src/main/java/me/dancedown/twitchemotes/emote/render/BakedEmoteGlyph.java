package me.dancedown.twitchemotes.emote.render;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.dancedown.twitchemotes.TwitchEmotes;
import me.dancedown.twitchemotes.emote.image.EmoteImage;
import me.dancedown.twitchemotes.exception.EmoteStyleNotRecognizedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

import java.util.function.Function;

public class BakedEmoteGlyph extends BakedGlyph {

    private final int maxHeight;
    private final float emoteHeight;
    private final float emoteWidth;
    private final EmoteGlyphInfo emoteGlyphInfo;
    private static final float offsetY = 1;

    /**
     * Creates a new BakedEmoteGlyph from the given emote name.
     * @param emoteName The name of the emote to render
     * @throws EmoteStyleNotRecognizedException If the emote is not in the cache
     */
    public BakedEmoteGlyph(@NonNull String emoteName) throws EmoteStyleNotRecognizedException {
        this(getEmoteImage(emoteName));
    }

    private BakedEmoteGlyph(@NonNull EmoteImage image) {
        super(
                image.glyphRenderTypes(),
                0, 1, 0, 1,
                0,
                getEmoteWidth(image),
                getMaxHeight() - getEmoteHeight(image) - offsetY,
                getMaxHeight() - offsetY
        );

        this.maxHeight = getMaxHeight();
        this.emoteHeight = getEmoteHeight(image);
        this.emoteWidth = getEmoteWidth(image);
        this.emoteGlyphInfo = new EmoteGlyphInfo((int) Math.ceil(emoteWidth));
    }

    /**
     * Returns the EmoteImage from the cache.
     * @param emoteName The name of the emote to render
     * @return The EmoteImage stored in the cache
     * @throws EmoteStyleNotRecognizedException If the emote is not in the cache
     */
    private static EmoteImage getEmoteImage(@NonNull String emoteName) throws EmoteStyleNotRecognizedException {
        EmoteImage image = TwitchEmotes.EMOTE_IMAGE_CACHE.get(emoteName);
        if(image == null)
            throw new EmoteStyleNotRecognizedException();
        return image;
    }

    private static int getMaxHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }

    private static float getEmoteHeight(@NonNull EmoteImage image) {
        return Math.min(1.f, image.height() / (32.f * image.scale())) * getMaxHeight();
    }

    private static float getEmoteWidth(@NonNull EmoteImage image) {
        return getEmoteHeight(image) * image.width() / image.height();
    }

    /**
     * @return The GlyphInfo containing the advance
     */
    public @NonNull GlyphInfo info() {
        return emoteGlyphInfo;
    }

    /**
     * Renders the emote without using Minecraft's default glyph shadow rendering.
     * @param glyphInstance The glyph instance containing position and color
     * @param matrix4f The current transformation matrix
     * @param vertexConsumer The vertex consumer to draw to
     * @param light The packed light value
     */
    @Override
    public void renderChar(GlyphInstance glyphInstance, Matrix4f matrix4f, VertexConsumer vertexConsumer, int light) {
        float left = glyphInstance.x();
        float right = left + emoteWidth;
        float bottom = glyphInstance.y() + maxHeight - offsetY;
        float top = glyphInstance.y() + maxHeight - emoteHeight - offsetY;
        int color = glyphInstance.color();

        vertexConsumer.addVertex(matrix4f, left, top, 0f).setColor(color).setUv(0, 0).setLight(light);
        vertexConsumer.addVertex(matrix4f, left, bottom, 0f).setColor(color).setUv(0, 1).setLight(light);
        vertexConsumer.addVertex(matrix4f, right, bottom, 0f).setColor(color).setUv(1, 1).setLight(light);
        vertexConsumer.addVertex(matrix4f, right, top, 0f).setColor(color).setUv(1, 0).setLight(light);
    }

    static class EmoteGlyphInfo implements GlyphInfo {

        private final int advance;
        public EmoteGlyphInfo(int advance) {
            this.advance = advance;
        }
        /**
         * Returns the advance of the widest EmoteImages object
         * @return The advance of the widest emote
         */
        @Override
        public float getAdvance() {
            return advance;
        }

        /**
         * Returns the advance of the widest EmoteImages object
         * @param bl If bold style is applied (ignored)
         * @return The advance of the widest emote
         */
        @Override
        public float getAdvance(boolean bl) {
            return advance;
        }

        /**
         * Emotes can't be bold
         * @return Always <code>0</code>
         */
        @Override
        public float getBoldOffset() {
            return 0;
        }

        /**
         * Emotes don't have a shadow
         * @return Always <code>0</code>
         */
        @Override
        public float getShadowOffset() {
            return 0;
        }

        @Override
        public @NonNull BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
            throw new UnsupportedOperationException();
        }
    }

}
