package com.jumbodinosaurs.mongoloidbot.commands.discord.coin;


import com.jumbodinosaurs.devlib.commands.CommandWithParameters;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.coin.UserAccount;
import com.jumbodinosaurs.mongoloidbot.coin.exceptions.UserQueryException;
import com.jumbodinosaurs.mongoloidbot.coin.tasks.LotteryTask;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.RateLimitedException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ChangeNickName extends CommandWithParameters implements IDiscordChatEventable
{
    
    private static final BigDecimal nickNameChangeCost = new BigDecimal("5000");
    private GuildMessageReceivedEvent event;
    
    @Override
    public MessageResponse getExecutedMessage()
            throws WaveringParametersException
    {
        /*
         * Process for setting nick Names
         * 1. Check their Money
         * 2. Sanitize Nick Name input
         * 3. Change @Member's NickName
         * 4. Remove money for their account
         * 5. Add Money to Pot
         *
         *
         * */
        
        
        try
        {
            UserAccount accountToUpdate = UserAccount.getUser(event.getMember());
            //1. Check their Money
            if(accountToUpdate.getBalance().subtract(nickNameChangeCost).signum() <= -1)
            {
                return new MessageResponse("You Don't have Enough to change their Nick Name it costs " +
                                           nickNameChangeCost);
            }
    
            //2. Sanitize Nick Name input
            //Length
    
            if(getParameters() == null || getParameters().size() < 1)
            {
                throw new WaveringParametersException("You didn't Give enough Information");
            }
            String nickNameToSet = "";
            for(int i = 0; i < getParameters().size() - 1; i++)
            {
                nickNameToSet += getParameters().get(i).getParameter() + " ";
            }
    
    
            if(nickNameToSet.length() > 32)
            {
                return new MessageResponse("The Nick Name you Entered was to long");
            }
    
            if(nickNameToSet.length() <= 0)
            {
                return new MessageResponse("The Nick Name you Entered was to short");
            }
            
            
            //3. Change @Member's NickName
            
            List<Member> mentionedMembers = event.getMessage().getMentionedMembers();
            if(mentionedMembers.size() <= 0)
            {
                return new MessageResponse("You Didn't Tell me Who To Change");
            }
            Member memberToBePaid = mentionedMembers.get(0);
            
            try
            {
                setMemberNickName(memberToBePaid, nickNameToSet);
            }
            catch (HierarchyException e)
            {
                return new MessageResponse("Sorry You cannot Change their Name");
            }
            catch (RateLimitedException e)
            {
                return new MessageResponse("Ya'll changing peoples names to Fast Slow down");
            }


            //4. Remove money for their account
            accountToUpdate.setBalance(accountToUpdate.getBalance().subtract(nickNameChangeCost));


            UserAccount.updateUser(accountToUpdate);


            //5. Add Money to Pot
            LotteryTask.addToPot(nickNameChangeCost);


            return new MessageResponse("I changed their Nick Name");
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
    
    
    public void setMemberNickName(Member member, String nickName)
            throws HierarchyException, RateLimitedException
    {
        
        member.modifyNickname(nickName).complete(true);
        
    }
    
    @Override
    public String getHelpMessage()
    {
        return "Allows you to Change the Name of another Member\nCost: " +
               nickNameChangeCost +
               "Usage: ~" +
               getClass().getSimpleName() +
               " funnyName @Mac_tnoghit";
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
