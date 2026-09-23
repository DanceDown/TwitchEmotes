package me.dancedown.twitchemotes.mixin;

import me.dancedown.twitchemotes.TwitchEmotes;
import me.dancedown.twitchemotes.emote.render.BakedEmoteGlyph;
import me.dancedown.twitchemotes.exception.EmoteStyleNotRecognizedException;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Font.class)
public class FontWidthMixin {

    @Inject(method = "method_27516", at = @At("HEAD"), cancellable = true)
    private void getEmoteGlyphAdvance(int codePoint, Style style, CallbackInfoReturnable<Float> cir) {
        if (codePoint == 0xE000 && style.getInsertion() != null) {
            try {
                BakedEmoteGlyph glyph = TwitchEmotes.EMOTE_IMAGE_CACHE.getOrCreateGlyph(style.getInsertion());
                cir.setReturnValue(glyph.info().getAdvance(style.isBold()));
            } catch (EmoteStyleNotRecognizedException ignored) {}
        }
    }
}
