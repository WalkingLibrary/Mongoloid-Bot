package com.jumbodinosaurs.mongoloidbot.tasks.battle;

import com.google.gson.Gson;
import com.jumbodinosaurs.devlib.database.objectHolder.NoLimit;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDataBaseObjectHolder;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.devlib.task.ScheduledTask;
import com.jumbodinosaurs.mongoloidbot.Main;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Ability;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.CurrentTask;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Item;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Player;
import com.jumbodinosaurs.mongoloidbot.eventHandlers.EventListener;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.SetupDatabaseConnection;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BattleTask extends ScheduledTask
{
    public static String KING_KHAN_ID = "";
    public static String kingKhanRole = "1007490325834633287";


    public BattleTask(ScheduledThreadPoolExecutor executor)
    {
        super(executor);
    }

    public static Player battlePlayers(Player player1, Player player2)
    {
        Item weaponHands = new Item("Hands", new Ability(Ability.AbilityType.TAKE_HEALTH, 20));
        ArrayList<Item> player1Weapons = player1.getWeapons();
        if (player1Weapons.isEmpty())
        {
            player1Weapons.add(weaponHands);
        }

        ArrayList<Item> player2Weapons = player2.getWeapons();
        if (player2Weapons.isEmpty())
        {
            player1Weapons.add(weaponHands);
        }

        ArrayList<Item> player1Armor = player1.getArmor();
        ArrayList<Item> player2Armor = player2.getArmor();

        if (player1.getStamina() <= 0)
        {
            player1.setStamina(100);
        }
        if (player2.getStamina() <= 0)
        {
            player2.setStamina(100);
        }
        Item randomAttackerWeapon = weaponHands;
        Item randomKhanWeapon = weaponHands;
        while (player1.getHealth() > 0 && player2.getHealth() > 0)
        {
            //Battle Loop!!
            /*
             * Battle Loop Process
             * 1. Apply an Attack from random weapon from attacker
             * 2. Apply an Attack from random weapon from king khan
             *  */

            //1. Apply an Attack from random weapon from attacker

            randomAttackerWeapon = player1Weapons.get((int) (player1Weapons.size() * Math.random()));
            int attackerDamage = randomAttackerWeapon.getAbility().getIntensity();

            for (Item khanArmorItem : player2Armor)
            {
                attackerDamage -= khanArmorItem.getAbility().getIntensity();
                if (khanArmorItem.getAbility().getIntensity() > 0)
                {
                    khanArmorItem.getAbility().setIntensity(khanArmorItem.getAbility().getIntensity() - 1);
                }
                if (attackerDamage < 0)
                {
                    attackerDamage = 0;
                    break;
                }

            }

            if (player2.getHealth() - attackerDamage <= 0)
            {
                return player1;
            }


            //  2. Apply an Attack from random weapon from king khan
            randomKhanWeapon = player1Weapons.get((int) (player1Weapons.size() * Math.random()));
            int khanDamage = randomKhanWeapon.getAbility().getIntensity();

            for (Item attackerArmorItem : player1Armor)
            {
                khanDamage -= attackerArmorItem.getAbility().getIntensity();
                if (attackerArmorItem.getAbility().getIntensity() > 0)
                {
                    attackerArmorItem.getAbility().setIntensity(attackerArmorItem.getAbility().getIntensity() - 1);
                }
                if (khanDamage < 0)
                {
                    khanDamage = 0;
                    break;
                }

            }

            if (player1.getHealth() - khanDamage <= 0)
            {
                return player2;
            }
        }
        return player2;
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
             * Process For Battling
             * 1. Get All Players in the Database
             * 2. Check if they are battling
             * 3. Check if they are already Khan
             * 4. Check if no khan
             * 5. Parse Khan Player
             * 6. Run battle of them vs Khan
             * 7. update Players
             */
            System.out.println("Starting Battle!");
            Guild localGuild = Main.jdaController.getJda().getGuildById(Main.GUILD_ID);
            Role kingKhanRole = Main.jdaController.getJda().getRoleById(BattleTask.kingKhanRole);
            //1. Get All Players in the Database
            // - Parse Player
            for (SQLDataBaseObjectHolder objectHolder : SQLDatabaseObjectUtil.loadObjects(
                    SetupDatabaseConnection.mogoloidDatabase, Player.class,
                    new NoLimit()))
            {
                //// - Parse Player and Khan Player
                Player attackingPlayer = new Gson().fromJson(objectHolder.getJsonObject(), Player.class);
                attackingPlayer.setId(objectHolder.getId());
                if (attackingPlayer.getCurrentTask() == null)
                {
                    continue;
                }

                //2. Check if they are battling
                boolean isBattling = attackingPlayer.getCurrentTask()
                        .getTaskName()
                        .equals(CurrentTask.TaskType.BATTLING);
                if (!isBattling)
                {
                    continue;
                }

                //3. Check if they are already Khan
                if (attackingPlayer.getUserAccountId().equals(KING_KHAN_ID))
                {
                    attackingPlayer.setCurrentTask(null);
                    UserAccount.updatePlayer(attackingPlayer);
                    continue;
                }
                //4. Check if no khan
                Member attackingMember = UserAccount.getMemberFromAccountId(attackingPlayer.getUserAccountId());
                if (KING_KHAN_ID.equals(""))
                {
                    KING_KHAN_ID = attackingPlayer.getUserAccountId();
                    localGuild.addRoleToMember(attackingMember, kingKhanRole).complete();
                    continue;
                }


                //5. Parse Khan Player
                Player khanPlayer = UserAccount.getPlayer(KING_KHAN_ID);
                System.out.println(
                        "King Khan: " + khanPlayer.getUserAccountId() + " vs " + attackingPlayer.getUserAccountId());


                //6. Run battle of them vs Khan
                Player winningPlayer = battlePlayers(attackingPlayer, khanPlayer);


                //7. update Players
                khanPlayer.setCurrentTask(null);
                attackingPlayer.setCurrentTask(null);
                UserAccount.updatePlayer(khanPlayer);
                UserAccount.updatePlayer(attackingPlayer);

                Member kingKhan = UserAccount.getMemberFromAccountId(khanPlayer.getUserAccountId());


                if (winningPlayer.getUserAccountId().equals(KING_KHAN_ID))
                {
                    EventListener.sendMessage(
                            kingKhan.getEffectiveName() + " has defended their Position against " + attackingMember.getEffectiveName(),
                            Main.BATTLE_STEPPE_ID);
                    return;
                }

                KING_KHAN_ID = winningPlayer.getUserAccountId();
                localGuild.removeRoleFromMember(kingKhan, kingKhanRole).complete();
                localGuild.addRoleToMember(attackingMember, kingKhanRole).complete();
                EventListener.sendMessage(
                        attackingMember.getEffectiveName() + " has taken their Place as King Khan from " + kingKhan.getEffectiveName(),
                        Main.BATTLE_STEPPE_ID);


            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("Done Battling");

    }
}
