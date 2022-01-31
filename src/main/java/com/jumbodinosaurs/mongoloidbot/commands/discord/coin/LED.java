package com.jumbodinosaurs.mongoloidbot.commands.discord.coin;

import com.jumbodinosaurs.devlib.commands.CommandWithParameters;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.mongoloidbot.arduino.ArduinoUtil;
import com.jumbodinosaurs.mongoloidbot.arduino.exception.PhotoTimeoutException;
import com.jumbodinosaurs.mongoloidbot.coin.UserAccount;
import com.jumbodinosaurs.mongoloidbot.coin.exceptions.UserQueryException;
import com.jumbodinosaurs.mongoloidbot.coin.tasks.LotteryTask;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.SetupDatabaseConnection;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;

public class LED extends CommandWithParameters implements IDiscordChatEventable
{
    
    private GuildMessageReceivedEvent event;
    
    @Override
    public MessageResponse getExecutedMessage()
            throws WaveringParametersException
    {
        
        /*
         * Process for changing an LED Status
         * Check their Money
         * Get Light to Toggle
         * Toggle Light
         * Take Photo
         * Remove money for their account
         * Add Money to Pot
         * Return Photo message
         *
         *
         * */
        
        
        try
        {
            UserAccount accountToUpdate = UserAccount.getUser(event.getMember());
            BigDecimal costToToggleLight = new BigDecimal("5000");
            //1. Check their Money
            if(accountToUpdate.getBalance().subtract(costToToggleLight).signum() <= -1)
            {
                return new MessageResponse("You Don't have Enough to Toggle the Light");
            }
            
            //2. Get Light to Toggle
            
            if(getParameters() == null || getParameters().size() < 1)
            {
                throw new WaveringParametersException("You didn't Give enough Information");
            }
            
            
            String lightToToggleString = getParameters().get(0).getParameter();
            ArduinoUtil.Light lightToToggle = null;
            switch(lightToToggleString.toLowerCase())
            {
                case "blue":
                    lightToToggle = ArduinoUtil.Light.BLUE;
                    break;
                case "red":
                    lightToToggle = ArduinoUtil.Light.RED;
                    break;
                case "green":
                    lightToToggle = ArduinoUtil.Light.GREEN;
                    break;
                default:
                    String listOfLights = "";
                    for(ArduinoUtil.Light light : ArduinoUtil.Light.values())
                    {
                        listOfLights += light.name() + "\n";
                    }
                    throw new WaveringParametersException("Sorry, I Couldn't Find that Light.\nHere at the Lights " +
                                                          "Available:\n" +
                                                          listOfLights);
            }
            
            // Toggle Light
            
            try
            {
                ArduinoUtil.toggleLight(lightToToggle);
            }
            catch(IOException e)
            {
                e.printStackTrace();
                return new MessageResponse("Sorry here was Error Toggling that Light");
            }
            
            //Take Photo
            
            BufferedImage photoOfLight = null;
            
            
            try
            {
                photoOfLight = ArduinoUtil.takePhoto();
            }
            catch(IOException | PhotoTimeoutException e)
            {
                e.printStackTrace();
            }
            
            if(photoOfLight == null)
            {
                return new MessageResponse("The Light was toggled But there was an error Taking a Photo of it. Sorry " +
                                           ":(");
            }
            
            
            // Remove money for their account
            accountToUpdate.setBalance(accountToUpdate.getBalance().subtract(costToToggleLight));
            
            SQLDatabaseObjectUtil.putObject(SetupDatabaseConnection.mogoloidDatabase,
                                            accountToUpdate,
                                            accountToUpdate.getId());
            
            
            // Add Money to Pot
            LotteryTask.addToPot(costToToggleLight);
            
            
            // Return Photo message
            ArrayList<File> attachments = new ArrayList<File>();
            File streamToFile = null;
            try
            {
                streamToFile = File.createTempFile("stream2file", ".jpeg");
                ImageIO.write(photoOfLight, "jpeg", streamToFile);
                attachments.add(streamToFile);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            streamToFile.deleteOnExit();
            return new MessageResponse("The " + lightToToggle.name() + " Light Has Been Toggled", attachments);
        }
        catch(SQLException e)
        {
            e.printStackTrace();
            return new MessageResponse("Error Accessing the Database");
        }
        catch(UserQueryException e)
        {
            e.printStackTrace();
            return new MessageResponse("Account Error");
        }
    }
    
    @Override
    public String getHelpMessage()
    {
        return "Toggles a LED in Jumbos Room on or off.\nUsage: !LED green";
    }
    
    @Override
    public GuildMessageReceivedEvent getGuildMessageReceivedEvent()
    {
        return event;
    }
    
    @Override
    public void setGuildMessageReceivedEvent(GuildMessageReceivedEvent event)
    {
        this.event = event;
    }
}

