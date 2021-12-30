package com.jumbodinosaurs.mongoloidbot.commands.discord.coin;

import com.jumbodinosaurs.devlib.commands.CommandWithParameters;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.mongoloidbot.coin.UserAccount;
import com.jumbodinosaurs.mongoloidbot.coin.exceptions.UserQueryException;
import com.jumbodinosaurs.mongoloidbot.coin.tasks.LotteryTask;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.SetupDatabaseConnection;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.math.BigDecimal;
import java.sql.SQLException;

public class Lottery extends CommandWithParameters implements IDiscordChatEventable
{
    
    private GuildMessageReceivedEvent event;
    
    @Override
    public MessageResponse getExecutedMessage()
            throws WaveringParametersException
    {
        
        BigDecimal amountToBuy = new BigDecimal(String.valueOf(1));
        
        if(getParameters() != null && getParameters().size() >= 1)
        {
            try
            {
                amountToBuy = new BigDecimal(getParameters().get(0).getParameter());
            }
            catch(NumberFormatException e)
            {
                return new MessageResponse("Enter a Valid Number");
            }
        }
    
        if(amountToBuy.signum() <= -1)
        {
            return new MessageResponse("You Cannot Steal from the Pot");
        }
    
        BigDecimal lotteryTicketCost = new BigDecimal(String.valueOf(1000));
        BigDecimal amountToSpend = lotteryTicketCost.multiply(amountToBuy);
    
        if(amountToSpend.subtract(lotteryTicketCost).signum() <= -1)
        {
            return new MessageResponse("You cannot buy Partial Tickets");
        }
    
        try
        {
            UserAccount accountToUpdate = UserAccount.getUser(event.getMember());
        
            if(accountToUpdate.getBalance().subtract(amountToSpend).signum() <= -1)
            {
                return new MessageResponse("You Don't have Enough to buy That Many Tickets. Tickets Cost " +
                                           lotteryTicketCost);
            }
    
            accountToUpdate.setBalance(accountToUpdate.getBalance().subtract(amountToSpend));
    
            SQLDatabaseObjectUtil.putObject(SetupDatabaseConnection.mogoloidDatabase,
                                            accountToUpdate,
                                            accountToUpdate.getId());
    
    
            LotteryTask.addToPot(amountToSpend);
            LotteryTask.addTickets(accountToUpdate, amountToBuy);
    
    
            return new MessageResponse("You've been added to the Pot -> New Balance: " + accountToUpdate.getBalance());
        }
        catch(SQLException e)
        {
            e.printStackTrace();
            return new MessageResponse("Error Accessing the Database");
        }
        catch(UserQueryException e)
        {
            e.printStackTrace();
            return new MessageResponse("Account Error");
        }
        
        
    }
    
    @Override
    public String getHelpMessage()
    {
        return "Allows you to Buy a Classic Mongoloid Lottery Ticket.\nUsage:\n~BuyLotteryTicket";
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
