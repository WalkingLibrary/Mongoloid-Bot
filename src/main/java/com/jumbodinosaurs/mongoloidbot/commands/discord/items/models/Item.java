package com.jumbodinosaurs.mongoloidbot.commands.discord.items.models;

public class Item
{
    private String name;

    private Ability ability;


    public Item(String name, Ability ability)
    {
        this.name = name;
        this.ability = ability;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Ability getAbility()
    {
        return ability;
    }

    public void setAbility(Ability ability)
    {
        this.ability = ability;
    }
}
