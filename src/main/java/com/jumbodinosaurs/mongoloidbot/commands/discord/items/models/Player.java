package com.jumbodinosaurs.mongoloidbot.commands.discord.items.models;

import com.google.gson.Gson;
import com.jumbodinosaurs.devlib.database.DataBaseUtil;
import com.jumbodinosaurs.devlib.database.Query;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDataBaseObjectHolder;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.devlib.database.objectHolder.SelectLimiter;
import com.jumbodinosaurs.devlib.log.LogManager;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.util.ItemUntil;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.SetupDatabaseConnection;
import net.dv8tion.jda.api.entities.Member;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;

public class Player
{

    private transient int id;

    private int health;

    private int stamina;

    private Item itemForSale;
    private BigDecimal itemSellPrice;

    private Item pendingItem;

    private PlayerInventory inventory;

    private String userAccountId;

    private CurrentTask currentTask;

    private String promptNameBase64;

    private boolean isNPC;

    private String npcImageLink;

    public Player(String userAccountId)
    {
        this.health = 100;
        this.inventory = new PlayerInventory();
        this.userAccountId = userAccountId;
    }

    public int getHealth()
    {
        return health;
    }

    public void setHealth(int health)
    {
        this.health = health;
    }

    public PlayerInventory getInventory()
    {
        return inventory;
    }

    public void setInventory(PlayerInventory inventory)
    {
        this.inventory = inventory;
    }

    public String getUserAccountId()
    {
        return userAccountId;
    }

    public void setUserAccountId(String userAccountId)
    {
        this.userAccountId = userAccountId;
    }

    public CurrentTask getCurrentTask()
    {
        return currentTask;
    }

    public void setCurrentTask(CurrentTask currentTask)
    {
        this.currentTask = currentTask;
    }

    public int getStamina()
    {
        return stamina;
    }

    public void setStamina(int stamina)
    {
        this.stamina = stamina;
    }

    public Item getPendingItem()
    {
        return pendingItem;
    }

    public void setPendingItem(Item pendingItem)
    {
        this.pendingItem = pendingItem;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public Item getItemForSale()
    {
        return itemForSale;
    }

    public void setItemForSale(Item itemForSale)
    {
        this.itemForSale = itemForSale;
    }

    public BigDecimal getItemSellPrice()
    {
        return itemSellPrice;
    }

    public void setItemSellPrice(BigDecimal itemSellPrice)
    {
        this.itemSellPrice = itemSellPrice;
    }

    public ArrayList<Item> getWeapons()
    {
        return ItemUntil.getItemsOfAbilityType(this, Ability.AbilityType.TAKE_HEALTH);
    }

    public ArrayList<Item> getArmor()
    {
        return ItemUntil.getItemsOfAbilityType(this, Ability.AbilityType.ARMOR);
    }

    public ArrayList<Item> getMedicalItems()
    {
        return ItemUntil.getItemsOfAbilityType(this, Ability.AbilityType.GIVE_HEALTH);
    }

    public ArrayList<Item> getArmorBreak()
    {
        return ItemUntil.getItemsOfAbilityType(this, Ability.AbilityType.DAMAGE_ARMOR);
    }

    public ArrayList<Item> getStaminaItems()
    {
        return ItemUntil.getItemsOfAbilityType(this, Ability.AbilityType.GIVE_STAMINA);
    }


    public Member getMember()
    {
        return UserAccount.getMemberFromAccountId(getUserAccountId());
    }

    public String getPromptName()
    {
        return new String(Base64.getDecoder().decode(getPromptNameBase64()));
    }

    public void setPromptName(String promptName)
    {
        setPromptNameBase64(Base64.getEncoder().encodeToString(promptName.getBytes()));
    }

    public String getPromptNameBase64()
    {
        return promptNameBase64;
    }

    public void setPromptNameBase64(String promptName)
    {
        this.promptNameBase64 = promptName;
    }


    public String toStringBattleReport()
    {
        return "Player:\n" +
                "health: " + health + "\n" +
                "stamina: " + stamina + "\n" +
                "inventory: " + inventory.toStringBattleReport();
    }

    @Override
    public String toString()
    {
        return "Player{" +
                "health=" + health +
                ", isNPC=" + isNPC +
                ", stamina=" + stamina +
                ", itemForSale=" + itemForSale +
                ", itemSellPrice=" + itemSellPrice +
                ", pendingItem=" + pendingItem +
                ", inventory=" + inventory +
                ", userAccountId='" + userAccountId + '\'' +
                ", currentTask=" + currentTask +
                ", promptNameBase64='" + promptNameBase64 + '\'' +
                '}';
    }

    public boolean isNPC()
    {
        return isNPC;
    }

    public void setNPC(boolean NPC)
    {
        isNPC = NPC;
    }



    public static void removeAllNPCPlayers() throws SQLException
    {
        LogManager.consoleLogger.debug("🧹 Removing all NPC Players from database...");

        ArrayList<SQLDataBaseObjectHolder> loadedObjects = SQLDatabaseObjectUtil.loadObjects(
                SetupDatabaseConnection.mogoloidDatabase,
                Player.class,
                new SelectLimiter()
                {
                    @Override
                    public String getSelectLimiterStatement()
                    {
                        return "";
                    }
                }
        );

        int removedCount = 0;
        for (SQLDataBaseObjectHolder holder : loadedObjects)
        {
            Player player = new Gson().fromJson(holder.getJsonObject(), Player.class);
            player.setId(holder.getId());

            if (player.isNPC())
            {
                deletePlayer(player.getId());
                removedCount++;
            }
        }

        LogManager.consoleLogger.debug("✅ Removed " + removedCount + " NPC Player(s) from database.");
    }

    public static void deletePlayer(int playerId) throws SQLException
    {
        Query deleteQuery = new Query( "DELETE FROM Player WHERE id = " + playerId + ";") ;
        DataBaseUtil.manipulateDataBase(deleteQuery, SetupDatabaseConnection.mogoloidDatabase);
    }

    public static void addNPCPlayers(ArrayList<Player> npcCrew) throws SQLException
    {
        if (npcCrew == null || npcCrew.isEmpty())
        {
            LogManager.consoleLogger.debug("⚠️ No NPC players to add.");
            return;
        }

        LogManager.consoleLogger.debug("⚙️ Adding " + npcCrew.size() + " NPC Player(s) to database...");

        for (Player npc : npcCrew)
        {
            SQLDatabaseObjectUtil.putObject(SetupDatabaseConnection.mogoloidDatabase, npc, 0);
            LogManager.consoleLogger.debug("➕ Added NPC Player: " + npc.toString());
        }

        LogManager.consoleLogger.debug("✅ All NPC Players successfully added to database.");
    }

    public static ArrayList<Player> getAllNPCPlayers() throws SQLException
    {
        ArrayList<Player> npcPlayers = new ArrayList<>();

        ArrayList<SQLDataBaseObjectHolder> loadedObjects = SQLDatabaseObjectUtil.loadObjects(
                SetupDatabaseConnection.mogoloidDatabase,
                Player.class,
                new SelectLimiter()
                {
                    @Override
                    public String getSelectLimiterStatement()
                    {
                        // Just load all Players; filtering is done in-memory
                        return "";
                    }
                }
        );

        for (SQLDataBaseObjectHolder holder : loadedObjects)
        {
            Player player = new Gson().fromJson(holder.getJsonObject(), Player.class);
            player.setId(holder.getId());

            if (player.isNPC())
                npcPlayers.add(player);
        }

        return npcPlayers;
    }

    public static ArrayList<Player> getPlayersByUserAccountIds(ArrayList<String> base64UserAccountIds) throws SQLException
    {
        if (base64UserAccountIds == null || base64UserAccountIds.isEmpty())
            return new ArrayList<>();

        StringBuilder whereClause = new StringBuilder("WHERE ");

        for (int i = 0; i < base64UserAccountIds.size(); i++)
        {
            whereClause.append("JSON_UNQUOTE(JSON_EXTRACT(objectJson, '$.userAccountId')) = '")
                    .append(base64UserAccountIds.get(i))
                    .append("'");

            if (i < base64UserAccountIds.size() - 1)
                whereClause.append(" OR ");
        }
        ArrayList<SQLDataBaseObjectHolder> loadedObjects = SQLDatabaseObjectUtil.loadObjects(
                SetupDatabaseConnection.mogoloidDatabase,
                Player.class,
                new SelectLimiter()
                {
                    @Override
                    public String getSelectLimiterStatement()
                    {
                        return whereClause.toString();
                    }
                }
        );

        ArrayList<Player> players = new ArrayList<>();
        for (SQLDataBaseObjectHolder holder : loadedObjects)
        {
            Player player = new Gson().fromJson(holder.getJsonObject(), Player.class);
            player.setId(holder.getId());
            players.add(player);
        }

        return players;
    }



    public String getNpcImageLink()
    {
        return npcImageLink;
    }

    public void setNpcImageLink(String npcImageLink)
    {
        this.npcImageLink = npcImageLink;
    }
}
