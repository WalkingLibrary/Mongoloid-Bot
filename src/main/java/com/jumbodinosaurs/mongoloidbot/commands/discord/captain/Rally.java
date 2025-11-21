package com.jumbodinosaurs.mongoloidbot.commands.discord.captain;

import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.models.CaptainCandidate;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.exceptions.UserQueryException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;

public class Rally extends Command implements IDiscordChatEventable
{
    private GuildMessageReceivedEvent event;

    @Override
    public MessageResponse getExecutedMessage() throws WaveringParametersException
    {
        Member member = event.getMember();

        try
        {
            // Get the current user and their captain candidate record
            UserAccount currentUser = UserAccount.getUser(member);
            CaptainCandidate captainCandidate = currentUser.getCaptainCandidate(member);

            StringBuilder stringBuilder = new StringBuilder();

            // If they’re not an active candidate, remind them they must become one first
            if (!captainCandidate.isActiveCampaign())
            {
                stringBuilder.append("You are not currently running an active campaign!\n");
                stringBuilder.append("Use **!candidateNow** to declare yourself as a captain candidate.");
                return new MessageResponse(stringBuilder.toString());
            }

            // Display supporter count
            int supporterCount = captainCandidate.getSupportersLongIds().size();
            stringBuilder.append("⚓ **Rally Report for " + member.getEffectiveName() + "** ⚓\n\n");
            stringBuilder.append("📣 You currently have **" + supporterCount + "** supporter");
            if (supporterCount != 1)
            {
                stringBuilder.append("s");
            }
            stringBuilder.append(".\n");

            // Show when they can retake the ship
            LocalDateTime retakeDateTime = TakeShip.GetNextRetakeDateTime(captainCandidate);
            Duration remaining = Duration.between(LocalDateTime.now(), retakeDateTime);

            long hours = remaining.toHours();
            long minutes = remaining.toMinutes() % 60;
            long seconds = remaining.getSeconds() % 60;


            if(!retakeDateTime.isBefore(LocalDateTime.now()))
            {
                stringBuilder.append("⏰ You cannot attempt to take the ship yet.\n");
                stringBuilder.append("Time remaining: **" + hours + "h " + minutes + "m " + seconds + "s**.\n");
            }

            return new MessageResponse(stringBuilder.toString());
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return new MessageResponse("Error accessing the database while checking your rally status.");
        }
        catch (UserQueryException e)
        {
            e.printStackTrace();
            return new MessageResponse("There was an issue fetching your account data.");
        }
    }

    @Override
    public String getHelpMessage()
    {
        return "Shows your supporter count and when you can next attempt to take over the ship.";
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
