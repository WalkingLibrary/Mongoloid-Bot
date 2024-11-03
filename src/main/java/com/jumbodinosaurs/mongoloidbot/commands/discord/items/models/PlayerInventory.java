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

    public String toStringBattleReport()
    {

        String inventoryString = "Items:\n";
        for (Integer key : items.keySet())
        {
            inventoryString += key.toString() + " " + items.get(key).getName() + "\n";
            inventoryString += " Ability: " + items.get(key).getAbility().getType().displayName + "\n";
            inventoryString += " Intensity: " + items.get(key).getAbility().getIntensity() + "\n";
        }

        return inventoryString;
    }

    @Override
    public String toString()
    {
        return "PlayerInventory{" +
                "items=" + items +
                '}';
    }
}
