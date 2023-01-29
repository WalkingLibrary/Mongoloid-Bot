package com.jumbodinosaurs.mongoloidbot.commands.discord.items.models;

import com.jumbodinosaurs.mongoloidbot.commands.discord.items.util.ItemUntil;

import java.math.BigDecimal;
import java.util.ArrayList;

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
}
