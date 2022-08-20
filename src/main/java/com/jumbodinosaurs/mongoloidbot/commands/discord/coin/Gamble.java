package com.jumbodinosaurs.mongoloidbot.commands.discord.coin;

import com.jumbodinosaurs.devlib.commands.CommandWithParameters;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.exceptions.UserQueryException;
import com.jumbodinosaurs.mongoloidbot.tasks.lottery.LotteryTask;
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
        
        BigDecimal amountToGamble = new BigDecimal("1");
        
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
        
        if(amountToGamble.signum() <= 0)
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
         * 4: Add 4 Dollars To Winnings and negates ComboValue
         * 5: Negates Combo Value
         * 6: Quadruples Combo Value
         * 7: Half s the Combo Value
         * 8: Add 8 Dollars To Winnings and force negative combo value
         *
         *
         *  If Two of a Kind Rolled Half s your Winnings
         *
         *  If you Roll a 6 and not three of a kind. Guaranteed loss
         *
         *  If Three of a Kind Jack Pot and guaranteed Positive Combo Score
         *
         *  If you pass the one in a million chance you get 1 mil added on if you gambled more than 1 thousand
         *  */
    
        BigDecimal amountToGambleTemp = new BigDecimal(amountToGamble + "");
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
                    amountToGambleTemp = amountToGambleTemp.add(new BigDecimal("1"));
                    
                    break;
                //2: Double Combo Value
                case 1:
                    comboValue = comboValue.multiply(new BigDecimal("2"));
                    break;
                //3: Negates Combo Value
                case 2:
                    comboValue = comboValue.multiply(new BigDecimal("-1"));
                    break;
                //4: Add 4 Dollars To Winnings and negates ComboValue
                case 3:
                    amountToGambleTemp = amountToGambleTemp.add(new BigDecimal("4"));
                    comboValue = comboValue.multiply(new BigDecimal("-1"));
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
                //8: Add 8 Dollars To Winnings and force negative combo value
                case 7:
                    amountToGambleTemp = amountToGambleTemp.add(new BigDecimal("8"));
                    if(comboValue.signum() >= 0)
                    {
                        comboValue = comboValue.multiply(new BigDecimal("-1"));
                    }
                    break;
            }
        }
    
        //If Two of a Kind Rolled Half s your Winnings
        if((roll1 == roll2 || roll1 == roll3 || roll2 == roll3) && !(roll1 == roll2 && roll3 == roll2))
        {
            amountToGambleTemp = amountToGambleTemp.multiply(new BigDecimal(".5"));
        }
    
    
        //If you Roll a 6 and not three of a kind. Guaranteed loss
        if(roll1 == 5 || roll2 == 5 || roll3 == 5)
        {
            if(comboValue.signum() >= 0)
            {
                comboValue = comboValue.multiply(new BigDecimal("-1"));
            }
        }


        // If Three of a Kind Jack Pot and guaranteed Positive Combo Score
        if (roll1 == roll2 && roll3 == roll2)
        {
            if (comboValue.signum() <= -1)
            {
                comboValue = comboValue.multiply(new BigDecimal("-1"));
            }
        }

        BigDecimal totalWinnings = amountToGambleTemp.multiply(comboValue);


        //If you pass the one in a million chance you get 1 mil added on if you gambled more than 1 thousand
        if (amountToGamble.subtract(new BigDecimal(1000)).signum() >= 0)
        {
            int winningNumber = 1;
            int rolledNumber = (int) (1000000 * Math.random());


            if (rolledNumber == winningNumber)
            {
                totalWinnings = totalWinnings.add(new BigDecimal(1000000));
            }
        }

        //Stop Loosing More than you gambled Only lose what you have gambled
        if (amountToGamble.add(totalWinnings).signum() <= -1)
        {
            totalWinnings = amountToGamble.multiply(new BigDecimal("-1"));
        }

        //Actually Remove Gambled Winnings if you won. This way you may not lose entirely
        // but loose what you gambled
        if (totalWinnings.signum() >= 0)
        {
            accountToUpdate.setBalance(accountToUpdate.getBalance().subtract(amountToGamble));
        }
    
        accountToUpdate.setBalance(accountToUpdate.getBalance().add(totalWinnings));
    
    
        String finalMessage = "%s: -> %s\nNew Balance: %s";
        finalMessage = String.format(finalMessage, displayMessage, totalWinnings, accountToUpdate.getBalance());
    
        try
        {

            UserAccount.updateUser(accountToUpdate);

            if (totalWinnings.signum() <= -1)
            {
                LotteryTask.addToPot(totalWinnings.abs());
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
