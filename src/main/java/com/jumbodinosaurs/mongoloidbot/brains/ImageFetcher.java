package com.jumbodinosaurs.mongoloidbot.brains;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jumbodinosaurs.devlib.util.GeneralUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ImageFetcher
{

    private static final OkHttpClient client = createClient();
    private static final Gson gson = new Gson();

    public static File fetchImage(String prompt) throws Exception
    {
        String jsonResponse = makeApiCall("https://aiservices.calebwarren.dev/MongolBrains/api/image?prompt=" + prompt);
        String imageUrl = extractImageUrl(jsonResponse);
        return downloadAndSaveImage(imageUrl, prompt);
    }

    private static String makeApiCall(String url) throws Exception
    {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute())
        {
            if (response.code() != 200)
            {
                throw new Exception("Error Fetching Image - HTTP Response: " + response);
            }
            return response.body().string();
        }
    }

    private static String extractImageUrl(String jsonResponse)
    {
        JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
        return jsonObject.get("imageUrl").getAsString();
    }

    private static File downloadAndSaveImage(String imageUrl, String prompt) throws IOException
    {
        String md5Hash = DigestUtils.md5Hex(prompt);
        String savePath = "images/" + md5Hash + ".png";

        Request request = new Request.Builder().url(imageUrl).build();

        try (Response response = client.newCall(request).execute())
        {
            File fileToReturn = GeneralUtil.checkForLocalPath(GeneralUtil.userDir, savePath);
            new FileOutputStream(fileToReturn).write(response.body().bytes());
            return fileToReturn;
        }
    }

    public static OkHttpClient createClient()
    {
        return new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.MINUTES)  // Set connection timeout to 2 minutes
                .readTimeout(2, TimeUnit.MINUTES)     // Set read timeout to 2 minutes
                .build();
    }

}

