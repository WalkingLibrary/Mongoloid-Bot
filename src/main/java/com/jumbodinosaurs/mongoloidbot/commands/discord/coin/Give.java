package com.jumbodinosaurs.mongoloidbot.commands.discord.coin;

import com.jumbodinosaurs.devlib.commands.CommandWithParameters;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.coin.UserAccount;
import com.jumbodinosaurs.mongoloidbot.coin.exceptions.UserQueryException;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IAdminCommand;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class Give extends CommandWithParameters implements IDiscordChatEventable,
                                                                   IAdminCommand
{
    
    private GuildMessageReceivedEvent event;
    
    @Override
    public MessageResponse getExecutedMessage()
            throws WaveringParametersException
    {
        if(getParameters() == null || getParameters().size() < 1)
        {
            throw new WaveringParametersException("You didn't Give enough Information");
        }
        
        /*
         * Process For Paying a Person
         * Get the Amount To Pay
         * Get the Account To Pay
         * Check the Amount of Money in the Users Account
         * Pay the Money to the Specified Account
         * Remove the Amount paid from the Users Account
         *
         *
         *   */
        
        
        try
        {
            //Get the Amount To Pay
            String amountString = getParameters().get(0).getParameter();
            
            //Validate Number Passed
            
            BigDecimal amountToPay;
            try
            {
                amountToPay = new BigDecimal(amountString);
            }
            catch(NumberFormatException e)
            {
                return new MessageResponse("Amount Told To Pay is Not Valid");
            }
            
            // Check the Amount of Money in the Users Account
            ///Pay the Money to the Specified Account
            List<Member> mentionedMembers = event.getMessage().getMentionedMembers();
            if(mentionedMembers.size() <= 0)
            {
                return new MessageResponse("You Didn't Tell me Who To Pay");
            }
            
            Member memberToBePaid = mentionedMembers.get(0);
            UserAccount userToBePaid;
            try
            {
                userToBePaid = UserAccount.getUser(memberToBePaid);
            }
            catch (UserQueryException e)
            {
                userToBePaid = new UserAccount();
            }

            userToBePaid.setBalance(userToBePaid.getBalance().add(amountToPay));


            UserAccount.updateUser(userToBePaid);


            return new MessageResponse("Paid " +
                    amountToPay +
                    event.getGuild().getEmoteById("916589679518838794").getAsMention() +
                    " to " +
                    memberToBePaid.getUser().getName());
        }
        catch(SQLException e)
        {
            e.printStackTrace();
            return new MessageResponse("Error Checking Database");
        }
        
    }
    
    @Override
    public String getHelpMessage()
    {
        return "Allows you to Pay a Mentioned Member.\nUsage:\n~" + getClass().getSimpleName() + " 1000 @Mac_Tonight";
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
