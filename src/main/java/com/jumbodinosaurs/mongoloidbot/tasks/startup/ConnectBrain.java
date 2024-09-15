package com.jumbodinosaurs.mongoloidbot.tasks.startup;

import com.google.gson.Gson;
import com.jumbodinosaurs.devlib.log.LogManager;
import com.jumbodinosaurs.devlib.task.StartUpTask;
import com.jumbodinosaurs.devlib.util.GeneralUtil;
import com.jumbodinosaurs.mongoloidbot.JDAController;
import com.jumbodinosaurs.mongoloidbot.brains.BrainsController;
import com.jumbodinosaurs.mongoloidbot.brains.BrainsOptions;

import java.io.File;

public class ConnectBrain extends StartUpTask
{

    public static File brainsOptionsFile = GeneralUtil.checkFor(JDAController.optionsFolder, "brains.json");

    @Override
    public void run()
    {
        String brainsOptionsContents = GeneralUtil.scanFileContents(brainsOptionsFile);

        if (brainsOptionsContents == null || brainsOptionsContents.isEmpty())
        {
            LogManager.consoleLogger.warn("No Brain Options Found - Skipping connecting Brains ");
            return;
        }


        BrainsOptions savedOptions = new Gson().fromJson(brainsOptionsContents, BrainsOptions.class);
        BrainsController.brainsOptions = savedOptions;
        LogManager.consoleLogger.info("Brain Settings Found and Set: " + savedOptions.toString());
    }

    @Override
    public int getOrderingNumber()
    {
        return 1;
    }
}
