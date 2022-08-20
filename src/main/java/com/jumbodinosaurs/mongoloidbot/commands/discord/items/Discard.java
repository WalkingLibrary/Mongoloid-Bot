package com.jumbodinosaurs.mongoloidbot.commands.discord.items;

import com.jumbodinosaurs.devlib.commands.CommandWithParameters;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Item;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Player;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.exceptions.UserQueryException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.sql.SQLException;

public class Discard extends CommandWithParameters implements IDiscordChatEventable
{

    private GuildMessageReceivedEvent event;


    @Override
    public MessageResponse getExecutedMessage() throws WaveringParametersException
    {
        Member member = event.getMember();

        try
        {
            if (getParameters().size() < 1)
            {
                return new MessageResponse("You didn't Tell me which Slot!!");
            }

            String slotParameter = getParameters().get(0).getParameter();
            UserAccount currentUser = UserAccount.getUser(member);
            Player currentUsersPlayer = currentUser.getPlayer(member);

            if (slotParameter.startsWith("p"))
            {
                if (currentUsersPlayer.getPendingItem() == null)
                {
                    return new MessageResponse("You don't have a Pending Item");
                }

                Item removedItem = currentUsersPlayer.getPendingItem();
                currentUsersPlayer.setPendingItem(null);
                UserAccount.updatePlayer(currentUsersPlayer);
                return new MessageResponse(removedItem.getName() + " has been Discarded!!");

            }

            int slot = Integer.parseInt(getParameters().get(0).getParameter());
            if (slot <= 0 || slot > Inventory.maxInventoryAmount)
            {
                return new MessageResponse("That is not a valid slot!");
            }

            Item removedItem = currentUsersPlayer.getInventory().getItems().get(slot);
            currentUsersPlayer.getInventory().getItems().put(slot, null);
            UserAccount.updatePlayer(currentUsersPlayer);
            return new MessageResponse(removedItem.getName() + " has been Discarded!!");
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
        return "Allows the User to Discard an Item from their Inventory\nUsage: ~" + this.getClass()
                .getSimpleName() + " [P or Slot Number]";
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
