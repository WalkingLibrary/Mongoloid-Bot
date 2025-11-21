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
import java.util.Base64;

public class CandidateNow extends Command implements IDiscordChatEventable
{
    private GuildMessageReceivedEvent event;

    @Override
    public MessageResponse getExecutedMessage() throws WaveringParametersException
    {
        Member member = event.getMember();
        String uniqueIdentifier = UserAccount.getUniqueIdentifier(member);
        String discordUserNameBase64 = Base64.getEncoder().encodeToString(uniqueIdentifier.getBytes());

        try
        {
            UserAccount currentUser = UserAccount.getUser(member);

            CaptainCandidate captainCandidate = currentUser.getCaptainCandidate(member);

            for (CaptainCandidate candidate : CaptainCandidate.getAllCaptainCandidates())
            {
                candidate.removeSupporter(String.valueOf(member.getIdLong()));
                UserAccount.updateCaptainCandidate(candidate);
            }

            StringBuilder stringBuilder = new StringBuilder();
            if(!captainCandidate.isActiveCampaign())
            {
                stringBuilder.append(event.getMember().getEffectiveName() + " has stepped up!\n");
                captainCandidate.setActiveCampaign(true);
            }

            stringBuilder.append("You have " + captainCandidate.getSupportersLongIds().size() + " Supporters\n");

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
            UserAccount.updateCaptainCandidate(captainCandidate);
            return new MessageResponse(stringBuilder.toString());
        }
        catch(SQLException e)
        {
            e.printStackTrace();
            return new MessageResponse("Error Checking Database");
        }
        catch(UserQueryException e)
        {
            e.printStackTrace();
            return new MessageResponse("Account Error");
        }
    }

    @Override
    public String getHelpMessage()
    {
        return "Allows the User to become a Captain Candidate";
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
