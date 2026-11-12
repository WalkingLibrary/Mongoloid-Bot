package com.jumbodinosaurs.mongoloidbot.commands.discord.util;

import com.jumbodinosaurs.devlib.commands.CommandWithParameters;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.Main;
import com.jumbodinosaurs.mongoloidbot.eventHandlers.EventListener;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.exceptions.UserQueryException;
import com.jumbodinosaurs.mongoloidbot.tasks.lottery.LotteryTask;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class ToggleJoinVoiceChats extends CommandWithParameters implements IDiscordChatEventable, IAdminCommand
{

    private MessageReceivedEvent event;
    public static boolean joinVoiceChats = false;

    @Override
    public MessageResponse getExecutedMessage() throws WaveringParametersException
    {
        joinVoiceChats = !joinVoiceChats;


        if(!joinVoiceChats && EventListener.joinedVC != null && !com.jumbodinosaurs.mongoloidbot.brains.MaxVoice.hasSession())
        {
            EventListener.joinedVC.setAutoReconnect(false);
            EventListener.joinedVC.closeAudioConnection();
        }

        return new MessageResponse("Will Join Voice Chats: " + joinVoiceChats);

    }

    @Override
    public String getHelpMessage()
    {
        return "Toggles the bots ability to join Voice Chats.\nUsage: ~" + getClass().getSimpleName();
    }

    @Override
    public MessageReceivedEvent getMessageReceivedEvent()
    {
        return event;
    }

    @Override
    public void setMessageReceivedEvent(MessageReceivedEvent event)
    {
        this.event = event;
    }
}