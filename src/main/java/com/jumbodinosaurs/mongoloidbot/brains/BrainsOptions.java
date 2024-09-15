package com.jumbodinosaurs.mongoloidbot.brains;

public class BrainsOptions
{
    private String endPoint;
    private String channelIdToRespondIn;

    private boolean ShouldRespond;

    public BrainsOptions(String endPoint, String channelIdToRespondIn, boolean shouldRespond)
    {
        this.endPoint = endPoint;
        this.channelIdToRespondIn = channelIdToRespondIn;
        ShouldRespond = shouldRespond;
    }

    public String getEndPoint()
    {
        return endPoint;
    }

    public void setEndPoint(String endPoint)
    {
        this.endPoint = endPoint;
    }

    public String getChannelIdToRespondIn()
    {
        return channelIdToRespondIn;
    }

    public void setChannelIdToRespondIn(String channelIdToRespondIn)
    {
        this.channelIdToRespondIn = channelIdToRespondIn;
    }

    public boolean isShouldRespond()
    {
        return ShouldRespond;
    }

    public void setShouldRespond(boolean shouldRespond)
    {
        ShouldRespond = shouldRespond;
    }
}
