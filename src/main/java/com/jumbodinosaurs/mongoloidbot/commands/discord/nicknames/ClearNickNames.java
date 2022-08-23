package com.jumbodinosaurs.mongoloidbot.commands.discord.nicknames;

import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IAdminCommand;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.RateLimitedException;

public class ClearNickNames extends Command implements IDiscordChatEventable,
        IAdminCommand
{
    private GuildMessageReceivedEvent event;


    @Override
    public String getCategory()
    {
        return "Nicknames";
    }

    @Override
    public MessageResponse getExecutedMessage()
            throws WaveringParametersException
    {

        Guild guild = event.getGuild();
        if (guild == null)
        {
            return new MessageResponse("Error No Guild Given");
        }
        
        for(Member guildMember : guild.getMemberCache().asList())
        {
            setMemberNickName(guildMember, guildMember.getUser().getName());
        }
        return new MessageResponse("Nick Names Cleared!");
    }
    
    public void setMemberNickName(Member member, String nickName)
    {
        try
        {
            System.out.println("Setting " + member.getNickname() + " to: " + nickName);
            member.modifyNickname(nickName).complete(true);
            try
            {
                Thread.sleep(500);
            }
            catch(InterruptedException ignored)
            {
            
            }
        }
        catch(RateLimitedException e)
        {
            try
            {
                Thread.sleep(100);
            }
            catch(InterruptedException ignored)
            {
            
            }
            setMemberNickName(member, nickName);
        }
        catch(HierarchyException ignored)
        {
            System.out.println("Cannot Change: " + member.getNickname());
        }
    }
    
    @Override
    public String getHelpMessage()
    {
        return "Usage: ~ClearNickNames";
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
