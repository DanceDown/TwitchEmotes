package me.dancedown.twitchemotes.gui;

import me.dancedown.twitchemotes.TwitchEmotes;
import me.dancedown.twitchemotes.exception.InvalidTwitchChannelException;
import me.dancedown.twitchemotes.exception.TwitchNotLoggedInException;
import me.dancedown.twitchemotes.exception.UnknownTwitchEmotesException;
import me.dancedown.twitchemotes.network.NetworkHandler;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.NonNull;

import java.awt.*;

public class TwitchEmotesConfigScreen extends Screen {

    Screen lastScreen;
    boolean enabled;
    String channelName;
    boolean loadTwitchEmotes;
    boolean loadSTVEmotes;
    boolean loadBTTVEmotes;
    boolean loadFFZEmotes;
    boolean loadUnlistedEmotes;
    boolean animateEmotes;
    boolean overlayEmotes;
    int qualityPreference;

    public TwitchEmotesConfigScreen(Screen lastScreen) {
        super(Component.translatable("title.twitchemotes.config"));
        this.lastScreen = lastScreen;

        enabled = TwitchEmotes.CONFIG.enabled;
        channelName = TwitchEmotes.CONFIG.twitchChannelName;
        loadTwitchEmotes = TwitchEmotes.CONFIG.loadTwitch;
        loadSTVEmotes = TwitchEmotes.CONFIG.load7TV;
        loadBTTVEmotes = TwitchEmotes.CONFIG.loadBTTV;
        loadFFZEmotes = TwitchEmotes.CONFIG.loadFFZ;
        loadUnlistedEmotes = TwitchEmotes.CONFIG.includeUnlisted;
        qualityPreference = TwitchEmotes.CONFIG.preferredQuality;
        animateEmotes = TwitchEmotes.CONFIG.animateEmotes;
        overlayEmotes = TwitchEmotes.CONFIG.overlayEmotes;
    }

    @Override
    protected void init() {
        clearWidgets();

        Button enabledButton = new EnabledButton(width - 24, 4);
        addRenderableWidget(enabledButton);

        // Header and Settings
        assert minecraft != null;
        addRenderableWidget(new StringWidget(0, 6, width, minecraft.font.lineHeight,
                Component.translatable("title.twitchemotes.config"), minecraft.font).alignCenter());

        int fieldWidth = width / 3;
        int headerGap = Math.max(8, height / 8);
        int contentY = minecraft.font.lineHeight + headerGap;
        addRenderableWidget(new StringWidget(0, contentY, width, minecraft.font.lineHeight,
                Component.translatable("editboxlabel.twitchemotes.channelbox"), minecraft.font).alignCenter());

        EditBox channelEditBox = new EditBox(minecraft.font, (width - fieldWidth) / 2,
                contentY + minecraft.font.lineHeight + 4, fieldWidth, 20, Component.empty());
        channelEditBox.setValue(channelName);
        channelEditBox.setResponder(value -> channelName = value);
        addRenderableWidget(channelEditBox);

        OptionInstance<Integer> emoteQualityOption = new OptionInstance<>(
                "slider.twitchemotes.quality",
                OptionInstance.noTooltip(),
                (text, value) -> Component.literal(value + "x"),
                new OptionInstance.IntRange(1,4),
                qualityPreference,
                value -> qualityPreference = value
        );

        int qualityLabelY = channelEditBox.getY() + channelEditBox.getHeight() + 12;
        addRenderableWidget(new StringWidget(0, qualityLabelY, width, minecraft.font.lineHeight,
                Component.translatable("editboxlabel.twitchemotes.quality"), minecraft.font).alignCenter());
        addRenderableWidget(emoteQualityOption.createButton(minecraft.options, (width - fieldWidth) / 2,
                qualityLabelY + minecraft.font.lineHeight + 4, fieldWidth));

        // Buttons Grid
        int footerY = height - 28;
        int buttonWidth = width / 5;
        int gridGapX = 8;
        int gridGapY = 4;
        int gridWidth = buttonWidth * 4 + gridGapX * 3;
        int gridX = (width - gridWidth) / 2;
        int gridY = qualityLabelY + minecraft.font.lineHeight + 36;
        int footerGap = 12;
        int availableGridHeight = footerY - footerGap - gridY - gridGapY;
        int buttonHeight = Math.max(20, Math.min(height / 5, availableGridHeight / 2));
        // twitch
        addRenderableWidget(new IconToggleButton(gridX, gridY,
                buttonWidth, buttonHeight,
                Component.translatable("button.twitchemotes.loadtwitchemotes"),
                new ResourceLocation("twitchemotes", "twitch"),
                loadTwitchEmotes, true, value -> loadTwitchEmotes = value));
        // 7tv
        addRenderableWidget(new IconToggleButton(gridX + buttonWidth + gridGapX, gridY,
                buttonWidth, buttonHeight,
                Component.translatable("button.twitchemotes.loadstv"),
                new ResourceLocation("twitchemotes", "7tv"),
                loadSTVEmotes, true, value -> loadSTVEmotes = value));
        // bttv
        addRenderableWidget(new IconToggleButton(gridX + (buttonWidth + gridGapX) * 2, gridY,
                buttonWidth, buttonHeight,
                Component.translatable("button.twitchemotes.loadbttv"),
                new ResourceLocation("twitchemotes", "betterttv"),
                loadBTTVEmotes, true, value -> loadBTTVEmotes = value));
        // ffz
        addRenderableWidget(new IconToggleButton(gridX + (buttonWidth + gridGapX) * 3, gridY,
                buttonWidth, buttonHeight,
                Component.translatable("button.twitchemotes.loadffz"),
                new ResourceLocation("twitchemotes", "frankerfacez"),
                loadFFZEmotes, true, value -> loadFFZEmotes = value));
        // unlisted
        addRenderableWidget(new IconToggleButton(gridX, gridY + buttonHeight + gridGapY,
                buttonWidth, buttonHeight,
                Component.translatable("button.twitchemotes.unlisted"),
                new ResourceLocation("twitchemotes", "unlisted"),
                loadUnlistedEmotes, true, value -> loadUnlistedEmotes = value));
        // overlay
        addRenderableWidget(new IconToggleButton(gridX + buttonWidth + gridGapX, gridY + buttonHeight + gridGapY,
                buttonWidth, buttonHeight,
                Component.translatable("button.twitchemotes.overlay"),
                new ResourceLocation("twitchemotes", "overlay"),
                overlayEmotes, true, value -> overlayEmotes = value));
        // animate
        addRenderableWidget(new IconToggleButton(gridX + (buttonWidth + gridGapX) * 2, gridY + buttonHeight + gridGapY,
                buttonWidth, buttonHeight,
                Component.translatable("button.twitchemotes.animate"),
                new ResourceLocation("twitchemotes", "animated"),
                animateEmotes, true, value -> animateEmotes = value));
        // twitch login
        final boolean loggedIn = TwitchEmotes.CONFIG.twitchClientId != null
                && TwitchEmotes.CONFIG.twitchOAuthToken != null
                && !TwitchEmotes.CONFIG.twitchClientId.isBlank()
                && !TwitchEmotes.CONFIG.twitchOAuthToken.isBlank();
        IconToggleButton btn = addRenderableWidget(new IconToggleButton(gridX + (buttonWidth + gridGapX) * 3,
                gridY + buttonHeight + gridGapY,
                buttonWidth, buttonHeight,
                Component.translatable("button.twitchemotes.login"),
                new ResourceLocation("twitchemotes", "key"),
                loggedIn, true, value -> {
            if(value) {
                minecraft.setScreen(new ConfirmScreen(
                        confirmed -> {
                            if(confirmed) {
                                Util.getPlatform().openUri("https://chatterino.com/client_login");
                                minecraft.setScreen(new TwitchPasteLoginScreen(this));
                            } else minecraft.setScreen(this);
                        }, Component.translatable("chat.link.confirmTrusted"),
                        Component.literal("https://chatterino.com/client_login").withStyle(style -> style.withColor(0x8800FF))
                ));
            } else {
                TwitchEmotes.CONFIG.twitchUserName = null;
                TwitchEmotes.CONFIG.twitchUserId = null;
                TwitchEmotes.CONFIG.twitchClientId = null;
                TwitchEmotes.CONFIG.twitchOAuthToken = null;
                TwitchEmotes.CONFIG.twitchDisplayName = null;
            }
        }));
        if(loggedIn && TwitchEmotes.CONFIG.twitchDisplayName != null)
            btn.setTooltip(Tooltip.create(
                    Component.translatable("tooltip.twitchemotes.loggedinas",
                            TwitchEmotes.CONFIG.twitchDisplayName).withStyle(style -> style.withColor(0x8800FF)))
            );

        // Footer
        int footerX = width / 2 - 154;
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL,
                button -> minecraft.setScreen(lastScreen)).bounds(footerX, footerY, 150, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("button.twitchemotes.save"),
                button -> {
                    if(handleSave()) minecraft.setScreen(lastScreen);
                }).bounds(footerX + 158, footerY, 150, 20).build());
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int i, int j, float f) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, i, j, f);
    }

    /**
     * Handles the save logic for the global config and displays error toasts upon errors
     * @return <code>true</code> if successful, <code>false</code> when errors occur
     */
    private boolean handleSave() {

        if(!updateChannelName())
            return false;

        boolean reloadEverything = TwitchEmotes.CONFIG.preferredQuality != qualityPreference ||
                (loadUnlistedEmotes != TwitchEmotes.CONFIG.includeUnlisted);

        TwitchEmotes.CONFIG.enabled = enabled;
        TwitchEmotes.CONFIG.loadTwitch = loadTwitchEmotes;
        TwitchEmotes.CONFIG.load7TV = loadSTVEmotes;
        TwitchEmotes.CONFIG.loadBTTV = loadBTTVEmotes;
        TwitchEmotes.CONFIG.loadFFZ = loadFFZEmotes;
        TwitchEmotes.CONFIG.includeUnlisted = loadUnlistedEmotes;
        TwitchEmotes.CONFIG.preferredQuality = qualityPreference;
        TwitchEmotes.CONFIG.animateEmotes = animateEmotes;
        TwitchEmotes.CONFIG.overlayEmotes = overlayEmotes;

        TwitchEmotes.CONFIG.save();
        if(reloadEverything)
            TwitchEmotes.execute(() -> TwitchEmotes.reload(true));
        else
            TwitchEmotes.execute(() -> TwitchEmotes.refresh(true));
        return true;
    }

    private boolean updateChannelName() {
        if(channelName != null && !channelName.equals(TwitchEmotes.CONFIG.twitchChannelName))
            if(channelName.isBlank()) {
                TwitchEmotes.CONFIG.twitchChannelId = "";
                TwitchEmotes.CONFIG.twitchChannelName = "";
            } else try {
                TwitchEmotes.CONFIG.twitchChannelId = new NetworkHandler().getTwitchIdFromName(channelName);
                TwitchEmotes.CONFIG.twitchChannelName = channelName;
            } catch (InvalidTwitchChannelException e) {
                ToastNotification.toast("toast.twitchemotes.invalidchannel",null, Color.RED);
                return false;
            } catch (UnknownTwitchEmotesException e) {
                ToastNotification.toast("toast.twitchemotes.unknownerror", null, Color.RED);
                return false;
            } catch (TwitchNotLoggedInException e) {
                ToastNotification.toast("toast.title.twitchemotes.failedauthresponse", "toast.twitchemotes.failedauthresponse", Color.RED);
                return false;
            }
        return true;
    }

    private class EnabledButton extends Button {
        private EnabledButton(int x, int y) {
            super(x, y, 20, 20, Component.empty(), button -> {
                enabled = !enabled;
                TwitchEmotesConfigScreen.this.init();
            }, DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
            int bgColor = isHoveredOrFocused() ? 0x4F888888 : 0x4F000000;
            guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bgColor);
            IconToggleButton.renderToggleMark(guiGraphics, getX() + 4, getY() + 4, 12, enabled);
        }
    }

}
