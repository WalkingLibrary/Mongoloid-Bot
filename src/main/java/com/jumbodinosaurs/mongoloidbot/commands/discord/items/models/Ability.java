package com.jumbodinosaurs.mongoloidbot.commands.discord.items.models;

public class Ability
{
    private AbilityType type;

    private int intensity;

    public Ability(AbilityType type, int intensity)
    {
        this.type = type;
        this.intensity = intensity;
    }


    public AbilityType getType()
    {
        return type;
    }

    public void setType(AbilityType type)
    {
        this.type = type;
    }

    public int getIntensity()
    {
        return intensity;
    }

    public void setIntensity(int intensity)
    {
        this.intensity = intensity;
    }

    public String toInfoString()
    {
        return "This Ability is " + type.name() + " with the Intensity of " + intensity;
    }

    public enum AbilityType
    {
        TAKE_HEALTH("Weapon", 200),
        GIVE_HEALTH("Healing", 200),
        ARMOR("Armor", 450),
        DAMAGE_ARMOR("Armor Breaking", 200),
        GIVE_STAMINA("Stamina", 1000);

        public String displayName;
        public int maxIntensity;

        AbilityType(String displayName, int maxIntensity)
        {
            this.displayName = displayName;
            this.maxIntensity = maxIntensity;
        }

    }
}
