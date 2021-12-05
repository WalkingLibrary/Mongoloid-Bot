package com.jumbodinosaurs.mongoloidbot.commands.discord.coin;

import com.jumbodinosaurs.devlib.commands.CommandWithParameters;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.mongoloidbot.coin.UserAccount;
import com.jumbodinosaurs.mongoloidbot.coin.exceptions.UserQueryException;
import com.jumbodinosaurs.mongoloidbot.coin.tasks.Lottery;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.SetupDatabaseConnection;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.math.BigDecimal;
import java.sql.SQLException;

public class BuyLotteryTicket extends CommandWithParameters implements IDiscordChatEventable
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
        
        BigDecimal lotteryTicketCost = new BigDecimal(String.valueOf(1000));
        BigDecimal amountToSpend = lotteryTicketCost.multiply(amountToBuy);
        try
        {
            UserAccount accountToUpdate = UserAccount.getUser(event.getMember());
            
            if(accountToUpdate.getBalance().subtract(lotteryTicketCost).signum() <= -1)
            {
                return new MessageResponse("You Don't have Enough to buy a Ticket. Tickets Cost " + lotteryTicketCost);
            }
            
            accountToUpdate.setBalance(accountToUpdate.getBalance().subtract(amountToSpend));
            
            SQLDatabaseObjectUtil.putObject(SetupDatabaseConnection.mogoloidDatabase,
                                            accountToUpdate,
                                            accountToUpdate.getId());
            
            
            Lottery.pot = Lottery.pot.add(amountToSpend);
            Lottery.addTickets(accountToUpdate, amountToBuy);
            
            
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
