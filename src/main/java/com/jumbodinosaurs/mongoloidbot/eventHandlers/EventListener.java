package com.jumbodinosaurs.mongoloidbot.eventHandlers;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class EventListener extends ListenerAdapter
{
    
    
    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event)
    {
        VoiceChannel voiceChannelJoined = event.getChannelJoined();
        System.out.println("Voice Channel Joined: " + voiceChannelJoined.getName());
        if(voiceChannelJoined.getName().equals("cigar lounge"))
        {
            Role ashTrayRole = event.getGuild().getRolesByName("ashtray", true).get(0);
            event.getGuild().addRoleToMember(event.getMember(), ashTrayRole).complete();
        }
        super.onGuildVoiceJoin(event);
    }
    
    
    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event)
    {
        VoiceChannel voiceChannelLeft = event.getChannelLeft();
        System.out.println("Voice Channel Left: " + voiceChannelLeft.getName());
        if(voiceChannelLeft.getName().equals("cigar lounge"))
        {
            Role ashTrayRole = event.getGuild().getRolesByName("ashtray", true).get(0);
            event.getGuild().removeRoleFromMember(event.getMember(), ashTrayRole).complete();
        }
        super.onGuildVoiceLeave(event);
    }
    
    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event)
    {
        VoiceChannel voiceChannelJoined = event.getChannelJoined();
        VoiceChannel voiceChannelLeft = event.getChannelLeft();
        System.out.println("User Moved from " + voiceChannelLeft.getName() + " to " + voiceChannelJoined.getName());
        
        if(voiceChannelLeft.getName().equals("cigar lounge"))
        {
            Role ashTrayRole = event.getGuild().getRolesByName("ashtray", true).get(0);
            event.getGuild().removeRoleFromMember(event.getMember(), ashTrayRole).complete();
        }
        
        if(voiceChannelJoined.getName().equals("cigar lounge"))
        {
            Role ashTrayRole = event.getGuild().getRolesByName("ashtray", true).get(0);
            event.getGuild().addRoleToMember(event.getMember(), ashTrayRole).complete();
        }
        super.onGuildVoiceMove(event);
    }
    
}
