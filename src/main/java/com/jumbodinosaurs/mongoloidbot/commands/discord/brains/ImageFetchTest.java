package com.jumbodinosaurs.mongoloidbot.commands.discord.brains;

import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.brains.ImageFetcher;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IOwnerCommand;

import java.io.File;
import java.util.ArrayList;

public class ImageFetchTest extends Command implements IOwnerCommand
{
    @Override
    public MessageResponse getExecutedMessage() throws WaveringParametersException
    {
        try
        {
            ArrayList<File> genImages = new ArrayList<>();
            String prompt = "Dinosaur";
            genImages.add(ImageFetcher.fetchImage(prompt));
            return new MessageResponse(prompt, genImages);
        }
        catch (Exception e)
        {
            return new MessageResponse("Error: " + e.getMessage());
        }

    }

    @Override
    public String getHelpMessage()
    {
        return "Allows the Owner of the Bot to test Image Generation";
    }
}
