package com.jumbodinosaurs.mongoloidbot.tasks.explore;

import com.google.gson.Gson;
import com.jumbodinosaurs.devlib.database.objectHolder.NoLimit;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDataBaseObjectHolder;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.devlib.log.LogManager;
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

            /*
             * Process for Running Explore task
             * 1. Get all the Players in the Database
             * 2. Check if they are Exploring
             * 3. Run Chance to Find Item
             * 4. Add Item to their Inventory or Pending Slot
             * */
            //1. Get all the Players in the Database
            // - Parse the Player
            for (SQLDataBaseObjectHolder objectHolder : SQLDatabaseObjectUtil.loadObjects(
                    SetupDatabaseConnection.mogoloidDatabase, Player.class,
                    new NoLimit()))
            {

                //- Parse the Player
                Player currentAccountsPlayer = new Gson().fromJson(objectHolder.getJsonObject(), Player.class);
                currentAccountsPlayer.setId(objectHolder.getId());

                //2. Check if they are Exploring
                //If there task is null continue in the loop of all players
                if (currentAccountsPlayer.getCurrentTask() == null)
                {
                    continue;
                }

                boolean isExploring = currentAccountsPlayer.getCurrentTask()
                        .getTaskName()
                        .equals(CurrentTask.TaskType.EXPLORING);
                //IF their Task is not Exploring Continue
                if (!isExploring)
                {
                    continue;
                }


                //3. Run Chance to Find Item
                //10% chance
                int rolledNumber = (int) (Math.random() * 10);
                if (rolledNumber > 10)
                {
                    continue;
                }
                LogManager.consoleLogger.debug(currentAccountsPlayer.getUserAccountId() + " has Found an Item");

                // 4. Add Item to their Inventory or Pending Slot
                Item randomItem = ItemUntil.generateRandomItem();
                PlayerInventory playersCurrentInventory = currentAccountsPlayer.getInventory();


                // Check their inventory size, if full then place in the pending slot and update the player
                if (playersCurrentInventory.getItems().size() >= Inventory.maxInventoryAmount)
                {
                    currentAccountsPlayer.setPendingItem(randomItem);
                    currentAccountsPlayer.setCurrentTask(null);

                    LogManager.consoleLogger.debug(
                            "Updating Pending Slot: " + new Gson().toJson(currentAccountsPlayer));
                    UserAccount.updatePlayer(currentAccountsPlayer);
                    continue;
                }

                //Since there inventory isn't full go over each slot and find the empty one add it
                // and update the player
                for (int i = 1; i <= Inventory.maxInventoryAmount; i++)
                {
                    Item currentSlotItem = playersCurrentInventory.getItems().get(i);
                    if (currentSlotItem == null)
                    {
                        playersCurrentInventory.getItems().put(i, randomItem);
                        currentAccountsPlayer.setCurrentTask(null);
                        LogManager.consoleLogger.debug(
                                "Updating Slot " + i + ": " + new Gson().toJson(currentAccountsPlayer));
                        UserAccount.updatePlayer(currentAccountsPlayer);
                        break;
                    }
                }
            }
            LogManager.consoleLogger.debug("Done Running Exploring");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
