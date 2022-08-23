package com.jumbodinosaurs.mongoloidbot.commands.discord.coin;

import com.google.gson.Gson;
import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.devlib.database.DataBaseUtil;
import com.jumbodinosaurs.devlib.database.Query;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDataBaseObjectHolder;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.mongoloidbot.commands.discord.util.IDiscordChatEventable;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.SetupDatabaseConnection;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CoinStats extends Command implements IDiscordChatEventable
{

    @Override
    public String getCategory()
    {
        return "Coin";
    }

    private GuildMessageReceivedEvent event;

    @Override
    public MessageResponse getExecutedMessage()
            throws WaveringParametersException
    {

        try
        {
            String selectAllAccounts = "Select * from %s;";
            
            selectAllAccounts = String.format(selectAllAccounts, UserAccount.class.getSimpleName());
            
            Query topTenQuery = new Query(selectAllAccounts);
            System.out.println("Executing: " + topTenQuery.getStatement());
            DataBaseUtil.queryDataBase(topTenQuery, SetupDatabaseConnection.mogoloidDatabase);
            
            //Create Object Holders from Returned Information
            ArrayList<SQLDataBaseObjectHolder> allAccounts = new ArrayList<SQLDataBaseObjectHolder>();
            ResultSet queryResult = topTenQuery.getResultSet();
            while(queryResult.next())
            {
                String objectJson = queryResult.getString(SQLDatabaseObjectUtil.OBJECT_COLUMN_NAME);
                int id = queryResult.getInt("id");
                allAccounts.add(new SQLDataBaseObjectHolder(id, objectJson));
            }
            topTenQuery.getStatementObject().getConnection().close();
            
            BigDecimal totalMongolCoinInCirculation = new BigDecimal("0");
    
            for(SQLDataBaseObjectHolder objectUtil : allAccounts)
            {
                UserAccount currentAccount = new Gson().fromJson(objectUtil.getJsonObject(), UserAccount.class);
                currentAccount.setId(objectUtil.getId());
                totalMongolCoinInCirculation = totalMongolCoinInCirculation.add(currentAccount.getBalance());
            }
            
            String output = "Total Mongoloid Coin in Circulation: " + totalMongolCoinInCirculation;
            return new MessageResponse(output);
        }
        catch(SQLException e)
        {
            e.printStackTrace();
            return new MessageResponse("Error Checking Database");
        }
    }
    
    @Override
    public String getHelpMessage()
    {
        return "Shows The Status and Statistics of Mongoloid Coin\nUsage ~" + getClass().getSimpleName();
    }
    
    @Override
    public GuildMessageReceivedEvent getGuildMessageReceivedEvent()
    {
        return this.event;
    }
    
    @Override
    public void setGuildMessageReceivedEvent(GuildMessageReceivedEvent event)
    {
        this.event = event;
    }
}
