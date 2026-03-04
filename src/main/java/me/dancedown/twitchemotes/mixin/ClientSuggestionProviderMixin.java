package me.dancedown.twitchemotes.mixin;

import me.dancedown.twitchemotes.TwitchEmotes;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(ClientSuggestionProvider.class)
public class ClientSuggestionProviderMixin {

    @Inject(method = "getCustomTabSugggestions", at = @At("RETURN"), cancellable = true)
    private void addEmoteSuggestions(CallbackInfoReturnable<Collection<String>> cir) {
        Collection<String> suggestions = cir.getReturnValue();
        suggestions.addAll(TwitchEmotes.EMOTE_REGISTRY.getKeys());
        cir.setReturnValue(suggestions);
    }
}
