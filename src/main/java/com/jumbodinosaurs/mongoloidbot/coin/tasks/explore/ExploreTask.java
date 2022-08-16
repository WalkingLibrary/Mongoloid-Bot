package com.jumbodinosaurs.mongoloidbot.coin.tasks.explore;

import com.jumbodinosaurs.devlib.task.ScheduledTask;
import com.jumbodinosaurs.mongoloidbot.Main;
import com.jumbodinosaurs.mongoloidbot.coin.UserAccount;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.Inventory;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.CurrentTask;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Item;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Player;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.util.ItemUntil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExploreTask extends ScheduledTask
{
    public ExploreTask(ScheduledThreadPoolExecutor executor)
    {
        super(executor);
    }

    @Override
    public TimeUnit getTimeUnit()
    {
        return TimeUnit.MINUTES;
    }

    @Override
    public int getPeriod()
    {
        return 1;
    }

    @Override
    public void run()
    {
        try
        {
            //Go Though all the Server Members
            JDA localController = Main.jdaController.getJda();
            Guild localGuild = localController.getGuildById(Main.GUILD_ID);
            for (Member guildMember : localGuild.getMemberCache().asList())
            {

                UserAccount account = UserAccount.getUser(guildMember);
                Player currentAccountsPlayer = account.getPlayer(guildMember);

                if (currentAccountsPlayer.getCurrentTask() == null)
                {
                    continue;
                }

                boolean isExploring = currentAccountsPlayer.getCurrentTask()
                        .getTaskName()
                        .equals(CurrentTask.TaskType.EXPLORING);
                if (!isExploring)
                {
                    continue;
                }


                //Roll to see if they get an Item
                int rolledNumber = (int) (Math.random() * 100);
                if (rolledNumber > 10)
                {
                    continue;
                }

                Item randomItem = ItemUntil.generateRandomItem();
                com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Inventory playersCurrentInventory = currentAccountsPlayer.getInventory();
                System.out.println("Explorer Found Item");

                if (playersCurrentInventory.getItems().size() >= Inventory.maxInventoryAmount)
                {
                    currentAccountsPlayer.setPendingItem(randomItem);
                    currentAccountsPlayer.setCurrentTask(null);
                    UserAccount.updatePlayer(currentAccountsPlayer);
                    continue;
                }

                playersCurrentInventory.getItems().put(playersCurrentInventory.getItems().size() + 1, randomItem);
                currentAccountsPlayer.setCurrentTask(null);
                UserAccount.updatePlayer(currentAccountsPlayer);
            }
            System.out.println("Done Running Exploring");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
