package com.jumbodinosaurs.mongoloidbot.tasks.explore;

import com.google.gson.Gson;
import com.jumbodinosaurs.devlib.database.objectHolder.NoLimit;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDataBaseObjectHolder;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.devlib.task.ScheduledTask;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.Inventory;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.CurrentTask;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Item;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Player;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.PlayerInventory;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.util.ItemUntil;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.SetupDatabaseConnection;

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
            //Go Though all the Players in the database
            for (SQLDataBaseObjectHolder objectHolder : SQLDatabaseObjectUtil.loadObjects(
                    SetupDatabaseConnection.mogoloidDatabase, Player.class,
                    new NoLimit()))
            {
                Player currentAccountsPlayer = new Gson().fromJson(objectHolder.getJsonObject(), Player.class);
                currentAccountsPlayer.setId(objectHolder.getId());
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
                int rolledNumber = (int) (Math.random() * 10);
                if (rolledNumber > 10)
                {
                    continue;
                }

                Item randomItem = ItemUntil.generateRandomItem();
                PlayerInventory playersCurrentInventory = currentAccountsPlayer.getInventory();
                System.out.println("Explorer Found Item");

                if (playersCurrentInventory.getItems().size() >= Inventory.maxInventoryAmount)
                {
                    currentAccountsPlayer.setPendingItem(randomItem);
                    currentAccountsPlayer.setCurrentTask(null);
                    UserAccount.updatePlayer(currentAccountsPlayer);
                    continue;
                }


                for (int i = 1; i < Inventory.maxInventoryAmount; i++)
                {
                    if (!playersCurrentInventory.getItems().containsKey(i))
                    {
                        playersCurrentInventory.getItems().put(i, randomItem);
                        currentAccountsPlayer.setCurrentTask(null);
                        UserAccount.updatePlayer(currentAccountsPlayer);
                        break;
                    }
                }
            }
            System.out.println("Done Running Exploring");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
