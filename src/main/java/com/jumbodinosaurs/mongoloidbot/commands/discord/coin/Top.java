package com.jumbodinosaurs.mongoloidbot.commands.discord.coin;

import com.google.gson.Gson;
import com.jumbodinosaurs.devlib.commands.Command;
import com.jumbodinosaurs.devlib.commands.MessageResponse;
import com.jumbodinosaurs.devlib.commands.exceptions.WaveringParametersException;
import com.jumbodinosaurs.devlib.database.DataBaseUtil;
import com.jumbodinosaurs.devlib.database.Query;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDataBaseObjectHolder;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.mongoloidbot.Main;
import com.jumbodinosaurs.mongoloidbot.models.UserAccount;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.SetupDatabaseConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;

public class Top extends Command
{
    @Override
    public MessageResponse getExecutedMessage()
            throws WaveringParametersException
    {
        
        
        try
        {
            String topTenStatement = "Select *, JSON_EXTRACT(%s, \"$.%s\") as balance from %s ORDER BY " +
                                     "balance DESC LIMIT 10;";
            
            topTenStatement = String.format(topTenStatement,
                                            SQLDatabaseObjectUtil.OBJECT_COLUMN_NAME,
                                            "balance",
                                            UserAccount.class.getSimpleName());
            
            Query topTenQuery = new Query(topTenStatement);
            System.out.println("Executing: " + topTenQuery.getStatement());
            DataBaseUtil.queryDataBase(topTenQuery, SetupDatabaseConnection.mogoloidDatabase);
            
            //Create Object Holders from Returned Information
            ArrayList<SQLDataBaseObjectHolder> topTen = new ArrayList<SQLDataBaseObjectHolder>();
            ResultSet queryResult = topTenQuery.getResultSet();
            while(queryResult.next())
            {
                String objectJson = queryResult.getString(SQLDatabaseObjectUtil.OBJECT_COLUMN_NAME);
                int id = queryResult.getInt("id");
                topTen.add(new SQLDataBaseObjectHolder(id, objectJson));
            }
            topTenQuery.getStatementObject().getConnection().close();
            
            String topTenOutPut = "Top Ten:\n";
            for(int i = 0; i < topTen.size(); i++)
            {
                SQLDataBaseObjectHolder current = topTen.get(i);
                UserAccount currentAccount = new Gson().fromJson(current.getJsonObject(), UserAccount.class);
                currentAccount.setId(current.getId());
    
                String bytes = new String(Base64.getDecoder().decode(currentAccount.getUsernameBase64()));
                System.out.println(bytes);
                String discordName;
                try
                {
                    discordName = Main.jdaController.getJda().getUserById(bytes).getName();
                }
                catch(NullPointerException e)
                {
                    discordName = "A Lost Nomad";
                }
                topTenOutPut += (i + 1) + ": " + discordName + " " + currentAccount.getBalance() + "\n";
            }
            return new MessageResponse(topTenOutPut);
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
        return "Shows The Top 10 Mongoloid Coin holders\nUsage:\n~Top";
    }
}
