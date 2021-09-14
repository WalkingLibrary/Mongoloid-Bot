package com.jumbodinosaurs.mongoloidbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;


public class JDAController
{
    private JDA jda;
    
    public JDAController(String token)
    {
        try
        {
            this.jda = JDABuilder.createDefault(token).build();
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