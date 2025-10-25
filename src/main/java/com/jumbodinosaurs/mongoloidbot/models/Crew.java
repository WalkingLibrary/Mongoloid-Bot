package com.jumbodinosaurs.mongoloidbot.models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumbodinosaurs.devlib.json.GsonUtil;
import com.jumbodinosaurs.devlib.util.GeneralUtil;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Player;

import java.io.File;
import java.util.ArrayList;

public class Crew
{
    private String crewNPCsFileName;

    private String promptMessage;

    private NPCMessage takeOVerMessage = new NPCMessage();

    private ArrayList<NPCMessage> handCraftedMessages = new ArrayList<>();

    public String getCrewNPCsFileName()
    {
        return crewNPCsFileName;
    }

    public void setCrewNPCsFileName(String crewNPCsFileName)
    {
        this.crewNPCsFileName = crewNPCsFileName;
    }

    public String getPromptMessage()
    {
        return promptMessage;
    }

    public void setPromptMessage(String promptMessage)
    {
        this.promptMessage = promptMessage;
    }

    public NPCMessage getTakeOVerMessage()
    {
        return takeOVerMessage;
    }

    public void setTakeOVerMessage(NPCMessage takeOVerMessage)
    {
        this.takeOVerMessage = takeOVerMessage;
    }

    public ArrayList<NPCMessage> getHandCraftedMessages()
    {
        return handCraftedMessages;
    }

    public void setHandCraftedMessages(ArrayList<NPCMessage> handCraftedMessages)
    {
        this.handCraftedMessages = handCraftedMessages;
    }

    public ArrayList<Player> getPlayers()
    {
        File npcFile = GeneralUtil.checkFor(GeneralUtil.userDir, "Options/Crews/NPCs/" + crewNPCsFileName, false);
        return GsonUtil.readList(npcFile, Player.class, new TypeToken<ArrayList<Player>>(){}, false);
    }


    public static Crew loadCrewFromFile(String crewFileName)
    {
        File crewFile = GeneralUtil.checkFor(GeneralUtil.userDir, "Options/Crews/" + crewFileName, false);
        return new Gson().fromJson(GeneralUtil.scanFileContents(crewFile), Crew.class);
    }
}
