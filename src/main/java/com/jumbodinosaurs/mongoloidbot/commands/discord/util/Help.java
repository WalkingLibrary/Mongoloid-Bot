package com.jumbodinosaurs.mongoloidbot.commands.discord.util;

import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.CommandManager;
import com.jumbodinosaurs.devlib.commands.MessageResponse;

public class Help extends com.jumbodinosaurs.devlib.commands.Help
{
    @Override
    public MessageResponse getExecutedMessage()
    {
        if (getParameters().size() > 0)
        {
            return super.getExecutedMessage();
        }
        String commandsToList = "**Commands:**\n";

        for (String category : CommandManager.getCategories())
        {
            commandsToList += "**" + category + "**:\n```\n";
            for (Command command : CommandManager.getLoadedCommands())
            {
                if (command.getCategory().equals(category))
                {
                    commandsToList += command.getCommand() + "\n";
                }
            }
            commandsToList += "\n```";
        }
        return new MessageResponse(commandsToList);
    }

    @Override
    public int getVersion()
    {
        return 0;
    }
}
