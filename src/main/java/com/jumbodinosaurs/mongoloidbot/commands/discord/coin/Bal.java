package com.jumbodinosaurs.mongoloidbot.commands.discord.coin;

import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.AppSettingsManager;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.exceptions.UserQueryException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.sql.SQLException;
import java.util.Base64;

public class Bal extends Command implements IDiscordChatEventable
{

    private GuildMessageReceivedEvent event;

    @Override
    public String getCategory()
    {
        return "Coin";
    }

    @Override
    public MessageResponse getExecutedMessage()
            throws WaveringParametersException
    {
        Member member = event.getMember();
        String uniqueIdentifier = UserAccount.getUniqueIdentifier(member);
        String discordUserNameBase64 = Base64.getEncoder().encodeToString(uniqueIdentifier.getBytes());

        try
        {
            UserAccount currentUser = UserAccount.getUser(member);
            
            String username = event.getMember().getUser().getName();
            return new MessageResponse(username +
                                       "'s Balance: " +
                                       currentUser.getBalance().toString() +
                                       " " +
                                       event.getGuild().getEmoteById(AppSettingsManager.getStringValue("emoteId")).getAsMention());
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
        return "Allows you to check you Coin Balance\nUsage: \n~" + getClass().getSimpleName();
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
