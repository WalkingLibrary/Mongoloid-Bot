package com.jumbodinosaurs.mongoloidbot.commands.discord.captain;

import com.jumbodinosaurs.devlib.commands.CommandWithParameters;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.models.CaptainCandidate;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.exceptions.UserQueryException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class CastLot extends CommandWithParameters implements IDiscordChatEventable
{
    private GuildMessageReceivedEvent event;

    @Override
    public MessageResponse getExecutedMessage() throws WaveringParametersException
    {
        try
        {
            Member member = event.getMember();
            // Get the person to Support
            if (getParameters() == null || getParameters().size() < 1)
            {
                throw new WaveringParametersException("You didn't tell me who to support");
            }

            // Pay the Money to the Specified Account
            List<Member> mentionedMembers = event.getMessage().getMentionedMembers();
            if (mentionedMembers.size() <= 0)
            {
                return new MessageResponse("You didn't tell me who to support");
            }
            Member memberToSupport = mentionedMembers.get(0);

            // Make sure not same guy
            if (memberToSupport.getIdLong() == member.getIdLong())
            {
                return new MessageResponse("You can't support yourself");
            }

            UserAccount currentUser = UserAccount.getUser(member);


            UserAccount userAccountToSupport = UserAccount.getUser(memberToSupport);
            CaptainCandidate newCaptainCandidate = userAccountToSupport.getCaptainCandidate(memberToSupport);

            if(!newCaptainCandidate.isActiveCampaign())
            {
                return new MessageResponse("They haven't stepped up to be Captain");
            }

            for (CaptainCandidate candidate : CaptainCandidate.getAllCaptainCandidates())
            {
                candidate.removeSupporter(String.valueOf(member.getIdLong()));
                UserAccount.updateCaptainCandidate(candidate);
            }
            CaptainCandidate currentCaptainCandidate = currentUser.getCaptainCandidate(member);
            // Transfer supporters from current to new candidate
            for (String supporterId : currentCaptainCandidate.getSupportersLongIds())
            {
                newCaptainCandidate.getSupportersLongIds().add(supporterId);
            }
            // Add current member as a supporter to the new candidate
            newCaptainCandidate.getSupportersLongIds().add(String.valueOf(member.getIdLong()));

            currentCaptainCandidate.setActiveCampaign(false); // Set current campaign to inactive

            UserAccount.updateCaptainCandidate(currentCaptainCandidate);
            UserAccount.updateCaptainCandidate(newCaptainCandidate);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(member.getEffectiveName() + " has supported a new captain, " + memberToSupport.getEffectiveName() + "!\n");
            stringBuilder.append("The new captain now has " + newCaptainCandidate.getSupportersLongIds().size() + " supporters.\n");

            LocalDateTime retakeDateTime = TakeShip.GetNextRetakeDateTime(newCaptainCandidate);
            String reTakeTime = "at " + retakeDateTime.toString();
            if (retakeDateTime.isBefore(LocalDateTime.now()))
            {
                reTakeTime = "now!";
            }
            stringBuilder.append("The new captain can attempt to take the ship over " + reTakeTime + ".\n");

            return new MessageResponse(stringBuilder.toString());
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return new MessageResponse("Error Checking Database");
        }
        catch (UserQueryException e)
        {
            e.printStackTrace();
            return new MessageResponse("Account Error");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new MessageResponse("Error");
        }
    }


    @Override
    public String getHelpMessage()
    {
        return "Allows the User to Cast their Support to a Captain Candidate";
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