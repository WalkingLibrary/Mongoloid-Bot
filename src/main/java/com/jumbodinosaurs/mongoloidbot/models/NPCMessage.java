package com.jumbodinosaurs.mongoloidbot.models;

import com.jumbodinosaurs.mongoloidbot.AppSettingsManager;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Player;

import java.sql.SQLException;

public class NPCMessage
{

    private boolean specificNPC;
    private String specificNPCBase64Username;
    private String message;

    public boolean isSpecificNPC()
    {
        return specificNPC;
    }

    public void setSpecificNPC(boolean specificNPC)
    {
        this.specificNPC = specificNPC;
    }

    public String getSpecificNPCBase64Username()
    {
        return specificNPCBase64Username;
    }

    public void setSpecificNPCBase64Username(String specificNPCBase64Username)
    {
        this.specificNPCBase64Username = specificNPCBase64Username;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }


    public static Player getSpecificNPCPlayer(NPCMessage npcMessage) throws SQLException
    {

        for(Player player: Player.getAllNPCPlayers())
        {
            if (player.getPromptNameBase64().equals(npcMessage.specificNPCBase64Username))
            {
                return player;
            }
        }
        Player fillingPlayer = new Player("");
        fillingPlayer.setNPC(true);
        fillingPlayer.setPromptNameBase64(npcMessage.specificNPCBase64Username);
        fillingPlayer.setNpcImageLink(AppSettingsManager.getStringValue("fillingPlayerIconURL"));
        return fillingPlayer;
    }

}
