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

import java.sql.SQLException;
import java.util.HashMap;

public class Inventory extends Command implements IDiscordChatEventable
{

    public static int maxInventoryAmount = 5;
    private GuildMessageReceivedEvent event;


    @Override
    public String getCategory()
    {
        return "Item";
    }

    @Override
    public MessageResponse getExecutedMessage()
            throws WaveringParametersException
    {
        Member member = event.getMember();

        try
        {
            UserAccount currentUser = UserAccount.getUser(member);
            Player currentUsersPlayer = currentUser.getPlayer(member);

            String username = event.getMember().getUser().getName();
            String inventoryContents = "";

            if (currentUsersPlayer.getInventory() == null)
            {
                currentUsersPlayer.setInventory(
                        new PlayerInventory());
            }

            HashMap<Integer, Item> playersInventory = currentUsersPlayer.getInventory().getItems();

            if (playersInventory == null)
            {
                playersInventory = new HashMap<>();
                currentUsersPlayer.getInventory().setItems(playersInventory);
            }

            for (Integer key : playersInventory.keySet())
            {
                Item currentItem = playersInventory.get(key);
                if (currentItem == null)
                {
                    continue;
                }

                inventoryContents += key + ". " + currentItem.getName() + "\n";
            }

            if (currentUsersPlayer.getPendingItem() != null)
            {
                inventoryContents += "Pending Item: " + currentUsersPlayer.getPendingItem().getName() + "\n";
            }

            if (currentUsersPlayer.getItemForSale() != null)
            {
                inventoryContents += "Item For Sale: " + currentUsersPlayer.getItemForSale()
                        .getName() + "\nPrice: " + currentUsersPlayer.getItemSellPrice() + " " + event.getGuild()
                        .getEmoteById("916589679518838794")
                        .getAsMention();
            }


            if (inventoryContents.equals(""))
            {
                inventoryContents = " Empty!";
            }

            return new MessageResponse(username + "'s Inventory:\n" + inventoryContents);
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
        return "Allows the User to check their Inventory\nUsage:~" + this.getClass().getSimpleName();
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
