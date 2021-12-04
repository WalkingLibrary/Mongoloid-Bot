package com.jumbodinosaurs.mongoloidbot.commands.discord.util;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public interface IDiscordChatEventable
{
    GuildMessageReceivedEvent getGuildMessageReceivedEvent();
    
    void setGuildMessageReceivedEvent(GuildMessageReceivedEvent event);
}
