package com.jumbodinosaurs.mongoloidbot.commands.discord.items.models;

import java.util.HashMap;

public class PlayerInventory
{
    private HashMap<Integer, Item> items;

    public PlayerInventory()
    {
        this.items = new HashMap<>();
    }

    public HashMap<Integer, Item> getItems()
    {
        return items;
    }

    public void setItems(HashMap<Integer, Item> items)
    {
        this.items = items;
    }
}
