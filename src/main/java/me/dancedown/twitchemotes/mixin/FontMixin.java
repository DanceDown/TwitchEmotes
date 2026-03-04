package me.dancedown.twitchemotes.mixin;

import me.dancedown.twitchemotes.TwitchEmotes;
import me.dancedown.twitchemotes.emote.render.BakedEmoteGlyph;
import me.dancedown.twitchemotes.exception.EmoteStyleNotRecognizedException;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Font.class)
public class FontMixin {

    @Inject(method = "getGlyph", at = @At("HEAD"), cancellable = true)
    void getGlyph(int i, Style style, CallbackInfoReturnable<BakedGlyph> cir) {
        try {
            if (i == 0xE000 && style.getInsertion() != null) {
                String emoteName = style.getInsertion();
                BakedEmoteGlyph bakedEmoteGlyph = TwitchEmotes.EMOTE_IMAGE_CACHE.getGlyph(emoteName);
                if(bakedEmoteGlyph == null) {
                    bakedEmoteGlyph = new BakedEmoteGlyph(emoteName);
                    TwitchEmotes.EMOTE_IMAGE_CACHE.addGlyph(emoteName, bakedEmoteGlyph);
                }
                cir.setReturnValue(bakedEmoteGlyph);
            }
        } catch (EmoteStyleNotRecognizedException ignored) {}
    }
}
