package me.dancedown.twitchemotes.network.debug;

import com.google.gson.JsonObject;
import me.dancedown.twitchemotes.network.NetworkHandler;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;

public class STVGlobalEmotesRetriever {

    public static void main(String[] args) throws Exception {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;
        File output = chooser.getSelectedFile();

        NetworkHandler net = new NetworkHandler();
        JsonObject json = net.getObjectResponse("https://7tv.io/v3/emote-sets/global");

        try (FileWriter w = new FileWriter(output)) {
            w.write(json.toString());
        }
    }

}
