package com.jumbodinosaurs.mongoloidbot.brains;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jumbodinosaurs.mongoloidbot.AppSettingsManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/** Direct OpenAI Responses API client used by commands that need summarization. */
public final class OpenAIController
{
    private static final String RESPONSES_ENDPOINT = "https://api.openai.com/v1/responses";

    private OpenAIController()
    {
    }

    public static void generateResponse(String prompt, IResponseUser callback)
    {
        CompletableFuture.runAsync(() ->
        {
            try
            {
                callback.UseResponse(requestResponse(prompt));
            }
            catch (Exception e)
            {
                callback.UseResponse("Error generating OpenAI response: " + e.getMessage());
            }
        });
    }

    private static String requestResponse(String prompt) throws Exception
    {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty())
        {
            apiKey = AppSettingsManager.getStringValue("OPENAI_API_KEY");
        }
        if (apiKey == null || apiKey.trim().isEmpty())
        {
            throw new IllegalStateException("OPENAI_API_KEY is not configured.");
        }

        String model = System.getenv("OPENAI_MODEL");
        if (model == null || model.trim().isEmpty())
        {
            model = AppSettingsManager.getStringValue("OPENAI_MODEL");
        }
        if (model == null || model.trim().isEmpty())
        {
            model = "gpt-4o-mini";
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.addProperty("instructions",
                "Summarize the supplied Discord conversation into exactly one concise paragraph. "
                        + "Do not use bullets or a title.");
        requestBody.addProperty("input", prompt);
        requestBody.addProperty("max_output_tokens", 500);

        HttpURLConnection connection = (HttpURLConnection) new URL(RESPONSES_ENDPOINT).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        byte[] body = new Gson().toJson(requestBody).getBytes(StandardCharsets.UTF_8);
        try (OutputStream output = connection.getOutputStream())
        {
            output.write(body);
        }

        int statusCode = connection.getResponseCode();
        InputStream responseStream = statusCode >= 200 && statusCode < 300
                ? connection.getInputStream()
                : connection.getErrorStream();
        String responseBody = readResponse(responseStream);
        if (statusCode < 200 || statusCode >= 300)
        {
            throw new IllegalStateException("OpenAI returned HTTP " + statusCode + ": " + responseBody);
        }

        return parseResponseText(responseBody);
    }

    private static String readResponse(InputStream responseStream) throws Exception
    {
        if (responseStream == null)
        {
            return "";
        }
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(responseStream, StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                response.append(line);
            }
        }
        return response.toString();
    }

    private static String parseResponseText(String responseBody)
    {
        JsonObject response = new Gson().fromJson(responseBody, JsonObject.class);
        if (response.has("output_text"))
        {
            return response.get("output_text").getAsString();
        }

        JsonArray output = response.getAsJsonArray("output");
        if (output != null)
        {
            for (int i = 0; i < output.size(); i++)
            {
                JsonObject item = output.get(i).getAsJsonObject();
                JsonArray content = item.getAsJsonArray("content");
                if (content == null)
                {
                    continue;
                }
                for (int j = 0; j < content.size(); j++)
                {
                    JsonObject contentItem = content.get(j).getAsJsonObject();
                    if (contentItem.has("text"))
                    {
                        return contentItem.get("text").getAsString();
                    }
                }
            }
        }

        throw new IllegalStateException("OpenAI returned no text output.");
    }
}
