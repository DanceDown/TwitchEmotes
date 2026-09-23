package me.dancedown.twitchemotes.mixin;

import com.mojang.blaze3d.font.GlyphInfo;
import me.dancedown.twitchemotes.TwitchEmotes;
import me.dancedown.twitchemotes.emote.render.BakedEmoteGlyph;
import me.dancedown.twitchemotes.exception.EmoteStyleNotRecognizedException;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public class FontMixin {

    /**
     * Replaces the Minecraft glyph with the emote glyph.
     * @param fontSet The current font set
     * @param codePoint The code point to render
     * @param index The index of the character in the text
     * @param style The style containing the emote name
     * @param originalCodePoint The original code point before filtering
     * @return The emote glyph or the default Minecraft glyph
     */
    @Redirect(
            method = "accept",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/FontSet;getGlyph(I)Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;")
    )
    private BakedGlyph getGlyph(FontSet fontSet, int codePoint, int index, Style style, int originalCodePoint) {
        BakedEmoteGlyph bakedEmoteGlyph = getOrCreateEmoteGlyph(originalCodePoint, style);
        if(bakedEmoteGlyph != null)
            return bakedEmoteGlyph;
        return fontSet.getGlyph(codePoint);
    }

    /**
     * Replaces the Minecraft glyph info with the emote glyph info.
     * @param fontSet The current font set
     * @param codePoint The code point to measure
     * @param filterFishyGlyphs If fishy glyphs should be filtered
     * @param index The index of the character in the text
     * @param style The style containing the emote name
     * @param originalCodePoint The original code point before filtering
     * @return The emote glyph info or the default Minecraft glyph info
     */
    @Redirect(
            method = "accept",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/FontSet;getGlyphInfo(IZ)Lcom/mojang/blaze3d/font/GlyphInfo;")
    )
    private GlyphInfo getGlyphInfo(FontSet fontSet, int codePoint, boolean filterFishyGlyphs, int index, Style style, int originalCodePoint) {
        BakedEmoteGlyph bakedEmoteGlyph = getOrCreateEmoteGlyph(originalCodePoint, style);
        if(bakedEmoteGlyph != null)
            return bakedEmoteGlyph.info();
        return fontSet.getGlyphInfo(codePoint, filterFishyGlyphs);
    }

    /**
     * Returns the emote glyph from the cache or creates a new one.
     * @param codePoint The code point to check
     * @param style The style containing the emote name
     * @return The emote glyph or <code>null</code> if the code point is not an emote
     */
    @Unique
    private BakedEmoteGlyph getOrCreateEmoteGlyph(int codePoint, Style style) {
        try {
            if (codePoint == 0xE000 && style.getInsertion() != null) {
                return TwitchEmotes.EMOTE_IMAGE_CACHE.getOrCreateGlyph(style.getInsertion());
            }
        } catch (EmoteStyleNotRecognizedException ignored) {}
        return null;
    }
}
