package me.dancedown.twitchemotes.mixin;

import me.dancedown.twitchemotes.gui.TwitchEmotesConfigScreen;
import me.dancedown.twitchemotes.gui.IconToggleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void addTwitchEmotesConfigButton(CallbackInfo info) {
        ResourceLocation icon = new ResourceLocation("twitchemotes", "twitch");
        IconToggleButton widget = new IconToggleButton(
                4, 2, 20, 20, Component.empty(), icon, false, false,
                b -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new TwitchEmotesConfigScreen(this));
                }
        );
        addRenderableWidget(widget);
    }
}
