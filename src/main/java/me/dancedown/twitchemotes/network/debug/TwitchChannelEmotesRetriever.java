package me.dancedown.twitchemotes.network.debug;

import com.google.gson.JsonElement;
import me.dancedown.twitchemotes.config.TwitchEmotesConfig;
import me.dancedown.twitchemotes.network.NetworkHandler;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

public class TwitchChannelEmotesRetriever {

    public static void main(String[] args) throws Exception {

        JFileChooser chooser = new JFileChooser();
        File configFile, outputFile;
        int ret = chooser.showOpenDialog(null);
        if (ret != JFileChooser.APPROVE_OPTION)
            System.exit(0);
        configFile = chooser.getSelectedFile();
        ret = chooser.showSaveDialog(null);
        if (ret != JFileChooser.APPROVE_OPTION)
            System.exit(0);
        outputFile = chooser.getSelectedFile();

        getAndWriteTwitchChannelEmotesJson(configFile, outputFile);

    }

    private static void getAndWriteTwitchChannelEmotesJson(File configFrom, File outputFile) throws IOException, URISyntaxException {
        TwitchEmotesConfig config = TwitchEmotesConfig.load(configFrom);

        if(!outputFile.exists())
            if (!outputFile.createNewFile())
                System.err.println("Couldn't create emotes.json");

        FileWriter writer = new FileWriter(outputFile);
        NetworkHandler networkHandler = new NetworkHandler();
        JsonElement element;
        if(config.twitchOAuthToken == null || config.twitchOAuthToken.isEmpty())
            element = networkHandler.getObjectResponse("https://api.ivr.fi/v2/twitch/emotes/channel/" + config.twitchChannelName);
        else
            element = networkHandler.getJsonAuthResponse("https://api.twitch.tv/helix/chat/emotes?broadcaster_id=" + config.twitchChannelId, config);
        writer.write(element.toString());
        writer.close();
    }
}
