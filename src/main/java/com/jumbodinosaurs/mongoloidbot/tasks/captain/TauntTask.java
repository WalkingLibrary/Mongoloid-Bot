package com.jumbodinosaurs.mongoloidbot.tasks.captain;

import com.google.gson.Gson;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDataBaseObjectHolder;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.devlib.database.objectHolder.SelectLimiter;
import com.jumbodinosaurs.devlib.log.LogManager;
import com.jumbodinosaurs.devlib.task.ScheduledTask;
import com.jumbodinosaurs.mongoloidbot.brains.BrainsController;
import com.jumbodinosaurs.mongoloidbot.brains.IResponseUser;
import com.jumbodinosaurs.mongoloidbot.commands.discord.captain.CaptainNow;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Player;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.util.NPCUtil;
import com.jumbodinosaurs.mongoloidbot.models.CaptainCandidate;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.SetupDatabaseConnection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TauntTask extends ScheduledTask
{
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
            ArrayList<SQLDataBaseObjectHolder> loadedObjects = SQLDatabaseObjectUtil.loadObjects(
                    SetupDatabaseConnection.mogoloidDatabase,
                    Player.class,
                    new SelectLimiter()
                    {
                        @Override
                        public String getSelectLimiterStatement()
                        {
                            return ""; // Load all players
                        }
                    }
            );

            // 2. Collect NPCs
            List<Player> npcPlayers = new ArrayList<>();

            for (SQLDataBaseObjectHolder holder : loadedObjects)
            {
                Player player = new Gson().fromJson(holder.getJsonObject(), Player.class);
                player.setId(holder.getId());

                if (player.isNPC())
                {
                    npcPlayers.add(player);
                }
            }

            LogManager.consoleLogger.debug("Loaded " + npcPlayers.size() + " NPC player(s).");

            if (npcPlayers.isEmpty())
            {
                LogManager.consoleLogger.debug("No NPC players found — skipping taunt task.");
                return;
            }

            // 3. Pick a random NPC
            Random random = new Random();
            Player chosenNPC = npcPlayers.get(random.nextInt(npcPlayers.size()));


            StringBuilder tauntResponseGenerationMessageBuilder = new StringBuilder();
            tauntResponseGenerationMessageBuilder.append("Please Generate a Response to the Following Context:");
            tauntResponseGenerationMessageBuilder.append("Your name is " + chosenNPC.getPromptName() + " and you are a part of a skeleton pirate ");
            tauntResponseGenerationMessageBuilder.append("crew that wants to insight the players of a game to try and take the ship");
            tauntResponseGenerationMessageBuilder.append(" Now with this in mind generate a taunt to send to the players. Note that what you say back will be sent directly to the live game.");
            tauntResponseGenerationMessageBuilder.append("Make it down right mean and controversial, use expletives!  Lean into that the ship is yours");
            LogManager.consoleLogger.debug("Generating a new Taunt");
            BrainsController.generateResponse(tauntResponseGenerationMessageBuilder.toString(),
                    new IResponseUser()
                    {
                        @Override
                        public void UseResponse(String response)
                        {

                            // 5. Send taunt to Discord captain channel
                            try
                            {
                                NPCUtil.SendNPCChatMessage("captainChannel", response, chosenNPC);
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
}
