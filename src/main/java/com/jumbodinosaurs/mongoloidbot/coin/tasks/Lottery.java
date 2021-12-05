package com.jumbodinosaurs.mongoloidbot.coin.tasks;


import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.devlib.task.ScheduledTask;
import com.jumbodinosaurs.mongoloidbot.coin.UserAccount;
import com.jumbodinosaurs.mongoloidbot.coin.exceptions.UserQueryException;
import com.jumbodinosaurs.mongoloidbot.eventHandlers.EventListener;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.SetupDatabaseConnection;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Lottery extends ScheduledTask
{
    public static BigDecimal pot = new BigDecimal("0");
    public static CopyOnWriteArrayList<UserAccount> accountsInThePot = new CopyOnWriteArrayList<UserAccount>();
    
    public Lottery(ScheduledThreadPoolExecutor executor)
    {
        super(executor);
    }
    
    public static void addTickets(UserAccount account, BigDecimal amount)
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
            accountsInThePot.add(account);
        }
    }
    
    @Override
    public void run()
    {
        System.out.println("Spinning the Wheel");
        try
        {
            int amountOfAccounts = accountsInThePot.size();
            
            if(amountOfAccounts <= 0)
            {
                //House Wins
                int randomAmount = (int) (1000 * Math.random());
                BigDecimal amountToAdd = new BigDecimal(randomAmount + "");
                pot = pot.add(amountToAdd);
                EventListener.sendMessage("No One Played The Lottery... Adding to the Pot -> Pot Now: " + pot);
                return;
            }
            
            BigDecimal amountOfTicketsBought = new BigDecimal("0");
            for(UserAccount userAccount : accountsInThePot)
            {
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
                EventListener.sendMessage("The House Won the Lottery of " + pot.toString());
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
                
                updatedAccount.setBalance(updatedAccount.getBalance().add(pot));
                SQLDatabaseObjectUtil.putObject(SetupDatabaseConnection.mogoloidDatabase,
                                                updatedAccount,
                                                updatedAccount.getId());
                EventListener.sendMessage(updatedAccount.getMember().getEffectiveName() + " wins " + pot.toString());
                resetThePot();
            }
            catch(SQLException | UserQueryException e)
            {
                //If There is an error we Will Just Keep the Pot Around
                e.printStackTrace();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        
    }
    
    public void resetThePot()
    {
        accountsInThePot = new CopyOnWriteArrayList<UserAccount>();
        pot = new BigDecimal("0");
    }
    
    @Override
    public int getPeriod()
    {
        return 5;
    }
    
    @Override
    public TimeUnit getTimeUnit()
    {
        return TimeUnit.MINUTES;
    }
    
}
