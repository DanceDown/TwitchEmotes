package me.dancedown.twitchemotes.gui;

import me.dancedown.twitchemotes.TwitchEmotes;
import me.dancedown.twitchemotes.config.TwitchEmotesConfig;
import me.dancedown.twitchemotes.network.NetworkHandler;
import me.dancedown.twitchemotes.network.ProviderType;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

public class TwitchPasteLoginScreen extends Screen {

    private final Screen screen;
    private final AtomicBoolean isLoggingIn = new AtomicBoolean(false);

    // Set using reflections (bc I'm lazy)
    private String user_id;
    private String username;
    private String client_id;
    private String oauth_token;

    public TwitchPasteLoginScreen(Screen screen) {
        super(Component.translatable("title.twitchemotes.pastelogin"));
        this.screen = screen;
    }

    @Override
    protected void init() {
        clearWidgets();
        HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
        layout.addTitleHeader(Component.translatable("title.twitchemotes.pastelogin"), minecraft.font);
        layout.addToContents(new IconToggleButton(
                0, 0, width / 3, height / 2,
                Component.translatable("button.twitchemotes.pastelogin"),
                Identifier.fromNamespaceAndPath("twitchemotes", "key"),
                false,
                false,
                b -> handleLogin()
        ));
        layout.addToFooter(Button.builder(CommonComponents.GUI_CANCEL, b -> minecraft.setScreen(screen)).build());
        layout.visitWidgets(this::addRenderableWidget);
        layout.arrangeElements();
    }

    /**
     * Reads the clipboard information, sets the info in the config and shows error toasts when login failed.
     * Lastly sets the screen back to the last screen
     */
    private void handleLogin() {
        // Preventing multiple presses
        if(isLoggingIn.get())
            return;
        isLoggingIn.set(true);

        // Getting clipboard and setting local variables
        String clipboard = minecraft.keyboardHandler.getClipboard();
        String[] fields = clipboard.split(";");
        for(String field : fields) {
            String[] pair = field.split("=");
            if(pair.length != 2)
                continue;
            String key = pair[0];
            String value = pair[1];
            try {
                Field f = this.getClass().getDeclaredField(key);
                f.set(this, value);
            } catch (NullPointerException | NoSuchFieldException | IllegalAccessException ignored) {}
        }

        // Checking if input is valid
        NetworkHandler networkHandler = new NetworkHandler();
        TwitchEmotesConfig config = TwitchEmotesConfig.create(null);
        config.twitchUserId = user_id;
        config.twitchUserName = username;
        config.twitchClientId = client_id;
        config.twitchOAuthToken = oauth_token;
        String displayName = networkHandler.validateTwitchUser(config);
        if(displayName != null) {
            ToastNotification.toast(Component.translatable("toast.twitchemotes.loggedinas", displayName).withColor(Color.GREEN.getRGB()),null);
            TwitchEmotes.CONFIG.twitchUserId = user_id;
            TwitchEmotes.CONFIG.twitchUserName = username;
            TwitchEmotes.CONFIG.twitchClientId = client_id;
            TwitchEmotes.CONFIG.twitchOAuthToken = oauth_token;
            TwitchEmotes.CONFIG.twitchDisplayName = displayName;
            // Forcing the official Twitch API instead of the Scalar API
            TwitchEmotes.execute(() -> TwitchEmotes.reloadProvider(ProviderType.TWITCH, true));
        } else ToastNotification.toast("toast.twitchemotes.invalidlogin", null, Color.RED);
        minecraft.setScreen(screen);
    }

}
