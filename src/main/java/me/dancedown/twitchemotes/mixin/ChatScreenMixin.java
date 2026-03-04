package me.dancedown.twitchemotes.mixin;

import me.dancedown.twitchemotes.gui.TwitchEmotesConfigScreen;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {

    protected ChatScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void addTwitchEmotesConfigButton(CallbackInfo info) {
        Identifier icon = Identifier.fromNamespaceAndPath("twitchemotes", "twitch");
        SpriteIconButton widget = new SpriteIconButton.CenteredIcon(
                20, 20, Component.empty(), 12, 14, new WidgetSprites(icon), b ->
                this.minecraft.setScreen(new TwitchEmotesConfigScreen(this)), null, null
        ) {
            @Override
            public @Nullable ComponentPath nextFocusPath(@NotNull FocusNavigationEvent focusNavigationEvent) {
                return null;
            }
        };
        widget.setPosition(4, 2);
        addRenderableWidget(widget);
    }
}
