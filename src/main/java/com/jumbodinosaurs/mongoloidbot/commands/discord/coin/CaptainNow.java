package com.jumbodinosaurs.mongoloidbot.commands.discord.coin;

import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.mongoloidbot.Main;
import com.jumbodinosaurs.mongoloidbot.coin.UserAccount;
import com.jumbodinosaurs.mongoloidbot.coin.exceptions.UserQueryException;
import com.jumbodinosaurs.mongoloidbot.coin.tasks.LotteryTask;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.SetupDatabaseConnection;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.math.BigDecimal;
import java.sql.SQLException;

public class CaptainNow extends Command implements IDiscordChatEventable
{
    
    private GuildMessageReceivedEvent event;
    
    @Override
    public MessageResponse getExecutedMessage()
            throws WaveringParametersException
    {
        
        /*
         * Process for setting Captain
         * 1. Check their Money
         * 2. Remove Rank From everyone else
         * 3. Add Rank to them
         * 4. Remove money for their account
         * 5. Add Money to Pot
         *
         *
         * */
        
        
        try
        {
            UserAccount accountToUpdate = UserAccount.getUser(event.getMember());
            BigDecimal costOfCaptainRank = new BigDecimal("1000000");
            //1. Check their Money
            if(accountToUpdate.getBalance().subtract(costOfCaptainRank).signum() <= -1)
            {
                return new MessageResponse("You Don't have Enough to be the Captain of the Might Ship");
            }
            
            //2. Remove Rank From everyone else
            Role captainRole = event.getGuild().getRoleById("820504481153286145");
            for(Member member : Main.jdaController.getJda().getGuildById("472944533089550397").getMembers())
            {
                if(member.getRoles().contains(captainRole))
                {
                    event.getGuild().removeRoleFromMember(member, captainRole).complete();
                    
                }
            }
            
            //3. Add Rank to them
            event.getGuild().addRoleToMember(event.getMember(), captainRole).complete();
            
            //4. Remove money for their account
            accountToUpdate.setBalance(accountToUpdate.getBalance().subtract(costOfCaptainRank));
            
            SQLDatabaseObjectUtil.putObject(SetupDatabaseConnection.mogoloidDatabase,
                                            accountToUpdate,
                                            accountToUpdate.getId());
            
            
            //5. Add Money to Pot
            LotteryTask.addToPot(costOfCaptainRank);
            
            
            return new MessageResponse("You are the Captain Now");
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
        return "You are the Captain Now";
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
