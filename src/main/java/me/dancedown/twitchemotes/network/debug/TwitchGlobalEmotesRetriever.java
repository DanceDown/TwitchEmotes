package me.dancedown.twitchemotes.network.debug;

import com.google.gson.*;
import me.dancedown.twitchemotes.config.TwitchEmotesConfig;
import me.dancedown.twitchemotes.network.NetworkHandler;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

public class TwitchGlobalEmotesRetriever {

    public static void main(String[] args) throws Exception {

        JFileChooser chooser = new JFileChooser();
        File configFile, outputFile;
        int ret = chooser.showOpenDialog(null);
        if (ret != JFileChooser.APPROVE_OPTION)
            configFile = null;
        else
            configFile = chooser.getSelectedFile();

        ret = chooser.showSaveDialog(null);
        if (ret != JFileChooser.APPROVE_OPTION)
            System.exit(0);
        outputFile = chooser.getSelectedFile();

        getAndWriteTwitchGlobalEmotesJson(configFile, outputFile);

    }

    private static void getAndWriteTwitchGlobalEmotesJson(File configFrom, File outputFile) throws IOException, URISyntaxException {

        if (!outputFile.exists())
            if (!outputFile.createNewFile())
                System.err.println("Couldn't create emotes.json");

        FileWriter writer = new FileWriter(outputFile);
        NetworkHandler networkHandler = new NetworkHandler();
        JsonElement element;

        if (configFrom != null) {
            TwitchEmotesConfig config = TwitchEmotesConfig.load(configFrom);
            element = networkHandler.getJsonAuthResponse("https://api.twitch.tv/helix/chat/emotes/global", config);
        } else {
            element = networkHandler.getArrayResponse("https://api.ivr.fi/v2/twitch/emotes/sets?set_id=0");
        }
        writer.write(element.toString());
        writer.close();
    }
}
