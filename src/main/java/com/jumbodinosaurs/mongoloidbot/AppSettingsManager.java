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


    public static String getStringValue(String key)
    {
        return (String) getValue(key);
    }
    public static Object getValue(String key)
    {
        if (settings == null)
        {
            loadAppSettingsFile(isDebugMode);
        }

        JsonElement element = settings.get(key);
        if (element == null)
        {
            return null; // Key not found
        }

        // Check and return the appropriate type
        if (element.isJsonPrimitive())
        {
            if (element.getAsJsonPrimitive().isBoolean())
            {
                return element.getAsBoolean();
            }
            else if (element.getAsJsonPrimitive().isNumber())
            {
                // Try to detect what type of number (int, long, double)
                String numStr = element.getAsString();
                if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E"))
                {
                    return element.getAsDouble();
                }
                else
                {
                    try
                    {
                        return element.getAsInt();
                    }
                    catch (NumberFormatException e)
                    {
                        return element.getAsLong();
                    }
                }
            }
            else
            {
                return element.getAsString();
            }
        }
        return element.getAsString(); // Default to string if something else
    }
}

