package com.jumbodinosaurs.mongoloidbot.commands;

import com.jumbodinosaurs.devlib.commands.CommandWithParameters;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.eventHandlers.EventListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.RateLimitedException;

public class SetNickNamesPreFix extends CommandWithParameters
{
    @Override
    public MessageResponse getExecutedMessage()
            throws WaveringParametersException
    {
        if(getParameters() == null || getParameters().size() <= 0)
        {
            throw new WaveringParametersException("No List Given");
        }
        
        String prefix = getParameters().get(0).getParameter();
        
        
        Guild guild = EventListener.getGuild();
        if(guild == null)
        {
            return new MessageResponse("Error No Guild Given");
        }
        
        for(Member guildMember : guild.getMemberCache().asList())
        {
            String nickName = prefix + " ";
            
            nickName += guildMember.getNickname() == null ? guildMember.getUser().getName() : guildMember.getNickname();
            
            int maxNicknameSize = Math.min(31, nickName.length());
            nickName = nickName.substring(0, maxNicknameSize);
            setMemberNickName(guildMember, nickName);
        }
        
        return new MessageResponse("Nick Names Prefixed With: " + prefix);
        
        
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
        return "Usage: ~SetNickNamesPreFix [PreFix]";
    }
}
