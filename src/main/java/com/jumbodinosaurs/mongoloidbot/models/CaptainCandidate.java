package com.jumbodinosaurs.mongoloidbot.models;

import com.google.gson.Gson;
import com.jumbodinosaurs.devlib.database.Identifiable;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDataBaseObjectHolder;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLDatabaseObjectUtil;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLStoreObject;
import com.jumbodinosaurs.devlib.database.objectHolder.SelectLimiter;
import com.jumbodinosaurs.mongoloidbot.tasks.startup.SetupDatabaseConnection;
import net.dv8tion.jda.api.entities.Member;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class CaptainCandidate implements SQLStoreObject, Identifiable
{
    private transient int id;

    private boolean isActiveCampaign;
    private String userAccountId;

    private boolean isCaptain;

    private ArrayList<String> supportersLongIds;

    private LocalDateTime lastTakeOverAttempt;

    private boolean wasLastTakeOverAttemptMutiny;

    public CaptainCandidate(String userAccountId)
    {
        this.userAccountId = userAccountId;
        this.lastTakeOverAttempt = LocalDateTime.now().minusYears(1);
        this.supportersLongIds = new ArrayList<String>();
        this.wasLastTakeOverAttemptMutiny = false;
        this.isActiveCampaign = false;
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

    public String getUserAccountId()
    {
        return userAccountId;
    }

    public void setUserAccountId(String userAccountId)
    {
        this.userAccountId = userAccountId;
    }


    public LocalDateTime getLastTakeOverAttempt()
    {
        return lastTakeOverAttempt;
    }

    public void setLastTakeOverAttempt(LocalDateTime lastTakeOverAttempt)
    {
        this.lastTakeOverAttempt = lastTakeOverAttempt;
    }

    public boolean isWasLastTakeOverAttemptMutiny()
    {
        return wasLastTakeOverAttemptMutiny;
    }

    public void setWasLastTakeOverAttemptMutiny(boolean wasLastTakeOverAttemptMutiny)
    {
        this.wasLastTakeOverAttemptMutiny = wasLastTakeOverAttemptMutiny;
    }

    public ArrayList<String> getSupportersLongIds()
    {
        return supportersLongIds;
    }

    public void setSupportersLongIds(ArrayList<String> supportersLongIds)
    {
        this.supportersLongIds = supportersLongIds;
    }

    public void removeSupporter(String supporterId)
    {
        if (this.supportersLongIds == null || supporterId == null)
        {
            return;
        }

        // Remove any entries that exactly match the supporterId
        this.supportersLongIds.removeIf(id -> id.equals(supporterId));
    }

    public boolean isActiveCampaign()
    {
        return isActiveCampaign;
    }

    public void setActiveCampaign(boolean activeCampaign)
    {
        isActiveCampaign = activeCampaign;
    }


    public static ArrayList<CaptainCandidate> getAllCaptainCandidates() throws SQLException
    {
        /*
         * Process for Getting All Captain Candidates
         * 1. Query all CaptainCandidate objects from the database
         * 2. Convert JSON objects into CaptainCandidate instances
         * 3. Return as a list
         */

        // 1. Load all objects of type CaptainCandidate (no limiter)
        SelectLimiter limiter = new SelectLimiter()
        {
            @Override
            public String getSelectLimiterStatement()
            {
                return "";
            }
        };
        ArrayList<SQLDataBaseObjectHolder> loadedObjects = SQLDatabaseObjectUtil.loadObjects(
                SetupDatabaseConnection.mogoloidDatabase,
                CaptainCandidate.class,
                limiter // No limiter means fetch all
        );

        ArrayList<CaptainCandidate> candidates = new ArrayList<>();

        // 2. Convert each JSON object into a CaptainCandidate instance
        for (SQLDataBaseObjectHolder object : loadedObjects)
        {
            CaptainCandidate candidate = new Gson().fromJson(object.getJsonObject(), CaptainCandidate.class);
            candidate.setId(object.getId());
            candidates.add(candidate);
        }

        // 3. Return the crafted list
        return candidates;
    }

    public boolean isCaptain()
    {
        return isCaptain;
    }

    public void setCaptain(boolean captain)
    {
        isCaptain = captain;
    }
}
