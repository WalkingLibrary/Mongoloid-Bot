package com.jumbodinosaurs.mongoloidbot.commands.discord.coin;

import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.tasks.lottery.LotteryTask;

public class Pot extends Command
{

    @Override
    public String getCategory()
    {
        return "Coin";
    }

    @Override
    public MessageResponse getExecutedMessage()
            throws WaveringParametersException
    {
        return new MessageResponse("Lottery Pot: " + LotteryTask.getPot());
    }

    @Override
    public String getHelpMessage()
    {
        return "Tells you how much is in the Lottery Pot.\nUsage:\n~Pot";
    }
}
