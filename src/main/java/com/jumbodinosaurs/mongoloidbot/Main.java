package com.jumbodinosaurs.mongoloidbot;

import com.jumbodinosaurs.devlib.task.DefaultStartUpTask;
import com.jumbodinosaurs.mongoloidbot.eventHandlers.EventListener;

public class Main
{
    public static JDAController jdaController;
    public static void main(String[] args)
    {
        if(args.length <= 0)
        {
            System.out.println("Usage: Java -jar jarName.jar discordToken");
            System.exit(1);
        }
    
        DefaultStartUpTask defaultStartUpTask = new DefaultStartUpTask();
        defaultStartUpTask.run();
    
        String botToken = args[0];
        jdaController = new JDAController(botToken);
        jdaController.getJda().addEventListener(new EventListener());
    
    }
}
