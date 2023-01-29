package com.jumbodinosaurs.mongoloidbot.tasks.battle;

import com.google.gson.Gson;
import com.jumbodinosaurs.devlib.database.objectHolder.NoLimit;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDataBaseObjectHolder;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.devlib.log.LogManager;
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

import java.sql.SQLException;
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

    public static void updatePlayerAfterBattle(Player player) throws SQLException
    {
        player.setCurrentTask(null);
        if (player.getHealth() <= 0)
        {
            player.setHealth(100);
        }
        UserAccount.updatePlayer(player);
    }

    public static Player getBattleWinner(Player player1, Player player2)
    {
        Item weaponHands = new Item("Hands", new Ability(Ability.AbilityType.TAKE_HEALTH, 20));

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

            randomAttackerWeapon = weaponHands;
            if(player1.getWeapons().size() > 0)
            {
                randomAttackerWeapon = player1.getWeapons().get((int) (player1.getWeapons().size() * Math.random()));
            }
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
                    attackerDamage = 1;
                    break;
                }

            }

            player2.setHealth(player2.getHealth() - attackerDamage);
            if (player2.getHealth() <= 0)
            {
                return player1;
            }


            //  2. Apply an Attack from random weapon from king khan
            randomKhanWeapon = weaponHands;
            if(player2.getWeapons().size() > 0)
            {
                randomKhanWeapon = player2.getWeapons().get((int) (player2.getWeapons().size() * Math.random()));
            }
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
                    khanDamage = 1;
                    break;
                }

            }

            player1.setHealth(player1.getHealth() - khanDamage);

            if (player1.getHealth() <= 0)
            {
                return player2;
            }
        }
        return player2;
    }

    public void runAttack(Player player1, Player player2)
    {
        /*
         * Process for Attacking a Player
         * 1. set Weapon Heads
         * 2. Get Random Weapon if available
         * 3. Run chance to Crit
         * 4. Set damage
         * 5. Run Damage though Armor of Other Player
         * 6. Apply Damage
         * */
        //1. set Weapon Heads
        Item weaponHands = new Item("Hands", new Ability(Ability.AbilityType.TAKE_HEALTH, 20));
        Item player1Weapon = weaponHands;


        //2. Get Random Weapon if available
        if(player1.getWeapons().size() > 0)
        {
            player1Weapon = player1.getWeapons().get((int) (player1.getWeapons().size() * Math.random()));
        }

        //3. Run chance to Crit
        // The Chance to Crit is based of the Stamin of a Player

        //4. Set damage
        int player1Damage = player1Weapon.getAbility().getIntensity();



        //5. Run Damage though Armor of Other Player
        for (Item attackerArmorItem : player2.getArmor())
        {
            player1Damage -= attackerArmorItem.getAbility().getIntensity();

            if (player1Damage < 0)
            {
                player1Damage = 1;
                break;
            }

        }

        //6. Apply Damage
        player2.setHealth(player2.getHealth() - player1Damage);
    }



    @Override
    public void run()
    {

        try
        {
            /*
             * Process For Battling
             * --
             * 1. Get All Current King Khans
             * 2. Run Battles for King Khans
             * --
             * 1. Get All Players in the Database
             * 2. Check if they are battling
             * 3. Check if they are already Khan
             * 4. Check if no khan
             * 5. Parse Khan Player
             * 6. Run battle of them vs Khan
             * 7. update Players
             */


            LogManager.consoleLogger.info("Starting Battle!");
            Guild localGuild = Main.jdaController.getJda().getGuildById(Main.GUILD_ID);
            Role kingKhanRole = Main.jdaController.getJda().getRoleById(BattleTask.kingKhanRole);


            /*
             * 1. Get All Current King Khans
             * 2. Run Battles for King Khans
             * */

            //1. Get All Current King Khans
            ArrayList<Member> membersWithKingKhanRoles = (ArrayList<Member>) localGuild.getMembersWithRoles(
                    kingKhanRole);
            //If there is more than one king khan, MAKE EM BATTLE
            if(membersWithKingKhanRoles.size() > 1)
            {
                Member bestKingKhan = membersWithKingKhanRoles.get(0);

                KING_KHAN_ID = UserAccount.getUniqueIdentifier(bestKingKhan);


                for (int i = 1; i < membersWithKingKhanRoles.size(); i++)
                {


                    Member challengingKhan = membersWithKingKhanRoles.get(i);
                    Player bestKhanPlayer = UserAccount.getPlayer(UserAccount.getUniqueIdentifier(bestKingKhan));
                    Player challengingKhanPlayer = UserAccount.getPlayer(
                            UserAccount.getUniqueIdentifier(challengingKhan));

                    //2. Run Battles for King Khans
                    Player winningKhan = getBattleWinner(challengingKhanPlayer, bestKhanPlayer);


                    //7. update Players
                    updatePlayerAfterBattle(challengingKhanPlayer);
                    updatePlayerAfterBattle(bestKhanPlayer);


                    if (winningKhan.getUserAccountId().equals(KING_KHAN_ID))
                    {
                        EventListener.sendMessage(
                                bestKingKhan.getEffectiveName() + " has defended their Position against " + challengingKhan.getEffectiveName() + ", there can only be one Khan",
                                Main.BATTLE_STEPPE_ID);
                        localGuild.removeRoleFromMember(challengingKhan, kingKhanRole).complete();
                        return;
                    }

                    KING_KHAN_ID = winningKhan.getUserAccountId();
                    localGuild.removeRoleFromMember(bestKingKhan, kingKhanRole).complete();
                    localGuild.addRoleToMember(challengingKhan, kingKhanRole).complete();
                    EventListener.sendMessage(
                            challengingKhan.getEffectiveName() + " has taken their Place as King Khan from " + bestKingKhan.getEffectiveName() + ", there can only be one Khan",
                            Main.BATTLE_STEPPE_ID);
                    bestKingKhan = challengingKhan;
                }
            }

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
                LogManager.consoleLogger.info(
                        "King Khan: " + khanPlayer.getUserAccountId() + " vs " + attackingPlayer.getUserAccountId());


                //6. Run battle of them vs Khan
                Player winningPlayer = getBattleWinner(attackingPlayer, khanPlayer);


                //7. update Players
                updatePlayerAfterBattle(attackingPlayer);
                updatePlayerAfterBattle(khanPlayer);

                Member kingKhan = UserAccount.getMemberFromAccountId(khanPlayer.getUserAccountId());
                LogManager.consoleLogger.info("Done Battling");

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


    }
}
