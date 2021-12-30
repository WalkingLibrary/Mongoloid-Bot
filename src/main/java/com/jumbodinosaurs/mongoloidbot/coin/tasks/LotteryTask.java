package com.jumbodinosaurs.mongoloidbot.coin.tasks;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.devlib.task.ScheduledTask;
import com.jumbodinosaurs.devlib.util.GeneralUtil;
import com.jumbodinosaurs.mongoloidbot.JDAController;
import com.jumbodinosaurs.mongoloidbot.Main;
import com.jumbodinosaurs.mongoloidbot.coin.UserAccount;
import com.jumbodinosaurs.mongoloidbot.coin.exceptions.UserQueryException;
import com.jumbodinosaurs.mongoloidbot.eventHandlers.EventListener;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.SetupDatabaseConnection;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.io.File;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LotteryTask extends ScheduledTask
{
    
    private static final File savedInstanceFile = GeneralUtil.checkFor(JDAController.optionsFolder,
                                                                       "savedLotteryInstance" + ".json");
    private static LotteryInstance currentInstance;
    private final boolean firstInit = true;
    
    public LotteryTask(ScheduledThreadPoolExecutor executor)
    {
        super(executor);
        loadPot();
    }
    
    public static void addTickets(UserAccount account, BigDecimal amount)
    {
        List<UserAccount> accountsInThePot = Collections.synchronizedList(currentInstance.getAccountsInThePot());
        synchronized(accountsInThePot)
        {
            boolean addedTickets = false;
            for(UserAccount userAccount : accountsInThePot)
            {
                if(userAccount.getId() == account.getId())
                {
                    userAccount.setTicketsBought(userAccount.getTicketsBought().add(amount));
                    addedTickets = true;
                }
            }
            
            if(!addedTickets)
            {
                account.setTicketsBought(amount);
                currentInstance.getAccountsInThePot().add(account);
            }
            savePot();
        }
    }
    
    public static void addToPot(BigDecimal amount)
    {
        currentInstance.setPot(currentInstance.getPot().add(amount));
        savePot();
    }
    
    private static void loadPot()
    {
        try
        {
            String previouslySavedInstance = GeneralUtil.scanFileContents(savedInstanceFile);
            Gson transientIgnorableGson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.VOLATILE).create();
            LotteryInstance newInstance = transientIgnorableGson.fromJson(previouslySavedInstance,
                                                                          LotteryInstance.class);
            currentInstance = newInstance;
        }
        catch(JsonParseException e)
        {
    
            EventListener.sendMessage("No Lottery Loaded");
        }
    
        if(currentInstance == null)
        {
            currentInstance = new LotteryInstance(new BigDecimal("0"), new ArrayList<UserAccount>());
            savePot();
        }
    
        for(UserAccount userAccount : currentInstance.getAccountsInThePot())
        {
            Member linkedMember = null;
            String decodedUserAccount = new String(Base64.getDecoder().decode(userAccount.getUsernameBase64()));
            for(Guild guild : Main.jdaController.getJda().getGuilds())
            {
                for(Member member : guild.getMembers())
                {
                    if(UserAccount.getUniqueIdentifier(member).equals(decodedUserAccount))
                    {
                        linkedMember = member;
                        break;
                    }
                }
            
                if(linkedMember != null)
                {
                    break;
                }
            
            }
        
            userAccount.setMember(linkedMember);
        }
    }
    
    private static void savePot()
    {
        Gson transientIgnorableGson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.PROTECTED).create();
        String savedInstance = transientIgnorableGson.toJson(currentInstance);
    
        GeneralUtil.writeContents(savedInstanceFile, savedInstance, false);
    }
    
    public static BigDecimal getPot()
    {
        return currentInstance.getPot();
    }
    
    @Override
    public void run()
    {
    
    
        System.out.println("Spinning the Wheel");
        //        if(firstInit)
        //        {
        //            firstInit = false;
        //            return;
        //        }
        try
        {
            List<UserAccount> accountsInThePot = Collections.synchronizedList(currentInstance.getAccountsInThePot());
            synchronized(accountsInThePot)
            {
                int amountOfAccounts = accountsInThePot.size();
            
                if(amountOfAccounts <= 0)
                {
                    //House Wins
                    int randomAmount = (int) (1000 * Math.random());
                    BigDecimal amountToAdd = new BigDecimal(randomAmount + "");
                    addToPot(amountToAdd);
                    return;
                }
                
                BigDecimal amountOfTicketsBought = new BigDecimal("0");
                for(UserAccount userAccount : accountsInThePot)
                {
                    System.out.println(userAccount.toString());
                    amountOfTicketsBought = amountOfTicketsBought.add(userAccount.getTicketsBought());
                }
                BigDecimal winningNumber = amountOfTicketsBought.multiply(new BigDecimal(Math.random()));
                
                UserAccount winningAccount = null;
                BigDecimal currentThreshold = new BigDecimal("0");
                System.out.println("Amount of Accounts in the Pot: " + accountsInThePot.size());
                System.out.println("Winning Number:  " + winningNumber);
                System.out.println("Threshold: " + currentThreshold);
                while(winningNumber.subtract(currentThreshold).signum() >= 0)
                {
                    
                    int randomSelector = (int) (accountsInThePot.size() * Math.random());
                    winningAccount = accountsInThePot.remove(randomSelector);
                    currentThreshold = currentThreshold.add(winningAccount.getTicketsBought());
                    System.out.println("Threshold: " + currentThreshold);
                }
                
                if(winningAccount == null)
                {
                    throw new IllegalStateException("No One Wins?");
                }
                
                //House Plays
                
                int houseWinningNumber = 1;
                int rolledNumber = (int) (1000000 * Math.random());
                
                
                if(rolledNumber == houseWinningNumber)
                {
                    //House Wins
                    resetThePot();
                    EventListener.sendMessage("The House Won the Lottery of " + currentInstance.getPot().toString());
                    return;
                    
                }
                
                
                /* Process of Paying the Winnings
                 * Get the Most up-to-date balance of the winner
                 * Add Winnings
                 *
                 * Update UserAccount
                 *
                 */
                try
                {
                    //Get the Most up-to-date balance of the winner
                    UserAccount updatedAccount = UserAccount.getUser(winningAccount.getMember());
    
                    //If they are a Lost nomad Just reset the pot and add the value back
                    if(updatedAccount.getMember() == null)
                    {
                        BigDecimal lastPot = getPot();
                        resetThePot();
                        addToPot(lastPot);
                        return;
                    }
    
                    updatedAccount.setBalance(updatedAccount.getBalance().add(currentInstance.getPot()));
                    SQLDatabaseObjectUtil.putObject(SetupDatabaseConnection.mogoloidDatabase,
                                                    updatedAccount,
                                                    updatedAccount.getId());
                    EventListener.sendMessage(updatedAccount.getMember().getEffectiveName() +
                                              " wins " +
                                              currentInstance.getPot().toString());
                    resetThePot();
                }
                catch(SQLException | UserQueryException e)
                {
                    //If There is an error we Will Just Keep the Pot Around
                    e.printStackTrace();
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        
    }
    
    public void resetThePot()
    {
        currentInstance = new LotteryInstance(new BigDecimal("0"), new ArrayList<>());
        savePot();
    }
    
    @Override
    public int getPeriod()
    {
        return 1;
    }
    
    @Override
    public TimeUnit getTimeUnit()
    {
        return TimeUnit.HOURS;
    }
    
}
