package com.jumbodinosaurs.mongoloidbot.commands.discord.items;

import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.AppSettingsManager;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Item;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Player;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.PlayerInventory;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.models.DiscordANSITextHelper;
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

            int alternatorSpace = 0;
            for (Integer key : playersInventory.keySet())
            {

                Item currentItem = playersInventory.get(key);
                if (currentItem == null)
                {
                    continue;
                }
                //Every Other Item
                alternatorSpace++;
                if (alternatorSpace % 2 == 0)
                {
                    inventoryContents += " " + key + ". " + currentItem.toInventoryDisplay() + "\n";
                    continue;
                }

                inventoryContents += key + ". " + currentItem.toInventoryDisplay() + "\n";
            }
            if (currentUsersPlayer.getPendingItem() != null)
            {
                inventoryContents += "\n";
                inventoryContents += "Pending Item:" + currentUsersPlayer.getPendingItem().toInventoryDisplay() + "\n";
            }

            if (currentUsersPlayer.getItemForSale() != null)
            {
                inventoryContents += "\n";
                inventoryContents += "Item For Sale: " + currentUsersPlayer.getItemForSale().toInventoryDisplay()
                         + "\nPrice: " + currentUsersPlayer.getItemSellPrice();
            }


            if (inventoryContents.equals(""))
            {
                inventoryContents = " Empty!";
            }
            System.out.println(DiscordANSITextHelper.finalWrap(username + "'s Inventory:\n" + inventoryContents));
            return new MessageResponse(
                    DiscordANSITextHelper.finalWrap(username + "'s Inventory:\n" + inventoryContents));
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
