package me.dancedown.twitchemotes.network.debug;

import com.google.gson.JsonArray;
import me.dancedown.twitchemotes.network.NetworkHandler;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;

public class BTTVGlobalEmotesRetriever {

    public static void main(String[] args) throws Exception {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;
        File output = chooser.getSelectedFile();

        NetworkHandler net = new NetworkHandler();
        JsonArray json = net.getArrayResponse("https://api.betterttv.net/3/cached/emotes/global");

        try (FileWriter w = new FileWriter(output)) {
            w.write(json.toString());
        }
    }

}
