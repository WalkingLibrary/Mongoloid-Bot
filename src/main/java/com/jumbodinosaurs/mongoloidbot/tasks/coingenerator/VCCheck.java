package com.jumbodinosaurs.mongoloidbot.tasks.coingenerator;

import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.devlib.task.ScheduledTask;
import com.jumbodinosaurs.mongoloidbot.Main;
import com.jumbodinosaurs.mongoloidbot.commands.discord.captain.CaptainNow;
import com.jumbodinosaurs.mongoloidbot.commands.discord.captain.DeputyNow;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.battle.BattleTask;
import com.jumbodinosaurs.mongoloidbot.tasks.exceptions.UserQueryException;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.SetupDatabaseConnection;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class VCCheck extends ScheduledTask
{
    public VCCheck(ScheduledThreadPoolExecutor executor)
    {
        super(executor);
    }
    
    @Override
    public void run()
    {
        for(VoiceChannel voiceChannel : Main.jdaController.getJda()
                                                          .getGuildById(Main.GUILD_ID)
                                                          .getVoiceChannels())
        {
            for(Member member : voiceChannel.getMembers())
            {
                try
                {
                    UserAccount account = UserAccount.getUser(member);
                    String amountToAdd = "1000";
                    BigDecimal amountToAddNum = new BigDecimal(amountToAdd);

                    if(member.getRoles().contains(Main.jdaController.getJda().getRoleById(BattleTask.kingKhanRole)))
                    {
                        amountToAddNum = amountToAddNum.add(new BigDecimal("5000"));
                    }

                    if(member.getRoles().contains(Main.jdaController.getJda().getRoleById(CaptainNow.captainID)))
                    {
                        amountToAddNum = amountToAddNum.add(new BigDecimal("2500"));
                    }

                    if(member.getRoles().contains(Main.jdaController.getJda().getRoleById(DeputyNow.deputyID)))
                    {

                        amountToAddNum = amountToAddNum.add(new BigDecimal("2000"));
                    }

                    account.setBalance(account.getBalance().add(amountToAddNum));
                    SQLDatabaseObjectUtil.putObject(SetupDatabaseConnection.mogoloidDatabase, account, account.getId());
                }
                catch(SQLException | UserQueryException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @Override
    public TimeUnit getTimeUnit()
    {
        return TimeUnit.MINUTES;
    }
    
    @Override
    public int getPeriod()
    {
        return 5;
    }
    
    
}
