package com.jumbodinosaurs.mongoloidbot.commands;

import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.eventHandlers.EventListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.RateLimitedException;

public class ClearNickNames extends Command
{
    @Override
    public MessageResponse getExecutedMessage()
            throws WaveringParametersException
    {
        
        Guild guild = EventListener.getGuild();
        if(guild == null)
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
}
