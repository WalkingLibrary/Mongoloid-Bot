package com.jumbodinosaurs.mongoloidbot.commands.discord.items;

import com.jumbodinosaurs.devlib.commands.Command;
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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;

public class UnList extends Command implements IDiscordChatEventable
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
            UserAccount currentUser = UserAccount.getUser(member);
            Player currentUsersPlayer = currentUser.getPlayer(member);
            PlayerInventory playerInventory = currentUsersPlayer.getInventory();

            if (currentUsersPlayer.getItemForSale() == null)
            {
                return new MessageResponse("You Don't have an Item for Sale!");
            }

            if (currentUsersPlayer.getInventory() == null)
            {
                currentUsersPlayer.setInventory(new PlayerInventory());
            }

            HashMap<Integer, Item> playersInventory = currentUsersPlayer.getInventory().getItems();

            if (playerInventory.getItems().size() >= Inventory.maxInventoryAmount)
            {
                return new MessageResponse("You Don't have Enough Space in your Inventory");
            }


            for (int i = 1; i <= Inventory.maxInventoryAmount; i++)
            {
                if (!playerInventory.getItems().containsKey(i))
                {
                    String itemName = currentUsersPlayer.getItemForSale().getName();
                    playerInventory.getItems().put(i, currentUsersPlayer.getItemForSale());
                    currentUsersPlayer.setItemForSale(null);
                    currentUsersPlayer.setItemSellPrice(new BigDecimal("0"));
                    UserAccount.updatePlayer(currentUsersPlayer);
                    return new MessageResponse(itemName + " is no Longer Listed");
                }
            }
            return new MessageResponse("No Slots Open in your Inventory!");
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
