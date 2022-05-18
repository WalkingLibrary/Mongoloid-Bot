package com.jumbodinosaurs.mongoloidbot.coin.tasks.lottery;

import com.jumbodinosaurs.mongoloidbot.coin.UserAccount;

import java.math.BigDecimal;
import java.util.ArrayList;

public class LotteryInstance
{
    private BigDecimal pot = new BigDecimal("0");
    private ArrayList<UserAccount> accountsInThePot = new ArrayList<UserAccount>();
    
    public LotteryInstance(BigDecimal pot, ArrayList<UserAccount> accountsInThePot)
    {
        this.pot = pot;
        this.accountsInThePot = accountsInThePot;
    }
    
    public LotteryInstance()
    {
    }
    
    public BigDecimal getPot()
    {
        return pot;
    }
    
    public void setPot(BigDecimal pot)
    {
        this.pot = pot;
    }
    
    public ArrayList<UserAccount> getAccountsInThePot()
    {
        return accountsInThePot;
    }
    
    public void setAccountsInThePot(ArrayList<UserAccount> accountsInThePot)
    {
        this.accountsInThePot = accountsInThePot;
    }
}
