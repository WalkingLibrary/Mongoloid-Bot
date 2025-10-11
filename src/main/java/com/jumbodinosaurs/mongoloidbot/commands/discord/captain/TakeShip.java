package com.jumbodinosaurs.mongoloidbot.commands.discord.captain;

import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.AppSettingsManager;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.models.CaptainCandidate;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.exceptions.UserQueryException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class TakeShip extends Command implements IDiscordChatEventable
{
    public static int intervalHoursForReTakeShip = (int) AppSettingsManager.getValue("intervalHoursForReTakeShip");
    public static int intervalHoursForMutiny = (int) AppSettingsManager.getValue("intervalHoursForMutiny");

    public static boolean isPirateWarActive = false;

    private GuildMessageReceivedEvent event;

    public static LocalDateTime GetNextRetakeDateTime(CaptainCandidate captainCandidate)
    {
        if(captainCandidate.isWasLastTakeOverAttemptMutiny())
        {
            return captainCandidate.getLastTakeOverAttempt().minusHours(-intervalHoursForMutiny);
        }
        return captainCandidate.getLastTakeOverAttempt().minusHours(-intervalHoursForReTakeShip);
    }

    @Override
    public MessageResponse getExecutedMessage() throws WaveringParametersException
    {
        Member member = event.getMember();

        try
        {
            UserAccount currentUser = UserAccount.getUser(member);
            CaptainCandidate captainCandidate = currentUser.getCaptainCandidate(member);

            StringBuilder stringBuilder = new StringBuilder();

            if (!captainCandidate.isActiveCampaign())
            {
                stringBuilder.append("You are not currently an active captain candidate!\n");
                stringBuilder.append("Use **!candidateNow** to start your campaign.");
                return new MessageResponse(stringBuilder.toString());
            }

            LocalDateTime retakeTime = GetNextRetakeDateTime(captainCandidate);
            if (retakeTime.isAfter(LocalDateTime.now()))
            {
                stringBuilder.append("⏰ You cannot attempt to take the ship yet.\n");
                stringBuilder.append("You may try again **at " + retakeTime.toString() + "**.");
                return new MessageResponse(stringBuilder.toString());
            }

            stringBuilder.append("⚓ **" + member.getEffectiveName() + " attempts to take the ship!** ⚓\n");

            // ===========================================================
            // PLACEHOLDER: INSERT YOUR "TAKE SHIP" LOGIC HERE
            //
            // You have access to:
            //   - captainCandidate (this user's candidate record)
            //   - captainCandidate.getSupportersLongIds() (list of supporters)
            //   - ReTakeUtil.GetNextRetakeDateTime(captainCandidate)
            //   - You can compare against other CaptainCandidate objects
            //
            // Example flow (pseudo-code):
            //
            //   boolean success = determineTakeoverSuccess(captainCandidate);
            //   if (success) {
            //       stringBuilder.append("🏴‍☠️ The ship has been successfully taken over! "
            //           + member.getEffectiveName() + " is now the Captain!\n");
            //       captainCandidate.setLastTakeOverAttempt(LocalDateTime.now());
            //       captainCandidate.setCaptain(true);
            //       UserAccount.updateCaptainCandidate(captainCandidate);
            //   } else {
            //       stringBuilder.append("💀 The takeover attempt failed! Better luck next time.\n");
            //   }
            //
            // ===========================================================

            // Example default placeholder response:
            stringBuilder.append("⚠️ Ship takeover logic not yet implemented.\n");
            stringBuilder.append("This is where success/failure would be determined.\n");

            return new MessageResponse(stringBuilder.toString());
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return new MessageResponse("Error accessing the database while processing your takeover attempt.");
        }
        catch (UserQueryException e)
        {
            e.printStackTrace();
            return new MessageResponse("Error fetching your account data.");
        }
    }

    @Override
    public String getHelpMessage()
    {
        return "Attempts to take control of the ship if your campaign and timing allow it.";
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
