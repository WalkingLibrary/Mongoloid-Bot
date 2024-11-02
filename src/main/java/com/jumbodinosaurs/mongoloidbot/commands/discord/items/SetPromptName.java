package com.jumbodinosaurs.mongoloidbot.commands.discord.items;

import com.jumbodinosaurs.devlib.commands.CommandWithParameters;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Player;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.exceptions.UserQueryException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.sql.SQLException;

public class SetPromptName extends CommandWithParameters implements IDiscordChatEventable
{

    private GuildMessageReceivedEvent event;

    @Override
    public MessageResponse getExecutedMessage() throws WaveringParametersException
    {
        if (getParameters().size() <= 0)
        {
            return new MessageResponse("You didn't Give a Name!");
        }

        String givenName = getParameters().get(0).getParameter();

        if (givenName.isEmpty())
        {
            return new MessageResponse("You didn't Give a Name!");
        }

        if (givenName.length() > 20)
        {
            return new MessageResponse("Name is Too Long Must be less than 20");
        }

        Member member = event.getMember();
        UserAccount currentUser = null;
        try
        {
            currentUser = UserAccount.getUser(member);
            Player currentUsersPlayer = currentUser.getPlayer(member);
            currentUsersPlayer.setPromptName(givenName);
            UserAccount.updatePlayer(currentUsersPlayer);
            return new MessageResponse("Your Prompt Name is now: " + givenName);
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
    }

    @Override
    public String getHelpMessage()
    {
        return "Lets you set what name will be used for you in the Khan Battles";
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
