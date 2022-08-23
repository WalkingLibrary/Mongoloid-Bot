package com.jumbodinosaurs.mongoloidbot.commands.discord.items;

import com.jumbodinosaurs.devlib.commands.CommandWithParameters;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Item;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Player;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.PlayerInventory;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.exceptions.UserQueryException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.sql.SQLException;
import java.util.HashMap;

public class ItemStats extends CommandWithParameters implements IDiscordChatEventable
{

    private GuildMessageReceivedEvent event;

    @Override
    public String getCategory()
    {
        return "Item";
    }

    @Override
    public MessageResponse getExecutedMessage() throws WaveringParametersException
    {
        Member member = event.getMember();
        try
        {

            if (getParameters().size() < 1)
            {
                return new MessageResponse("You did not tell me which slot to Look At!");
            }


            int slotToStat = Integer.parseInt(getParameters().get(0).getParameter());

            UserAccount currentUser = UserAccount.getUser(member);
            Player currentUsersPlayer = currentUser.getPlayer(member);


            if (currentUsersPlayer.getInventory() == null)
            {
                currentUsersPlayer.setInventory(new PlayerInventory());
            }

            HashMap<Integer, Item> playersInventory = currentUsersPlayer.getInventory().getItems();

            if (!playersInventory.containsKey(slotToStat))
            {
                return new MessageResponse("That isn't a valid slot!");
            }

            Item itemToStat = playersInventory.get(slotToStat);


            String formattedOutput = "The %s\nIntensity: %s\nType: %s";


            formattedOutput = String.format(formattedOutput, itemToStat.getName(),
                    itemToStat.getAbility().getIntensity(), itemToStat.getAbility().getType().displayName);

            return new MessageResponse(formattedOutput);
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
        return "Allows the Player to view the Stats of their Items\nUsage:~" + getClass().getSimpleName() + " [inventory slot to check]";
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
