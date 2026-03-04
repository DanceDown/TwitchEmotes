package me.dancedown.twitchemotes.mixin;

import me.dancedown.twitchemotes.gui.TwitchEmotesConfigScreen;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void addTwitchEmotesConfigButton(CallbackInfo info) {
        Identifier icon = Identifier.fromNamespaceAndPath("twitchemotes", "twitch");
        SpriteIconButton widget = SpriteIconButton.builder(Component.empty(),
                b -> this.minecraft.setScreen(new TwitchEmotesConfigScreen(this)),
                true).size(20, 20).sprite(icon, 12, 14).build();
        widget.setPosition(4, 2);
        addRenderableWidget(widget);
    }
}
