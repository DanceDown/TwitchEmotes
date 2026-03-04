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
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
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

        SpriteIconButton enabledButton = SpriteIconButton.builder(Component.empty(), x -> {
            enabled = !enabled;
            this.init();
                }, true)
                .sprite(Identifier.withDefaultNamespace("pending_invite/" + (enabled ? "accept" : "reject")), 16, 16)
                .size(20, 20).build();
        enabledButton.setPosition(width - enabledButton.getWidth() - 4, 4);
        addRenderableWidget(enabledButton);

        // Header and Settings
        HeaderAndFooterLayout mainLayout = new HeaderAndFooterLayout(this);
        mainLayout.addTitleHeader(Component.translatable("title.twitchemotes.config"), minecraft.font);
        mainLayout.setHeaderHeight(minecraft.font.lineHeight + 12);
        LinearLayout contentLayout = mainLayout.addToContents(LinearLayout.vertical());
        contentLayout.defaultCellSetting().alignHorizontallyCenter();
        contentLayout.addChild(new StringWidget(Component.translatable("editboxlabel.twitchemotes.channelbox"), minecraft.font));
        contentLayout.addChild(new SpacerElement(width,4));
        EditBox channelEditBox = contentLayout.addChild(new EditBox(minecraft.font, width / 3, 20, Component.empty()));
        channelEditBox.setValue(channelName);
        channelEditBox.setResponder(value -> channelName = value);
        OptionInstance<Integer> emoteQualityOption = new OptionInstance<>(
                "slider.twitchemotes.quality",
                OptionInstance.noTooltip(),
                (text, value) -> Component.literal(value + "x"),
                new OptionInstance.IntRange(1,4),
                qualityPreference,
                value -> qualityPreference = value
        );
        contentLayout.addChild(new SpacerElement(width,12));
        contentLayout.addChild(new StringWidget(Component.translatable("editboxlabel.twitchemotes.quality"), minecraft.font));
        contentLayout.addChild(new SpacerElement(width,4));
        contentLayout.addChild(emoteQualityOption.createButton(minecraft.options, 0, 0, width / 3));
        contentLayout.addChild(new SpacerElement(0, 12));

        // Buttons Grid
        GridLayout buttonLayout = contentLayout.addChild(new GridLayout());
        buttonLayout.defaultCellSetting().paddingHorizontal(4).paddingBottom(4).alignHorizontallyCenter();
        GridLayout.RowHelper rh = buttonLayout.createRowHelper(4);
        // twitch
        rh.addChild(new IconToggleButton(0, 0,
                width / 5, height / 5,
                Component.translatable("button.twitchemotes.loadtwitchemotes"),
                Identifier.fromNamespaceAndPath("twitchemotes", "twitch"),
                loadTwitchEmotes, true, value -> loadTwitchEmotes = value));
        // 7tv
        rh.addChild(new IconToggleButton(0, 0,
                width / 5, height / 5,
                Component.translatable("button.twitchemotes.loadstv"),
                Identifier.fromNamespaceAndPath("twitchemotes", "7tv"),
                loadSTVEmotes, true, value -> loadSTVEmotes = value));
        // bttv
        rh.addChild(new IconToggleButton(0, 0,
                width / 5, height / 5,
                Component.translatable("button.twitchemotes.loadbttv"),
                Identifier.fromNamespaceAndPath("twitchemotes", "betterttv"),
                loadBTTVEmotes, true, value -> loadBTTVEmotes = value));
        // ffz
        rh.addChild(new IconToggleButton(0, 0,
                width / 5, height / 5,
                Component.translatable("button.twitchemotes.loadffz"),
                Identifier.fromNamespaceAndPath("twitchemotes", "frankerfacez"),
                loadFFZEmotes, true, value -> loadFFZEmotes = value));
        // unlisted
        rh.addChild(new IconToggleButton(0, 0,
                width / 5, height / 5,
                Component.translatable("button.twitchemotes.unlisted"),
                Identifier.fromNamespaceAndPath("twitchemotes", "unlisted"),
                loadUnlistedEmotes, true, value -> loadUnlistedEmotes = value));
        // overlay
        rh.addChild(new IconToggleButton(0, 0,
                width / 5, height / 5,
                Component.translatable("button.twitchemotes.overlay"),
                Identifier.fromNamespaceAndPath("twitchemotes", "overlay"),
                overlayEmotes, true, value -> overlayEmotes = value));
        // animate
        rh.addChild(new IconToggleButton(0, 0,
                width / 5, height / 5,
                Component.translatable("button.twitchemotes.animate"),
                Identifier.fromNamespaceAndPath("twitchemotes", "animated"),
                animateEmotes, true, value -> animateEmotes = value));
        // twitch login
        final boolean loggedIn = TwitchEmotes.CONFIG.twitchClientId != null
                && TwitchEmotes.CONFIG.twitchOAuthToken != null
                && !TwitchEmotes.CONFIG.twitchClientId.isBlank()
                && !TwitchEmotes.CONFIG.twitchOAuthToken.isBlank();
        IconToggleButton btn = rh.addChild(new IconToggleButton(0, 0,
                width / 5, height / 5,
                Component.translatable("button.twitchemotes.login"),
                Identifier.fromNamespaceAndPath("twitchemotes", "key"),
                loggedIn, true, value -> {
            if(value) {
                minecraft.setScreen(new ConfirmScreen(
                        confirmed -> {
                            if(confirmed) {
                                Util.getPlatform().openUri("https://chatterino.com/client_login");
                                minecraft.setScreen(new TwitchPasteLoginScreen(this));
                            } else minecraft.setScreen(this);
                        }, Component.translatable("chat.link.confirmTrusted"),
                        Component.literal("https://chatterino.com/client_login").withColor(0x8800FF)
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
                            TwitchEmotes.CONFIG.twitchDisplayName).withColor(0x8800FF))
            );

        // Footer
        LinearLayout footerButtonLayout = mainLayout.addToFooter(LinearLayout.horizontal().spacing(4));
        footerButtonLayout.defaultCellSetting().alignHorizontallyCenter();
        footerButtonLayout.addChild(Button.builder(CommonComponents.GUI_CANCEL,
                button -> minecraft.setScreen(lastScreen)).build());
        footerButtonLayout.addChild(Button.builder(Component.translatable("button.twitchemotes.save"),
                button -> {
                    if(handleSave()) minecraft.setScreen(lastScreen);
                }).build());
        mainLayout.visitWidgets(this::addRenderableWidget);
        mainLayout.arrangeElements();
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int i, int j, float f) {
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

}
