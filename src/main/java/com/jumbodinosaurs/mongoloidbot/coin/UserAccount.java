package com.jumbodinosaurs.mongoloidbot.coin;

import com.google.gson.Gson;
import com.jumbodinosaurs.devlib.database.Identifiable;
import com.jumbodinosaurs.devlib.database.objectHolder.JsonExtractLimiter;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDataBaseObjectHolder;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLStoreObject;
import com.jumbodinosaurs.devlib.util.GeneralUtil;
import com.jumbodinosaurs.mongoloidbot.coin.exceptions.UserQueryException;
import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Player;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.SetupDatabaseConnection;
import net.dv8tion.jda.api.entities.Member;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;

public class UserAccount implements SQLStoreObject,
                                            Identifiable
{
    private transient int id;
    private String usernameBase64;
    private BigDecimal balance;
    //This is Set to protect to avoid gson serialization during persistent Pot Procedures
    protected transient Member member;
    private transient BigDecimal ticketsBought;
    
    public UserAccount(String usernameBase64, BigDecimal balance)
    {
        this.usernameBase64 = usernameBase64;
        this.balance = balance;
    }
    
    public UserAccount()
    {
    }
    
    public static String getUniqueIdentifier(Member member)
    {
        return member.getIdLong() + "";
    }
    
    public static UserAccount getUser(Member member)
            throws SQLException, UserQueryException
    {
        /*
         * Process for Getting User
         * 1. Get Unique ID From Member
         * 2. Craft Limiter
         * 3. Query For User
         * 4. Error For Edge Cases
         * 5. Create Account if Missing -> Recursive Call For User Return
         * 6. Return Crafted User
         *  */

        //1. Get Unique ID From Member
        String uniqueID = Base64.getEncoder().encodeToString(getUniqueIdentifier(member).getBytes());

        //2. Craft Limiter
        String idToSearchFor = GeneralUtil.replaceUnicodeCharacters(uniqueID).toString();

        JsonExtractLimiter limiter = new JsonExtractLimiter("usernameBase64", idToSearchFor);

        //3. Query For User
        ArrayList<SQLDataBaseObjectHolder> loadedObjects = SQLDatabaseObjectUtil.loadObjects(
                SetupDatabaseConnection.mogoloidDatabase,
                UserAccount.class,
                limiter);

        //4. Error For Edge Cases
        if (loadedObjects.size() > 1)
        {
            throw new UserQueryException("More than One User for " + uniqueID);
        }


        // 5. Create Account if Missing -> Recursive Call For User Return
        if (loadedObjects.size() == 0)
        {
            UserAccount newAccount = new UserAccount(uniqueID, new BigDecimal("0"));
            SQLDatabaseObjectUtil.putObject(SetupDatabaseConnection.mogoloidDatabase, newAccount, 0);
            return getUser(member);
        }

        //6. Return Crafted User
        UserAccount account = new Gson().fromJson(loadedObjects.get(0).getJsonObject(), UserAccount.class);
        account.setId(loadedObjects.get(0).getId());
        account.setMember(member);
        return account;
    }

    public static void updatePlayer(Player player) throws SQLException
    {
        SQLDatabaseObjectUtil.putObject(SetupDatabaseConnection.mogoloidDatabase,
                player,
                player.getId());
    }

    public Player getPlayer(Member member) throws SQLException, UserQueryException
    {
        /*
         * Process for Getting User
         * 1. Get Player From Member
         * 2. Craft Limiter
         * 3. Query For Player
         * 4. Error For Edge Cases
         * 5. Create Player if Missing -> Recursive Call For Player Return
         * 6. Return Crafted Player
         *  */

        //1.  Get Player From Member
        String uniqueID = Base64.getEncoder().encodeToString(getUniqueIdentifier(member).getBytes());

        //2.  Craft Limiter
        String idToSearchFor = GeneralUtil.replaceUnicodeCharacters(uniqueID).toString();

        JsonExtractLimiter limiter = new JsonExtractLimiter("userAccountId", idToSearchFor);

        //3.  Query For Player
        ArrayList<SQLDataBaseObjectHolder> loadedObjects = SQLDatabaseObjectUtil.loadObjects(
                SetupDatabaseConnection.mogoloidDatabase,
                Player.class,
                limiter);

        //4. Error For Edge Cases
        if (loadedObjects.size() > 1)
        {
            throw new UserQueryException("More than One Player for User " + uniqueID);
        }


        // 5. Create Account if Missing -> Recursive Call For User Return
        if (loadedObjects.size() == 0)
        {
            Player newPlayer = new Player(idToSearchFor);
            SQLDatabaseObjectUtil.putObject(SetupDatabaseConnection.mogoloidDatabase, newPlayer, 0);
            return getPlayer(member);
        }

        //6. Return Crafted User
        Player player = new Gson().fromJson(loadedObjects.get(0).getJsonObject(), Player.class);
        player.setUserAccountId(idToSearchFor);
        player.setId(loadedObjects.get(0).getId());
        return player;
    }

    public static void updateUser(UserAccount account) throws SQLException
    {
        SQLDatabaseObjectUtil.putObject(SetupDatabaseConnection.mogoloidDatabase,
                account,
                account.getId());
    }


    public String getUsernameBase64()
    {
        return usernameBase64;
    }

    public void setUsernameBase64(String usernameBase64)
    {
        this.usernameBase64 = usernameBase64;
    }
    
    public BigDecimal getBalance()
    {
        return balance;
    }
    
    public void setBalance(BigDecimal balance)
    {
        this.balance = balance;
    }
    
    @Override
    public int getId()
    {
        return id;
    }
    
    @Override
    public void setId(int id)
    {
        this.id = id;
    }
    
    public Member getMember()
    {
        return member;
    }
    
    public void setMember(Member member)
    {
        this.member = member;
    }
    
    public BigDecimal getTicketsBought()
    {
        return ticketsBought;
    }
    
    public void setTicketsBought(BigDecimal ticketsBought)
    {
        this.ticketsBought = ticketsBought;
    }
    
    @Override
    public String toString()
    {
        return "UserAccount{" +
               "id=" +
               id +
               ", usernameBase64='" +
               usernameBase64 +
               '\'' +
               ", balance=" +
               balance +
               ", member=" +
               member +
               ", ticketsBought=" +
               ticketsBought +
               '}';
    }
}
