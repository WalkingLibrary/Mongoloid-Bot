package com.jumbodinosaurs.mongoloidbot.commands.discord.items.models;

import com.jumbodinosaurs.mongoloidbot.models.DiscordANSITextHelper;

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


    public static DiscordANSITextHelper GetColor(Ability ability)
    {
        if (ability.getType() == Ability.AbilityType.GIVE_STAMINA)
        {
            return DiscordANSITextHelper.YELLOW;
        }

        if (ability.getType() == Ability.AbilityType.ARMOR)
        {
            return DiscordANSITextHelper.CYAN;
        }

        if (ability.getType() == Ability.AbilityType.GIVE_HEALTH)
        {
            return DiscordANSITextHelper.RED;
        }

        if (ability.getType() == Ability.AbilityType.TAKE_HEALTH)
        {
            return DiscordANSITextHelper.BLUE;
        }

        if (ability.getType() == Ability.AbilityType.DAMAGE_ARMOR)
        {
            return DiscordANSITextHelper.GREEN;
        }
        return DiscordANSITextHelper.WHITE;
    }

    public String toInventoryDisplay()
    {
        String stringBuilder = name +
                "\n" +
                DiscordANSITextHelper.BOLD.wrap("Item Type:") +
                " " +
                GetColor(ability).wrap(ability.getType().toString()) +
                " " +
                DiscordANSITextHelper.BOLD.wrap("Intensity:") +
                " " +
                ability.getIntensity();

        return stringBuilder;
    }
}
