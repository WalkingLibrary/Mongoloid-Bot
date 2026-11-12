package com.jumbodinosaurs.mongoloidbot.commands.discord.util;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface IDiscordChatEventable
{
    MessageReceivedEvent getMessageReceivedEvent();
    
    void setMessageReceivedEvent(MessageReceivedEvent event);
}
