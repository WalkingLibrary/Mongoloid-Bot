package com.jumbodinosaurs.mongoloidbot.commands.discord.items.util;

import com.jumbodinosaurs.devlib.discord.DiscordWebHookAPIMessage;
import com.jumbodinosaurs.devlib.util.GeneralUtil;
import com.jumbodinosaurs.devlib.util.WebUtil;
import com.jumbodinosaurs.mongoloidbot.AppSettingsManager;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Player;

import java.io.IOException;

public class NPCUtil
{
    public static void SendNPCChatMessage(String channel, String message, Player npcPlayer) throws IOException
    {
        DiscordWebHookAPIMessage apiMessage = new DiscordWebHookAPIMessage(npcPlayer.getPromptName(), npcPlayer.getNpcImageLink(), message);
        WebUtil.sendMessageToWebHook(AppSettingsManager.getStringValue(channel), apiMessage);
    }
}
