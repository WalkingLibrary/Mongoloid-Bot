package com.jumbodinosaurs.mongoloidbot.tasks.captain;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDataBaseObjectHolder;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.devlib.database.objectHolder.SelectLimiter;
import com.jumbodinosaurs.devlib.log.LogManager;
import com.jumbodinosaurs.devlib.task.ScheduledTask;
import com.jumbodinosaurs.devlib.util.GeneralUtil;
import com.jumbodinosaurs.mongoloidbot.JDAController;
import com.jumbodinosaurs.mongoloidbot.brains.BrainsController;
import com.jumbodinosaurs.mongoloidbot.brains.IResponseUser;
import com.jumbodinosaurs.mongoloidbot.commands.discord.captain.CaptainNow;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Player;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.util.NPCUtil;
import com.jumbodinosaurs.mongoloidbot.models.CaptainCandidate;
import com.jumbodinosaurs.mongoloidbot.models.Crew;
import com.jumbodinosaurs.mongoloidbot.models.NPCMessage;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.SetupDatabaseConnection;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TauntTask extends ScheduledTask
{

    private static File currentTakeOVerCrewFile = GeneralUtil.checkFor(JDAController.optionsFolder, "currentTakeOverCrew.json");

    public TauntTask(ScheduledThreadPoolExecutor executor)
    {
        super(executor);
    }

    @Override
    public void run()
    {
        try
        {
            LogManager.consoleLogger.debug("Starting Taunt Task");
            CaptainCandidate defenderCandidate = CaptainCandidate.getAllCaptainCandidates()
                    .stream()
                    .filter(CaptainCandidate::isCaptain)
                    .findFirst()
                    .orElse(null);

            boolean isCurrentCaptainAnNPC = defenderCandidate == null;
            if(!isCurrentCaptainAnNPC)
            {
                LogManager.consoleLogger.debug("Skipping Taunt Task - Current Captain is not an NPC");
                return;
            }

            // 1. Load all Player objects
            Crew currentNPCCrew = Crew.loadCrewFromFile(getCurrentCrew());


            // 2. Collect NPCs
            List<Player> npcPlayers = currentNPCCrew.getPlayers();

            LogManager.consoleLogger.debug("Loaded " + npcPlayers.size() + " NPC player(s).");

            if (npcPlayers.isEmpty())
            {
                LogManager.consoleLogger.debug("No NPC players found — skipping taunt task.");
                return;
            }



            // 3. Pick a random NPC
            Random random = new Random();
            Player chosenNPC = npcPlayers.get(random.nextInt(npcPlayers.size()));

            List<NPCMessage> handcraftedMessages = currentNPCCrew.getHandCraftedMessages();
            boolean useHandcrafted = !handcraftedMessages.isEmpty() && random.nextDouble() < .95;

            if (useHandcrafted)
            {
                NPCMessage selectedMessage = handcraftedMessages.get(random.nextInt(handcraftedMessages.size()));
                Player takeOverMessageSender = npcPlayers.get(random.nextInt(npcPlayers.size()));
                if(selectedMessage.isSpecificNPC())
                {
                    takeOverMessageSender = NPCMessage.getSpecificNPCPlayer(selectedMessage);
                }

                LogManager.consoleLogger.debug("Using handcrafted message: " + selectedMessage.getMessage());
                NPCUtil.SendNPCChatMessage("captainChannel", selectedMessage.getMessage(), takeOverMessageSender);
                return;
            }

            StringBuilder tauntResponseGenerationMessageBuilder = new StringBuilder();
            String prePrompt = "Please generate a response to the following context - Note that what you say back will be sent directly to the live game:";
            tauntResponseGenerationMessageBuilder.append(prePrompt);
            tauntResponseGenerationMessageBuilder.append(currentNPCCrew.getPromptMessage());

            // Build full prompt
            String tauntPrompt = tauntResponseGenerationMessageBuilder.toString();

            // Replace any variables in the prompt
            tauntPrompt = tauntPrompt
                    .replace("{npcName}", chosenNPC.getPromptName())
                    .replace("{npcBase64Name}", chosenNPC.getPromptNameBase64())
                    .replace("{crewName}", currentNPCCrew.getCrewNPCsFileName());

            LogManager.consoleLogger.debug("Generating a new Taunt");
            BrainsController.generateResponse(tauntPrompt,
                    new IResponseUser()
                    {
                        @Override
                        public void UseResponse(String response)
                        {

                            // 5. Send taunt to Discord captain channel
                            try
                            {
                                if(!response.contains("Error generating LLM response: Connection timed out: connect"))
                                {
                                    NPCUtil.SendNPCChatMessage("captainChannel", response, chosenNPC);
                                }
                                LogManager.consoleLogger.debug("🦴 " + chosenNPC.getPromptName() + " taunted: " + response);
                            }
                            catch (IOException e)
                            {
                                LogManager.consoleLogger.error("Failed to send NPC taunt: " + e.getMessage());
                            }
                        }
                    });



        }
        catch (SQLException e)
        {
            LogManager.consoleLogger.error("Database error in Taunt Task: " + e.getMessage());
        }
        catch (Exception e)
        {
            LogManager.consoleLogger.error("Unexpected error in Taunt Task: " + e.getMessage());
        }
    }

    @Override
    public TimeUnit getTimeUnit()
    {
        return TimeUnit.MINUTES;
    }

    @Override
    public int getPeriod()
    {
        return 60;
    }

    public int getOrderingNumber()
    {
        return 2;
    }



    public static void setCurrentCrew(String crewName)
    {
        Map<String, String> data = new HashMap<>();
        data.put("currentCrew", crewName);
        String takeOverSaveString = new Gson().toJson(data);
        GeneralUtil.writeContents(currentTakeOVerCrewFile, takeOverSaveString, false);
    }

    public static String getCurrentCrew()
    {
        String takeOverSaveString = GeneralUtil.scanFileContents(currentTakeOVerCrewFile);
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> data = new Gson().fromJson(takeOverSaveString, type);
        return data.get("currentCrew");
    }
}
