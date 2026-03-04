package me.dancedown.twitchemotes.mixin;

import me.dancedown.twitchemotes.TwitchEmotes;
import me.dancedown.twitchemotes.text.EmoteReplacingText;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.FormattedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ComponentRenderUtils.class)
public class ComponentRenderUtilsMixin {

    /**
     * Modifies the FormattedText to be an EmoteReplacingText
     * @param text The text, possibly containing emote names
     * @return The text with an emote-replacing (styled) visit function
     */
    @ModifyVariable(method = "wrapComponents", at = @At("HEAD"), argsOnly = true)
    private static FormattedText wrapComponents(FormattedText text) {
        return TwitchEmotes.CONFIG.enabled ? new EmoteReplacingText(text) : text;
    }
}
