package com.jumbodinosaurs.mongoloidbot.commands.discord.items.models;

import com.jumbodinosaurs.mongoloidbot.commands.discord.items.util.ItemUntil;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import net.dv8tion.jda.api.entities.Member;

import java.math.BigDecimal;
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
}
