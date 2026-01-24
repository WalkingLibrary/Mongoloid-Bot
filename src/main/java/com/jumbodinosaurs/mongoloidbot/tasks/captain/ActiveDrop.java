package com.jumbodinosaurs.mongoloidbot.tasks.captain;

import com.jumbodinosaurs.mongoloidbot.commands.discord.items.models.Item;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActiveDrop
{
    public final String messageId;
    public final String channelId;
    public final String guildId;
    public final Item item;

    private final AtomicBoolean claimed = new AtomicBoolean(false);

    public ActiveDrop(String messageId, String channelId, String guildId, Item item)
    {
        this.messageId = messageId;
        this.channelId = channelId;
        this.guildId = guildId;
        this.item = item;
    }

    public boolean tryClaim()
    {
        return claimed.compareAndSet(false, true);
    }


    public AtomicBoolean IsClaimed()
    {
        return claimed;
    }
}
