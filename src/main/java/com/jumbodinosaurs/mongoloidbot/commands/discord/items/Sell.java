package com.jumbodinosaurs.mongoloidbot.commands.discord.items;

import com.jumbodinosaurs.devlib.commands.CommandWithParameters;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.AppSettingsManager;
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

public class Sell extends CommandWithParameters implements IDiscordChatEventable
{

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
            if (getParameters().size() < 2)
            {
                return new MessageResponse("You did not Give Enough Information");
            }

            String slotToSellString = getParameters().get(0).getParameter();
            String priceToSellString = getParameters().get(1).getParameter();

            int slotToSell;
            BigDecimal priceToSell;
            try
            {
                slotToSell = Integer.parseInt(slotToSellString);
                priceToSell = new BigDecimal(priceToSellString);
            }
            catch (NumberFormatException e)
            {
                return new MessageResponse("Please Enter Valid Numbers");
            }

            if (priceToSell.signum() < 0)
            {
                return new MessageResponse("You cannot Steal Money from People");
            }

            if (slotToSell < 1 || slotToSell > Inventory.maxInventoryAmount)
            {
                return new MessageResponse("Please enter a valid Slot Number!");
            }


            UserAccount currentUser = UserAccount.getUser(member);
            Player currentUsersPlayer = currentUser.getPlayer(member);
            PlayerInventory playersInventory = currentUsersPlayer.getInventory();

            if (!playersInventory.getItems().containsKey(slotToSell))
            {
                return new MessageResponse("No Item in slot " + slotToSell);
            }

            if (currentUsersPlayer.getItemForSale() != null)
            {
                return new MessageResponse("You already have an Item for Sale");
            }

            Item itemToSell = playersInventory.getItems().get(slotToSell);
            currentUsersPlayer.setItemForSale(itemToSell);
            playersInventory.getItems().put(slotToSell, null);
            currentUsersPlayer.setItemSellPrice(priceToSell);
            UserAccount.updatePlayer(currentUsersPlayer);
            return new MessageResponse(
                    itemToSell.getName() + " is now for sale for " + priceToSellString + " " + event.getGuild()
                            .getEmoteById(AppSettingsManager.getStringValue("emoteId"))
                            .getAsMention());
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
        return "Allows the User to sell an Item\nUsage:~" + this.getClass().getSimpleName() + " [Slot To Sell] [Price]";
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
