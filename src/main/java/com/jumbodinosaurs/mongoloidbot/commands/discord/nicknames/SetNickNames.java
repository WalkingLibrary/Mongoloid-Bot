package com.jumbodinosaurs.mongoloidbot.commands.discord.nicknames;

import com.jumbodinosaurs.devlib.commands.CommandWithParameters;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.devlib.util.GeneralUtil;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IAdminCommand;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.RateLimitedException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class SetNickNames extends CommandWithParameters implements IDiscordChatEventable,
        IAdminCommand
{
    private static final File nickNamesDir = GeneralUtil.checkFor(GeneralUtil.userDir, "NickNameLists", true);
    private GuildMessageReceivedEvent event;


    @Override
    public String getCategory()
    {
        return "Nicknames";
    }

    public static String getRandomNickname(ArrayList<String> nickNames)
    {
        int randomIndex = (int) (nickNames.size() * Math.random());
        return nickNames.get(randomIndex);
    }

    @Override
    public MessageResponse getExecutedMessage()
            throws WaveringParametersException
    {
        if(getParameters() == null || getParameters().size() <= 0)
        {
            throw new WaveringParametersException("No List Given");
        }
        
        String listName = getParameters().get(0).getParameter();
        
        for(File file : GeneralUtil.listFilesRecursive(nickNamesDir))
        {
            if(file.getName().contains(listName))
            {
                String nickNamesList = GeneralUtil.scanFileContents(file);
                String[] nickNamesArray = nickNamesList.split("\n");
                ArrayList<String> nickNames = (new ArrayList<String>(Arrays.asList(nickNamesArray)));
                Guild guild = event.getGuild();
                
                if(guild == null)
                {
                    return new MessageResponse("Error No Guild Given");
                }
                
                
                for(Member guildMember : guild.getMemberCache().asList())
                {
                    String nickName = getRandomNickname(nickNames);
                    setMemberNickName(guildMember, nickName);
                }
                return new MessageResponse("Nick Names Set Using: " + file.getName());
            }
        }
        
        return new MessageResponse("No List Found with the Name: " + listName);
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
        String helpMessage = "Usage: ~SetNickNames [nicknameList]\n";
    
        helpMessage += "Nick Name Lists:\n";
    
        for(File file : GeneralUtil.listFilesRecursive(nickNamesDir))
        {
            helpMessage += file.getName() + "\n";
        }
    
        return helpMessage;
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
