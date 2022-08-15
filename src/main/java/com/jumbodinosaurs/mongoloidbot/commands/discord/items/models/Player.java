package com.jumbodinosaurs.mongoloidbot.commands.discord.items.models;

public class Player
{

    private transient int id;

    private int health;

    private int stamina;

    private Item pendingItem;

    private Inventory inventory;

    private String userAccountId;

    private CurrentTask currentTask;

    public Player(String userAccountId)
    {
        this.health = 100;
        this.inventory = new Inventory();
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

    public Inventory getInventory()
    {
        return inventory;
    }

    public void setInventory(Inventory inventory)
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
}
