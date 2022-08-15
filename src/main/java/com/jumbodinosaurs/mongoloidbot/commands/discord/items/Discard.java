package com.jumbodinosaurs.mongoloidbot.commands.discord.items;

import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.coin.UserAccount;
import com.jumbodinosaurs.mongoloidbot.coin.exceptions.UserQueryException;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Player;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.sql.SQLException;

public class Discard extends Command implements IDiscordChatEventable
{

    private GuildMessageReceivedEvent event;


    @Override
    public MessageResponse getExecutedMessage() throws WaveringParametersException
    {
        Member member = event.getMember();

        try
        {
            UserAccount currentUser = UserAccount.getUser(member);
            Player currentUsersPlayer = currentUser.getPlayer(member);

            String username = event.getMember().getUser().getName();

            currentUsersPlayer.setPendingItem(null);

            UserAccount.updatePlayer(currentUsersPlayer);
            return new MessageResponse("You no longer have a Pending Item");
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
        return "Allows the User to Discard their Pending Item\nUsage: ~" + this.getClass().getSimpleName();
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
