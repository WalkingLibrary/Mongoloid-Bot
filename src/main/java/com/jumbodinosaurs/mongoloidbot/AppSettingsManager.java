package com.jumbodinosaurs.mongoloidbot;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;

public class AppSettingsManager
{
    private static boolean isDebugMode = true;
    private static JsonObject settings;

    public AppSettingsManager()
    {

    }

    public static void loadAppSettingsFile(boolean isDevelopment)
    {
        // Determine the file path based on the environment
        String filePath = isDevelopment ? "Options/dev.appsettings.json" : "Options/appsettings.json";

        // Load the settings from the file
        try (FileReader reader = new FileReader(filePath))
        {
            settings = new JsonParser().parse(reader).getAsJsonObject();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Failed to load configuration file.");
        }
    }

    public static String getValue(String key)
    {
        if(settings == null)
        {
            loadAppSettingsFile(isDebugMode);
        }
        // Access the key value
        JsonElement element = settings.get(key);
        return element != null ? element.getAsString() : null; // Return null if key is not found or key is not a string
    }
}

