package com.jumbodinosaurs.mongoloidbot.tasks.battle;

import com.google.gson.Gson;
import com.jumbodinosaurs.devlib.database.objectHolder.NoLimit;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDataBaseObjectHolder;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.devlib.log.LogManager;
import com.jumbodinosaurs.devlib.task.ScheduledTask;
import com.jumbodinosaurs.devlib.util.GeneralUtil;
import com.jumbodinosaurs.mongoloidbot.Main;
import com.jumbodinosaurs.mongoloidbot.brains.BrainsController;
import com.jumbodinosaurs.mongoloidbot.brains.IResponseUser;
import com.jumbodinosaurs.mongoloidbot.brains.ImageFetcher;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.*;
import com.jumbodinosaurs.mongoloidbot.eventHandlers.EventListener;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.SetupDatabaseConnection;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
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


    public static Player getBattleWinner(Player player1, Player player2, StringBuilder fullReport)
    {
        Item weaponHands = new Item("Hands", new Ability(Ability.AbilityType.TAKE_HEALTH, 20));
        player1.setStamina(100);
        player2.setStamina(100);

        fullReport.append(player1.getMember().getEffectiveName() + "(" + player1.getPromptName() + ")\n");
        fullReport.append(player1.toStringBattleReport());
        fullReport.append("\n vs ");
        fullReport.append(player2.getMember().getEffectiveName() + "(" + player2.getPromptName() + ")\n");
        fullReport.append(player2.toStringBattleReport());
        fullReport.append("\n");

        boolean player1AttackFirst = new Random().nextBoolean();
        Player attacker, defender;
        attacker = player2;
        defender = player1;

        if (player1AttackFirst)
        {
            attacker = player1;
            defender = player2;
        }
        fullReport.append(attacker.getPromptName() + " Gets First attack!\n");
        // Continue the battle as long as both players have health above zero
        while (player1.getHealth() > 0 && player2.getHealth() > 0)
        {
            applyHealing(defender, fullReport);
            fullReport.append(attacker.getPromptName() + " Attacks " + defender.getPromptName() + "\n");
            if (!applyBattleTurn(attacker, defender, weaponHands, fullReport))
            {
                fullReport.append(attacker.getPromptName() + " SLAYS " + defender.getPromptName() + "\n");
                return attacker;
            }
            // Swap attacker and defender for the next turn
            Player temp = attacker;
            attacker = defender;
            defender = temp;
        }

        // Final health check in case both reach zero simultaneously
        if (player1.getHealth() > player2.getHealth())
        {
            fullReport.append(player2.getPromptName() + " SLAYS " + player1.getPromptName() + "\n");
            return player1;
        }

        fullReport.append(player1.getPromptName() + " SLAYS " + player2.getPromptName() + "\n");
        return player2;

    }


    public static void appendLastTenLines(StringBuilder source, StringBuilder destination)
    {
        String content = source.toString();
        if (content.isEmpty())
        {
            return;
        }

        String[] lines = content.split("\n");  // Split the content into lines

        // Calculate the starting index of the last 10 lines
        int start = Math.max(0, lines.length - 10);

        // Append the last 10 lines (or fewer, if not enough lines exist)
        for (int i = start; i < lines.length; i++)
        {
            destination.append(lines[i]).append("\n");
        }
    }


    private static boolean applyBattleTurn(Player attacker, Player defender, Item defaultWeapon, StringBuilder reportBuilder)
    {
        // Apply any stamina boosts before attacking
        applyStaminaBoosts(attacker, reportBuilder);

        // Select weapon from attacker's inventory or use default if none
        Item weapon = defaultWeapon;
        if (!attacker.getWeapons().isEmpty())
        {
            weapon = attacker.getWeapons().get((int) (attacker.getWeapons().size() * Math.random()));
        }
        int damage = weapon.getAbility().getIntensity();

        // Apply stamina factor in damage calculation
        double staminaModifier = 1 + (attacker.getStamina() * 0.005);
        damage = (int) (damage * staminaModifier);

        int totalArmorEffect = 0;
        int totalArmorBreakingEffect = 0;

        // Calculate total armor effect
        for (Item armorItem : defender.getArmor())
        {
            totalArmorEffect += armorItem.getAbility().getIntensity();
        }

        // Check for armor-breaking items and calculate total effect
        for (Item armorBreaking : attacker.getArmorBreak())
        {
            totalArmorBreakingEffect += armorBreaking.getAbility().getIntensity();
        }

        // Reporting total armor breaking effects
        if (totalArmorBreakingEffect > 0)
        {
            reportBuilder.append(
                    attacker.getPromptName() + " utilizes armor breaking effects totaling " + totalArmorBreakingEffect + " intensity\n");
        }

        // Calculate effective damage after considering armor
        damage -= totalArmorEffect;
        reportBuilder.append(
                defender.getPromptName() + " defends with total armor reducing damage by " + totalArmorEffect + "\n");

        // Adjust damage to ensure it does not go below 1
        if (damage < 1)
        {
            damage = 1;
        }

        reportBuilder.append(
                attacker.getPromptName() + " attacks " + defender.getPromptName() + " with " + weapon.getName() + " for an amount of " + damage + " damage\n");

        // Adjust the attacker's stamina based on the difference between armor breaking and armor effectiveness
        int staminaAdjustment = totalArmorBreakingEffect - totalArmorEffect;
        attacker.setStamina(
                Math.max(0, attacker.getStamina() + staminaAdjustment));  // Ensure stamina doesn't go negative

        reportBuilder.append(
                "Stamina adjustment for " + attacker.getPromptName() + " due to armor breaking is " + totalArmorBreakingEffect + "\n");

        // Apply the calculated damage to the defender's health
        defender.setHealth(defender.getHealth() - damage);

        // Check if the defender is still alive
        return defender.getHealth() > 0;
    }


    public static void updatePlayerAfterBattle(Player player) throws SQLException
    {
        player.setCurrentTask(null);
        if (player.getHealth() <= 0)
        {
            player.setHealth(5000);
        }
        UserAccount.updatePlayer(player);
    }

    /**
     * Applies healing using a player's inventory items.
     *
     * @param player The player who will use the healing item.
     */
    private static void applyHealing(Player player, StringBuilder reportBuilder)
    {
        PlayerInventory inventory = player.getInventory();  // Retrieve the player's inventory
        HashMap<Integer, Item> items = inventory.getItems();  // Get the map of items
        boolean inventoryChanged = false;  // Flag to check if the inventory needs updating

        // Create a list to hold keys of items to be removed to avoid ConcurrentModificationException
        ArrayList<Integer> keysToRemove = new ArrayList<>();

        int healthMultiplier = 20;
        // Iterate over the entries of the map
        for (Map.Entry<Integer, Item> entry : items.entrySet())
        {
            Item item = entry.getValue();
            // Check if the item is a healing item and has intensity greater than 0
            if (item.getAbility().getType() == Ability.AbilityType.GIVE_HEALTH && item.getAbility().getIntensity() > 0)
            {
                // Heal the player by the intensity of the item
                player.setHealth(player.getHealth() + (item.getAbility().getIntensity() * healthMultiplier));

                // Decrease the intensity of the item to simulate its usage
                item.getAbility().setIntensity(item.getAbility().getIntensity() - 1);
                reportBuilder.append(player.getPromptName() + " Heals " + item.getAbility()
                        .getIntensity() + " using " + item.getName() + "\n");
                // Check if the item's intensity is now zero or less, add it to remove list
                if (item.getAbility().getIntensity() <= 0)
                {
                    keysToRemove.add(entry.getKey());  // Add the key to the list of keys to remove
                    inventoryChanged = true;  // Indicate the inventory has changed
                }
            }
        }

        // Remove the exhausted healing items from the map
        for (Integer key : keysToRemove)
        {
            items.remove(key);
        }

        // If the inventory was changed, set the items back to the inventory
        if (inventoryChanged)
        {
            inventory.setItems(items);  // Update the player's inventory with the modified items map
            player.setInventory(inventory);  // Assuming a method to update the player's inventory reference
        }
    }

    /**
     * Applies stamina boosts using a player's inventory items.
     *
     * @param player The player who will use the stamina boost item.
     */
    private static void applyStaminaBoosts(Player player, StringBuilder reportBuilder)
    {
        PlayerInventory inventory = player.getInventory();  // Retrieve the player's inventory
        HashMap<Integer, Item> items = inventory.getItems();  // Get the map of items
        boolean inventoryChanged = false;  // Flag to check if the inventory needs updating

        // Create a list to hold keys of items to be removed to avoid ConcurrentModificationException
        ArrayList<Integer> keysToRemove = new ArrayList<>();

        // Iterate over the entries of the map
        for (Map.Entry<Integer, Item> entry : items.entrySet())
        {
            Item item = entry.getValue();
            // Check if the item is a stamina boost item and has intensity greater than 0
            if (item.getAbility().getType() == Ability.AbilityType.GIVE_STAMINA && item.getAbility().getIntensity() > 0)
            {
                // Increase the player's stamina by the intensity of the item
                player.setStamina(player.getStamina() + item.getAbility().getIntensity());

                Random random = new Random();
                // Decrease the intensity of the item to simulate its usage
                int reductionAmount = random.nextInt(75) + 1;  //
                item.getAbility().setIntensity(item.getAbility().getIntensity() - reductionAmount);
                reportBuilder.append(player.getPromptName() + " adds an Attack Boost of " + item.getAbility()
                        .getIntensity() + " using " + item.getName() + "\n");
                // Check if the item's intensity is now zero or less, add it to remove list
                if (item.getAbility().getIntensity() <= 0)
                {
                    keysToRemove.add(entry.getKey());  // Add the key to the list of keys to remove
                    inventoryChanged = true;  // Indicate the inventory has changed
                }
            }
        }

        // Remove the exhausted stamina items from the map
        for (Integer key : keysToRemove)
        {
            items.remove(key);
        }

        // If the inventory was changed, set the items back to the inventory
        if (inventoryChanged)
        {
            inventory.setItems(items);  // Update the player's inventory with the modified items map
            player.setInventory(inventory);  // Assuming a method to update the player's inventory reference
        }
    }

    @Override
    public TimeUnit getTimeUnit()
    {
        return TimeUnit.HOURS;
    }

    @Override
    public int getPeriod()
    {
        return 8;
    }

    @Override
    public void run()
    {
        try
        {
            // Logging the start of the battle process
            LogManager.consoleLogger.info("Starting Battle!");

            // Retrieve the guild by ID from the JDA controller
            Guild localGuild = Main.jdaController.getJda().getGuildById(Main.GUILD_ID);

            // Retrieve the specific role by ID from the JDA controller
            Role kingKhanRole = Main.jdaController.getJda().getRoleById(BattleTask.kingKhanRole);

            // Retrieve all members in the guild with the 'kingKhanRole'
            ArrayList<Member> membersWithKingKhanRoles = (ArrayList<Member>) localGuild.getMembersWithRoles(
                    kingKhanRole);

            // Placeholder for the current king
            Player kingKhan = null;

            // List to keep track of members challenging the king
            ArrayList<Player> challengingMembers = new ArrayList<Player>();

            // Process members who currently have the 'kingKhanRole'
            if (!membersWithKingKhanRoles.isEmpty())
            {
                // Get the player object of the first member with king role
                kingKhan = UserAccount.getPlayer(UserAccount.getUniqueIdentifier(membersWithKingKhanRoles.get(0)));

                // If there are more members with the king role, treat them as challengers
                if (membersWithKingKhanRoles.size() > 1)
                {
                    for (Member currentMember : membersWithKingKhanRoles.subList(1, membersWithKingKhanRoles.size()))
                    {
                        challengingMembers.add(UserAccount.getPlayer(UserAccount.getUniqueIdentifier(currentMember)));
                    }
                }
            }

            // Retrieve and process all players involved in battles from the database
            for (SQLDataBaseObjectHolder objectHolder : SQLDatabaseObjectUtil.loadObjects(
                    SetupDatabaseConnection.mogoloidDatabase, Player.class,
                    new NoLimit()))
            {
                Player currentPlayer = new Gson().fromJson(objectHolder.getJsonObject(), Player.class);
                currentPlayer.setId(objectHolder.getId());

                // Check if the player's current task is 'Battling'
                if (currentPlayer.getCurrentTask() == null)
                {
                    continue;
                }
                boolean isBattling = currentPlayer.getCurrentTask().getTaskName().equals(CurrentTask.TaskType.BATTLING);
                if (!isBattling)
                {
                    continue; // Skip non-battling players
                }

                // Add battling players to challengers list if not already included
                if (!challengingMembers.contains(currentPlayer))
                {
                    challengingMembers.add(currentPlayer);
                }
            }

            // If no challengers and no king, exit the method
            if (challengingMembers.size() < 1)
            {
                return;
            }

            // Assign the first challenger as king if no king is found
            if (kingKhan == null)
            {
                kingKhan = challengingMembers.get(0);
            }


            // Determine the winner of each battle and update players accordingly
            StringBuilder promptGen = new StringBuilder();
            promptGen.append("Mend this into a Summary and Exaggerate the Place it's at.");
            StringBuilder warReport = new StringBuilder();
            for (Player challenger : challengingMembers)
            {
                if (kingKhan.getUserAccountId().equals(challenger.getUserAccountId()))
                {
                    continue;
                }

                kingKhan = getBattleWinner(kingKhan, challenger, warReport);
                warReport.append("\n");
                updatePlayerAfterBattle(kingKhan);
                updatePlayerAfterBattle(challenger);
                appendLastTenLines(warReport, promptGen);
            }

            promptGen.append(
                    "Be sure it's only two sentences long, the summary will be used to generate an image. Be sure to add the names of the people mentioned.");
            // Randomly select a theme for this battle

            // Send the POST request and get the requestId
            try
            {

                String response = BrainsController.sendPostRequest(
                        BrainsController.brainsOptions.getEndPoint() + "/api/submit-prompt", promptGen.toString());
                String requestId = BrainsController.parseRequestId(response);
                // Start polling the status of the requestId every 2 seconds
                if (requestId != null)
                {
                    System.out.println("Request ID : " + requestId);
                    BrainsController.pollStatus(requestId, new IResponseUser()
                    {
                        @Override
                        public void UseResponse(String response)
                        {
                            try
                            {
                                // Define a list of themes for battle summaries
                                ArrayList<String> themes = (ArrayList<String>) Arrays.asList("Realistic", "Isometric",
                                        "Painting", "Wireframe", "Surreal", "Sketch", "Cartoon", "Retro", "Futuristic",
                                        "Pixel Art");
                                Random random = new Random();
                                String theme = themes.get(random.nextInt(themes.size()));
                                File battleImage = ImageFetcher.fetchImage("Theme: " + theme + " - " + response);
                                if (battleImage.length() > 8000000)
                                {
                                    LogManager.consoleLogger.error(
                                            "Error Battle Image To Large " + battleImage.length());
                                    return;
                                }
                                // Announce the new king in the guild
                                Main.jdaController.getJda()
                                        .getGuildById(Main.GUILD_ID)
                                        .getTextChannelById(Main.BATTLE_STEPPE_ID)
                                        .sendMessage(response + ": ").addFile(battleImage, battleImage.getName())
                                        .complete();
                            }
                            catch (IOException e)
                            {
                                LogManager.consoleLogger.error("Error Getting Image for BattleTask: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
            catch (Exception e)
            {
                LogManager.consoleLogger.error(e.getMessage());
                e.printStackTrace();
            }


            if (membersWithKingKhanRoles.size() > 0)
            {
                // Remove the king role from all members except the new king
                for (Member member : localGuild.getMembers())
                {


                    if (!kingKhan.equals(
                            UserAccount.getPlayer(UserAccount.getUniqueIdentifier(membersWithKingKhanRoles.get(0)))))
                    {
                        localGuild.removeRoleFromMember(member, kingKhanRole).complete();
                    }
                }
            }

            // Assign the king role to the new king
            Member kingKhanMember = kingKhan.getMember();
            localGuild.addRoleToMember(kingKhanMember, kingKhanRole).complete();

            // Announce the new king in the guild
            EventListener.sendMessage(kingKhanMember.getEffectiveName() + " Is King!", Main.BATTLE_STEPPE_ID);
            File streamToFile = null;
            try
            {
                streamToFile = File.createTempFile("batteReport", ".txt");
                GeneralUtil.writeContents(streamToFile, warReport.toString(), false);
                Main.jdaController.getJda()
                        .getGuildById(Main.GUILD_ID)
                        .getTextChannelById(Main.BATTLE_STEPPE_ID)
                        .sendMessage("Battle Report: ").addFile(streamToFile, streamToFile.getName())
                        .complete();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                LogManager.consoleLogger.error("Error Sending Battle Report: " + e.getMessage());
            }
            streamToFile.deleteOnExit();

        }
        catch (Exception e)
        {
            // Log any exceptions that occur during the battle process
            LogManager.consoleLogger.error("Error - run - Battle Task: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
