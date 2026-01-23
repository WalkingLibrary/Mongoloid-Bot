package com.jumbodinosaurs.mongoloidbot.commands.discord.captain;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.CommandWithParameters;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.devlib.json.GsonUtil;
import com.jumbodinosaurs.devlib.log.LogManager;
import com.jumbodinosaurs.devlib.options.OptionsManager;
import com.jumbodinosaurs.devlib.util.GeneralUtil;
import com.jumbodinosaurs.mongoloidbot.Main;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Player;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.util.NPCUtil;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IAdminCommand;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IOwnerCommand;
import com.jumbodinosaurs.mongoloidbot.models.CaptainCandidate;
import com.jumbodinosaurs.mongoloidbot.models.Crew;
import com.jumbodinosaurs.mongoloidbot.models.NPCMessage;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.captain.TauntTask;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StartEvent extends CommandWithParameters implements IOwnerCommand, IDiscordChatEventable
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
            if(getParameters() == null || getParameters().size() <= 0)
            {
                return new MessageResponse("No Crew File Given");
            }
            String crewFileName = getParameters().get(0).getParameter();
            replaceCurrentCaptain();
            return generateAndAddNPCCrew(crewFileName);
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


    private MessageResponse generateAndAddNPCCrew(String crewFileName) throws SQLException
    {
        LogManager.consoleLogger.debug("Generating and adding NPC crew players...");
        Crew npcCrew = Crew.loadCrewFromFile(crewFileName);
        Player.removeAllNPCPlayers();
        ArrayList<Player> npcPlayers = npcCrew.getPlayers();
        Player.addNPCPlayers(npcPlayers);

        NPCMessage takeOVerMessage = npcCrew.getTakeOVerMessage();
        Player takeOverMessageSender = npcPlayers.get(0);
        if(takeOVerMessage.isSpecificNPC())
        {
            takeOverMessageSender = NPCMessage.getSpecificNPCPlayer(takeOVerMessage);
        }

        try
        {
            NPCUtil.SendNPCChatMessage("captainChannel", takeOVerMessage.getMessage(), takeOverMessageSender);
        }
        catch (IOException e)
        {
            LogManager.consoleLogger.error(e.getMessage(), e);
        }
        TauntTask.setCurrentCrew(crewFileName);
        LogManager.consoleLogger.debug("NPC crew has been added.");
        TakeShip.isPirateWarActive = true;
        return new MessageResponse("So it Begins!");
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

        Role deputyCaptainRole = event.getGuild().getRoleById(DeputyNow.deputyID);
        for (Member member : Main.jdaController.getJda().getGuildById(Main.GUILD_ID).getMembers())
        {
            if (member.getRoles().contains(deputyCaptainRole))
            {
                event.getGuild().removeRoleFromMember(member, deputyCaptainRole).complete();

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
