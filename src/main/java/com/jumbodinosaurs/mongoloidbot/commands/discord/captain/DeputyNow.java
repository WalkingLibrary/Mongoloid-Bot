package com.jumbodinosaurs.mongoloidbot.commands.discord.captain;

import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.Main;
import com.jumbodinosaurs.mongoloidbot.coin.UserAccount;
import com.jumbodinosaurs.mongoloidbot.coin.exceptions.UserQueryException;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.sql.SQLException;

public class DeputyNow extends Command implements IDiscordChatEventable
{

    public static final String deputyID = "866729742827651073";
    private GuildMessageReceivedEvent event;

    @Override
    public MessageResponse getExecutedMessage() throws WaveringParametersException
    {
        /*
         * Process for setting Captain
         * 1. Check their Rank
         * 2. Get User
         * 3. Check to make sure they are not the Captain
         * 4. Remove Rank From everyone else
         * 5. Add Rank to them
         *
         *
         * */


        try
        {
            UserAccount messager = UserAccount.getUser(event.getMember());

            //1. Check their Rank
            Role captainRole = event.getGuild().getRoleById(CaptainNow.captainID);

            if (!messager.getMember().getRoles().contains(captainRole))
            {
                return new MessageResponse("You Are Not The Captain");
            }

            //2. Get User
            if (event.getMessage().getMentionedMembers().size() <= 0)
            {
                return new MessageResponse("You Didn't Tell me who to make the Deputy");
            }

            Member deputy = event.getMessage().getMentionedMembers().get(0);

            //3. Check to make sure they are not the Captain

            if (deputy.getRoles().contains(captainRole))
            {
                return new MessageResponse("You Are Already the Captain...");
            }

            //4. Remove Rank From everyone else

            Role deputyCaptainRole = event.getGuild().getRoleById(deputyID);
            for (Member member : Main.jdaController.getJda().getGuildById(Main.GUILD_ID).getMembers())
            {
                if (member.getRoles().contains(deputyCaptainRole))
                {
                    event.getGuild().removeRoleFromMember(member, deputyCaptainRole).complete();

                }
            }


            //5. Add Rank to them
            event.getGuild().addRoleToMember(deputy, deputyCaptainRole).complete();

            return new MessageResponse(deputy.getEffectiveName() + " is the Deputy Now");
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
        return "Usage: ~DeputyNow @Toad Rat\nLets you Select the Deputy Captain of the Ship";
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
