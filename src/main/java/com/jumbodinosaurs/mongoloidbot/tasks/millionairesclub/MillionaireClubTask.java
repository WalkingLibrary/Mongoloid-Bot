package com.jumbodinosaurs.mongoloidbot.tasks.millionairesclub;

import com.jumbodinosaurs.devlib.task.ScheduledTask;
import com.jumbodinosaurs.mongoloidbot.AppSettingsManager;
import com.jumbodinosaurs.mongoloidbot.Main;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MillionaireClubTask extends ScheduledTask
{
    public static String millionaireClubRankID = AppSettingsManager.getStringValue("millionaireClubRankID");

    public MillionaireClubTask(ScheduledThreadPoolExecutor executor)
    {
        super(executor);
    }

    @Override
    public TimeUnit getTimeUnit()
    {
        return TimeUnit.SECONDS;
    }


    @Override
    public int getPeriod()
    {
        return 10;
    }

    @Override
    public void run()
    {
        /* How the Millionaires Club works
         * If you have more than a million mongoloid coin you get the millionaires rank
         *
         * Process for checking the Coin
         *
         * Go Though all the Server Members
         * check their coin
         * if they have the rank and don't have enough coin remove
         * if they have enough coin and don't have the rank give them the rank
         *
         *  */

        try
        {
            //Go Though all the Server Members
            JDA localController = Main.jdaController.getJda();
            Guild localGuild = localController.getGuildById(Main.GUILD_ID);

            assert localGuild != null;
            for (Member guildMember : localGuild.getMemberCache().asList())
            {

                UserAccount account = UserAccount.getUser(guildMember);

                boolean has1Million = account.getBalance().subtract(new BigDecimal(1000000)).signum() >= 0;
                Role millionairesRole = Main.jdaController.getJda().getRoleById(millionaireClubRankID);
                boolean hasRank = guildMember.getRoles().contains(millionairesRole);
                if (has1Million)
                {
                    localGuild.addRoleToMember(guildMember, millionairesRole).complete();
                }

                if (hasRank && !has1Million)
                {
                    localGuild.removeRoleFromMember(guildMember, millionairesRole).complete();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
