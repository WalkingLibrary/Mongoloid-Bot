package com.jumbodinosaurs.mongoloidbot.commands.discord.captain;

import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.Main;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.exceptions.UserQueryException;
import com.jumbodinosaurs.mongoloidbot.tasks.lottery.LotteryTask;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.math.BigDecimal;
import java.sql.SQLException;

public class CaptainNow extends Command implements IDiscordChatEventable
{

    private GuildMessageReceivedEvent event;
    public static final String captainID = "820504481153286145";

    @Override
    public String getCategory()
    {
        return "Captain";
    }

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
            if (accountToUpdate.getBalance().subtract(costOfCaptainRank).signum() <= -1)
            {
                return new MessageResponse("You Don't have Enough to be the Captain of the Might Ship");
            }

            //2. Remove Rank From everyone else
            Role captainRole = event.getGuild().getRoleById(captainID);
            StringBuilder message = new StringBuilder();
            message.append(
                    "https://cdn.discordapp.com/attachments/946297978707136562/957794918330413126/iamdacaptainnow.mp4?ex=6745bbc7&is=67446a47&hm=6acff501c1808f4fcb4cf9774b2724fc9df5ef2b8541ebf965320239eb5654d7&\n");

            for (Member member : Main.jdaController.getJda().getGuildById(Main.GUILD_ID).getMembers())
            {
                if (member.getRoles().contains(captainRole))
                {
                    event.getGuild().removeRoleFromMember(member, captainRole).complete();
                    message.append(member.getAsMention() + "\n");
                }
            }

            //3. Add Rank to them
            event.getGuild().addRoleToMember(event.getMember(), captainRole).complete();
            
            //4. Remove money for their account
            accountToUpdate.setBalance(accountToUpdate.getBalance().subtract(costOfCaptainRank));
            
            UserAccount.updateUser(accountToUpdate);
            
            
            //5. Add Money to Pot
            LotteryTask.addToPot(costOfCaptainRank.divide(new BigDecimal("4")));


            return new MessageResponse(message.toString());
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
