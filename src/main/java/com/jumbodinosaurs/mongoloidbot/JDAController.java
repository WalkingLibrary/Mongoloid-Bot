package com.jumbodinosaurs.mongoloidbot;

import com.jumbodinosaurs.devlib.commands.CommandManager;
import com.jumbodinosaurs.devlib.util.GeneralUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.io.File;


public class JDAController
{
    private JDA jda;
    public static File optionsFolder = GeneralUtil.checkFor(GeneralUtil.userDir, "Options", true);
    
    public JDAController(String token)
    {
        try
        {
            CommandManager.refreshCommands();
            this.jda = JDABuilder.createDefault(token)
                                 .setChunkingFilter(ChunkingFilter.ALL) // enable member chunking for all guilds
                                 .setMemberCachePolicy(MemberCachePolicy.ALL) // ignored if chunking enabled
                                 .enableIntents(GatewayIntent.GUILD_MEMBERS)
                                 .build();
            jda.awaitReady();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println("Error Starting Up Bot");
        }
        
    }
    
    public JDA getJda()
    {
        return jda;
    }
    
    public void setJda(JDA jda)
    {
        this.jda = jda;
    }
}