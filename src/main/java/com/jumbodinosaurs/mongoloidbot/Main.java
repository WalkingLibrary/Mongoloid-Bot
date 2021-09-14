package com.jumbodinosaurs.mongoloidbot;

import com.jumbodinosaurs.mongoloidbot.eventHandlers.EventListener;

public class Main
{
    public static void main(String[] args)
    {
        if(args.length <= 0)
        {
            System.out.println("Usage: Java -jar jarName.jar discordToken");
            System.exit(1);
        }
        
        String botToken = args[0];
        JDAController controller = new JDAController(botToken);
        controller.getJda().addEventListener(new EventListener());
        
    }
}
