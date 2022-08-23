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

import java.math.BigDecimal;
import java.sql.SQLException;

public class BuyItem extends CommandWithParameters implements IDiscordChatEventable
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
            if (getParameters().size() < 1 || event.getMessage().getMentionedMembers().size() <= 0)
            {
                return new MessageResponse("You did not Tell me who to buy from!");
            }

            UserAccount currentUser = UserAccount.getUser(member);
            Player currentUsersPlayer = currentUser.getPlayer(member);
            PlayerInventory playersInventory = currentUsersPlayer.getInventory();

            Member memberToBuyFrom = event.getMessage().getMentionedMembers().get(0);
            UserAccount userAccountToBuyFrom = UserAccount.getUser(memberToBuyFrom);
            Player playerToBuyFrom = userAccountToBuyFrom.getPlayer(memberToBuyFrom);


            if (playerToBuyFrom.getItemForSale() == null)
            {
                return new MessageResponse(memberToBuyFrom.getEffectiveName() + " doesn't have an Item for Sale");
            }

            BigDecimal itemPrice = playerToBuyFrom.getItemSellPrice();
            if (currentUser.getBalance().subtract(itemPrice).signum() < 0)
            {
                return new MessageResponse(
                        "You don't Have Enough to buy " + playerToBuyFrom.getItemForSale().getName());
            }

            int slotToPutIn = -1;
            for (int i = 1; i < Inventory.maxInventoryAmount; i++)
            {
                if (!playersInventory.getItems().containsKey(i))
                {
                    slotToPutIn = i;
                    break;
                }
            }

            if (slotToPutIn == -1)
            {
                return new MessageResponse("You don't have enough Space in Your Inventory");
            }

            Item boughtItem = playerToBuyFrom.getItemForSale();
            playerToBuyFrom.setItemForSale(null);
            currentUser.setBalance(currentUser.getBalance().subtract(playerToBuyFrom.getItemSellPrice()));
            userAccountToBuyFrom.setBalance(userAccountToBuyFrom.getBalance().add(playerToBuyFrom.getItemSellPrice()));
            playerToBuyFrom.setItemSellPrice(new BigDecimal("0"));
            playersInventory.getItems().put(slotToPutIn, boughtItem);


            //Update All Four Objects
            UserAccount.updateUser(currentUser);
            UserAccount.updateUser(userAccountToBuyFrom);
            UserAccount.updatePlayer(currentUsersPlayer);
            UserAccount.updatePlayer(playerToBuyFrom);
            return new MessageResponse("You now own " + boughtItem.getName());
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
