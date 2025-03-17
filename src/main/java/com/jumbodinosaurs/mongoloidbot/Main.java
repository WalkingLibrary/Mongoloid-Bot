package com.jumbodinosaurs.mongoloidbot;

import com.jumbodinosaurs.devlib.log.LogManager;
import com.jumbodinosaurs.devlib.task.DefaultStartUpTask;
import com.jumbodinosaurs.devlib.task.ScheduledTask;
import com.jumbodinosaurs.devlib.task.TaskUtil;
import com.jumbodinosaurs.mongoloidbot.arduino.ArduinoUtil;
import com.jumbodinosaurs.mongoloidbot.arduino.exception.InitializationException;
import com.jumbodinosaurs.mongoloidbot.eventHandlers.EventListener;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Main
{
    public static JDAController jdaController;
    public static final String GUILD_ID = AppSettingsManager.getStringValue("GUILD_ID");
    public static final String BOT_CHANNEL_ID = AppSettingsManager.getStringValue("BOT_CHANNEL_ID");
    public static final String BATTLE_STEPPE_ID = AppSettingsManager.getStringValue("BATTLE_STEPPE_ID");
    private static final ScheduledThreadPoolExecutor threadScheduler = new ScheduledThreadPoolExecutor(4);


    public static void main(String[] args)
    {


        if (args.length <= 0)
        {
            LogManager.consoleLogger.info("Usage: Java -jar jarName.jar discordToken [noArduino (Optional)]");
            System.exit(1);
        }
    
    
        String botToken = args[0];
        jdaController = new JDAController(botToken);
        jdaController.getJda().addEventListener(new EventListener());
        DefaultStartUpTask defaultStartUpTask = new DefaultStartUpTask();
        defaultStartUpTask.run();
    
        ArrayList<ScheduledTask> scheduledServerTasks = TaskUtil.getScheduledTasks(threadScheduler);
    
        for(ScheduledTask scheduledTask : scheduledServerTasks)
        {
            LogManager.consoleLogger.info("Starting Scheduled Task: " + scheduledTask.getClass().getSimpleName());
            scheduledTask.start();
        }
        try
        {
            //Add Something to Args to Stop Arduino from init in dev
            if (args.length == 1)
            {
                ArduinoUtil.init();
            }
        }
        catch(InitializationException e)
        {
            e.printStackTrace();
        }
    
        LogManager.consoleLogger.info("Bot Setup Task Complete");
    
    }
}
