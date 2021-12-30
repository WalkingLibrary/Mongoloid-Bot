package com.jumbodinosaurs.mongoloidbot.commands.discord.coin;

import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.coin.tasks.LotteryTask;

public class Pot extends Command
{
    @Override
    public MessageResponse getExecutedMessage()
            throws WaveringParametersException
    {
        return new MessageResponse("Lottery Pot: " + LotteryTask.getPot().toString());
    }
    
    @Override
    public String getHelpMessage()
    {
        return "Tells you how much is in the Lottery Pot.\nUsage:\n~Pot";
    }
}
