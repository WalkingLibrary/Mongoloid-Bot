package com.jumbodinosaurs.mongoloidbot.commands.discord.captain;

import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.AppSettingsManager;
import com.jumbodinosaurs.mongoloidbot.Main;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.models.CaptainCandidate;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.exceptions.UserQueryException;
import com.jumbodinosaurs.mongoloidbot.tasks.lottery.LotteryTask;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;

public class CaptainNow extends Command implements IDiscordChatEventable
{

    private GuildMessageReceivedEvent event;
    public static final String captainID = AppSettingsManager.getStringValue("captainID");

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
         * 1. Check if war is active
         * 2. Get their user and candidate records
         * 3. If already captain, block
         * 4. Check funds
         * 5. Remove Captain role/flag from everyone else
         * 6. Add role and set them as captain
         * 7. Deduct funds and add to pot
         * 8. Return confirmation message
         * */

        if (TakeShip.isPirateWarActive)
        {
            return new MessageResponse("💥 Cannons firing! War’s on deck! No captain promotions while we’re dodging cannonballs! 🏴‍☠️");
        }

        try
        {
            Member member = event.getMember();
            UserAccount accountToUpdate = UserAccount.getUser(member);
            CaptainCandidate thisCandidate = accountToUpdate.getCaptainCandidate(member);

            // 1. Check if this user is already the captain
            if (thisCandidate.isCaptain())
            {
                return new MessageResponse("🏴‍☠️ You are already the Captain of the mighty ship! No need to claim the helm again.");
            }

            // 2. Check their Money
            BigDecimal costOfCaptainRank = new BigDecimal("1000000");
            if (accountToUpdate.getBalance().subtract(costOfCaptainRank).signum() <= -1)
            {
                return new MessageResponse("💰 You don't have enough gold to claim the Captain's title!");
            }

            // 3. Remove Rank and Captain Flag From everyone else
            Role captainRole = event.getGuild().getRoleById(captainID);
            ArrayList<CaptainCandidate> allCandidates = CaptainCandidate.getAllCaptainCandidates();
            for (CaptainCandidate candidate : allCandidates)
            {
                if (candidate.isCaptain())
                {
                    candidate.setCaptain(false);
                    UserAccount.updateCaptainCandidate(candidate);
                }
            }

            //Build Output Message
            StringBuilder message = new StringBuilder();
            message.append(
                    "https://cdn.discordapp.com/attachments/946297978707136562/957794918330413126/iamdacaptainnow.mp4?ex=6745bbc7&is=67446a47&hm=6acff501c1808f4fcb4cf9774b2724fc9df5ef2b8541ebf965320239eb5654d7&\n");

            for (Member guildMember : Main.jdaController.getJda().getGuildById(Main.GUILD_ID).getMembers())
            {
                if (guildMember.getRoles().contains(captainRole))
                {
                    event.getGuild().removeRoleFromMember(guildMember, captainRole).complete();

                    message.append(guildMember.getAsMention() + "\n");
                }
            }

            // 4. Add Captain role to this user
            event.getGuild().addRoleToMember(member, captainRole).complete();

            // 5. Update database: mark this candidate as captain
            thisCandidate.setCaptain(true);
            thisCandidate.setActiveCampaign(true);
            UserAccount.updateCaptainCandidate(thisCandidate);

            // 6. Remove money for their account
            accountToUpdate.setBalance(accountToUpdate.getBalance().subtract(costOfCaptainRank));
            UserAccount.updateUser(accountToUpdate);

            // 7. Add portion of the money to the lottery pot
            LotteryTask.addToPot(costOfCaptainRank.divide(new BigDecimal("4")));

            return new MessageResponse(message.toString());

        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return new MessageResponse("❌ Error accessing the database while updating captain status.");
        }
        catch (UserQueryException e)
        {
            e.printStackTrace();
            return new MessageResponse("⚠️ Error retrieving account information.");
        }
    }

    @Override
    public String getHelpMessage()
    {
        return "Claims the Captain title for a hefty price, if the seas are calm.";
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
