package com.jumbodinosaurs.mongoloidbot.commands.discord.items.models;

import java.util.HashMap;

public class Inventory
{
    private HashMap<Integer, Item> items;

    public Inventory()
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
