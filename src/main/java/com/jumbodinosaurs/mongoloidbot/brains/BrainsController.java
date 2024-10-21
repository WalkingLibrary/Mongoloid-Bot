package com.jumbodinosaurs.mongoloidbot.brains;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.jumbodinosaurs.devlib.log.LogManager;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;

public class BrainsController
{
    public static BrainsOptions brainsOptions;

    public BrainsController()
    {

    }


    public static void respond(GuildMessageReceivedEvent event, String message)
    {
        // Send the POST request and get the requestId
        try
        {
            String response = sendPostRequest(brainsOptions.getEndPoint() + "/api/submit-prompt", message);
            String requestId = parseRequestId(response);
            // Start polling the status of the requestId every 2 seconds
            if (requestId != null)
            {
                System.out.println("Request ID : " + requestId);
                pollStatus(event, requestId);
            }
        }
        catch (Exception e)
        {
            LogManager.consoleLogger.error(e.getMessage());
            e.printStackTrace();
        }
    }


    // Parse requestId from JSON response using Gson
    public static String parseRequestId(String response)
    {
        try
        {
            JsonObject jsonObject = new Gson().fromJson(response, JsonObject.class);
            if (jsonObject.has("requestId"))
            {
                return jsonObject.get("requestId").getAsString();
            }
        }
        catch (JsonSyntaxException e)
        {
            System.err.println("Failed to parse requestId from response: " + e.getMessage());
        }
        return null;
    }

    // Poll the status every 2 seconds
    public static void pollStatus(GuildMessageReceivedEvent event, String requestId)
    {
        Timer timer = new Timer();
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                try
                {
                    event.getChannel().sendTyping().complete();
                    // Define the status check endpoint
                    String endpoint = brainsOptions.getEndPoint() + "/api/prompt-status/" + requestId;

                    // Send GET request to check the status
                    String response = sendGetRequest(endpoint);

                    // Parse the status from the response
                    JsonObject jsonObject = new Gson().fromJson(response, JsonObject.class);
                    String status = jsonObject.get("status").getAsString();

                    LogManager.consoleLogger.debug("Brains Request: " + requestId);
                    if (status.equalsIgnoreCase("completed"))
                    {
                        // If status is completed, print the response and stop polling
                        LogManager.consoleLogger.info("Request Completed: " + jsonObject.get("response").getAsString());
                        String responseToPrompt = String.valueOf(jsonObject.get("response"));
                        responseToPrompt = responseToPrompt.replace("\\n", "\n");
                        event.getMessage().reply(responseToPrompt).complete();
                        event.getChannel().sendTyping().complete();
                        timer.cancel(); // Stop the timer
                    }
                    else if (status.toLowerCase().contains("waiting"))
                    {
                        // If status is waiting, continue polling
                        LogManager.consoleLogger.debug("Request is still waiting...");
                    }
                    else
                    {
                        // If status is neither completed nor waiting, it has failed, stop polling
                        LogManager.consoleLogger.error("Request Failed.");
                        timer.cancel(); // Stop the timer
                    }
                }
                catch (Exception e)
                {
                    LogManager.consoleLogger.error(e.getMessage());
                    e.printStackTrace();
                    timer.cancel(); // Stop polling in case of error
                }
            }
        };

        // Schedule the task to run every 2 seconds
        timer.scheduleAtFixedRate(task, 0, 2000);
    }

    // Send a GET request to check the status
    public static String sendGetRequest(String endpointUrl) throws Exception
    {
        // Create the URL object
        URL url = new URL(endpointUrl);

        // Open a connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set request method to GET
        connection.setRequestMethod("GET");

        // Set request headers
        connection.setRequestProperty("Accept", "application/json");

        // Read the response from the input stream
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null)
        {
            response.append(inputLine);
        }

        // Close the input stream
        in.close();

        // Return the response as a string
        return response.toString();
    }

    public static String sendPostRequest(String endpointUrl, String payload) throws Exception
    {
        // Create the URL object
        URL url = new URL(endpointUrl);

        // Open a connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set request method to POST
        connection.setRequestMethod("POST");

        // Set the request headers
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");

        // Enable input/output streams
        connection.setDoOutput(true);

        // Write the payload to the request body
        try (OutputStream os = connection.getOutputStream())
        {
            String base64Encoded = Base64.getEncoder().encodeToString(payload.getBytes());
            byte[] input = ("\"" + base64Encoded + "\"").getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Get the response code
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        // Read the response from the input stream
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null)
        {
            response.append(inputLine);
        }

        // Close the input stream
        in.close();

        // Return the response as a string
        return response.toString();
    }

    public static void disableSSLVerification() throws Exception
    {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager()
                {
                    public X509Certificate[] getAcceptedIssuers()
                    {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType)
                    {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType)
                    {
                    }
                }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Disable hostname verification
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

}
