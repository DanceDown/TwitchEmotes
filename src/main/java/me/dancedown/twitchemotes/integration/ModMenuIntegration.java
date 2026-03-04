package me.dancedown.twitchemotes.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.dancedown.twitchemotes.gui.TwitchEmotesConfigScreen;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TwitchEmotesConfigScreen::new;
    }
}
