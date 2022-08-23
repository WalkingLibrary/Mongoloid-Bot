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

public class Swap extends CommandWithParameters implements IDiscordChatEventable
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
                return new MessageResponse("You did not tell me which slot to Swap!");
            }


            int slotToSwap = Integer.parseInt(getParameters().get(0).getParameter());

            UserAccount currentUser = UserAccount.getUser(member);
            Player currentUsersPlayer = currentUser.getPlayer(member);

            if (currentUsersPlayer.getPendingItem() == null)
            {
                return new MessageResponse("You Don't have a Pending Item!");
            }

            if (currentUsersPlayer.getInventory() == null)
            {
                currentUsersPlayer.setInventory(new PlayerInventory());
            }

            HashMap<Integer, Item> playersInventory = currentUsersPlayer.getInventory().getItems();

            if (slotToSwap <= 0 || slotToSwap > com.jumbodinosaurs.mongoloidbot.commands.discord.items.Inventory.maxInventoryAmount)
            {
                return new MessageResponse("That isn't a valid slot!");
            }

            Item oldItem = null;
            if (playersInventory.containsKey(slotToSwap))
            {
                oldItem = playersInventory.get(slotToSwap);
            }
            playersInventory.put(slotToSwap, currentUsersPlayer.getPendingItem());

            currentUsersPlayer.setPendingItem(oldItem);
            UserAccount.updatePlayer(currentUsersPlayer);
            return new MessageResponse(oldItem.getName() + " has been swapped!");
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
        return "Allows the User to Swap their Pending Item with an Item in their Inventory\nUsage: ~" + this.getClass()
                .getSimpleName() + " [Number of the Slot you want to swap]";
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
