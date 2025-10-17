package com.jumbodinosaurs.mongoloidbot.commands.discord.captain;

import com.google.gson.reflect.TypeToken;
import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.devlib.json.GsonUtil;
import com.jumbodinosaurs.devlib.log.LogManager;
import com.jumbodinosaurs.devlib.util.GeneralUtil;
import com.jumbodinosaurs.mongoloidbot.Main;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Player;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IAdminCommand;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.models.CaptainCandidate;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

public class StartEvent extends Command implements IAdminCommand, IDiscordChatEventable
{
    private GuildMessageReceivedEvent event;

    @Override
    public MessageResponse getExecutedMessage() throws WaveringParametersException
    {
        /*
         * Process for StartEvent Command
         * 1. Generate and Add NPC Crew Players + NPCCaptainCandidate
         * 2. Replace the Current Captain
         */

        try
        {

            StringBuilder message = new StringBuilder();
            message.append("CAPTAIN AHOY! Something stirs below the waves!\n");
            message.append("https://cdn.discordapp.com/attachments/480122702888435734/1426651988963233843/CaptainLookStart.mp4?ex=68ec00b4&is=68eaaf34&hm=f89560749c4a5a15da502ca0cd6a5aa029c3e81db98c591b0d30cc4d75b2c1b7& \n");
            Role captainRole = event.getGuild().getRoleById(CaptainNow.captainID);
            for (Member guildMember : Main.jdaController.getJda().getGuildById(Main.GUILD_ID).getMembers())
            {
                if (guildMember.getRoles().contains(captainRole))
                {
                    event.getGuild().removeRoleFromMember(guildMember, captainRole).complete();

                    message.append(guildMember.getAsMention() + "\n");
                }
            }
            // 1. Generate and Add NPC Crew Players + NPCCaptainCandidate
            generateAndAddNPCCrew();
            // 2. Replace the Current Captain
            replaceCurrentCaptain();
            TakeShip.isPirateWarActive = true;
            return new MessageResponse(message.toString());
        }
        catch (Exception e)
        {
            LogManager.consoleLogger.error(e.getMessage());
            return new MessageResponse("❌ Failed to start event big dumby");
        }
    }

    @Override
    public String getHelpMessage()
    {
        return "Admin Command To Start an Event";
    }

    /**
     * 1. Generate and Add NPC Crew Players + NPCCaptainCandidate
     */
    private void generateAndAddNPCCrew() throws SQLException
    {
        /*
         * 1. Load Crew List Json File from Options/Crews/skeletoncrew.json
         * 2. Load using gson object util
         * 3. Get and Remove all the Player NPCs from the Player Table
         * 4. Add All the new NPC Players to the Player Table
         */

        LogManager.consoleLogger.debug("Generating and adding NPC crew players...");

        // Step 1: Load JSON file
        File skeletonCrewFile = GeneralUtil.checkFor(GeneralUtil.userDir, "Options/Crews/skeletoncrew.json", false);

        if (!skeletonCrewFile.exists())
        {
            LogManager.consoleLogger.error("❌ Crew file not found: " + skeletonCrewFile.getAbsolutePath());
            return;
        }

        // Step 2: Deserialize crew list using GsonUtil
        ArrayList<Player> npcCrew = GsonUtil.readList(
                skeletonCrewFile,
                Player.class,
                new TypeToken<ArrayList<Player>>() {},
                false
        );

        if (npcCrew == null || npcCrew.isEmpty())
        {
            LogManager.consoleLogger.error("❌ No NPC crew found in " + skeletonCrewFile.getAbsolutePath());
            return;
        }

        LogManager.consoleLogger.debug("✅ Loaded " + npcCrew.size() + " NPC crew members from file.");
        // Step 3: Remove all existing NPCs from the Player table
        Player.removeAllNPCPlayers();

        // Step 4: Add the new NPCs to the Player table
        Player.addNPCPlayers(npcCrew);

        LogManager.consoleLogger.debug("NPC crew has been added.");
    }


    /**
     * 2. Replace the Current Captain
     */
    private void replaceCurrentCaptain() throws SQLException
    {

        LogManager.consoleLogger.debug("Replacing the current captain...");

        // 3. Remove Rank and Captain Flag From everyone else
        Role captainRole = event.getGuild().getRoleById(CaptainNow.captainID);
        ArrayList<CaptainCandidate> allCandidates = CaptainCandidate.getAllCaptainCandidates();
        for (CaptainCandidate candidate : allCandidates)
        {
            if (candidate.isCaptain())
            {
                candidate.setCaptain(false);
                UserAccount.updateCaptainCandidate(candidate);
            }
        }

        for (Member guildMember : Main.jdaController.getJda().getGuildById(Main.GUILD_ID).getMembers())
        {
            if (guildMember.getRoles().contains(captainRole))
            {
                event.getGuild().removeRoleFromMember(guildMember, captainRole).complete();
            }
        }
    }

    @Override
    public GuildMessageReceivedEvent getGuildMessageReceivedEvent()
    {
        return event;
    }

    @Override
    public void setGuildMessageReceivedEvent(GuildMessageReceivedEvent event)
    {
        this.event = event;
    }
}
