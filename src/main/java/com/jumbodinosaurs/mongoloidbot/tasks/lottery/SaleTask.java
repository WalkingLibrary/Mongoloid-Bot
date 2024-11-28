package com.jumbodinosaurs.mongoloidbot.tasks.lottery;

import com.jumbodinosaurs.devlib.task.ScheduledTask;
import com.jumbodinosaurs.mongoloidbot.eventHandlers.EventListener;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SaleTask extends ScheduledTask
{
    private boolean hasRunToday = false; // Ensures the task only runs once per day


    public SaleTask(ScheduledThreadPoolExecutor executor)
    {
        super(executor);
    }


    public static boolean isBlackFriday()
    {
        LocalDate today = LocalDate.now();
        // Black Friday is the fourth Friday of November
        return today.getMonthValue() == 11 &&
                today.getDayOfWeek() == DayOfWeek.FRIDAY &&
                today.getDayOfMonth() >= 23 && today.getDayOfMonth() <= 30;
    }

    @Override
    public void run()
    {
        if (!hasRunToday && isBlackFriday())
        {
            hasRunToday = true;
            EventListener.sendMessage("LIMITED BLACK FRIDAY SALE ON CAPTAIN @everyone");
            EventListener.sendMessage("CAPTAIN COSTS 1000 For Today");
        }
    }

    @Override
    public int getPeriod()
    {
        return 1;
    }

    @Override
    public TimeUnit getTimeUnit()
    {
        return TimeUnit.HOURS; // Check every minute for precision
    }
}

