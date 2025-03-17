package com.jumbodinosaurs.mongoloidbot.models;

import com.jumbodinosaurs.devlib.database.Identifiable;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLStoreObject;
import net.dv8tion.jda.api.entities.Member;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class CaptainCandidate implements SQLStoreObject, Identifiable
{
    private transient int id;

    private boolean isActiveCampaign;
    private String userAccountId;

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

    public boolean isActiveCampaign()
    {
        return isActiveCampaign;
    }

    public void setActiveCampaign(boolean activeCampaign)
    {
        isActiveCampaign = activeCampaign;
    }
}
