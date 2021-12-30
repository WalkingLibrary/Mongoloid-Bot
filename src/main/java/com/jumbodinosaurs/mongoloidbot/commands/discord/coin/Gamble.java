package com.jumbodinosaurs.mongoloidbot.commands.discord.coin;

import com.jumbodinosaurs.devlib.commands.CommandWithParameters;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.mongoloidbot.coin.UserAccount;
import com.jumbodinosaurs.mongoloidbot.coin.exceptions.UserQueryException;
import com.jumbodinosaurs.mongoloidbot.coin.tasks.LotteryTask;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.SetupDatabaseConnection;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.math.BigDecimal;
import java.sql.SQLException;

public class Gamble extends CommandWithParameters implements IDiscordChatEventable
{
    
    private GuildMessageReceivedEvent event;
    
    @Override
    public MessageResponse getExecutedMessage()
            throws WaveringParametersException
    {
        
        BigDecimal amountToGamble = new BigDecimal(String.valueOf(1));
        
        if(getParameters() != null && getParameters().size() >= 1)
        {
            try
            {
                amountToGamble = new BigDecimal(getParameters().get(0).getParameter());
            }
            catch(NumberFormatException e)
            {
                return new MessageResponse("Enter a Valid Number");
            }
        }
        
        if(amountToGamble.signum() <= -1)
        {
            return new MessageResponse("You Cannot Steal from the House");
        }
        UserAccount accountToUpdate;
        try
        {
            accountToUpdate = UserAccount.getUser(event.getMember());
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
        
        if(accountToUpdate.getBalance().subtract(amountToGamble).signum() <= -1)
        {
            return new MessageResponse("You Can Not Gamble What you Do not Have");
        }
        
        /*
         * Process For Gambling Evaluation
         *
         * Winnings Value and Combo Value
         * Combo Value is Multiplied by the Winnings to decide Final Winnings
         *
         * Winnings Initial Value is the Amount Gambled
         *
         * Randomly Select Three Int Between 1 and 6
         *
         * If Value is
         *
         * 1: Add a Dollar To Winnings
         * 2: Double Combo Value
         * 3: Negates Combo Value
         * 4: Add 4 Dollars To Winnings
         * 5: Negates Combo Value
         * 6: Quadruples Combo Value
         * 7: Half s the Combo Value
         * 8: Add 8 Dollars To Winnings
         *
         *
         *  If Two of a Kind Rolled Half s your Winnings
         *  If Three of a Kind Jack Pot and guaranteed Positive Combo Score
         *
         *  */
        
        BigDecimal winnings = new BigDecimal(amountToGamble + "");
        BigDecimal comboValue = new BigDecimal("1");
        
        String[] emojies = {":100:", ":watch:", ":poultry_leg:", ":eye:", ":ring:", ":lizard:", ":monkey:", ":mouse:"};
        
        int roll1, roll2, roll3;
        
        roll1 = (int) (Math.random() * emojies.length);
        roll2 = (int) (Math.random() * emojies.length);
        roll3 = (int) (Math.random() * emojies.length);
        
        int[] rolls = {roll1, roll2, roll3};
        
        
        String displayMessage = "";
        
        for(int rollValue : rolls)
        {
            displayMessage += emojies[rollValue] + " ";
            switch(rollValue)
            {
                // 1: Add a Dollar To Winnings
                case 0:
                    winnings = winnings.add(new BigDecimal("1"));
                    
                    break;
                //2: Double Combo Value
                case 1:
                    comboValue = comboValue.multiply(new BigDecimal("2"));
                    break;
                //3: Negates Combo Value
                case 2:
                    comboValue = comboValue.multiply(new BigDecimal("-1"));
                    break;
                //4: Add 4 Dollars To Winnings
                case 3:
                    winnings = winnings.add(new BigDecimal("4"));
                    break;
                //5: Negates Combo Value
                case 4:
                    comboValue = comboValue.multiply(new BigDecimal("-1"));
                    break;
                //6: Quadruples Combo Value
                case 5:
                    comboValue = comboValue.multiply(new BigDecimal("4"));
                    break;
                //7: Half s the Combo Value
                case 6:
                    comboValue = comboValue.multiply(new BigDecimal("0.5"));
                    break;
                //8: Add 8 Dollars To Winnings
                case 7:
                    winnings = winnings.add(new BigDecimal("8"));
                    break;
            }
        }
        
        //If Two of a Kind Rolled Half s your Winnings
        if((roll1 == roll2 || roll1 == roll3 || roll2 == roll3) && !(roll1 == roll2 && roll3 == roll2))
        {
            winnings = winnings.multiply(new BigDecimal(".5"));
        }
        
        
        // If Three of a Kind Jack Pot and guaranteed Positive Combo Score
        if(roll1 == roll2 && roll3 == roll2)
        {
            if(comboValue.signum() <= -1)
            {
                comboValue = comboValue.multiply(new BigDecimal("-1"));
            }
        }
        
        BigDecimal totalWinnings = winnings.multiply(comboValue);
        
        accountToUpdate.setBalance(accountToUpdate.getBalance().add(totalWinnings));
        
        
        String finalMessage = "%s: -> %s\nNew Balance: %s";
        finalMessage = String.format(finalMessage, displayMessage, totalWinnings, accountToUpdate.getBalance());
        
        try
        {
            SQLDatabaseObjectUtil.putObject(SetupDatabaseConnection.mogoloidDatabase,
                                            accountToUpdate,
                                            accountToUpdate.getId());
            
            if(totalWinnings.signum() <= -1)
            {
                LotteryTask.pot = LotteryTask.pot.add(totalWinnings.abs());
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
            return new MessageResponse("Error Accessing the Database");
        }
        
        return new MessageResponse(finalMessage);
    }
    
    @Override
    public String getHelpMessage()
    {
        return null;
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
