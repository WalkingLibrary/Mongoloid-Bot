package com.jumbodinosaurs.mongoloidbot.commands.discord.items.models;

import java.time.LocalDateTime;

public class CurrentTask
{
    public LocalDateTime startDate;
    public TaskType taskName;

    public CurrentTask(TaskType taskName)
    {
        this.taskName = taskName;
        this.startDate = LocalDateTime.now();
    }

    public LocalDateTime getStartDate()
    {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate)
    {
        this.startDate = startDate;
    }

    public TaskType getTaskName()
    {
        return taskName;
    }

    public void setTaskName(TaskType taskName)
    {
        this.taskName = taskName;
    }

    public enum TaskType
    {
        EXPLORING, BATTLING;

        TaskType()
        {
        }
    }
}
