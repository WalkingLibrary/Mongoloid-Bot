package com.jumbodinosaurs.mongoloidbot.commands.discord.items.util;

import com.jumbodinosaurs.devlib.util.GeneralUtil;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Ability;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Item;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Player;

import java.io.File;
import java.util.ArrayList;

public class ItemUntil
{
    public static File adjectiveFile = GeneralUtil.checkForLocalPath(GeneralUtil.userDir, "Items/adjectives.txt");
    public static File nounFile = GeneralUtil.checkForLocalPath(GeneralUtil.userDir, "Items/nouns.txt");

    public static Item generateRandomItem()
    {
        int randomTypeRoll = (int) (Math.random() * Ability.AbilityType.values().length);

        Ability.AbilityType randomType = Ability.AbilityType.values()[randomTypeRoll];
        String randomName = generateRandomNoun() + " of " + generateRandomAdjective();

        //Set Intensity
        int randomIntensity = (int) (Math.random() * randomType.maxIntensity);

        Item randomItem = new Item(randomName, new Ability(randomType, randomIntensity));
        return randomItem;
    }


    public static String generateRandomNoun()
    {
        String fileContents = GeneralUtil.scanFileContents(nounFile);

        String[] nouns = fileContents.split("\n");

        int randomNounRolled = (int) (Math.random() * nouns.length);

        return nouns[randomNounRolled];
    }

    public static String generateRandomAdjective()
    {
        String fileContents = GeneralUtil.scanFileContents(adjectiveFile);

        String[] adjectives = fileContents.split("\n");

        int randomAdjectiveRolled = (int) (Math.random() * adjectives.length);

        return adjectives[randomAdjectiveRolled];
    }

    public static ArrayList<Item> getItemsOfAbilityType(Player player, Ability.AbilityType abilityType)
    {
        ArrayList<Item> playerItems = new ArrayList<Item>();
        for (Item item : player.getInventory().getItems().values())
        {
            if (item.getAbility().getType().equals(abilityType))
            {
                playerItems.add(item);
            }
        }
        return playerItems;
    }


}
