package com.jumbodinosaurs.mongoloidbot.commands.discord.brains;

import com.google.gson.Gson;
import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.devlib.util.GeneralUtil;
import com.jumbodinosaurs.mongoloidbot.brains.BrainsController;
import com.jumbodinosaurs.mongoloidbot.brains.BrainsOptions;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IOwnerCommand;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.ConnectBrain;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ToggleBrains extends Command implements IDiscordChatEventable, IOwnerCommand
{

    private GuildMessageReceivedEvent event;

    @Override
    public String getCategory()
    {
        return "Brains";
    }

    @Override
    public MessageResponse getExecutedMessage()
            throws WaveringParametersException
    {
        BrainsController.brainsOptions.setShouldRespond(!BrainsController.brainsOptions.isShouldRespond());
        GeneralUtil.writeContents(ConnectBrain.brainsOptionsFile, new Gson().toJson(BrainsController.brainsOptions,
                BrainsOptions.class), false);

        if (BrainsController.brainsOptions.isShouldRespond())
        {
            return new MessageResponse("I'm Awake");
        }
        return new MessageResponse("NO PLEASE, NO...");
    }

    @Override
    public String getHelpMessage()
    {
        return "Toggles Brains";
    }

    @Override
    public GuildMessageReceivedEvent getGuildMessageReceivedEvent()
    {
        return event;
    }

    @Override
    public void setGuildMessageReceivedEvent(GuildMessageReceivedEvent event)
    {
        this.event = event;
    }
}