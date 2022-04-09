package com.jumbodinosaurs.mongoloidbot.commands.discord.coin;

import com.jumbodinosaurs.devlib.commands.CommandWithParameters;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.mongoloidbot.Main;
import com.jumbodinosaurs.mongoloidbot.coin.UserAccount;
import com.jumbodinosaurs.mongoloidbot.coin.exceptions.UserQueryException;
import com.jumbodinosaurs.mongoloidbot.coin.tasks.LotteryTask;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.SetupDatabaseConnection;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class DeleteMessage extends CommandWithParameters implements IDiscordChatEventable
{

    private GuildMessageReceivedEvent event;

    @Override
    public MessageResponse getExecutedMessage() throws WaveringParametersException
    {


        /*
         * Process for Deleting another Users Message
         * Check their Money
         * Delete Message
         * Remove money for their account
         * Return Deleted message
         *
         *
         * */


        try
        {
            UserAccount accountToUpdate = UserAccount.getUser(event.getMember());
            BigDecimal costToToggleLight = new BigDecimal("5000");
            //1. Check their Money
            if (accountToUpdate.getBalance().subtract(costToToggleLight).signum() <= -1)
            {
                return new MessageResponse("You Don't have Enough to Delete that Message");
            }

            //2.  Get Message to Delete

            if (getParameters() == null || getParameters().size() < 1)
            {
                throw new WaveringParametersException("You didn't Give enough Information");
            }


            //Filter ID for Numbers only
            String messageToDelete = getParameters().get(0).getParameter();

            String idFilter = "^[0-9]*[0-9]$";
            Pattern filterPattern = Pattern.compile(idFilter);
            if (!filterPattern.matcher(messageToDelete).matches())
            {
                return new MessageResponse("Please enter a Valid ID");
            }


            //Delete Message
            for (TextChannel textChannel : Main.jdaController.getJda().getGuildById(Main.GUILD_ID).getTextChannels())
            {
                try
                {
                    textChannel.deleteMessageById(messageToDelete).complete();
                }
                catch (ErrorResponseException e)
                {
                    //Do nothing
                }
            }


            // Remove money for their account
            accountToUpdate.setBalance(accountToUpdate.getBalance().subtract(costToToggleLight));

            SQLDatabaseObjectUtil.putObject(SetupDatabaseConnection.mogoloidDatabase,
                    accountToUpdate,
                    accountToUpdate.getId());


            // Add Money to Pot
            LotteryTask.addToPot(costToToggleLight);


            return new MessageResponse(messageToDelete + " Has Been Deleted");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return new MessageResponse("Error Accessing the Database");
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
        return "Allows you To Delete Other Users Messages.\nUsage: ~" + getClass().getSimpleName() + " message_id";
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
